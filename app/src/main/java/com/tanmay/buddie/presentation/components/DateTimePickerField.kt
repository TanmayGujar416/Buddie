package com.tanmay.buddie.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tanmay.buddie.presentation.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: LocalDateTime,
    onValueChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val initialMillis = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    val timePickerState = rememberTimePickerState(initialHour = value.hour, initialMinute = value.minute)

    OutlinedTextField(
        value = value.format(DateTimeFormatter.ofPattern("MMM dd, yyyy • h:mm a")),
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        shape = InputFieldShape,
        modifier = modifier
            .clip(InputFieldShape)
            .clickable { showDatePicker = true },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = TextPrimary,
            disabledBorderColor = BorderOutline,
            disabledLeadingIconColor = TextSecondary,
            disabledTrailingIconColor = AccentBlueLight,
            disabledLabelColor = TextSecondary,
            disabledPlaceholderColor = TextMuted
        ),
        enabled = false
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("Next", color = AccentBlueLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = CharcoalSurface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = CharcoalSurface,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    yearContentColor = TextSecondary,
                    currentYearContentColor = AccentBlueLight,
                    selectedYearContentColor = TextPrimary,
                    selectedYearContainerColor = AccentBlue,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = TextPrimary,
                    selectedDayContainerColor = AccentBlue,
                    todayContentColor = AccentBlueLight,
                    todayDateBorderColor = AccentBlueLight
                )
            )
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = CharcoalSurface,
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    val selectedMillis = datePickerState.selectedDateMillis ?: initialMillis
                    val selectedDate = Instant.ofEpochMilli(selectedMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    val newDateTime = selectedDate.atTime(timePickerState.hour, timePickerState.minute)
                    onValueChange(newDateTime)
                }) {
                    Text("Confirm", color = AccentBlueLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Select Time",
                        modifier = Modifier.padding(bottom = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = CharcoalSurfaceElevated,
                            selectorColor = AccentBlue,
                            containerColor = CharcoalSurface,
                            periodSelectorBorderColor = BorderOutline,
                            periodSelectorSelectedContainerColor = AccentBlue,
                            periodSelectorUnselectedContainerColor = CharcoalSurfaceElevated,
                            periodSelectorSelectedContentColor = TextPrimary,
                            periodSelectorUnselectedContentColor = TextSecondary,
                            timeSelectorSelectedContainerColor = AccentBlue,
                            timeSelectorUnselectedContainerColor = CharcoalSurfaceElevated,
                            timeSelectorSelectedContentColor = TextPrimary,
                            timeSelectorUnselectedContentColor = TextPrimary
                        )
                    )
                }
            }
        )
    }
}
