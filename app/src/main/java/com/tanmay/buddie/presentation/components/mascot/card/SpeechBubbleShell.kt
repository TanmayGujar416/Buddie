package com.tanmay.buddie.presentation.components.mascot.card

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Path
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

@Composable
fun SpeechBubbleShell(
    mood: MascotMood,
    homeViewModel: HomeViewModel,
    timetableViewModel: TimetableViewModel,
    examsViewModel: ExamsViewModel,
    onCollapse: () -> Unit,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(CardSection.SUBMISSIONS) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                // Prevent tapping inside card from collapsing
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mascot sitting outside the card at the top + Close Button on the right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCollapse() }
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BuddieMascot(
                        mood = mood,
                        size = 46.dp,
                        animateIdle = true
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buddie",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Close Button in top row alongside mascot
            IconButton(
                onClick = onCollapse,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Collapse",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Speech bubble triangular tail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 36.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Canvas(modifier = Modifier.size(width = 16.dp, height = 10.dp)) {
                val path = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(path, color = CharcoalSurface)
            }
        }

        // Main Speech Bubble Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            border = BorderStroke(1.dp, BorderOutline),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 500.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with pill tabs (full card width!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CardSection.entries.forEach { section ->
                        val isSelected = section == selectedSection
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
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
                            }
                        }
                    }
                }

                Divider(color = DividerFaint, thickness = 0.5.dp)

                // Content View adapting to height
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    Crossfade(
                        targetState = selectedSection,
                        animationSpec = tween(150),
                        label = "section_crossfade"
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
    }
}
