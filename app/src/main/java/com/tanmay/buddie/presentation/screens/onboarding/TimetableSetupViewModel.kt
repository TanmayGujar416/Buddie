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
    val activeDays: List<DayOfWeek> = DayOfWeek.entries,
    val selectedDay: DayOfWeek? = null,
    val selectedTime: LocalTime? = null,
    val showSubjectPicker: Boolean = false,
    val showLabPromptForSubjectId: String? = null,
    val selectedSlotForDetails: TimetableSlotEntity? = null,
    val isCreatingSlot: Boolean = false,
    val showDeleteSlotConfirm: Boolean = false
)

@HiltViewModel
class TimetableSetupViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    
    val uiState: StateFlow<SetupUiState> = combine(
        subjectRepository.getAllSubjects(),
        timetableRepository.getAllSlots(),
        _uiState
    ) { subjects, slots, state ->
        state.copy(
            subjects = subjects,
            slots = slots,
            activeDays = DayOfWeek.entries
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SetupUiState())

    fun onEmptySlotClicked(day: DayOfWeek, time: LocalTime) {
        val defaultSubjectId = _uiState.value.subjects.firstOrNull()?.id ?: ""
        val draftSlot = TimetableSlotEntity(
            id = UUID.randomUUID().toString(),
            subjectId = defaultSubjectId,
            sessionType = SessionType.LECTURE,
            room = "",
            dayOfWeek = day,
            startTime = time,
            endTime = time.plusHours(1)
        )
        _uiState.value = _uiState.value.copy(
            selectedSlotForDetails = draftSlot,
            isCreatingSlot = true,
            showSubjectPicker = false,
            showDeleteSlotConfirm = false
        )
    }

    fun onFilledSlotClicked(slot: TimetableSlotEntity) {
        _uiState.value = _uiState.value.copy(
            selectedSlotForDetails = slot,
            isCreatingSlot = false,
            showSubjectPicker = false,
            showDeleteSlotConfirm = false
        )
    }

    fun saveSlot(slot: TimetableSlotEntity) {
        val isCreating = _uiState.value.isCreatingSlot
        viewModelScope.launch {
            if (isCreating) {
                timetableRepository.insertSlot(slot)
            } else {
                timetableRepository.updateSlot(slot)
            }
            _uiState.value = _uiState.value.copy(
                selectedSlotForDetails = null,
                isCreatingSlot = false
            )
        }
    }

    fun updateSlotDuration(slot: TimetableSlotEntity, newEndTime: LocalTime) {
        viewModelScope.launch {
            timetableRepository.updateSlot(slot.copy(endTime = newEndTime))
        }
    }

    fun dismissSlotDetails() {
        _uiState.value = _uiState.value.copy(
            selectedSlotForDetails = null,
            isCreatingSlot = false,
            showDeleteSlotConfirm = false
        )
    }

    fun requestDeleteSlot() {
        _uiState.value = _uiState.value.copy(showDeleteSlotConfirm = true)
    }

    fun dismissDeleteSlotConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteSlotConfirm = false)
    }

    fun confirmDeleteSlot() {
        val slot = _uiState.value.selectedSlotForDetails ?: return
        viewModelScope.launch {
            timetableRepository.deleteSlotById(slot.id)
            _uiState.value = _uiState.value.copy(
                selectedSlotForDetails = null,
                isCreatingSlot = false,
                showDeleteSlotConfirm = false
            )
        }
    }

    fun openSubjectPicker() {
        _uiState.value = _uiState.value.copy(showSubjectPicker = true)
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
                sessionType = SessionType.LECTURE,
                room = "",
                dayOfWeek = day,
                startTime = time,
                endTime = time.plusHours(1)
            )
            timetableRepository.insertSlot(newSlot)
            
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
    
    fun deleteSubject(subjectId: String) {
        viewModelScope.launch {
            // Cascades deletion of subject and its associated slots
            subjectRepository.deleteSubjectById(subjectId)
        }
    }

    fun undoDeleteSlot(slot: TimetableSlotEntity) {
        viewModelScope.launch {
            timetableRepository.insertSlot(slot)
        }
    }

    fun restoreSubjectWithSlots(subject: SubjectEntity, slots: List<TimetableSlotEntity>) {
        viewModelScope.launch {
            subjectRepository.insertSubject(subject)
            slots.forEach { timetableRepository.insertSlot(it) }
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
