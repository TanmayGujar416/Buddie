package com.tanmay.buddie.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tanmay.buddie.data.local.entity.SessionType
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.data.local.entity.SubmissionSource
import com.tanmay.buddie.presentation.components.EmptyStateView
import com.tanmay.buddie.presentation.components.SubmissionListSkeleton
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.components.mascot.MascotMoodHelper
import com.tanmay.buddie.presentation.components.mascot.MascotMoodIndicator
import com.tanmay.buddie.presentation.components.mascot.SelectedCompanionMascot
import com.tanmay.buddie.presentation.screens.timetable.SlotWithSubject
import com.tanmay.buddie.presentation.theme.*
import com.tanmay.buddie.presentation.util.rememberAppHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddSubmissionClick: () -> Unit,
    onEditSubmissionClick: (String) -> Unit,
    onNavigateToTimetable: (() -> Unit)? = null,
    onNavigateToExams: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val completingIds = remember { mutableStateListOf<String>() }
    val haptics = rememberAppHaptics()

    LaunchedEffect(Unit) {
        viewModel.syncMoodleOnOpen()
    }

    // Time-of-day greeting
    val currentHour = remember { LocalTime.now().hour }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning, scholar! ☀️"
            in 12..16 -> "Good afternoon! 🌤️"
            in 17..21 -> "Good evening! 🌙"
            else -> "Burning the midnight oil? 🌌"
        }
    }

    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
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
                onClick = onAddSubmissionClick,
                shape = SquircleShape,
                containerColor = AccentBlue,
                contentColor = TextPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Assignment")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Header: Greeting & persistent mascot mood pill
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    MascotMoodIndicator(
                        mood = uiState.mascotMood,
                        companionType = uiState.companionType
                    )
                }
            }

            // 2. Interactive Kawaii Companion Hero Card
            item {
                KawaiiCompanionHeroCard(
                    mood = uiState.mascotMood,
                    companionType = uiState.companionType,
                    activeTasksCount = uiState.allActiveSubmissions.size,
                    todaysClassesCount = uiState.todaysClasses.size,
                    upcomingExamsCount = uiState.upcomingExamsCount,
                    onNavigateToTimetable = onNavigateToTimetable,
                    onNavigateToExams = onNavigateToExams,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 3. Today's Classes Horizontal Horizon
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Today's Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (uiState.todaysClasses.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(AccentBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${uiState.todaysClasses.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentBlueLight
                                    )
                                }
                            }
                        }

                        if (onNavigateToTimetable != null) {
                            TextButton(
                                onClick = onNavigateToTimetable,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Full Grid",
                                        color = AccentBlueLight,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
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

                    if (uiState.todaysClasses.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = BorderStroke(1.dp, BorderOutline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "☕", fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = "No classes scheduled today",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Enjoy your break or review upcoming assignments!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.todaysClasses, key = { it.slot.id }) { item ->
                                TodayClassMiniCard(
                                    item = item,
                                    onClick = { onNavigateToTimetable?.invoke() }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Assignments Header & Filter Chips
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Deadlines & Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = if (uiState.submissions.isNotEmpty()) "${uiState.submissions.size} shown" else "0 items",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Smart Filter Chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TaskFilter.entries) { filter ->
                            val isSelected = filter == uiState.selectedFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptics.performShortHaptic()
                                    viewModel.setFilter(filter)
                                },
                                label = {
                                    Text(
                                        text = filter.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
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
                }
            }

            // 5. Tasks List / Empty State / Skeletons
            when {
                uiState.isLoading -> {
                    item {
                        SubmissionListSkeleton()
                    }
                }
                uiState.submissions.isEmpty() -> {
                    item {
                        EmptyStateView(
                            title = if (uiState.allActiveSubmissions.isEmpty()) "All Caught Up!" else "No Matching Tasks",
                            message = if (uiState.allActiveSubmissions.isEmpty())
                                "No upcoming assignments or deadlines. Relax and enjoy your free time!"
                            else
                                "No tasks found under \"${uiState.selectedFilter.label}\". Try selecting \"All\".",
                            mood = uiState.mascotMood,
                            companionType = uiState.companionType,
                            actionLabel = if (uiState.allActiveSubmissions.isEmpty()) "Add Assignment" else "Show All Tasks",
                            onActionClick = {
                                if (uiState.allActiveSubmissions.isEmpty()) {
                                    onAddSubmissionClick()
                                } else {
                                    viewModel.setFilter(TaskFilter.ALL)
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                else -> {
                    items(uiState.submissions, key = { it.submission.id }) { item ->
                        val isCompleting = item.submission.id in completingIds

                        AnimatedVisibility(
                            visible = !isCompleting,
                            enter = fadeIn(tween(250)) + expandVertically(tween(250)),
                            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) +
                                    fadeOut(tween(250)) +
                                    shrinkVertically(tween(250)),
                            modifier = Modifier
                                .animateItemPlacement()
                                .padding(horizontal = 16.dp)
                        ) {
                            SubmissionCard(
                                item = item,
                                onMarkDone = { sub ->
                                    haptics.performShortHaptic()
                                    scope.launch {
                                        completingIds.add(sub.id)
                                        delay(260)
                                        viewModel.markAsDone(sub)
                                        completingIds.remove(sub.id)

                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "\"${sub.title}\" marked done",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoMarkAsDone(sub)
                                        }
                                    }
                                },
                                onDelete = { sub ->
                                    haptics.performShortHaptic()
                                    scope.launch {
                                        completingIds.add(sub.id)
                                        delay(260)
                                        viewModel.deleteSubmission(sub)
                                        completingIds.remove(sub.id)

                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "\"${sub.title}\" deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreSubmission(sub)
                                        }
                                    }
                                },
                                onHide = { sub ->
                                    haptics.performShortHaptic()
                                    scope.launch {
                                        completingIds.add(sub.id)
                                        delay(260)
                                        viewModel.hideMoodleSubmission(sub)
                                        completingIds.remove(sub.id)

                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "\"${sub.title}\" hidden (not mine)",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.unhideMoodleSubmission(sub)
                                        }
                                    }
                                },
                                onClick = { onEditSubmissionClick(it.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Kawaii Companion Hero Card with speech bubble quotes and quick metrics.
 */
@Composable
fun KawaiiCompanionHeroCard(
    mood: MascotMood,
    companionType: CompanionType,
    activeTasksCount: Int,
    todaysClassesCount: Int,
    upcomingExamsCount: Int,
    onNavigateToTimetable: (() -> Unit)?,
    onNavigateToExams: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    var bounceTrigger by remember { mutableIntStateOf(0) }
    var quoteIndex by remember { mutableIntStateOf(0) }

    // Dialog messages tailored to mood
    val quotes = remember(mood, companionType) {
        val name = companionType.displayName
        when (mood) {
            MascotMood.CALM -> listOf(
                "All clear! You're totally on top of your schedule today. ☕✨",
                "Great job keeping everything organized! Time for a short break? 🌸",
                "$name says: Peace of mind unlocked! Ready for anything."
            )
            MascotMood.AWARE -> listOf(
                "Heads up! A few deadlines are creeping up in 2–3 days. 📚",
                "Steady pace wins the race! Tackle one step at a time. 🐾",
                "$name says: You're doing well! Just keep an eye on upcoming tasks."
            )
            MascotMood.URGENT -> listOf(
                "Focus mode! You have deadlines due within 24 hours! ⚡",
                "Take a deep breath! You've got the skills to knock these out. 💪",
                "$name is cheering for you! Let's get to work."
            )
            MascotMood.OVERDUE -> listOf(
                "Don't panic! Even overdue tasks get solved one checkmark at a time. ❤️",
                "Take a breath. Let's finish the most important item first! 🌸",
                "$name says: We can still fix this together!"
            )
        }
    }

    val currentQuote = quotes[quoteIndex % quotes.size]
    val moodColor = MascotMoodHelper.getMoodDotColor(mood)

    // Animated scale on mascot tap
    val scaleAnim by animateFloatAsState(
        targetValue = if (bounceTrigger > 0) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        finishedListener = {
            if (bounceTrigger > 0) bounceTrigger = 0
        },
        label = "hero_mascot_bounce"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, moodColor.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Section: Interactive Companion Avatar + Speech Bubble
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Interactive Mascot Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .scale(scaleAnim)
                        .clip(CircleShape)
                        .background(CharcoalSurfaceElevated)
                        .border(1.5.dp, moodColor.copy(alpha = 0.6f), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptics.performShortHaptic()
                            bounceTrigger++
                            quoteIndex++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    SelectedCompanionMascot(
                        companionType = companionType,
                        mood = mood,
                        size = 52.dp,
                        animateIdle = true
                    )
                }

                // Speech Bubble Card
                Card(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated),
                    border = BorderStroke(0.5.dp, BorderOutline),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            haptics.performShortHaptic()
                            quoteIndex++
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentQuote,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 17.sp
                        )
                        Text(
                            text = "Tap mascot to chat ✨",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Divider(color = DividerFaint, thickness = 0.5.dp)

            // Bottom Section: 3 Quick Metric Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    count = "$activeTasksCount",
                    label = "Tasks",
                    icon = Icons.Default.List,
                    accentColor = AccentBlueLight,
                    modifier = Modifier.weight(1f)
                )

                MetricChip(
                    count = "$todaysClassesCount",
                    label = "Classes",
                    icon = Icons.Default.DateRange,
                    accentColor = StatusSuccess,
                    onClick = onNavigateToTimetable,
                    modifier = Modifier.weight(1f)
                )

                MetricChip(
                    count = "$upcomingExamsCount",
                    label = "Exams",
                    icon = Icons.Default.Edit,
                    accentColor = if (upcomingExamsCount > 0) StatusWarning else TextSecondary,
                    onClick = onNavigateToExams,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricChip(
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalSurfaceElevated)
            .border(0.5.dp, BorderOutline, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = count,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Compact Today's Schedule Card shown horizontally in the schedule preview.
 */
@Composable
fun TodayClassMiniCard(
    item: SlotWithSubject,
    onClick: () -> Unit
) {
    val slot = item.slot
    val subject = item.subject
    val colorStr = subject?.color ?: "#888888"
    val subjectColor = try { Color(android.graphics.Color.parseColor(colorStr)) } catch (_: Exception) { AccentBlueLight }

    val now = LocalTime.now()
    val isLive = now.isAfter(slot.startTime) && now.isBefore(slot.endTime)
    val isUpcomingSoon = !isLive && now.isBefore(slot.startTime) && ChronoUnit.MINUTES.between(now, slot.startTime) in 0..45

    Card(
        modifier = Modifier
            .width(180.dp)
            .clip(CardShape)
            .clickable { onClick() },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(
            width = if (isLive) 1.5.dp else 1.dp,
            color = if (isLive) AccentBlue else BorderOutline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live / Status Badge + Session Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentBlue)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "● LIVE NOW",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (isUpcomingSoon) {
                    val minsLeft = ChronoUnit.MINUTES.between(now, slot.startTime)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusWarning.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "In $minsLeft min",
                            color = StatusWarning,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(subjectColor)
                    )
                }

                Text(
                    text = if (slot.sessionType == SessionType.LAB) "Lab" else "Lecture",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            // Subject Name
            Text(
                text = subject?.name ?: "Class",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Time & Room
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
            Text(
                text = "${slot.startTime.format(timeFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.5.sp
            )

            if (slot.room.isNotBlank()) {
                Text(
                    text = "Room: ${slot.room}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentBlueLight,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SubmissionCard(
    item: SubmissionWithSubject,
    onMarkDone: (SubmissionEntity) -> Unit,
    onDelete: (SubmissionEntity) -> Unit,
    onHide: ((SubmissionEntity) -> Unit)? = null,
    onClick: (SubmissionEntity) -> Unit
) {
    val sub = item.submission
    val subject = item.subject
    val colorStr = subject?.color ?: "#888888"
    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch(e: Exception) { AccentBlueLight }

    val now = LocalDateTime.now()
    val isOverdue = sub.deadline.isBefore(now)
    val hoursUntil = ChronoUnit.HOURS.between(now, sub.deadline)
    val isUrgent = !isOverdue && hoursUntil in 0..24

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable { onClick(sub) },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(
            1.dp,
            if (isOverdue) StatusError.copy(alpha = 0.5f)
            else if (isUrgent) Color(0xFFFF7A45).copy(alpha = 0.5f)
            else BorderOutline
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Urgency Banner on top
            if (isOverdue || isUrgent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isOverdue) StatusErrorContainer else Color(0xFF381E14))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isOverdue) "⚠️ OVERDUE" else "⚡ DUE WITHIN 24 HOURS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) StatusOnErrorContainer else Color(0xFFFF9E7A)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject color indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Text Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = sub.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))

                    // Subject & Deadline
                    val formattedDeadline = sub.deadline.format(DateTimeFormatter.ofPattern("MMM dd • h:mm a"))
                    val subjectName = subject?.name ?: "General"
                    Text(
                        text = "$subjectName • Due $formattedDeadline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // Room info if specified
                    val roomDisplay = sub.roomOverride ?: ""
                    if (roomDisplay.isNotBlank()) {
                        Text(
                            text = "Room: $roomDisplay",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    if (sub.moodleVanished) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StatusWarning.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚠️ Vanished from Moodle",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusWarning,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Action Buttons: Hide ("Not my batch") or Delete, and Checkmark
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sub.source == SubmissionSource.MOODLE) {
                        if (onHide != null) {
                            IconButton(
                                onClick = { onHide(sub) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Not my batch (Hide)",
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { onDelete(sub) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Submission",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { onMarkDone(sub) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark as Done",
                            tint = AccentBlueLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
