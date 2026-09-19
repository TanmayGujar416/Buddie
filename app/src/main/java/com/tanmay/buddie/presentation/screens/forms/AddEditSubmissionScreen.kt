package com.tanmay.buddie.presentation.screens.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanmay.buddie.data.local.entity.SessionType
import com.tanmay.buddie.presentation.components.DateTimePickerField
import com.tanmay.buddie.presentation.components.SubjectDropdown
import com.tanmay.buddie.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubmissionScreen(
    viewModel: AddEditSubmissionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            onNavigateBack()
        }
    }

    val isSaveEnabled = uiState.title.isNotBlank() && uiState.subjectId != null

    Scaffold(
        containerColor = CharcoalBackground,
        floatingActionButton = {
            if (isSaveEnabled) {
                FloatingActionButton(
                    onClick = { viewModel.saveSubmission() },
                    shape = SquircleShape,
                    containerColor = AccentBlue,
                    contentColor = TextPrimary
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save Submission")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Integrated Header: Edge-to-edge status bar blending
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Submission Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Track homework, assignments, and reports",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                TextButton(
                    onClick = { viewModel.saveSubmission() },
                    enabled = isSaveEnabled
                ) {
                    Text(
                        text = "Save",
                        fontWeight = FontWeight.Bold,
                        color = if (isSaveEnabled) AccentBlueLight else TextDisabled
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val inputColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlueLight,
                    unfocusedBorderColor = BorderOutline,
                    focusedLabelColor = AccentBlueLight,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentBlueLight
                )

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Assignment Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = InputFieldShape,
                    colors = inputColors
                )

                SubjectDropdown(
                    subjects = uiState.subjects,
                    selectedSubjectId = uiState.subjectId,
                    onSubjectSelected = { viewModel.updateSubject(it) }
                )

                // Session Type Toggle
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Link to Timetable Session",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = uiState.sessionType == SessionType.LECTURE,
                            onClick = {
                                viewModel.updateSessionType(if (uiState.sessionType == SessionType.LECTURE) null else SessionType.LECTURE)
                            },
                            label = { Text("Lecture") },
                            shape = ChipShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlue,
                                selectedLabelColor = TextPrimary,
                                containerColor = CharcoalSurface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = BorderOutline,
                                selectedBorderColor = AccentBlue
                            )
                        )
                        FilterChip(
                            selected = uiState.sessionType == SessionType.LAB,
                            onClick = {
                                viewModel.updateSessionType(if (uiState.sessionType == SessionType.LAB) null else SessionType.LAB)
                            },
                            label = { Text("Lab") },
                            shape = ChipShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlue,
                                selectedLabelColor = TextPrimary,
                                containerColor = CharcoalSurface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = BorderOutline,
                                selectedBorderColor = AccentBlue
                            )
                        )
                    }
                }

                // Room Override
                OutlinedTextField(
                    value = uiState.roomOverride,
                    onValueChange = { viewModel.updateRoomOverride(it) },
                    label = { Text("Room Override (Optional)") },
                    placeholder = { Text("Takes priority over timetable room", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = InputFieldShape,
                    colors = inputColors
                )

                // Deadline Picker
                DateTimePickerField(
                    label = "Deadline",
                    value = uiState.deadline,
                    onValueChange = { viewModel.updateDeadline(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
