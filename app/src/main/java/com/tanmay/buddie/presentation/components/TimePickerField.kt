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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    value: LocalTime,
    onValueChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.format(DateTimeFormatter.ofPattern("h:mm a")),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        shape = InputFieldShape,
        modifier = modifier
            .clip(InputFieldShape)
            .clickable { showTimePicker = true },
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

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = value.hour,
            initialMinute = value.minute
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = CharcoalSurface,
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    onValueChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
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
                        text = "Select $label",
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
