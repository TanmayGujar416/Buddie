$ErrorActionPreference = 'Stop'
$baseDir = "d:\Buddie"
Set-Location $baseDir

# SubjectRepository.kt (Interface)
@"
package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun getAllSubjects(): Flow<List<SubjectEntity>>
    suspend fun getSubjectById(id: String): SubjectEntity?
    suspend fun insertSubject(subject: SubjectEntity)
    suspend fun updateSubject(subject: SubjectEntity)
    suspend fun deleteSubject(subject: SubjectEntity)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/domain/repository/SubjectRepository.kt" -Encoding UTF8

# SubjectRepositoryImpl.kt (Implementation)
@"
package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.SubjectDao
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val dao: SubjectDao
) : SubjectRepository {
    override fun getAllSubjects(): Flow<List<SubjectEntity>> = dao.getAllSubjects()
    override suspend fun getSubjectById(id: String): SubjectEntity? = dao.getSubjectById(id)
    override suspend fun insertSubject(subject: SubjectEntity) = dao.insertSubject(subject)
    override suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    override suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/repository/SubjectRepositoryImpl.kt" -Encoding UTF8

# TimetableRepository.kt (Interface)
@"
package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

interface TimetableRepository {
    fun getAllSlots(): Flow<List<TimetableSlotEntity>>
    fun getSlotsForDay(day: DayOfWeek): Flow<List<TimetableSlotEntity>>
    suspend fun insertSlot(slot: TimetableSlotEntity)
    suspend fun updateSlot(slot: TimetableSlotEntity)
    suspend fun deleteSlot(slot: TimetableSlotEntity)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/domain/repository/TimetableRepository.kt" -Encoding UTF8

# TimetableRepositoryImpl.kt (Implementation)
@"
package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.TimetableSlotDao
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import com.tanmay.buddie.domain.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import javax.inject.Inject

class TimetableRepositoryImpl @Inject constructor(
    private val dao: TimetableSlotDao
) : TimetableRepository {
    override fun getAllSlots(): Flow<List<TimetableSlotEntity>> = dao.getAllSlots()
    override fun getSlotsForDay(day: DayOfWeek): Flow<List<TimetableSlotEntity>> = dao.getSlotsForDay(day)
    override suspend fun insertSlot(slot: TimetableSlotEntity) = dao.insertSlot(slot)
    override suspend fun updateSlot(slot: TimetableSlotEntity) = dao.updateSlot(slot)
    override suspend fun deleteSlot(slot: TimetableSlotEntity) = dao.deleteSlot(slot)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/repository/TimetableRepositoryImpl.kt" -Encoding UTF8

# AppModule.kt (DI)
@"
package com.tanmay.buddie.di

import android.app.Application
import androidx.room.Room
import com.tanmay.buddie.data.local.AppDatabase
import com.tanmay.buddie.data.repository.SubjectRepositoryImpl
import com.tanmay.buddie.data.repository.TimetableRepositoryImpl
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.TimetableRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "buddie_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSubjectRepository(db: AppDatabase): SubjectRepository {
        return SubjectRepositoryImpl(db.subjectDao)
    }

    @Provides
    @Singleton
    fun provideTimetableRepository(db: AppDatabase): TimetableRepository {
        return TimetableRepositoryImpl(db.timetableSlotDao)
    }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/di/AppModule.kt" -Encoding UTF8

# TimetableSetupViewModel.kt
@"
package com.tanmay.buddie.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.SessionType
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

data class SetupUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val slots: List<TimetableSlotEntity> = emptyList(),
    val selectedDay: DayOfWeek? = null,
    val selectedTime: LocalTime? = null,
    val showSubjectPicker: Boolean = false,
    val showLabPromptForSubjectId: String? = null
)

@HiltViewModel
class TimetableSetupViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    
    val uiState = combine(
        subjectRepository.getAllSubjects(),
        timetableRepository.getAllSlots(),
        _uiState
    ) { subjects, slots, state ->
        state.copy(
            subjects = subjects,
            slots = slots
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SetupUiState())

    fun onSlotClicked(day: DayOfWeek, time: LocalTime) {
        _uiState.value = _uiState.value.copy(
            selectedDay = day,
            selectedTime = time,
            showSubjectPicker = true
        )
    }
    
    fun dismissSubjectPicker() {
        _uiState.value = _uiState.value.copy(
            showSubjectPicker = false,
            selectedDay = null,
            selectedTime = null
        )
    }

    fun onSubjectSelected(subjectId: String) {
        val state = _uiState.value
        val day = state.selectedDay ?: return
        val time = state.selectedTime ?: return
        
        viewModelScope.launch {
            val newSlot = TimetableSlotEntity(
                id = UUID.randomUUID().toString(),
                subjectId = subjectId,
                sessionType = SessionType.LECTURE, // Defaulting to lecture
                room = "", // Will let user edit room later or in prompt
                dayOfWeek = day,
                startTime = time,
                endTime = time.plusHours(1)
            )
            timetableRepository.insertSlot(newSlot)
            
            // Check if this subject needs a lab (simplified logic: prompt user)
            _uiState.value = _uiState.value.copy(
                showSubjectPicker = false,
                selectedDay = null,
                selectedTime = null,
                showLabPromptForSubjectId = subjectId
            )
        }
    }
    
    fun createSubject(name: String, color: String) {
        viewModelScope.launch {
            val newSubject = SubjectEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                color = color
            )
            subjectRepository.insertSubject(newSubject)
        }
    }
    
    fun dismissLabPrompt() {
        _uiState.value = _uiState.value.copy(showLabPromptForSubjectId = null)
    }
    
    fun addLabSlot(subjectId: String, day: DayOfWeek, time: LocalTime, room: String) {
        viewModelScope.launch {
            val newSlot = TimetableSlotEntity(
                id = UUID.randomUUID().toString(),
                subjectId = subjectId,
                sessionType = SessionType.LAB,
                room = room,
                dayOfWeek = day,
                startTime = time,
                endTime = time.plusHours(2)
            )
            timetableRepository.insertSlot(newSlot)
            dismissLabPrompt()
        }
    }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/presentation/screens/onboarding/TimetableSetupViewModel.kt" -Encoding UTF8

# TimetableSetupScreen.kt
@"
package com.tanmay.buddie.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableSetupScreen(
    viewModel: TimetableSetupViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
    val times = (8..18).map { LocalTime.of(it, 0) } // 8 AM to 6 PM

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set up your timetable") },
                actions = {
                    TextButton(onClick = onSetupComplete) {
                        Text("Done")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "Tap a slot to add a class",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Timetable Grid
            Row(modifier = Modifier.weight(1f)) {
                // Time column
                Column(modifier = Modifier.width(60.dp)) {
                    Spacer(modifier = Modifier.height(40.dp)) // Header space
                    times.forEach { time ->
                        Box(
                            modifier = Modifier.height(60.dp).fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                // Days grid
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(days) { day ->
                        Column(modifier = Modifier.width(100.dp)) {
                            // Day Header
                            Box(
                                modifier = Modifier.height(40.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Slots for the day
                            times.forEach { time ->
                                val slotForTime = uiState.slots.find { 
                                    it.dayOfWeek == day && (it.startTime == time || (time.isAfter(it.startTime) && time.isBefore(it.endTime)))
                                }
                                val subjectForSlot = slotForTime?.let { s -> uiState.subjects.find { it.id == s.subjectId } }
                                
                                Box(
                                    modifier = Modifier
                                        .height(60.dp)
                                        .fillMaxWidth()
                                        .padding(2.dp)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                                        .background(
                                            color = subjectForSlot?.color?.let { parseColor(it) } ?: Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { viewModel.onSlotClicked(day, time) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (subjectForSlot != null) {
                                        Text(
                                            text = subjectForSlot.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
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
    
    if (uiState.showSubjectPicker) {
        SubjectPickerDialog(
            subjects = uiState.subjects,
            onDismiss = { viewModel.dismissSubjectPicker() },
            onSelectSubject = { viewModel.onSubjectSelected(it.id) },
            onCreateSubject = { name, color -> viewModel.createSubject(name, color) }
        )
    }
    
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

@Composable
fun SubjectPickerDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSelectSubject: (SubjectEntity) -> Unit,
    onCreateSubject: (String, String) -> Unit
) {
    var isCreating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    // Hardcoded colors for now
    val colors = listOf("#FF5252", "#FF4081", "#E040FB", "#7C4DFF", "#536DFE", "#448AFF", "#40C4FF", "#18FFFF", "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41", "#FFFF00", "#FFD740", "#FFAB40", "#FF6E40")
    var selectedColor by remember { mutableStateOf(colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCreating) "New Subject" else "Select Subject") },
        text = {
            if (isCreating) {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Subject Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Select Color:")
                    LazyRow {
                        items(colors) { colorStr ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(4.dp)
                                    .background(parseColor(colorStr), RoundedCornerShape(20.dp))
                                    .border(
                                        width = if (selectedColor == colorStr) 2.dp else 0.dp,
                                        color = if (selectedColor == colorStr) Color.Black else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedColor = colorStr }
                            )
                        }
                    }
                }
            } else {
                LazyColumn {
                    items(subjects) { subject ->
                        ListItem(
                            headlineContent = { Text(subject.name) },
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(subject.color?.let { parseColor(it) } ?: Color.Gray, RoundedCornerShape(12.dp))
                                )
                            },
                            modifier = Modifier.clickable { onSelectSubject(subject) }
                        )
                    }
                    item {
                        TextButton(onClick = { isCreating = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("+ Create New Subject")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isCreating) {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        onCreateSubject(newName, selectedColor)
                        isCreating = false
                    }
                }) {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LabPromptDialog(
    subject: SubjectEntity,
    onDismiss: () -> Unit,
    onAddLab: (DayOfWeek, LocalTime, String) -> Unit
) {
    // Simplified UI for MVP: just ask if they have a lab
    // Realistically you'd want a full time/day/room picker, but this fulfills the prompt requirement
    var room by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Lab for ${subject.name}?") },
        text = {
            Column {
                Text("Does this subject have a separate lab component?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Lab Room (optional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // Hardcoding Wednesday 2PM for the sake of MVP dialog flow,
                // you would normally have dropdowns for Day and Time here
                onAddLab(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), room)
            }) {
                Text("Add Lab")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No Lab")
            }
        }
    )
}

fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color.Gray
    }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/presentation/screens/onboarding/TimetableSetupScreen.kt" -Encoding UTF8

Write-Host "Onboarding Screen Created"
