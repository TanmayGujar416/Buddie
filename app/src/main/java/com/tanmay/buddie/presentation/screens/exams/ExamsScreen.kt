package com.tanmay.buddie.presentation.screens.exams

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.presentation.components.EmptyStateView
import com.tanmay.buddie.presentation.components.ExamListSkeleton
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.components.mascot.MascotMoodIndicator
import com.tanmay.buddie.presentation.theme.*
import com.tanmay.buddie.presentation.util.rememberAppHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExamsScreen(
    viewModel: ExamsViewModel = hiltViewModel(),
    onAddExamClick: () -> Unit,
    onEditExamClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletingIds = remember { mutableStateListOf<String>() }
    val haptics = rememberAppHaptics()

    Scaffold(
        containerColor = CharcoalBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CharcoalSurfaceElevated,
                    contentColor = TextPrimary,
                    actionColor = AccentBlueLight,
                    shape = DialogShape
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExamClick,
                shape = SquircleShape,
                containerColor = AccentBlue,
                contentColor = TextPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exam")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Integrated Header: Edge-to-edge status bar blending with persistent Mascot Mood Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Exams",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (uiState.exams.isNotEmpty()) "${uiState.exams.size} scheduled" else "No exams scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Persistent Mascot Mood Indicator
                MascotMoodIndicator(
                    mood = uiState.mascotMood
                )
            }

            when {
                uiState.isLoading -> {
                    ExamListSkeleton()
                }
                uiState.exams.isEmpty() -> {
                    EmptyStateView(
                        title = "All Clear!",
                        message = "No upcoming exams on your schedule. You're completely in the clear!",
                        mood = uiState.mascotMood,
                        actionLabel = "Add Exam",
                        onActionClick = onAddExamClick
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.exams, key = { it.exam.id }) { item ->
                            val isDeleting = item.exam.id in deletingIds

                            AnimatedVisibility(
                                visible = !isDeleting,
                                enter = fadeIn(tween(250)) + expandVertically(tween(250)),
                                exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(250)) +
                                        fadeOut(tween(250)) +
                                        shrinkVertically(tween(250)),
                                modifier = Modifier.animateItemPlacement()
                            ) {
                                ExamCard(
                                    item = item,
                                    onClick = { onEditExamClick(it.id) },
                                    onDelete = { exam ->
                                        haptics.performShortHaptic()
                                        scope.launch {
                                            deletingIds.add(exam.id)
                                            delay(260)
                                            viewModel.deleteExam(exam)
                                            deletingIds.remove(exam.id)

                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            val result = snackbarHostState.showSnackbar(
                                                message = "\"${exam.title}\" deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.restoreExam(exam)
                                            }
                                        }
                                    },
                                    onMarkDone = { exam ->
                                        haptics.performShortHaptic()
                                        scope.launch {
                                            deletingIds.add(exam.id)
                                            delay(260)
                                            viewModel.deleteExam(exam)
                                            deletingIds.remove(exam.id)

                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            val result = snackbarHostState.showSnackbar(
                                                message = "\"${exam.title}\" completed",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.restoreExam(exam)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamCard(
    item: ExamWithSubject,
    onClick: (ExamEntity) -> Unit,
    onDelete: (ExamEntity) -> Unit,
    onMarkDone: (ExamEntity) -> Unit
) {
    val exam = item.exam
    val subject = item.subject
    val colorStr = subject?.color ?: "#888888"
    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch(e: Exception) { AccentBlueLight }

    val daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), exam.datetime)
    val isUrgent = daysUntil in 0..3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable { onClick(exam) },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, if (isUrgent) StatusError.copy(alpha = 0.5f) else BorderOutline)
    ) {
        Column {
            // Prominent Countdown Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isUrgent) StatusErrorContainer else color.copy(alpha = 0.16f))
                    .padding(vertical = 10.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject?.name ?: "Unknown Subject",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isUrgent) StatusOnErrorContainer else TextPrimary
                    )

                    val countdownText = when {
                        daysUntil < 0 -> "Completed"
                        daysUntil == 0L -> "TODAY"
                        daysUntil == 1L -> "Tomorrow"
                        else -> "In $daysUntil days"
                    }

                    Text(
                        text = countdownText,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUrgent) StatusError else AccentBlueLight,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Exam Details
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = exam.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onDelete(exam) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Exam",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { onMarkDone(exam) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Mark Exam Done",
                                tint = AccentBlueLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = exam.datetime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy • h:mm a")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${exam.durationMinutes} mins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Location Details
                val location = exam.roomOverride ?: exam.hall
                if (location.isNotBlank()) {
                    val seatText = if (!exam.seatNumber.isNullOrBlank()) " • Seat: ${exam.seatNumber}" else ""
                    Text(
                        text = "Location: $location$seatText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }

                if (!exam.syllabusScope.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Syllabus: ${exam.syllabusScope}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                if (!exam.allowedMaterials.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Allowed: ${exam.allowedMaterials}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
