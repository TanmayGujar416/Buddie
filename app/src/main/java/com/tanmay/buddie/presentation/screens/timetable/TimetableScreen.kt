package com.tanmay.buddie.presentation.screens.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanmay.buddie.data.local.entity.SessionType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.tanmay.buddie.presentation.theme.*

@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 24-hour range: 12 AM (0:00) through 11 PM (23:00) - memoized to prevent re-allocation on scroll
    val times = remember { (0..23).map { LocalTime.of(it, 0) } }

    val hourHeight: Dp = 60.dp
    val timeColumnWidth: Dp = 50.dp
    val dayColumnWidth: Dp = 80.dp
    val totalGridHeight: Dp = hourHeight * 24

    val today = remember { LocalDate.now().dayOfWeek }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // Memoize overlapping slot layout per day so grid scroll frames don't re-run layout calculation
    val slotsByDay = remember(uiState.slots) {
        uiState.slots.groupBy { it.slot.dayOfWeek }.mapValues { (_, daySlots) ->
            computeOverlappingLayout(daySlots) {
                it.slot.startTime to it.slot.endTime
            }
        }
    }

    // Extend dark background fully to the top edge, blending with status bar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlueLight)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Integrated Top Header (no separate app-bar block)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Timetable",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Weekly Schedule",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Pinned Days Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CharcoalBackground)
                ) {
                    // Spacer matching left time axis width
                    Spacer(modifier = Modifier.width(timeColumnWidth))

                    // Horizontally scrollable day headers (synchronized with body)
                    Row(
                        modifier = Modifier.horizontalScroll(horizontalScrollState)
                    ) {
                        uiState.activeDays.forEach { day ->
                            val isToday = day == today
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(dayColumnWidth)
                                    .padding(vertical = 8.dp)
                            ) {
                                // Day name abbreviation (e.g. MON, TUE)
                                Text(
                                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isToday) AccentBlueLight else TextSecondary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Current day emphasis: filled colored circle behind day label
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isToday) AccentBlue else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Top divider below day headers
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(DividerFaint)
                )

                // Scrollable Grid Area (Time column + Day columns scroll together vertically)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                ) {
                    // Time axis column
                    Box(
                        modifier = Modifier
                            .width(timeColumnWidth)
                            .height(totalGridHeight)
                    ) {
                        times.forEachIndexed { index, time ->
                            val topY = (index * hourHeight.value).dp
                            // Hour label aligned with divider line: 12 AM, 1 AM, ..., 12 PM, ..., 11 PM
                            Text(
                                text = formatHourLabel(time.hour),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = topY - 7.dp)
                                    .padding(end = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }

                    // Days grid (horizontally scrollable in lockstep with day headers)
                    Row(
                        modifier = Modifier
                            .horizontalScroll(horizontalScrollState)
                            .height(totalGridHeight)
                    ) {
                        uiState.activeDays.forEach { day ->
                            val isToday = day == today
                            val positionedSlots = slotsByDay[day] ?: emptyList()

                            Box(
                                modifier = Modifier
                                    .width(dayColumnWidth)
                                    .height(totalGridHeight)
                                    .then(
                                        if (isToday) Modifier.background(AccentBlue.copy(alpha = 0.03f))
                                        else Modifier
                                    )
                            ) {
                                // Horizontal faint grid dividers spanning across
                                times.forEachIndexed { index, _ ->
                                    val lineY = (index * hourHeight.value).dp
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(0.5.dp)
                                            .offset(y = lineY)
                                            .background(DividerFaint)
                                    )
                                }

                                // Right vertical divider line separating days
                                Box(
                                    modifier = Modifier
                                        .width(0.5.dp)
                                        .fillMaxHeight()
                                        .align(Alignment.CenterEnd)
                                        .background(DividerVertical)
                                )

                                // Event Blocks with Google Calendar-style overlap handling
                                positionedSlots.forEach { positioned ->
                                    val slotData = positioned.item
                                    val slot = slotData.slot
                                    val subject = slotData.subject
                                    val colIndex = positioned.colIndex
                                    val totalCols = positioned.totalCols

                                    val startMinutes = slot.startTime.hour * 60 + slot.startTime.minute
                                    val durationMinutes = ChronoUnit.MINUTES.between(slot.startTime, slot.endTime).coerceAtLeast(20)

                                    val topOffset = (startMinutes * hourHeight.value / 60).dp
                                    val blockHeight = (durationMinutes * hourHeight.value / 60).dp

                                    val slotWidth = dayColumnWidth / totalCols
                                    val leftOffset = slotWidth * colIndex

                                    val subjectColor = subject?.color?.let { parseColor(it) } ?: AccentBlueLight
                                    val timeRangeStr = "${slot.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${slot.endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                                    val roomStr = if (slot.room.isNotBlank()) ", Room ${slot.room}" else ""

                                    // Core visual styling: dark/transparent interior, thin colored border, matching colored text
                                    Box(
                                        modifier = Modifier
                                            .offset(x = leftOffset, y = topOffset)
                                            .width(slotWidth)
                                            .height(blockHeight)
                                            .padding(horizontal = 1.5.dp, vertical = 1.dp)
                                            .clip(EventBlockShape)
                                            .background(subjectColor.copy(alpha = 0.14f))
                                            .border(
                                                width = 1.dp,
                                                color = subjectColor.copy(alpha = 0.9f),
                                                shape = EventBlockShape
                                            )
                                            .semantics {
                                                contentDescription = "${subject?.name ?: "Class"}, $timeRangeStr$roomStr"
                                            }
                                            .padding(horizontal = if (totalCols > 1) 3.dp else 5.dp, vertical = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            // Subject title in matching colored text
                                            Text(
                                                text = subject?.name ?: "Class",
                                                fontSize = if (totalCols > 1) 10.sp else 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = subjectColor,
                                                maxLines = if (durationMinutes > 60) 2 else 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // Time range if height allows
                                            if (durationMinutes >= 35) {
                                                val compactTimeRange = "${slot.startTime.format(DateTimeFormatter.ofPattern("h:mm"))}-${slot.endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                                                Text(
                                                    text = compactTimeRange,
                                                    fontSize = if (totalCols > 1) 8.sp else 9.sp,
                                                    color = subjectColor.copy(alpha = 0.85f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Session info / Room if height allows
                                            if (durationMinutes >= 55) {
                                                val detail = slot.room.takeIf { it.isNotBlank() }
                                                    ?: if (slot.sessionType == SessionType.LAB) "Lab" else "Lecture"
                                                Text(
                                                    text = detail,
                                                    fontSize = if (totalCols > 1) 9.sp else 10.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = subjectColor.copy(alpha = 0.75f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        AccentBlueLight
    }
}

fun formatHourLabel(hour: Int): String {
    return when (hour) {
        0 -> "12 AM"
        12 -> "12 PM"
        in 1..11 -> "$hour AM"
        in 13..23 -> "${hour - 12} PM"
        else -> "$hour"
    }
}
