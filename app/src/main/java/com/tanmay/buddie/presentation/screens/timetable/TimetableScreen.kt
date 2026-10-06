package com.tanmay.buddie.presentation.screens.timetable

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tanmay.buddie.data.local.entity.SessionType
import com.tanmay.buddie.presentation.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class TimetableDisplayMode {
    AGENDA,
    WEEK_GRID
}

@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var displayMode by rememberSaveable { mutableStateOf(TimetableDisplayMode.AGENDA) }
    var selectedDay by rememberSaveable { mutableStateOf(LocalDate.now().dayOfWeek) }

    val today = remember { LocalDate.now().dayOfWeek }
    val now = remember { LocalTime.now() }

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
                // Top Header with Title and Mode Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Timetable",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (displayMode == TimetableDisplayMode.AGENDA) {
                                "${selectedDay.getDisplayName(TextStyle.FULL, Locale.getDefault())}'s Schedule"
                            } else {
                                "Weekly Schedule"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // View Mode Toggle (Agenda / Week Grid)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CharcoalSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
                    ) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            FilterChipTab(
                                text = "Agenda",
                                icon = Icons.Default.List,
                                isSelected = displayMode == TimetableDisplayMode.AGENDA,
                                onClick = { displayMode = TimetableDisplayMode.AGENDA }
                            )
                            FilterChipTab(
                                text = "Week",
                                icon = Icons.Default.DateRange,
                                isSelected = displayMode == TimetableDisplayMode.WEEK_GRID,
                                onClick = { displayMode = TimetableDisplayMode.WEEK_GRID }
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = displayMode,
                    label = "TimetableModeTransition"
                ) { mode ->
                    when (mode) {
                        TimetableDisplayMode.AGENDA -> {
                            AgendaView(
                                slots = uiState.slots,
                                activeDays = uiState.activeDays,
                                selectedDay = selectedDay,
                                onSelectDay = { selectedDay = it },
                                today = today,
                                currentTime = now
                            )
                        }
                        TimetableDisplayMode.WEEK_GRID -> {
                            WeekGridView(
                                slots = uiState.slots,
                                activeDays = uiState.activeDays,
                                today = today,
                                currentTime = now
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipTab(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) AccentBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}

@Composable
private fun AgendaView(
    slots: List<SlotWithSubject>,
    activeDays: List<DayOfWeek>,
    selectedDay: DayOfWeek,
    onSelectDay: (DayOfWeek) -> Unit,
    today: DayOfWeek,
    currentTime: LocalTime
) {
    val daySlots = remember(slots, selectedDay) {
        slots.filter { it.slot.dayOfWeek == selectedDay }
            .sortedBy { it.slot.startTime }
    }

    // Days that have at least one class
    val daysWithClasses = remember(slots) {
        slots.map { it.slot.dayOfWeek }.toSet()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Day Selector Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val daysToDisplay = if (activeDays.isNotEmpty()) activeDays else DayOfWeek.values().toList()
            daysToDisplay.forEach { day ->
                val isSelected = day == selectedDay
                val isToday = day == today
                val hasClasses = day in daysWithClasses

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isSelected -> AccentBlue
                                isToday -> AccentBlue.copy(alpha = 0.12f)
                                else -> Color.Transparent
                            }
                        )
                        .clickable { onSelectDay(day) }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isSelected -> Color.White
                            isToday -> AccentBlueLight
                            else -> TextSecondary
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isSelected -> Color.White
                            isToday -> AccentBlueLight
                            else -> TextPrimary
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Class indicator dot
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> Color.White.copy(alpha = 0.85f)
                                    hasClasses -> AccentBlueLight
                                    else -> Color.Transparent
                                }
                            )
                    )
                }
            }
        }

        Divider(color = DividerFaint, thickness = 0.5.dp)

        if (daySlots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "☕",
                        fontSize = 44.sp
                    )
                    Text(
                        text = "No Classes on ${selectedDay.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Enjoy your free time, review past notes, or catch up on pending deadlines!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val totalMinutes = daySlots.sumOf {
                        ChronoUnit.MINUTES.between(it.slot.startTime, it.slot.endTime)
                    }
                    val hours = totalMinutes / 60
                    val mins = totalMinutes % 60
                    val durationText = buildString {
                        if (hours > 0) append("${hours}h ")
                        if (mins > 0 || hours == 0L) append("${mins}m")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${daySlots.size} ${if (daySlots.size == 1) "Class" else "Classes"}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = "Total: $durationText",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                items(daySlots, key = { it.slot.id }) { item ->
                    AgendaClassCard(
                        item = item,
                        isToday = selectedDay == today,
                        currentTime = currentTime
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaClassCard(
    item: SlotWithSubject,
    isToday: Boolean,
    currentTime: LocalTime
) {
    val slot = item.slot
    val subject = item.subject
    val subjectColor = subject?.color?.let { parseColor(it) } ?: AccentBlueLight

    val isHappeningNow = isToday && !currentTime.isBefore(slot.startTime) && currentTime.isBefore(slot.endTime)
    val isPast = isToday && !currentTime.isBefore(slot.endTime)
    val minutesUntil = if (isToday && currentTime.isBefore(slot.startTime)) {
        ChronoUnit.MINUTES.between(currentTime, slot.startTime)
    } else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isHappeningNow) CharcoalSurfaceElevated else CharcoalSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHappeningNow) AccentBlue else BorderOutline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .intrinsicHeightModifier()
        ) {
            // Subject color bar indicator on the left
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(subjectColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header row: Status badges & Time range
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timeFormat = DateTimeFormatter.ofPattern("h:mm a")
                    val timeStr = "${slot.startTime.format(timeFormat)} - ${slot.endTime.format(timeFormat)}"

                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    // Live / Upcoming / Completed status chip
                    when {
                        isHappeningNow -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusSuccessContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(StatusSuccess)
                                    )
                                    Text(
                                        text = "LIVE NOW",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StatusSuccess
                                    )
                                }
                            }
                        }
                        minutesUntil != null && minutesUntil in 1..60 -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusWarningContainer
                            ) {
                                Text(
                                    text = "In ${minutesUntil}m",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusWarning
                                )
                            }
                        }
                        isPast -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CharcoalSurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Done",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                        else -> {
                            val durationMinutes = ChronoUnit.MINUTES.between(slot.startTime, slot.endTime)
                            Text(
                                text = "${durationMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Subject Name
                Text(
                    text = subject?.name ?: "Unknown Subject",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Chips Row: Room & Session Type
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (slot.room.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CharcoalBackground,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderOutline)
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
                                    text = slot.room,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    val sessionLabel = when (slot.sessionType) {
                        SessionType.LECTURE -> "Lecture"
                        SessionType.LAB -> "Lab"
                        SessionType.TUTORIAL -> "Tutorial"
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subjectColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = sessionLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = subjectColor
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.intrinsicHeightModifier(): Modifier = this.height(IntrinsicSize.Min)

@Composable
private fun WeekGridView(
    slots: List<SlotWithSubject>,
    activeDays: List<DayOfWeek>,
    today: DayOfWeek,
    currentTime: LocalTime
) {
    val times = remember { (0..23).map { LocalTime.of(it, 0) } }

    val hourHeight: Dp = 60.dp
    val timeColumnWidth: Dp = 50.dp
    val dayColumnWidth: Dp = 80.dp
    val totalGridHeight: Dp = hourHeight * 24

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val density = LocalDensity.current

    // Auto-scroll to 8 AM (or 1 hour before current time) on initial appearance
    LaunchedEffect(Unit) {
        val targetHour = (currentTime.hour - 1).coerceIn(7, 20)
        val targetY = with(density) { (targetHour * hourHeight.value).dp.roundToPx() }
        verticalScrollState.scrollTo(targetY)

        val todayIndex = activeDays.indexOf(today)
        if (todayIndex > 0) {
            val targetX = with(density) { (todayIndex * dayColumnWidth.value).dp.roundToPx() }
            horizontalScrollState.scrollTo(targetX)
        }
    }

    // Memoize overlapping slot layout per day
    val slotsByDay = remember(slots) {
        slots.groupBy { it.slot.dayOfWeek }.mapValues { (_, daySlots) ->
            computeOverlappingLayout(daySlots) {
                it.slot.startTime to it.slot.endTime
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Pinned Days Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalBackground)
        ) {
            Spacer(modifier = Modifier.width(timeColumnWidth))

            Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                activeDays.forEach { day ->
                    val isToday = day == today
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(dayColumnWidth)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) AccentBlueLight else TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(DividerFaint)
        )

        // Scrollable Grid Area
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
                    Text(
                        text = formatHourLabel(time.hour),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = topY - 7.dp)
                            .padding(end = 8.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Days grid
            Row(
                modifier = Modifier
                    .horizontalScroll(horizontalScrollState)
                    .height(totalGridHeight)
            ) {
                activeDays.forEach { day ->
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
                        // Horizontal faint grid dividers
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

                        // Right vertical divider
                        Box(
                            modifier = Modifier
                                .width(0.5.dp)
                                .fillMaxHeight()
                                .align(Alignment.CenterEnd)
                                .background(DividerVertical)
                        )

                        // Event Blocks
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
                                    Text(
                                        text = subject?.name ?: "Class",
                                        fontSize = if (totalCols > 1) 10.sp else 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = subjectColor,
                                        maxLines = if (durationMinutes > 60) 2 else 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

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

                        // Live "NOW" indicator line for today's column
                        if (isToday) {
                            val currentMinutes = currentTime.hour * 60 + currentTime.minute
                            val nowY = (currentMinutes * hourHeight.value / 60).dp

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .offset(y = nowY)
                                    .background(StatusError)
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .offset(x = (-3).dp, y = nowY - 2.dp)
                                    .clip(CircleShape)
                                    .background(StatusError)
                            )
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
    } catch (_: Exception) {
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
