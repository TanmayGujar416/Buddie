package com.tanmay.buddie.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.presentation.theme.*

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDropdown(
    subjects: List<SubjectEntity>,
    selectedSubjectId: String?,
    onSubjectSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCreateSubject: ((name: String, color: String) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val selectedSubject = subjects.find { it.id == selectedSubjectId }

    if (showCreateDialog && onCreateSubject != null) {
        var newSubjectName by remember { mutableStateOf("") }
        val colors = listOf(
            "#448AFF", "#22C55E", "#E040FB", "#FF5252",
            "#FF9800", "#00BCD4", "#FF4081", "#7C4DFF"
        )
        var selectedColor by remember { mutableStateOf(colors[0]) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = CharcoalSurface,
            shape = DialogShape,
            title = { Text("New Subject", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newSubjectName,
                        onValueChange = { newSubjectName = it },
                        label = { Text("Subject Name") },
                        placeholder = { Text("e.g. Physics, Data Structures") },
                        singleLine = true,
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
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(colors) { colorStr ->
                            val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch (_: Exception) { AccentBlueLight }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColor == colorStr) 3.dp else 0.dp,
                                        color = if (selectedColor == colorStr) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorStr }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubjectName.isNotBlank()) {
                            onCreateSubject(newSubjectName.trim(), selectedColor)
                            showCreateDialog = false
                        }
                    },
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Create", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedSubject?.name ?: "Select Subject",
            onValueChange = {},
            readOnly = true,
            label = { Text("Subject") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = {
                if (selectedSubject != null) {
                    val colorStr = selectedSubject.color ?: "#888888"
                    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch (_: Exception) { AccentBlueLight }
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
                }
            },
            shape = InputFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlueLight,
                unfocusedBorderColor = BorderOutline,
                focusedLabelColor = AccentBlueLight,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentBlueLight
            ),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CharcoalSurfaceElevated)
        ) {
            if (subjects.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No subjects yet", color = TextSecondary) },
                    onClick = {}
                )
            } else {
                subjects.forEach { subject ->
                    val colorStr = subject.color ?: "#888888"
                    val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch (_: Exception) { AccentBlueLight }

                    DropdownMenuItem(
                        text = { Text(subject.name, color = TextPrimary) },
                        leadingIcon = {
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
                        },
                        onClick = {
                            onSubjectSelected(subject.id)
                            expanded = false
                        }
                    )
                }
            }

            if (onCreateSubject != null) {
                Divider(color = DividerFaint)
                DropdownMenuItem(
                    text = { Text("+ Create New Subject", color = AccentBlueLight, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AccentBlueLight, modifier = Modifier.size(18.dp))
                    },
                    onClick = {
                        expanded = false
                        showCreateDialog = true
                    }
                )
            }
        }
    }
}
