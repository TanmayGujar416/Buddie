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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tanmay.buddie.presentation.theme.*
import com.tanmay.buddie.service.BubbleService

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onEditTimetableClick: () -> Unit
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
            .height(110.dp)
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
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AccentBlueLight else TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
