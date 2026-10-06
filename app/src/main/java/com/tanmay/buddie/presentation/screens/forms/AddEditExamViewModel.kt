package com.tanmay.buddie.presentation.screens.forms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.repository.ExamRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class ExamFormState(
    val title: String = "",
    val subjectId: String? = null,
    val datetime: LocalDateTime = LocalDateTime.now().plusDays(7).withHour(9).withMinute(0),
    val durationMinutes: String = "120",
    val hall: String = "",
    val seatNumber: String = "",
    val syllabusScope: String = "",
    val allowedMaterials: String = "",
    val roomOverride: String = "",
    val subjects: List<SubjectEntity> = emptyList(),
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false
)

@HiltViewModel
class AddEditExamViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val subjectRepository: SubjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val examId: String? = savedStateHandle["examId"]
    
    private val _formState = MutableStateFlow(ExamFormState())
    
    val uiState: StateFlow<ExamFormState> = combine(
        _formState,
        subjectRepository.getAllSubjects()
    ) { state, subjects ->
        state.copy(subjects = subjects)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExamFormState())

    init {
        if (examId != null) {
            viewModelScope.launch {
                val exam = examRepository.getExamById(examId)
                if (exam != null) {
                    _formState.value = _formState.value.copy(
                        title = exam.title,
                        subjectId = exam.subjectId,
                        datetime = exam.datetime,
                        durationMinutes = exam.durationMinutes.toString(),
                        hall = exam.hall,
                        seatNumber = exam.seatNumber ?: "",
                        syllabusScope = exam.syllabusScope ?: "",
                        allowedMaterials = exam.allowedMaterials ?: "",
                        roomOverride = exam.roomOverride ?: ""
                    )
                }
            }
        }
    }

    fun updateField(
        title: String? = null,
        subjectId: String? = null,
        datetime: LocalDateTime? = null,
        durationMinutes: String? = null,
        hall: String? = null,
        seatNumber: String? = null,
        syllabusScope: String? = null,
        allowedMaterials: String? = null,
        roomOverride: String? = null
    ) {
        _formState.value = _formState.value.copy(
            title = title ?: _formState.value.title,
            subjectId = subjectId ?: _formState.value.subjectId,
            datetime = datetime ?: _formState.value.datetime,
            durationMinutes = durationMinutes ?: _formState.value.durationMinutes,
            hall = hall ?: _formState.value.hall,
            seatNumber = seatNumber ?: _formState.value.seatNumber,
            syllabusScope = syllabusScope ?: _formState.value.syllabusScope,
            allowedMaterials = allowedMaterials ?: _formState.value.allowedMaterials,
            roomOverride = roomOverride ?: _formState.value.roomOverride
        )
    }

    fun createSubject(name: String, color: String) {
        viewModelScope.launch {
            val newSubject = SubjectEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                color = color
            )
            subjectRepository.insertSubject(newSubject)
            _formState.value = _formState.value.copy(subjectId = newSubject.id)
        }
    }

    fun saveExam() {
        val state = _formState.value
        if (state.title.isBlank() || state.subjectId == null || state.hall.isBlank() || state.isSaving || state.saveCompleted) return
        
        _formState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val entity = ExamEntity(
                    id = examId ?: UUID.randomUUID().toString(),
                    subjectId = state.subjectId,
                    title = state.title.trim(),
                    datetime = state.datetime,
                    durationMinutes = state.durationMinutes.toIntOrNull() ?: 120,
                    hall = state.hall.trim(),
                    seatNumber = state.seatNumber.takeIf { it.isNotBlank() },
                    syllabusScope = state.syllabusScope.takeIf { it.isNotBlank() },
                    allowedMaterials = state.allowedMaterials.takeIf { it.isNotBlank() },
                    roomOverride = state.roomOverride.takeIf { it.isNotBlank() }
                )
                
                if (examId == null) {
                    examRepository.insertExam(entity)
                } else {
                    examRepository.updateExam(entity)
                }
                
                _formState.value = _formState.value.copy(isSaving = false, saveCompleted = true)
            } catch (_: Exception) {
                _formState.value = _formState.value.copy(isSaving = false)
            }
        }
    }
}
