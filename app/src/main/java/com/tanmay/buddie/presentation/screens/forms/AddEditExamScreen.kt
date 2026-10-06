package com.tanmay.buddie.presentation.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tanmay.buddie.presentation.components.DateTimePickerField
import com.tanmay.buddie.presentation.components.SubjectDropdown
import com.tanmay.buddie.presentation.theme.*

@Composable
fun AddEditExamScreen(
    viewModel: AddEditExamViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            onNavigateBack()
        }
    }

    val isSaveEnabled = uiState.title.isNotBlank() && uiState.subjectId != null && uiState.hall.isNotBlank()

    Scaffold(
        containerColor = CharcoalBackground,
        floatingActionButton = {
            if (isSaveEnabled) {
                FloatingActionButton(
                    onClick = { viewModel.saveExam() },
                    shape = SquircleShape,
                    containerColor = AccentBlue,
                    contentColor = TextPrimary
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save Exam")
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
                        text = "Exam Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Schedule midterms, finals, and quizzes",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                TextButton(
                    onClick = { viewModel.saveExam() },
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
                    onValueChange = { viewModel.updateField(title = it) },
                    label = { Text("Exam Title (e.g. Midterm 1)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = InputFieldShape,
                    colors = inputColors
                )

                SubjectDropdown(
                    subjects = uiState.subjects,
                    selectedSubjectId = uiState.subjectId,
                    onSubjectSelected = { viewModel.updateField(subjectId = it) },
                    onCreateSubject = { name, color -> viewModel.createSubject(name, color) }
                )

                DateTimePickerField(
                    label = "Exam Date & Time",
                    value = uiState.datetime,
                    onValueChange = { viewModel.updateField(datetime = it) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.hall,
                        onValueChange = { viewModel.updateField(hall = it) },
                        label = { Text("Exam Hall") },
                        modifier = Modifier.weight(1f),
                        shape = InputFieldShape,
                        colors = inputColors
                    )
                    OutlinedTextField(
                        value = uiState.seatNumber,
                        onValueChange = { viewModel.updateField(seatNumber = it) },
                        label = { Text("Seat No.") },
                        modifier = Modifier.weight(1f),
                        shape = InputFieldShape,
                        colors = inputColors
                    )
                }

                OutlinedTextField(
                    value = uiState.durationMinutes,
                    onValueChange = { viewModel.updateField(durationMinutes = it) },
                    label = { Text("Duration (mins)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = InputFieldShape,
                    colors = inputColors
                )

                OutlinedTextField(
                    value = uiState.roomOverride,
                    onValueChange = { viewModel.updateField(roomOverride = it) },
                    label = { Text("Room Override (Optional)") },
                    placeholder = { Text("Overrides regular timetable logic", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = InputFieldShape,
                    colors = inputColors
                )

                OutlinedTextField(
                    value = uiState.syllabusScope,
                    onValueChange = { viewModel.updateField(syllabusScope = it) },
                    label = { Text("Syllabus Scope") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = InputFieldShape,
                    colors = inputColors
                )

                OutlinedTextField(
                    value = uiState.allowedMaterials,
                    onValueChange = { viewModel.updateField(allowedMaterials = it) },
                    label = { Text("Allowed Materials") },
                    placeholder = { Text("e.g. Calculator, Cheatsheet", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = InputFieldShape,
                    colors = inputColors
                )
            }
        }
    }
}
