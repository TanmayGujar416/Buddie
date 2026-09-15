package com.tanmay.buddie.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.service.BubbleService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun userPreferences(): UserPreferences
    }

    companion object {
        private const val TAG = "BootReceiver"
        private const val QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != QUICKBOOT_POWERON
        ) {
            return
        }

        Log.d(TAG, "Received broadcast: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = try {
                    EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        BootReceiverEntryPoint::class.java
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get EntryPoint, falling back to direct UserPreferences instance", e)
                    null
                }
                val userPreferences = entryPoint?.userPreferences() ?: UserPreferences(context.applicationContext)

                val hasOverlayPermission = Settings.canDrawOverlays(context)
                val isBubbleEnabled = userPreferences.floatingBubbleEnabledFlow.first()

                Log.d(TAG, "Boot conditions check - overlayPermission: $hasOverlayPermission, bubbleEnabled: $isBubbleEnabled")

                if (hasOverlayPermission && isBubbleEnabled) {
                    Log.i(TAG, "Restarting BubbleService after reboot")
                    BubbleService.start(context)
                } else {
                    Log.d(TAG, "Skipping BubbleService restart (permission: $hasOverlayPermission, enabled: $isBubbleEnabled)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking preferences or restarting BubbleService", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
