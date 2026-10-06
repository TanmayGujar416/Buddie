package com.tanmay.buddie.presentation.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanmay.buddie.data.local.entity.SessionType
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import com.tanmay.buddie.presentation.components.SubjectDropdown
import com.tanmay.buddie.presentation.components.TimePickerField
import com.tanmay.buddie.presentation.screens.timetable.computeOverlappingLayout
import com.tanmay.buddie.presentation.screens.timetable.formatHourLabel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.launch
import com.tanmay.buddie.presentation.util.rememberAppHaptics

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.tanmay.buddie.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TimetableSetupScreen(
    viewModel: TimetableSetupViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Drag-resize state for visual adjustments on the grid
    var resizingSlotId by remember { mutableStateOf<String?>(null) }
    var resizingMinutesOffset by remember { mutableLongStateOf(0L) }
    var accumulatedDragPixels by remember { mutableFloatStateOf(0f) }

    // Full 24-hour range: 12 AM through 11 PM - memoized to prevent re-allocation
    val times = remember { (0..23).map { LocalTime.of(it, 0) } }

    val hourHeight: Dp = 60.dp
    val timeColumnWidth: Dp = 50.dp
    val dayColumnWidth: Dp = 80.dp
    val totalGridHeight: Dp = hourHeight * 24

    val today = remember { LocalDate.now().dayOfWeek }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = rememberAppHaptics()
    val density = LocalDensity.current

    // Auto-scroll to 8 AM on initial load so user doesn't see empty midnight hours
    LaunchedEffect(Unit) {
        val targetY = with(density) { (8 * hourHeight.value).dp.roundToPx() }
        verticalScrollState.scrollTo(targetY)
    }

    // Memoize overlapping slot layout per day so grid scroll and drag-resize do not repeatedly re-calculate
    val slotsByDay = remember(uiState.slots, resizingSlotId, resizingMinutesOffset) {
        uiState.activeDays.associateWith { day ->
            val daySlots = uiState.slots.filter { it.dayOfWeek == day }
            computeOverlappingLayout(daySlots) { slot ->
                val isResizingThis = resizingSlotId == slot.id
                val effectiveEnd = if (isResizingThis) {
                    val durationMins = ChronoUnit.MINUTES.between(slot.startTime, slot.endTime)
                    val newDuration = (durationMins + resizingMinutesOffset).coerceAtLeast(15)
                    slot.startTime.plusMinutes(newDuration)
                } else slot.endTime
                slot.startTime to effectiveEnd
            }
        }
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(CharcoalBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onBackClick != null) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Set up your timetable",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Tap cell to add class • Tap class to edit/delete",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { viewModel.openSubjectPicker() },
                            shape = ButtonShape,
                            border = BorderStroke(1.dp, BorderOutline)
                        ) {
                            Text("Subjects", color = AccentBlueLight)
                        }
                        Button(
                            onClick = onSetupComplete,
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Done")
                        }
                    }
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

            // Divider below day headers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(DividerFaint)
            )

            // Scrollable Grid Area (Synchronized 2D Scroll)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                // Left Time Axis Column
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
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }

                // Days Grid
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
                            // Empty Cell Tap Targets with Ripple visual feedback (supports tap and long-press)
                            times.forEachIndexed { index, time ->
                                val lineY = (index * hourHeight.value).dp

                                // Horizontal grid line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .offset(y = lineY)
                                        .background(DividerFaint)
                                )

                                // Clickable & long-pressable empty cell with ripple feedback
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(hourHeight)
                                        .offset(y = lineY)
                                        .combinedClickable(
                                            onClick = {
                                                viewModel.onEmptySlotClicked(day, time)
                                            },
                                            onLongClick = {
                                                viewModel.onEmptySlotClicked(day, time)
                                            }
                                        )
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

                            // Event Blocks (Filled Slots) with drag-handle resize gesture & Google Calendar overlap handling
                            positionedSlots.forEach { positioned ->
                                val slot = positioned.item
                                val colIndex = positioned.colIndex
                                val totalCols = positioned.totalCols

                                val subject = uiState.subjects.find { it.id == slot.subjectId }
                                val subjectColor = subject?.color?.let { parseColor(it) } ?: AccentBlueLight

                                val isResizingThis = resizingSlotId == slot.id
                                val effectiveEndTime = if (isResizingThis) {
                                    val durationMins = ChronoUnit.MINUTES.between(slot.startTime, slot.endTime)
                                    val newDuration = (durationMins + resizingMinutesOffset).coerceAtLeast(15)
                                    slot.startTime.plusMinutes(newDuration)
                                } else slot.endTime

                                val startMinutes = slot.startTime.hour * 60 + slot.startTime.minute
                                val durationMinutes = ChronoUnit.MINUTES.between(slot.startTime, effectiveEndTime).coerceAtLeast(15)

                                val topOffset = (startMinutes * hourHeight.value / 60).dp
                                val blockHeight = (durationMinutes * hourHeight.value / 60).dp

                                val slotWidth = dayColumnWidth / totalCols
                                val leftOffset = slotWidth * colIndex
                                val timeRangeStr = "${slot.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${effectiveEndTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                                val roomStr = if (slot.room.isNotBlank()) ", Room ${slot.room}" else ""

                                Box(
                                    modifier = Modifier
                                        .offset(x = leftOffset, y = topOffset)
                                        .width(slotWidth)
                                        .height(blockHeight)
                                        .padding(horizontal = 1.5.dp, vertical = 1.dp)
                                        .clip(EventBlockShape)
                                        .background(subjectColor.copy(alpha = 0.16f))
                                        .border(
                                            width = 1.dp,
                                            color = subjectColor.copy(alpha = 0.9f),
                                            shape = EventBlockShape
                                        )
                                        .semantics {
                                            contentDescription = "${subject?.name ?: "Class"}, $timeRangeStr$roomStr"
                                        }
                                        .clickable {
                                            viewModel.onFilledSlotClicked(slot)
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
                                            val compactTimeRange = "${slot.startTime.format(DateTimeFormatter.ofPattern("h:mm"))}-${effectiveEndTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
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

                                    // Drag-handle resize gesture at bottom edge with smooth pixel accumulation
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(14.dp)
                                            .align(Alignment.BottomCenter)
                                            .pointerInput(slot.id) {
                                                detectVerticalDragGestures(
                                                    onDragStart = {
                                                        resizingSlotId = slot.id
                                                        resizingMinutesOffset = 0L
                                                        accumulatedDragPixels = 0f
                                                    },
                                                    onVerticalDrag = { change, dragAmount ->
                                                        change.consume()
                                                        accumulatedDragPixels += dragAmount
                                                        val dpDelta = accumulatedDragPixels / density.density
                                                        val minutesDelta = (dpDelta * 60f / hourHeight.value).toLong()
                                                        if (minutesDelta != resizingMinutesOffset) {
                                                            resizingMinutesOffset = minutesDelta
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        val durationMins = ChronoUnit.MINUTES.between(slot.startTime, slot.endTime)
                                                        val finalDuration = (durationMins + resizingMinutesOffset).coerceAtLeast(15)
                                                        val finalEndTime = slot.startTime.plusMinutes(finalDuration)
                                                        viewModel.updateSlotDuration(slot, finalEndTime)
                                                        resizingSlotId = null
                                                        resizingMinutesOffset = 0L
                                                        accumulatedDragPixels = 0f
                                                    },
                                                    onDragCancel = {
                                                        resizingSlotId = null
                                                        resizingMinutesOffset = 0L
                                                        accumulatedDragPixels = 0f
                                                    }
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(if (totalCols > 1) 16.dp else 22.dp)
                                                .height(3.dp)
                                                .clip(BuddieShapes.extraSmall)
                                                .background(subjectColor.copy(alpha = 0.75f))
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

    // 1. Subject Picker Dialog (When empty slot tapped)
    if (uiState.showSubjectPicker) {
        SubjectPickerDialog(
            subjects = uiState.subjects,
            onDismiss = { viewModel.dismissSubjectPicker() },
            onSelectSubject = { viewModel.onSubjectSelected(it.id) },
            onCreateSubject = { name, color -> viewModel.createSubject(name, color) },
            onDeleteSubject = { sub ->
                haptics.performShortHaptic()
                val associatedSlots = uiState.slots.filter { it.subjectId == sub.id }
                viewModel.deleteSubject(sub.id)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = "\"${sub.name}\" deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreSubjectWithSlots(sub, associatedSlots)
                    }
                }
            }
        )
    }

    // 2. Slot Details Dialog (Used for both creating and editing a slot)
    uiState.selectedSlotForDetails?.let { slot ->
        SlotDetailsDialog(
            slot = slot,
            isCreating = uiState.isCreatingSlot,
            subjects = uiState.subjects,
            onDismiss = { viewModel.dismissSlotDetails() },
            onSave = { updatedSlot -> viewModel.saveSlot(updatedSlot) },
            onDeleteClick = { viewModel.requestDeleteSlot() },
            onCreateSubject = { name, color -> viewModel.createSubject(name, color) }
        )
    }

    // 3. Confirm Delete Slot Dialog
    if (uiState.showDeleteSlotConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteSlotConfirm() },
            containerColor = CharcoalSurface,
            shape = DialogShape,
            title = { Text("Delete Class?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove this class from your timetable?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.performShortHaptic()
                        val slotToDelete = uiState.selectedSlotForDetails
                        val subjectOfSlot = uiState.subjects.find { it.id == slotToDelete?.subjectId }
                        viewModel.confirmDeleteSlot()
                        if (slotToDelete != null) {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    message = "${subjectOfSlot?.name ?: "Class"} removed",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoDeleteSlot(slotToDelete)
                                }
                            }
                        }
                    },
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteSlotConfirm() }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // 4. Lab Prompt Dialog
    if (uiState.showLabPromptForSubjectId != null) {
        val subject = uiState.subjects.find { it.id == uiState.showLabPromptForSubjectId }
        if (subject != null) {
            LabPromptDialog(
                subject = subject,
                onDismiss = { viewModel.dismissLabPrompt() },
                onAddLab = { day, time, room ->
                    viewModel.addLabSlot(subject.id, day, time, room)
                }
            )
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotDetailsDialog(
    slot: TimetableSlotEntity,
    isCreating: Boolean,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (TimetableSlotEntity) -> Unit,
    onDeleteClick: () -> Unit,
    onCreateSubject: (String, String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(slot.subjectId.ifBlank { subjects.firstOrNull()?.id ?: "" }) }
    var sessionType by remember { mutableStateOf(slot.sessionType) }
    var dayOfWeek by remember { mutableStateOf(slot.dayOfWeek) }
    var startTime by remember { mutableStateOf(slot.startTime) }
    var endTime by remember { mutableStateOf(slot.endTime) }
    var room by remember { mutableStateOf(slot.room) }
    var showCreateSubjectDialog by remember { mutableStateOf(false) }

    val activeDays = DayOfWeek.entries

    if (showCreateSubjectDialog) {
        var newSubjectName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf("#448AFF") }
        val colors = listOf(
            "#FF5252", "#FF4081", "#E040FB", "#7C4DFF",
            "#536DFE", "#448AFF", "#40C4FF", "#18FFFF",
            "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41",
            "#FFFF00", "#FFD740", "#FFAB40", "#FF6E40"
        )

        AlertDialog(
            onDismissRequest = { showCreateSubjectDialog = false },
            containerColor = CharcoalSurface,
            shape = DialogShape,
            title = { Text("New Subject", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newSubjectName,
                        onValueChange = { newSubjectName = it },
                        label = { Text("Subject Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = InputFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlueLight,
                            unfocusedBorderColor = BorderOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentBlueLight,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    Text("Select Color:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(colors) { colorStr ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { selectedColor = colorStr },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(parseColor(colorStr))
                                        .border(
                                            width = if (selectedColor == colorStr) 3.dp else 0.dp,
                                            color = if (selectedColor == colorStr) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubjectName.isNotBlank()) {
                            onCreateSubject(newSubjectName.trim(), selectedColor)
                            showCreateSubjectDialog = false
                        }
                    },
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Create", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSubjectDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        shape = DialogShape,
        title = {
            Text(
                text = if (isCreating) "Add Class" else "Edit Class",
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Subject selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subject",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        TextButton(
                            onClick = { showCreateSubjectDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+ New Subject", color = AccentBlueLight, fontSize = 12.sp)
                        }
                    }
                    if (subjects.isEmpty()) {
                        OutlinedButton(
                            onClick = { showCreateSubjectDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = InputFieldShape,
                            border = BorderStroke(1.dp, BorderOutline)
                        ) {
                            Text("+ Create your first subject", color = AccentBlueLight)
                        }
                    } else {
                        SubjectDropdown(
                            subjects = subjects,
                            selectedSubjectId = selectedSubjectId.ifBlank { subjects.firstOrNull()?.id },
                            onSubjectSelected = { selectedSubjectId = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Day of Week
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Day",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(activeDays) { day ->
                            val isSelected = day == dayOfWeek
                            FilterChip(
                                selected = isSelected,
                                onClick = { dayOfWeek = day },
                                label = {
                                    Text(
                                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                        fontSize = 12.sp
                                    )
                                },
                                shape = ChipShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentBlue,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = CharcoalSurfaceElevated,
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

                // Start Time & End Time fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimePickerField(
                        label = "Start time",
                        value = startTime,
                        onValueChange = { newTime ->
                            startTime = newTime
                            if (!endTime.isAfter(newTime)) {
                                endTime = newTime.plusHours(1)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TimePickerField(
                        label = "End time",
                        value = endTime,
                        onValueChange = { newTime ->
                            if (newTime.isAfter(startTime)) {
                                endTime = newTime
                            } else {
                                endTime = startTime.plusMinutes(30)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Session Type
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Session Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = sessionType == SessionType.LECTURE,
                            onClick = { sessionType = SessionType.LECTURE },
                            label = { Text("Lecture") },
                            shape = ChipShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlue,
                                selectedLabelColor = TextPrimary,
                                containerColor = CharcoalSurfaceElevated,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = BorderOutline,
                                selectedBorderColor = AccentBlue
                            )
                        )
                        FilterChip(
                            selected = sessionType == SessionType.LAB,
                            onClick = { sessionType = SessionType.LAB },
                            label = { Text("Lab") },
                            shape = ChipShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlue,
                                selectedLabelColor = TextPrimary,
                                containerColor = CharcoalSurfaceElevated,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = BorderOutline,
                                selectedBorderColor = AccentBlue
                            )
                        )
                    }
                }

                // Room field
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room (Optional)") },
                    placeholder = { Text("e.g. Room 204", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = InputFieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlueLight,
                        unfocusedBorderColor = BorderOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentBlueLight
                    )
                )
            }
        },
        confirmButton = {
            val canSave = selectedSubjectId.isNotBlank() || subjects.isNotEmpty()
            Button(
                onClick = {
                    val finalSubjectId = selectedSubjectId.ifBlank { subjects.firstOrNull()?.id ?: "" }
                    if (finalSubjectId.isNotBlank()) {
                        onSave(
                            slot.copy(
                                subjectId = finalSubjectId,
                                sessionType = sessionType,
                                dayOfWeek = dayOfWeek,
                                startTime = startTime,
                                endTime = endTime,
                                room = room.trim()
                            )
                        )
                    }
                },
                enabled = canSave,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(if (isCreating) "Add Class" else "Save Changes")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!isCreating) {
                    TextButton(onClick = onDeleteClick) {
                        Text("Delete", color = StatusError)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    )
}

@Composable
fun SubjectPickerDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSelectSubject: (SubjectEntity) -> Unit,
    onCreateSubject: (String, String) -> Unit,
    onDeleteSubject: (SubjectEntity) -> Unit
) {
    var isCreating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var subjectPendingDelete by remember { mutableStateOf<SubjectEntity?>(null) }

    val colors = listOf(
        "#FF5252", "#FF4081", "#E040FB", "#7C4DFF",
        "#536DFE", "#448AFF", "#40C4FF", "#18FFFF",
        "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41",
        "#FFFF00", "#FFD740", "#FFAB40", "#FF6E40"
    )
    var selectedColor by remember { mutableStateOf(colors[0]) }

    // Confirmation dialog before deleting subject
    subjectPendingDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { subjectPendingDelete = null },
            containerColor = CharcoalSurface,
            shape = DialogShape,
            title = { Text("Delete Subject?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Deleting \"${sub.name}\" will also permanently remove all its classes from your timetable. Are you sure?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSubject(sub)
                        subjectPendingDelete = null
                    },
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectPendingDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        shape = DialogShape,
        title = { Text(if (isCreating) "New Subject" else "Select Subject", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            if (isCreating) {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Subject Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = InputFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlueLight,
                            unfocusedBorderColor = BorderOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentBlueLight,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Color:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(colors) { colorStr ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { selectedColor = colorStr },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(parseColor(colorStr))
                                        .border(
                                            width = if (selectedColor == colorStr) 3.dp else 0.dp,
                                            color = if (selectedColor == colorStr) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            } else {
                if (subjects.isEmpty()) {
                    Text(
                        "No subjects created yet. Tap below to create your first subject!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(subjects, key = { it.id }) { subject ->
                            val color = parseColor(subject.color ?: "#888888")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ChipShape)
                                    .clickable { onSelectSubject(subject) }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = subject.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }

                                // Delete Subject button with explicit confirmation - 48dp touch target
                                IconButton(
                                    onClick = { subjectPendingDelete = subject },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete ${subject.name}",
                                        tint = StatusError
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isCreating) {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onCreateSubject(newName.trim(), selectedColor)
                            isCreating = false
                        }
                    },
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Create", color = TextPrimary)
                }
            } else {
                Button(
                    onClick = { isCreating = true },
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("+ New Subject", color = TextPrimary)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (isCreating) isCreating = false else onDismiss()
                }
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabPromptDialog(
    subject: SubjectEntity,
    onDismiss: () -> Unit,
    onAddLab: (DayOfWeek, LocalTime, String) -> Unit
) {
    var room by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(14, 0)) }

    var dayDropdownExpanded by remember { mutableStateOf(false) }
    var timeDropdownExpanded by remember { mutableStateOf(false) }

    val days = remember { DayOfWeek.entries }
    val times = remember { (0..23).map { LocalTime.of(it, 0) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        shape = DialogShape,
        title = { Text("Add Lab for ${subject.name}?", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Does this subject have a separate lab component?", color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))

                // Day Picker
                ExposedDropdownMenuBox(
                    expanded = dayDropdownExpanded,
                    onExpandedChange = { dayDropdownExpanded = !dayDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDay.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lab Day") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = InputFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlueLight,
                            unfocusedBorderColor = BorderOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentBlueLight,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = dayDropdownExpanded,
                        onDismissRequest = { dayDropdownExpanded = false }
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day.getDisplayName(TextStyle.FULL, Locale.getDefault())) },
                                onClick = {
                                    selectedDay = day
                                    dayDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Picker (24 hours formatted with 12 AM / 12 PM)
                ExposedDropdownMenuBox(
                    expanded = timeDropdownExpanded,
                    onExpandedChange = { timeDropdownExpanded = !timeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = formatHourLabel(selectedTime.hour),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lab Time (Starts at)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = InputFieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlueLight,
                            unfocusedBorderColor = BorderOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentBlueLight,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = timeDropdownExpanded,
                        onDismissRequest = { timeDropdownExpanded = false }
                    ) {
                        times.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(formatHourLabel(time.hour)) },
                                onClick = {
                                    selectedTime = time
                                    timeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Lab Room (optional)") },
                    shape = InputFieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlueLight,
                        unfocusedBorderColor = BorderOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentBlueLight,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddLab(selectedDay, selectedTime, room.trim())
                },
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Add Lab", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No Lab", color = TextSecondary)
            }
        }
    )
}

private fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (_: Exception) {
        AccentBlueLight
    }
}
