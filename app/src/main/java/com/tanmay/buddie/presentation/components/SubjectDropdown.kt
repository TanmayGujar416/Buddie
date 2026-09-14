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
import androidx.compose.ui.unit.dp
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDropdown(
    subjects: List<SubjectEntity>,
    selectedSubjectId: String?,
    onSubjectSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedSubject = subjects.find { it.id == selectedSubjectId }

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
    }
}
