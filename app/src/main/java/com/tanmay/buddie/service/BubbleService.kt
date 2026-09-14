package com.tanmay.buddie.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.tanmay.buddie.MainActivity
import com.tanmay.buddie.R
import com.tanmay.buddie.data.datastore.BubbleEdge
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.domain.usecase.GetMascotMoodUseCase
import com.tanmay.buddie.presentation.components.mascot.BuddieMascot
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.hypot

@AndroidEntryPoint
class BubbleService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    @Inject
    lateinit var getMascotMoodUseCase: GetMascotMoodUseCase

    @Inject
    lateinit var userPreferences: UserPreferences

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val _viewModelStore = ViewModelStore()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    private var windowManager: WindowManager? = null
    private var bubbleView: ComposeView? = null
    private var snapAnimator: ValueAnimator? = null
    private var isAttaching = false

    companion object {
        const val CHANNEL_ID = "buddie_floating_bubble_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, BubbleService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BubbleService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        createNotificationChannel()
        startInForeground()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        attachBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        attachBubble()
        return START_STICKY
    }

    override fun onDestroy() {
        snapAnimator?.cancel()
        snapAnimator = null
        serviceScope.cancel()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        detachBubble()
        _viewModelStore.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getScreenDimensions(): Pair<Int, Int> {
        val displayMetrics = resources.displayMetrics
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    private fun attachBubble() {
        if (!Settings.canDrawOverlays(this)) return
        if (bubbleView != null || isAttaching) return
        isAttaching = true

        serviceScope.launch {
            val savedEdge = try {
                userPreferences.bubbleEdgeFlow.first()
            } catch (_: Exception) {
                BubbleEdge.RIGHT
            }
            val savedY = try {
                userPreferences.bubblePositionYFlow.first()
            } catch (_: Exception) {
                null
            }
            performAttachBubble(savedEdge, savedY)
            isAttaching = false
        }
    }

    private fun performAttachBubble(savedEdge: BubbleEdge, savedY: Int?) {
        if (!Settings.canDrawOverlays(this)) return
        if (bubbleView != null) return

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val density = resources.displayMetrics.density
        val bubbleSizePx = (52 * density).toInt()
        val marginPx = (8 * density).toInt()

        val (screenWidth, screenHeight) = getScreenDimensions()

        val initialX = if (savedEdge == BubbleEdge.LEFT) {
            marginPx
        } else {
            (screenWidth - bubbleSizePx - marginPx).coerceAtLeast(0)
        }

        val minY = (24 * density).toInt()
        val maxY = (screenHeight - bubbleSizePx - (48 * density).toInt()).coerceAtLeast(minY)
        val initialY = (savedY ?: ((screenHeight - bubbleSizePx) / 2)).coerceIn(minY, maxY)

        val layoutParams = WindowManager.LayoutParams(
            bubbleSizePx,
            bubbleSizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@BubbleService)
            setViewTreeSavedStateRegistryOwner(this@BubbleService)
            setViewTreeViewModelStoreOwner(this@BubbleService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                val mood by getMascotMoodUseCase().collectAsState(initial = MascotMood.CALM)
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BuddieMascot(
                        mood = mood,
                        size = 48.dp,
                        animateIdle = true
                    )
                }
            }
        }

        var initialTouchRawX = 0f
        var initialTouchRawY = 0f
        var initialParamX = 0
        var initialParamY = 0
        var isDragging = false
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        composeView.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    snapAnimator?.cancel()
                    snapAnimator = null
                    initialParamX = layoutParams.x
                    initialParamY = layoutParams.y
                    initialTouchRawX = event.rawX
                    initialTouchRawY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchRawX
                    val dy = event.rawY - initialTouchRawY
                    if (!isDragging && hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                        isDragging = true
                    }
                    if (isDragging) {
                        val (currWidth, currHeight) = getScreenDimensions()
                        val currMinY = (24 * density).toInt()
                        val currMaxY = (currHeight - bubbleSizePx - (48 * density).toInt()).coerceAtLeast(currMinY)
                        val currMaxX = (currWidth - bubbleSizePx).coerceAtLeast(0)

                        layoutParams.x = (initialParamX + dx).toInt().coerceIn(0, currMaxX)
                        layoutParams.y = (initialParamY + dy).toInt().coerceIn(currMinY, currMaxY)
                        try {
                            wm.updateViewLayout(composeView, layoutParams)
                        } catch (e: Exception) {
                            Log.e("BubbleService", "Error updating layout during drag", e)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        snapToEdge(layoutParams, bubbleSizePx, marginPx)
                    } else {
                        v.performClick()
                        openMainActivity()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        snapToEdge(layoutParams, bubbleSizePx, marginPx)
                    }
                    true
                }
                else -> false
            }
        }

        try {
            wm.addView(composeView, layoutParams)
            bubbleView = composeView
        } catch (e: SecurityException) {
            Log.e("BubbleService", "Overlay permission revoked or missing", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e("BubbleService", "Failed to add bubble overlay to WindowManager", e)
        }
    }

    private fun snapToEdge(
        layoutParams: WindowManager.LayoutParams,
        bubbleSizePx: Int,
        marginPx: Int
    ) {
        val view = bubbleView ?: return
        val wm = windowManager ?: return
        val (screenWidth, _) = getScreenDimensions()

        val currentX = layoutParams.x
        val bubbleCenterX = currentX + bubbleSizePx / 2
        val screenCenterX = screenWidth / 2

        val targetEdge = if (bubbleCenterX < screenCenterX) BubbleEdge.LEFT else BubbleEdge.RIGHT
        val targetX = if (targetEdge == BubbleEdge.LEFT) {
            marginPx
        } else {
            (screenWidth - bubbleSizePx - marginPx).coerceAtLeast(0)
        }
        val currentY = layoutParams.y

        snapAnimator?.cancel()
        var wasCancelled = false
        val animator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 250L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                if (bubbleView != null) {
                    layoutParams.x = animation.animatedValue as Int
                    try {
                        wm.updateViewLayout(view, layoutParams)
                    } catch (e: Exception) {
                        Log.e("BubbleService", "Failed to update layout during snap animation", e)
                    }
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    wasCancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    snapAnimator = null
                    if (!wasCancelled) {
                        serviceScope.launch {
                            try {
                                userPreferences.setBubblePosition(targetEdge, currentY)
                            } catch (e: Exception) {
                                Log.e("BubbleService", "Failed to persist bubble position", e)
                            }
                        }
                    }
                }
            })
        }
        snapAnimator = animator
        animator.start()
    }

    private fun openMainActivity() {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(appIntent)
    }

    private fun detachBubble() {
        snapAnimator?.cancel()
        snapAnimator = null
        bubbleView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                Log.e("BubbleService", "Failed to remove bubble overlay from WindowManager", e)
            }
            bubbleView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Buddie Floating Bubble",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Buddie floating reminder bubble active"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun startInForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Buddie")
            .setContentText("Buddie is watching your deadlines")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
}
