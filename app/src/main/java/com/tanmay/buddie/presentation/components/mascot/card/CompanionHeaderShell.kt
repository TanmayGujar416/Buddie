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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.components.mascot.MascotMoodHelper
import com.tanmay.buddie.presentation.components.mascot.SelectedCompanionMascot
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
    onOpenApp: (String) -> Unit,
    modifier: Modifier = Modifier,
    companionType: CompanionType = CompanionType.BUDDIE,
    animateIdle: Boolean = true
) {
    var selectedSection by remember { mutableStateOf(CardSection.SUBMISSIONS) }

    val activeRoute = remember(selectedSection) {
        when (selectedSection) {
            CardSection.SUBMISSIONS -> "home"
            CardSection.TIMETABLE -> "timetable"
            CardSection.EXAMS -> "exams"
        }
    }

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

    // Modern Unified Floating Card Container (Matches Mockup 1:1)
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141417)),
        border = BorderStroke(1.dp, Color(0xFF24242B)),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp, max = 520.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Prevent tap inside card from collapsing
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Header Bar (Inside Card bounds)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Avatar + Title + Status Dot & Text (Clickable to open app)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenApp(activeRoute) }
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E24))
                            .border(1.dp, Color(0xFF2B2B34), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        SelectedCompanionMascot(
                            companionType = companionType,
                            mood = mood,
                            size = 32.dp,
                            animateIdle = animateIdle
                        )
                    }

                    Column {
                        Text(
                            text = "Buddie Companion",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 18.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            // 4 Distinct at-a-glance status dot colors
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MascotMoodHelper.getMoodDotColor(mood))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = mood.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Right: Circular Close Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF24242A))
                        .clickable(onClick = onCollapse),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Collapse",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Segmented Pill Tab Bar ("Assignments", "Timetable", "Exams")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D0D10))
                    .padding(3.5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF282830) else Color.Transparent)
                                .clickable { selectedSection = section }
                                .padding(vertical = 7.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimary else Color(0xFF71717A),
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                if (showBadge) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Content Area adapting to height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                Crossfade(
                    targetState = selectedSection,
                    animationSpec = tween(160),
                    label = "companion_section_crossfade"
                ) { section ->
                    when (section) {
                        CardSection.SUBMISSIONS -> SubmissionsSection(
                            viewModel = homeViewModel,
                            onOpenApp = { onOpenApp("home") },
                            companionType = companionType,
                            animateIdle = animateIdle
                        )
                        CardSection.TIMETABLE -> TimetableSection(
                            viewModel = timetableViewModel,
                            onOpenApp = { onOpenApp("timetable") },
                            companionType = companionType,
                            animateIdle = animateIdle
                        )
                        CardSection.EXAMS -> ExamsSection(
                            viewModel = examsViewModel,
                            onOpenApp = { onOpenApp("exams") },
                            companionType = companionType,
                            animateIdle = animateIdle
                        )
                    }
                }
            }

            Divider(
                color = Color(0xFF222228),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            // Footer with "Tap outside to collapse" and "Open Buddie →"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap outside to collapse",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280),
                    fontSize = 11.5.sp
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = { onOpenApp(activeRoute) })
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Open Buddie",
                        color = Color(0xFF3B82F6),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
