package com.tanmay.buddie.presentation.components.mascot.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.presentation.components.ExamListSkeleton
import com.tanmay.buddie.presentation.components.SubmissionListSkeleton
import com.tanmay.buddie.presentation.components.mascot.BuddieMascot
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.screens.exams.ExamWithSubject
import com.tanmay.buddie.presentation.screens.exams.ExamsViewModel
import com.tanmay.buddie.presentation.screens.home.HomeViewModel
import com.tanmay.buddie.presentation.screens.home.SubmissionWithSubject
import com.tanmay.buddie.presentation.screens.timetable.TimetableViewModel
import com.tanmay.buddie.presentation.theme.*
import com.tanmay.buddie.presentation.util.rememberAppHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun OverlayEmptyState(
    title: String,
    message: String,
    mood: MascotMood,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BuddieMascot(
            mood = mood,
            size = 56.dp,
            animateIdle = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = onOpenApp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text("Open App", color = AccentBlueLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun OverlaySubmissionCard(
    item: SubmissionWithSubject,
    onMarkDone: (SubmissionEntity) -> Unit,
    onDelete: (SubmissionEntity) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sub = item.submission
    val subject = item.subject
    val colorStr = subject?.color ?: "#888888"
    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch (_: Exception) { AccentBlueLight }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable { onClick() },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, BorderOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject color indicator dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))

            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sub.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val formattedDeadline = sub.deadline.format(DateTimeFormatter.ofPattern("MMM dd • h:mm a"))
                val subjectName = subject?.name ?: "General"
                Text(
                    text = "$subjectName • Due $formattedDeadline",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Action Buttons with generous 8dp spacing and full 48dp touch targets
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onDelete(sub) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Assignment",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onMarkDone(sub) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark as Done",
                        tint = AccentBlueLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OverlayExamCard(
    item: ExamWithSubject,
    onMarkDone: (ExamEntity) -> Unit,
    onDelete: (ExamEntity) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val exam = item.exam
    val subject = item.subject
    val colorStr = subject?.color ?: "#888888"
    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch (_: Exception) { AccentBlueLight }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable { onClick() },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, BorderOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject color indicator dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))

            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val formattedDate = exam.datetime.format(DateTimeFormatter.ofPattern("MMM dd • h:mm a"))
                val subjectName = subject?.name ?: "Exam"
                val location = exam.roomOverride ?: exam.hall
                val locationText = if (location.isNotBlank()) " • $location" else ""
                Text(
                    text = "$subjectName • $formattedDate$locationText",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Action Buttons with generous 8dp spacing and full 48dp touch targets
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onDelete(exam) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Exam",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onMarkDone(exam) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark Exam Done",
                        tint = AccentBlueLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SubmissionsSection(
    viewModel: HomeViewModel,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val completingIds = remember { mutableStateListOf<String>() }
    val haptics = rememberAppHaptics()

    Box(modifier = modifier.fillMaxWidth()) {
        when {
            uiState.isLoading -> {
                SubmissionListSkeleton(itemCount = 2)
            }
            uiState.submissions.isEmpty() -> {
                OverlayEmptyState(
                    title = "All Caught Up!",
                    message = "No upcoming assignments or deadlines.",
                    mood = uiState.mascotMood,
                    onOpenApp = onOpenApp
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.submissions, key = { it.submission.id }) { item ->
                        val isCompleting = item.submission.id in completingIds

                        AnimatedVisibility(
                            visible = !isCompleting,
                            enter = fadeIn(tween(200)),
                            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                        ) {
                            OverlaySubmissionCard(
                                item = item,
                                onMarkDone = { sub ->
                                    haptics.performShortHaptic()
                                    scope.launch {
                                        completingIds.add(sub.id)
                                        delay(220)
                                        viewModel.markAsDone(sub)
                                        completingIds.remove(sub.id)
                                    }
                                },
                                onDelete = { sub ->
                                    haptics.performShortHaptic()
                                    scope.launch {
                                        completingIds.add(sub.id)
                                        delay(220)
                                        viewModel.deleteSubmission(sub)
                                        completingIds.remove(sub.id)
                                    }
                                },
                                onClick = { onOpenApp() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableSection(
    viewModel: TimetableViewModel,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfWeek) }
    val days = DayOfWeek.entries
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    val daySlots = remember(uiState.slots, selectedDay) {
        uiState.slots
            .filter { it.slot.dayOfWeek == selectedDay }
            .sortedBy { it.slot.startTime }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Day selector row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(days) { day ->
                val isSelected = day == selectedDay
                val isToday = day == LocalDate.now().dayOfWeek
                val dayName = day.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) AccentBlue else CharcoalSurfaceElevated
                        )
                        .border(
                            width = 1.dp,
                            color = if (isToday && !isSelected) AccentBlueLight else BorderOutline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedDay = day }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else if (isToday) AccentBlueLight else TextSecondary
                    )
                }
            }
        }

        Divider(color = DividerFaint, thickness = 0.5.dp)

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlueLight, modifier = Modifier.size(28.dp))
            }
        } else if (daySlots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Classes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Free day on ${selectedDay.getDisplayName(TextStyle.FULL, Locale.getDefault())}!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = onOpenApp) {
                        Text("View Full Timetable", color = AccentBlueLight, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daySlots, key = { it.slot.id }) { item ->
                    val subject = item.subject
                    val slot = item.slot
                    val colorStr = subject?.color ?: "#888888"
                    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch (_: Exception) { AccentBlueLight }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenApp() },
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, BorderOutline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subject?.name ?: "Unknown Subject",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "${slot.startTime.format(timeFormatter)} - ${slot.endTime.format(timeFormatter)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    if (slot.room.isNotBlank()) {
                                        Text(
                                            text = "• ${slot.room}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AccentBlueLight,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            Text(
                                text = slot.sessionType.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamsSection(
    viewModel: ExamsViewModel,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val deletingIds = remember { mutableStateListOf<String>() }
    val haptics = rememberAppHaptics()

    Box(modifier = modifier.fillMaxWidth()) {
        when {
            uiState.isLoading -> {
                ExamListSkeleton(itemCount = 2)
            }
            uiState.exams.isEmpty() -> {
                OverlayEmptyState(
                    title = "No Upcoming Exams",
                    message = "No exams scheduled. Relax and enjoy your free time!",
                    mood = uiState.mascotMood,
                    onOpenApp = onOpenApp
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.exams, key = { it.exam.id }) { item ->
                        val isDeleting = item.exam.id in deletingIds

                        AnimatedVisibility(
                            visible = !isDeleting,
                            enter = fadeIn(tween(200)),
                            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                        ) {
                            OverlayExamCard(
                                item = item,
                                onClick = { onOpenApp() },
                                onDelete = { exam ->
                                    haptics.performShortHaptic()
                                    scope.launch {
                                        deletingIds.add(exam.id)
                                        delay(220)
                                        viewModel.deleteExam(exam)
                                        deletingIds.remove(exam.id)
                                    }
                                },
                                onMarkDone = { exam ->
                                    haptics.performShortHaptic()
                                    scope.launch {
                                        deletingIds.add(exam.id)
                                        delay(220)
                                        viewModel.deleteExam(exam)
                                        deletingIds.remove(exam.id)
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
