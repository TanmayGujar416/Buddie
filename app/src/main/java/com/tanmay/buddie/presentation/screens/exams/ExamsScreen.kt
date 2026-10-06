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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.presentation.components.EmptyStateView
import com.tanmay.buddie.presentation.components.ExamListSkeleton
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletingIds = remember { mutableStateListOf<String>() }
    val haptics = rememberAppHaptics()

    val currentList = remember(uiState.currentTab, uiState.upcomingExams, uiState.pastExams) {
        if (uiState.currentTab == ExamTab.UPCOMING) uiState.upcomingExams else uiState.pastExams
    }

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
            // Integrated Header: Edge-to-edge status bar blending with Mascot Mood Indicator
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
                        text = "${uiState.upcomingExams.size} upcoming • ${uiState.pastExams.size} past",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                MascotMoodIndicator(
                    mood = uiState.mascotMood,
                    companionType = uiState.companionType
                )
            }

            // Tab Filter: Upcoming vs Past
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, BorderOutline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val upcomingSelected = uiState.currentTab == ExamTab.UPCOMING
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (upcomingSelected) AccentBlue else Color.Transparent)
                            .clickable { viewModel.setTab(ExamTab.UPCOMING) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Upcoming (${uiState.upcomingExams.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (upcomingSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (upcomingSelected) Color.White else TextSecondary
                        )
                    }

                    val pastSelected = uiState.currentTab == ExamTab.PAST
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (pastSelected) AccentBlue else Color.Transparent)
                            .clickable { viewModel.setTab(ExamTab.PAST) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Past (${uiState.pastExams.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (pastSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (pastSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    ExamListSkeleton()
                }
                currentList.isEmpty() -> {
                    if (uiState.currentTab == ExamTab.UPCOMING) {
                        EmptyStateView(
                            title = "All Clear!",
                            message = "No upcoming exams on your schedule. You're completely in the clear!",
                            mood = uiState.mascotMood,
                            companionType = uiState.companionType,
                            actionLabel = "Add Exam",
                            onActionClick = onAddExamClick
                        )
                    } else {
                        EmptyStateView(
                            title = "No Past Exams",
                            message = "Completed exams will show up here for your records.",
                            mood = uiState.mascotMood,
                            companionType = uiState.companionType
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(currentList, key = { it.exam.id }) { item ->
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
                                    isPast = uiState.currentTab == ExamTab.PAST,
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
    isPast: Boolean = false,
    onClick: (ExamEntity) -> Unit,
    onDelete: (ExamEntity) -> Unit
) {
    val exam = item.exam
    val subject = item.subject
    val colorStr = subject?.color ?: "#888888"
    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch(e: Exception) { AccentBlueLight }

    val now = LocalDateTime.now()
    val isHappeningNow = !isPast && !now.isBefore(exam.datetime) && now.isBefore(exam.datetime.plusMinutes(exam.durationMinutes.toLong()))
    val daysUntil = ChronoUnit.DAYS.between(now.toLocalDate().atStartOfDay(), exam.datetime.toLocalDate().atStartOfDay())
    val isUrgent = !isPast && daysUntil in 0..3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable { onClick(exam) },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(
            1.dp,
            when {
                isHappeningNow -> StatusSuccess
                isUrgent -> StatusError.copy(alpha = 0.6f)
                else -> BorderOutline
            }
        )
    ) {
        Column {
            // Prominent Countdown Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when {
                            isHappeningNow -> StatusSuccessContainer
                            isUrgent -> StatusErrorContainer
                            isPast -> CharcoalSurfaceElevated
                            else -> color.copy(alpha = 0.16f)
                        }
                    )
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
                        color = when {
                            isHappeningNow -> StatusSuccess
                            isUrgent -> StatusOnErrorContainer
                            isPast -> TextSecondary
                            else -> TextPrimary
                        }
                    )

                    val countdownText = when {
                        isHappeningNow -> "● HAPPENING NOW"
                        isPast -> "Completed"
                        daysUntil == 0L -> "TODAY"
                        daysUntil == 1L -> "Tomorrow"
                        daysUntil in 2..7 -> "In $daysUntil days"
                        else -> "In $daysUntil days"
                    }

                    Text(
                        text = countdownText,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isHappeningNow -> StatusSuccess
                            isUrgent -> StatusError
                            isPast -> TextMuted
                            else -> AccentBlueLight
                        },
                        style = MaterialTheme.typography.titleSmall
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { onDelete(exam) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Exam",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Date & Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exam.datetime.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy • h:mm a")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "⏱️ ${exam.durationMinutes} mins",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                // Hall & Seat Chips
                val location = exam.roomOverride ?: exam.hall
                if (location.isNotBlank() || !exam.seatNumber.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (location.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CharcoalBackground,
                                border = BorderStroke(0.5.dp, BorderOutline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = AccentBlueLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = location,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        if (!exam.seatNumber.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CharcoalBackground,
                                border = BorderStroke(0.5.dp, BorderOutline)
                            ) {
                                Text(
                                    text = "🪑 Seat ${exam.seatNumber}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // Syllabus Scope
                if (!exam.syllabusScope.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CharcoalBackground.copy(alpha = 0.6f),
                        border = BorderStroke(0.5.dp, BorderOutline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "📖", fontSize = 12.sp)
                            Text(
                                text = "Syllabus: ${exam.syllabusScope}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Allowed Materials
                if (!exam.allowedMaterials.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CharcoalBackground.copy(alpha = 0.6f),
                        border = BorderStroke(0.5.dp, BorderOutline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "📝", fontSize = 12.sp)
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
    }
}
