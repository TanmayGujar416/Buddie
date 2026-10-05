package com.tanmay.buddie.presentation.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.components.mascot.SelectedCompanionMascot
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tanmay.buddie.presentation.theme.*
import com.tanmay.buddie.service.BubbleService

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onEditTimetableClick: () -> Unit,
    onConnectMoodleClick: () -> Unit,
    onMapCoursesClick: () -> Unit = {},
    onFilterCodesClick: () -> Unit = {},
    onSyncPreviewClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val powerManager = remember { context.getSystemService(PowerManager::class.java) }
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true)
    }

    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var showPermissionExplanation by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Re-check overlay permission state onResume (e.g. returning from system settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isIgnoringBatteryOptimizations = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
                val hasOverlayPermission = Settings.canDrawOverlays(context)
                if (permissionRequested) {
                    permissionRequested = false
                    if (hasOverlayPermission) {
                        viewModel.setFloatingBubbleEnabled(true)
                        showPermissionExplanation = false
                        checkAndRequestNotificationPermission()
                        BubbleService.start(context)
                    } else {
                        viewModel.setFloatingBubbleEnabled(false)
                        showPermissionExplanation = true
                        BubbleService.stop(context)
                    }
                } else {
                    // Reconcile if permission was revoked externally while backgrounded
                    if (!hasOverlayPermission && uiState.floatingBubbleEnabled) {
                        viewModel.setFloatingBubbleEnabled(false)
                        showPermissionExplanation = true
                        BubbleService.stop(context)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val onFloatingBubbleToggle: (Boolean) -> Unit = { enabled ->
        if (enabled) {
            val hasOverlayPermission = Settings.canDrawOverlays(context)
            if (hasOverlayPermission) {
                viewModel.setFloatingBubbleEnabled(true)
                showPermissionExplanation = false
                checkAndRequestNotificationPermission()
                BubbleService.start(context)
            } else {
                permissionRequested = true
                showPermissionExplanation = false
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.w("SettingsScreen", "Failed to open package-specific overlay settings, falling back to general settings", e)
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                }
            }
        } else {
            permissionRequested = false
            showPermissionExplanation = false
            viewModel.setFloatingBubbleEnabled(false)
            BubbleService.stop(context)
        }
    }

    Scaffold(
        containerColor = CharcoalBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Integrated Header: Edge-to-edge status bar blending
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Appearance & notification preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Floating Reminder Bubble section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Floating Reminder Bubble",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Show a floating companion overlay for deadlines and exams",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Card(
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, BorderOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = "Enable floating reminder bubble",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Floating mascot bubble visible over other apps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                Switch(
                                    checked = uiState.floatingBubbleEnabled,
                                    onCheckedChange = onFloatingBubbleToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = TextPrimary,
                                        checkedTrackColor = AccentBlue,
                                        checkedBorderColor = AccentBlue,
                                        uncheckedThumbColor = TextSecondary,
                                        uncheckedTrackColor = CharcoalSurfaceElevated,
                                        uncheckedBorderColor = BorderOutline
                                    )
                                )
                            }

                            if (showPermissionExplanation) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = StatusError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Needed for the floating bubble to work",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusError,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.floatingBubbleEnabled && !isIgnoringBatteryOptimizations && !uiState.batteryOptimizationPromptDismissed) {
                        Card(
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated),
                            border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = StatusWarning,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Keep Buddie Alive in Background",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Text(
                                    text = "Some devices (Xiaomi, OnePlus, Samsung, etc.) aggressively kill background services. Disabling battery optimization lets Buddie stay running and show reminders reliably.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { viewModel.dismissBatteryOptimizationPrompt() }
                                    ) {
                                        Text("Not Now", color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Log.w("SettingsScreen", "Failed to open direct battery optimization intent, falling back to general settings", e)
                                                try {
                                                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                                } catch (e2: Exception) {
                                                    Log.e("SettingsScreen", "Failed to open battery optimization settings", e2)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AccentBlue,
                                            contentColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text("Disable Optimization", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Battery Saver Card (opt-in static pose for bubble)
                    Card(
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, BorderOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Battery Saver",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Static pose in collapsed bubble to save power; animates when opened",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Switch(
                                checked = uiState.batterySaverEnabled,
                                onCheckedChange = { viewModel.setBatterySaverEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextPrimary,
                                    checkedTrackColor = AccentBlue,
                                    checkedBorderColor = AccentBlue,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CharcoalSurfaceElevated,
                                    uncheckedBorderColor = BorderOutline
                                )
                            )
                        }
                    }

                    // Battery Troubleshooting Collapsible Section
                    BatteryTroubleshootingCard()
                }

                Divider(color = DividerFaint, thickness = 0.5.dp)

                // Companion Character section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Companion Character",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Choose your companion for reminders & floating overlay cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CompanionPreviewCard(
                            companionType = CompanionType.BUDDIE,
                            title = "Buddie",
                            subtitle = "Mochi",
                            isSelected = uiState.companionType == CompanionType.BUDDIE,
                            onClick = { viewModel.updateCompanionType(CompanionType.BUDDIE) },
                            modifier = Modifier.weight(1f)
                        )
                        CompanionPreviewCard(
                            companionType = CompanionType.MOMO,
                            title = "Momo",
                            subtitle = "Cat",
                            isSelected = uiState.companionType == CompanionType.MOMO,
                            onClick = { viewModel.updateCompanionType(CompanionType.MOMO) },
                            modifier = Modifier.weight(1f)
                        )
                        CompanionPreviewCard(
                            companionType = CompanionType.WISP,
                            title = "Wisp",
                            subtitle = "Ghost",
                            isSelected = uiState.companionType == CompanionType.WISP,
                            onClick = { viewModel.updateCompanionType(CompanionType.WISP) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Divider(color = DividerFaint, thickness = 0.5.dp)

                // Card Style section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Card Visual Style",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Controls the floating mascot card shell presentation (v2)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        StylePreviewCard(
                            title = "Speech Bubble",
                            subtitle = "Comic style bubble",
                            isSelected = uiState.cardStyle == "SPEECH_BUBBLE",
                            onClick = { viewModel.updateCardStyle("SPEECH_BUBBLE") },
                            modifier = Modifier.weight(1f)
                        )
                        StylePreviewCard(
                            title = "Companion Header",
                            subtitle = "Clean card header",
                            isSelected = uiState.cardStyle == "COMPANION_HEADER",
                            onClick = { viewModel.updateCardStyle("COMPANION_HEADER") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Divider(color = DividerFaint, thickness = 0.5.dp)

                // Notifications section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Card(
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, BorderOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Advance Reminder",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Alert before deadlines & exams",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { expanded = true }) {
                                    Text(
                                        text = "${uiState.notificationHours}h before",
                                        fontWeight = FontWeight.Bold,
                                        color = AccentBlueLight
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(CharcoalSurfaceElevated)
                                ) {
                                    listOf(1, 12, 24, 48, 72).forEach { hours ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "$hours hours before",
                                                    color = if (uiState.notificationHours == hours) AccentBlueLight else TextPrimary
                                                )
                                            },
                                            onClick = {
                                                viewModel.updateNotificationHours(hours)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = DividerFaint, thickness = 0.5.dp)

                // Timetable Configuration section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Timetable Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Add, remove, or modify your weekly recurring classes",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedButton(
                        onClick = onEditTimetableClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = ButtonShape,
                        border = BorderStroke(1.dp, BorderOutline)
                    ) {
                        Text(
                            text = "Edit Timetable Grid",
                            color = AccentBlueLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Divider(color = DividerFaint, thickness = 0.5.dp)

                // Moodle Connection section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Moodle Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Link your college Moodle account to synchronize assignments and course schedules",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Card(
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, BorderOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (uiState.isMoodleConnected) StatusSuccess else TextMuted)
                                    )
                                    Text(
                                        text = if (uiState.isMoodleConnected) "Connected" else "Not connected",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (uiState.isMoodleConnected) StatusSuccess else TextSecondary
                                    )
                                }

                                if (uiState.isMoodleConnected && !uiState.moodleSiteUrl.isNullOrBlank()) {
                                    Text(
                                        text = uiState.moodleSiteUrl?.removePrefix("https://")?.removePrefix("http://") ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentBlueLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (uiState.isMoodleConnected) {
                                Text(
                                    text = "Access token is encrypted with Android Keystore. Your username and password are not stored.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 11.5.sp
                                )

                                if (uiState.syncMessage != null) {
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (uiState.syncMessage!!.startsWith("Sync error")) StatusError.copy(alpha = 0.12f) else AccentBlue.copy(alpha = 0.12f)
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (uiState.syncMessage!!.startsWith("Sync error")) StatusError.copy(alpha = 0.35f) else AccentBlue.copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = uiState.syncMessage ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { viewModel.clearSyncMessage() },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Dismiss",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = { viewModel.runSync() },
                                    enabled = !uiState.isSyncing,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentBlue,
                                        contentColor = TextPrimary
                                    )
                                ) {
                                    if (uiState.isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = TextPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Syncing with Moodle...",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Text(
                                            text = "Sync Now",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = onMapCoursesClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CharcoalSurfaceElevated,
                                        contentColor = TextPrimary
                                    ),
                                    border = BorderStroke(1.dp, BorderOutline)
                                ) {
                                    Text(
                                        text = "Course Mappings",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                OutlinedButton(
                                    onClick = onFilterCodesClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderOutline),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text(
                                        text = "Batch Filters & Review",
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = TextSecondary
                                    )
                                }

                                OutlinedButton(
                                    onClick = onSyncPreviewClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderOutline),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text(
                                        text = "Sync Preview (Raw Fetch)",
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = TextSecondary
                                    )
                                }

                                var showDisconnectDialog by remember { mutableStateOf(false) }

                                if (showDisconnectDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showDisconnectDialog = false },
                                        title = { Text("Disconnect Moodle?", color = TextPrimary) },
                                        text = {
                                            Text(
                                                "This will remove your stored authentication token and institution URL from Buddie.",
                                                color = TextSecondary
                                            )
                                        },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    viewModel.disconnectMoodle()
                                                    showDisconnectDialog = false
                                                }
                                            ) {
                                                Text("Disconnect", color = StatusError, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDisconnectDialog = false }) {
                                                Text("Cancel", color = TextSecondary)
                                            }
                                        },
                                        containerColor = CharcoalSurfaceElevated,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showDisconnectDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, StatusError.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                                ) {
                                    Text(
                                        text = "Disconnect",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Connect to your institution's Moodle portal to sync deadlines and coursework directly into Buddie.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 17.sp
                                )

                                Button(
                                    onClick = onConnectMoodleClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentBlue,
                                        contentColor = TextPrimary
                                    )
                                ) {
                                    Text(
                                        text = "Connect Moodle",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StylePreviewCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(125.dp)
            .clip(CardShape)
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CharcoalSurfaceElevated else CharcoalSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) AccentBlueLight else BorderOutline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Miniature visual preview thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color(0xFF141417) else Color(0xFF19191E))
                    .border(
                        0.5.dp,
                        if (isSelected) AccentBlueLight.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (title.contains("Companion", ignoreCase = true)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B2B34))
                            )
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(TextSecondary)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF24242A))
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2B2B34))
                        )
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(AccentBlue)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) AccentBlueLight else TextPrimary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun CompanionPreviewCard(
    companionType: CompanionType,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clip(CardShape)
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CharcoalSurfaceElevated else CharcoalSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) AccentBlueLight else BorderOutline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF1E1E24) else CharcoalSurface)
                    .border(1.dp, if (isSelected) AccentBlueLight.copy(alpha = 0.6f) else BorderOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SelectedCompanionMascot(
                    companionType = companionType,
                    mood = MascotMood.CALM,
                    size = 36.dp,
                    animateIdle = isSelected
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AccentBlueLight else TextPrimary,
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun BatteryTroubleshootingCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val detectedBrand = remember { BatteryGuideBrand.detect() }
    var selectedBrand by rememberSaveable { mutableStateOf(detectedBrand) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(200),
        label = "troubleshoot_expand_rotation"
    )

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, BorderOutline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row (Clickable to toggle expand)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CharcoalSurfaceElevated)
                            .border(1.dp, BorderOutline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AccentBlueLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Battery Troubleshooting",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isExpanded) "Tailored for ${selectedBrand.skinName}" else "Keep bubble running on ${detectedBrand.brandName.split(" /")[0]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CharcoalSurfaceElevated)
                        .border(1.dp, BorderOutline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(13.dp)
                            .rotate(rotationAngle)
                    )
                }
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(220)) + fadeIn(tween(220)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(180))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider(color = DividerFaint, thickness = 0.5.dp)

                    // Detected brand badge + OEM selector chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "DEVICE GUIDANCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            if (selectedBrand == detectedBrand) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentBlue.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Auto-detected",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentBlueLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Scrollable brand selection chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(BatteryGuideBrand.entries.toTypedArray()) { brand ->
                                val isSelected = brand == selectedBrand
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentBlue else CharcoalSurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) AccentBlueLight else BorderOutline,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedBrand = brand }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (brand == detectedBrand) "${brand.brandName.split(" /")[0]} ★" else brand.brandName.split(" /")[0],
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Numbered Steps
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        selectedBrand.steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(CharcoalSurfaceElevated)
                                        .border(1.dp, BorderOutline, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentBlueLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    )
                                }

                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Pro-tip if available
                    selectedBrand.proTip?.let { tip ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CharcoalSurfaceElevated)
                                .border(1.dp, BorderOutline, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "💡 $tip",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Quick Jump Action Button
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("SettingsScreen", "Failed to open application details settings", e)
                                try {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                } catch (_: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CharcoalSurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderOutline),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 9.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Open Buddie App Settings",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = AccentBlueLight
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = AccentBlueLight,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

