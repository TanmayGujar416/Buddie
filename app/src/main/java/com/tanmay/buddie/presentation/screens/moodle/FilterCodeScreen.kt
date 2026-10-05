@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tanmay.buddie.presentation.screens.moodle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanmay.buddie.data.local.entity.FilterCode
import com.tanmay.buddie.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterCodeScreen(
    onNavigateBack: () -> Unit,
    viewModel: FilterCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CharcoalBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Batch Filters & Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Allow-list filtering by section / batch code",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshItems() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.subjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            } else if (uiState.subjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No subjects mapped to Moodle courses yet.\nMap subjects under Settings > Course Mappings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Explanatory guidance banner
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated),
                            border = BorderStroke(1.dp, BorderOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AccentBlueLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Smart Allow-List Filtering",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Configure only YOUR batch codes (e.g. C1 or D3). Other batch codes will be automatically hidden while course-wide assignments stay visible.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    items(uiState.subjects, key = { it.subject.id }) { subjectSection ->
                        SubjectFilterSectionCard(
                            section = subjectSection,
                            onToggleExpand = { viewModel.toggleSubjectExpanded(subjectSection.subject.id) },
                            onStartCalibration = { viewModel.startCalibration(subjectSection.subject.id) },
                            onCancelCalibration = { viewModel.cancelCalibration(subjectSection.subject.id) },
                            onSetCalibrationLabel = { patternId, isMine ->
                                viewModel.setCalibrationPatternLabel(subjectSection.subject.id, patternId, isMine)
                            },
                            onToggleMineProposal = { code ->
                                viewModel.toggleMineProposal(subjectSection.subject.id, code)
                            },
                            onToggleNotMineProposal = { code ->
                                viewModel.toggleNotMineProposal(subjectSection.subject.id, code)
                            },
                            onConfirmCalibration = { viewModel.confirmAndSaveCalibration(subjectSection.subject.id) },
                            onAddFilterCode = { code, isMine ->
                                viewModel.addFilterCode(subjectSection.subject.id, code, isMine)
                            },
                            onFlipFilterCode = { viewModel.flipFilterCode(it) },
                            onDeleteFilterCode = { viewModel.deleteFilterCode(it.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectFilterSectionCard(
    section: SubjectFilterSectionUi,
    onToggleExpand: () -> Unit,
    onStartCalibration: () -> Unit,
    onCancelCalibration: () -> Unit,
    onSetCalibrationLabel: (patternId: String, isMine: Boolean) -> Unit,
    onToggleMineProposal: (code: String) -> Unit,
    onToggleNotMineProposal: (code: String) -> Unit,
    onConfirmCalibration: () -> Unit,
    onAddFilterCode: (code: String, isMine: Boolean) -> Unit,
    onFlipFilterCode: (FilterCode) -> Unit,
    onDeleteFilterCode: (FilterCode) -> Unit
) {
    var newCodeText by remember { mutableStateOf("") }
    var newCodeIsMine by remember { mutableStateOf(true) }

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, BorderOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (section.mappedCourseNames.isNotEmpty()) {
                        Text(
                            text = section.mappedCourseNames.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Retargeted Warning Badge: shows when items have tokens matching NO configured code
                    if (section.hasWarningBadge) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusWarning.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusWarning,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${section.unresolvedCount} unresolved",
                                    color = StatusWarning,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else if (section.filterCodes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${section.filterCodes.size} configured",
                                color = StatusSuccess,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = if (section.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (section.isExpanded) "Collapse" else "Expand",
                        tint = TextSecondary
                    )
                }
            }

            AnimatedVisibility(
                visible = section.isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Divider(color = DividerFaint)

                    // Calibration Mode
                    if (section.isCalibrating) {
                        CalibrationFlowSection(
                            patterns = section.calibrationPatterns,
                            proposal = section.calibrationProposal,
                            confirmedMine = section.confirmedMineProposals,
                            confirmedNotMine = section.confirmedNotMineProposals,
                            onSetLabel = onSetCalibrationLabel,
                            onToggleMineProposal = onToggleMineProposal,
                            onToggleNotMineProposal = onToggleNotMineProposal,
                            onConfirm = onConfirmCalibration,
                            onCancel = onCancelCalibration
                        )
                    } else {
                        // If no filter codes yet and items exist: show Calibration prompt button
                        if (section.filterCodes.isEmpty()) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated),
                                border = BorderStroke(1.dp, BorderOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "No filter codes configured yet",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Buddie can automatically calibrate your codes by asking you about a few sample assignments.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    if (section.totalFetchedCount > 0) {
                                        Button(
                                            onClick = onStartCalibration,
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(text = "Start Calibration Flow", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Calibration helper button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Filter Codes",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                if (section.totalFetchedCount > 0) {
                                    TextButton(onClick = onStartCalibration) {
                                        Text(
                                            text = "Re-calibrate",
                                            color = AccentBlueLight,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Configured codes list
                            section.filterCodes.forEach { codeItem ->
                                FilterCodeItemCard(
                                    filterCode = codeItem,
                                    onFlip = { onFlipFilterCode(codeItem) },
                                    onDelete = { onDeleteFilterCode(codeItem) }
                                )
                            }
                        }

                        // Add Code Manually Row
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Add Code Manually",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newCodeText,
                                    onValueChange = { newCodeText = it },
                                    placeholder = { Text("Code (e.g. C1)", fontSize = 13.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentBlueLight,
                                        unfocusedBorderColor = BorderOutline,
                                        focusedContainerColor = CharcoalSurfaceElevated,
                                        unfocusedContainerColor = CharcoalSurfaceElevated,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                FilterChip(
                                    selected = newCodeIsMine,
                                    onClick = { newCodeIsMine = !newCodeIsMine },
                                    label = {
                                        Text(
                                            text = if (newCodeIsMine) "Mine" else "Excluded",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (newCodeIsMine) StatusSuccess.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f),
                                        selectedLabelColor = if (newCodeIsMine) StatusSuccess else StatusError
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        if (newCodeText.isNotBlank()) {
                                            onAddFilterCode(newCodeText.trim(), newCodeIsMine)
                                            newCodeText = ""
                                        }
                                    },
                                    enabled = newCodeText.isNotBlank(),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (newCodeText.isNotBlank()) AccentBlue else CharcoalSurfaceElevated)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Code",
                                        tint = if (newCodeText.isNotBlank()) TextPrimary else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun CalibrationFlowSection(
    patterns: List<CalibrationPatternUi>,
    proposal: com.tanmay.buddie.domain.sync.CalibrationProposal?,
    confirmedMine: Set<String>,
    confirmedNotMine: Set<String>,
    onSetLabel: (patternId: String, isMine: Boolean) -> Unit,
    onToggleMineProposal: (code: String) -> Unit,
    onToggleNotMineProposal: (code: String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Calibration: Sample Labels",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlueLight
                )
                Text(
                    text = "Mark each sample as yours or not. Capped at ${patterns.size} unique patterns.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )
            }
            TextButton(onClick = onCancel) {
                Text("Cancel", color = TextMuted, fontSize = 12.sp)
            }
        }

        // List of deduplicated patterns
        patterns.forEach { pattern ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated),
                border = BorderStroke(1.dp, BorderOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = pattern.representativeTitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tokens badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            pattern.tokens.forEach { tok ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CharcoalBackground)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tok,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Yes / No toggles
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // No Button
                            OutlinedButton(
                                onClick = { onSetLabel(pattern.id, false) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (pattern.userLabel == false) StatusError else BorderOutline
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (pattern.userLabel == false) StatusError.copy(alpha = 0.2f) else CharcoalSurface,
                                    contentColor = if (pattern.userLabel == false) StatusError else TextSecondary
                                )
                            ) {
                                Text("No", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            // Yes Button
                            OutlinedButton(
                                onClick = { onSetLabel(pattern.id, true) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (pattern.userLabel == true) StatusSuccess else BorderOutline
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (pattern.userLabel == true) StatusSuccess.copy(alpha = 0.2f) else CharcoalSurface,
                                    contentColor = if (pattern.userLabel == true) StatusSuccess else TextSecondary
                                )
                            ) {
                                Text("Yes", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Proposal Confirmation Card
        if (proposal != null && (proposal.proposedMineCodes.isNotEmpty() || proposal.proposedNotMineCodes.isNotEmpty())) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalBackground),
                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Proposed Filter Codes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlueLight
                    )

                    if (proposal.proposedMineCodes.isNotEmpty()) {
                        Text(
                            text = "Tokens unique to your \"Yes\" items (isMine = true):",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusSuccess
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            proposal.proposedMineCodes.forEach { code ->
                                val selected = code in confirmedMine
                                FilterChip(
                                    selected = selected,
                                    onClick = { onToggleMineProposal(code) },
                                    label = { Text(code, fontWeight = FontWeight.Bold) },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StatusSuccess.copy(alpha = 0.25f),
                                        selectedLabelColor = StatusSuccess,
                                        selectedLeadingIconColor = StatusSuccess
                                    )
                                )
                            }
                        }
                    }

                    if (proposal.proposedNotMineCodes.isNotEmpty()) {
                        Text(
                            text = "Tokens found only in \"No\" items (isMine = false):",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusError
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            proposal.proposedNotMineCodes.forEach { code ->
                                val selected = code in confirmedNotMine
                                FilterChip(
                                    selected = selected,
                                    onClick = { onToggleNotMineProposal(code) },
                                    label = { Text(code, fontWeight = FontWeight.Bold) },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StatusError.copy(alpha = 0.25f),
                                        selectedLabelColor = StatusError,
                                        selectedLeadingIconColor = StatusError
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = "Review and confirm the proposals above before saving. Nothing is auto-saved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = onConfirm,
                        enabled = confirmedMine.isNotEmpty() || confirmedNotMine.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm and Save Filter Codes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterCodeItemCard(
    filterCode: FilterCode,
    onFlip: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated),
        border = BorderStroke(1.dp, BorderOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CharcoalBackground)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = filterCode.code,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (filterCode.isMine) StatusSuccess.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (filterCode.isMine) "MINE" else "EXCLUDED",
                        color = if (filterCode.isMine) StatusSuccess else StatusError,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onFlip) {
                    Text(
                        text = if (filterCode.isMine) "Flip to Exclude" else "Flip to Mine",
                        color = AccentBlueLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete code",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
