$ErrorActionPreference = 'Stop'
$baseDir = "d:\Buddie"
Set-Location $baseDir

# TimetableViewModel.kt
@"
package com.tanmay.buddie.presentation.screens.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import javax.inject.Inject

data class SlotWithSubject(
    val slot: TimetableSlotEntity,
    val subject: SubjectEntity?
)

data class TimetableUiState(
    val slots: List<SlotWithSubject> = emptyList(),
    val activeDays: List<DayOfWeek> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val subjectRepository: SubjectRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<TimetableUiState> = combine(
        timetableRepository.getAllSlots(),
        subjectRepository.getAllSubjects(),
        userPreferences.includeSaturdayFlow,
        userPreferences.includeSundayFlow
    ) { slots, subjects, includeSat, includeSun ->
        val days = mutableListOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        if (includeSat) days.add(DayOfWeek.SATURDAY)
        if (includeSun) days.add(DayOfWeek.SUNDAY)

        val enrichedSlots = slots.map { slot ->
            SlotWithSubject(
                slot = slot,
                subject = subjects.find { it.id == slot.subjectId }
            )
        }

        TimetableUiState(
            slots = enrichedSlots,
            activeDays = days,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimetableUiState())
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/presentation/screens/timetable/TimetableViewModel.kt" -Encoding UTF8

# TimetableScreen.kt
@"
package com.tanmay.buddie.presentation.screens.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // We display from 8 AM to 6 PM like the setup screen
    val times = (8..18).map { java.time.LocalTime.of(it, 0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timetable") }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Timetable Grid
                Row(modifier = Modifier.weight(1f).padding(8.dp)) {
                    // Time column
                    Column(modifier = Modifier.width(50.dp)) {
                        Spacer(modifier = Modifier.height(40.dp)) // Header space
                        times.forEach { time ->
                            Box(
                                modifier = Modifier.height(80.dp).fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Days grid
                    LazyRow(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.activeDays) { day ->
                            Column(modifier = Modifier.width(120.dp).padding(horizontal = 4.dp)) {
                                // Day Header
                                Box(
                                    modifier = Modifier.height(40.dp).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                
                                // Slots for the day
                                times.forEach { time ->
                                    val slotData = uiState.slots.find { 
                                        it.slot.dayOfWeek == day && 
                                        (it.slot.startTime == time || (time.isAfter(it.slot.startTime) && time.isBefore(it.slot.endTime)))
                                    }
                                    
                                    if (slotData != null && slotData.slot.startTime == time) {
                                        // Render the block
                                        val durationHours = java.time.Duration.between(slotData.slot.startTime, slotData.slot.endTime).toHours().toInt()
                                        val height = (80 * durationHours).dp
                                        val colorStr = slotData.subject?.color ?: "#888888"
                                        val color = try { Color(android.graphics.Color.parseColor(colorStr)) } catch(e:Exception) { Color.Gray }
                                        
                                        Card(
                                            modifier = Modifier
                                                .height(height)
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = color)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp).fillMaxSize(),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = slotData.subject?.name ?: "Unknown",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    textAlign = TextAlign.Center
                                                )
                                                Text(
                                                    text = "${slotData.slot.sessionType.name.lowercase().capitalize()} • ${slotData.slot.room}",
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else if (slotData == null) {
                                        // Empty slot
                                        Box(
                                            modifier = Modifier
                                                .height(80.dp)
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        )
                                    }
                                    // If slotData != null but startTime != time, it means the slot started earlier and spans across this hour block.
                                    // We skip rendering anything here because the block above is sized to span multiple hours.
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/presentation/screens/timetable/TimetableScreen.kt" -Encoding UTF8

Write-Host "Timetable Screen Created"
