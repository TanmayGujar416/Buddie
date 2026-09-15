package com.tanmay.buddie.presentation.components.mascot.card

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanmay.buddie.presentation.components.mascot.BuddieMascot
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.screens.exams.ExamsViewModel
import com.tanmay.buddie.presentation.screens.home.HomeViewModel
import com.tanmay.buddie.presentation.screens.timetable.TimetableViewModel
import com.tanmay.buddie.presentation.theme.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun CompanionHeaderShell(
    mood: MascotMood,
    homeViewModel: HomeViewModel,
    timetableViewModel: TimetableViewModel,
    examsViewModel: ExamsViewModel,
    onCollapse: () -> Unit,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(CardSection.SUBMISSIONS) }

    val homeUiState by homeViewModel.uiState.collectAsState()
    val examsUiState by examsViewModel.uiState.collectAsState()

    // Urgency indicators
    val hasUrgentSubmission = remember(homeUiState.submissions) {
        homeUiState.submissions.any {
            ChronoUnit.HOURS.between(LocalDateTime.now(), it.submission.deadline) in 0..24
        }
    }
    val hasUrgentExam = remember(examsUiState.exams) {
        examsUiState.exams.any {
            ChronoUnit.DAYS.between(LocalDateTime.now(), it.exam.datetime) in 0..3
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                // Prevent tap inside from collapsing
            }
    ) {
        // Unified Card Container with perched top offset
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            border = BorderStroke(1.dp, BorderOutline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .heightIn(min = 180.dp, max = 500.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Companion Header Bar inside Card bounds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(CharcoalSurfaceElevated)
                        .padding(start = 68.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Buddie Companion",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = mood.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (mood) {
                                MascotMood.CALM -> AccentBlueLight
                                MascotMood.AWARE -> StatusWarning
                                MascotMood.URGENT -> StatusError
                                MascotMood.OVERDUE -> StatusError
                            },
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Collapse",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Tab bar with urgency badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CardSection.entries.forEach { section ->
                        val isSelected = section == selectedSection
                        val showBadge = when (section) {
                            CardSection.SUBMISSIONS -> hasUrgentSubmission
                            CardSection.EXAMS -> hasUrgentExam
                            CardSection.TIMETABLE -> false
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentBlue else CharcoalSurfaceElevated)
                                .clickable { selectedSection = section }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = section.title,
                                    tint = if (isSelected) TextPrimary else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (showBadge) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(StatusError)
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = DividerFaint, thickness = 0.5.dp)

                // Content Section adapting to height
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    Crossfade(
                        targetState = selectedSection,
                        animationSpec = tween(150),
                        label = "companion_section_crossfade"
                    ) { section ->
                        when (section) {
                            CardSection.SUBMISSIONS -> SubmissionsSection(
                                viewModel = homeViewModel,
                                onOpenApp = onOpenApp
                            )
                            CardSection.TIMETABLE -> TimetableSection(
                                viewModel = timetableViewModel,
                                onOpenApp = onOpenApp
                            )
                            CardSection.EXAMS -> ExamsSection(
                                viewModel = examsViewModel,
                                onOpenApp = onOpenApp
                            )
                        }
                    }
                }

                Divider(color = DividerFaint, thickness = 0.5.dp)

                // Footer with Open App button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap outside to collapse",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    TextButton(
                        onClick = onOpenApp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Open Buddie", color = AccentBlueLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AccentBlueLight, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        }

        // Mascot Avatar perched on the card's top edge
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(CharcoalSurfaceElevated)
                .border(2.dp, BorderOutline, CircleShape)
                .clickable { onCollapse() },
            contentAlignment = Alignment.Center
        ) {
            BuddieMascot(
                mood = mood,
                size = 36.dp,
                animateIdle = true
            )
        }
    }
}
