package com.tanmay.buddie.presentation.screens.home

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
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import com.tanmay.buddie.presentation.components.EmptyStateView
import com.tanmay.buddie.presentation.components.SubmissionListSkeleton
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.components.mascot.MascotMoodIndicator
import com.tanmay.buddie.presentation.util.rememberAppHaptics

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddSubmissionClick: () -> Unit,
    onEditSubmissionClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val completingIds = remember { mutableStateListOf<String>() }
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
                onClick = onAddSubmissionClick,
                shape = SquircleShape,
                containerColor = AccentBlue,
                contentColor = TextPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Assignment")
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
                        text = "Upcoming Deadlines",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (uiState.submissions.isNotEmpty()) "${uiState.submissions.size} pending tasks" else "All caught up",
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
                    SubmissionListSkeleton()
                }
                uiState.submissions.isEmpty() -> {
                    EmptyStateView(
                        title = "All Caught Up!",
                        message = "No upcoming assignments or deadlines. Relax and enjoy your free time!",
                        mood = uiState.mascotMood,
                        actionLabel = "Add Assignment",
                        onActionClick = onAddSubmissionClick
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.submissions, key = { it.submission.id }) { item ->
                            val isCompleting = item.submission.id in completingIds

                            AnimatedVisibility(
                                visible = !isCompleting,
                                enter = fadeIn(tween(250)) + expandVertically(tween(250)),
                                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) +
                                        fadeOut(tween(250)) +
                                        shrinkVertically(tween(250)),
                                modifier = Modifier.animateItemPlacement()
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
                                    onClick = { onEditSubmissionClick(it.id) }
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
fun SubmissionCard(
    item: SubmissionWithSubject,
    onMarkDone: (SubmissionEntity) -> Unit,
    onDelete: (SubmissionEntity) -> Unit,
    onClick: (SubmissionEntity) -> Unit
) {
    val sub = item.submission
    val subject = item.subject
    val colorStr = subject?.color ?: "#888888"
    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch(e: Exception) { AccentBlueLight }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable { onClick(sub) },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, BorderOutline)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject color indicator
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(14.dp))

            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sub.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))

                // Subject & Deadline
                val formattedDeadline = sub.deadline.format(DateTimeFormatter.ofPattern("MMM dd • h:mm a"))
                val subjectName = subject?.name ?: "General"
                Text(
                    text = "$subjectName • Due $formattedDeadline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                // Room info if specified
                val roomDisplay = sub.roomOverride ?: ""
                if (roomDisplay.isNotBlank()) {
                    Text(
                        text = "Room: $roomDisplay",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Action Buttons: Delete & Mark Done (48dp accessible touch target)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onDelete(sub) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Submission",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
