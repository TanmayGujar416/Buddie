package com.tanmay.buddie.presentation.screens.forms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.SessionType
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.data.local.entity.SubmissionSource
import com.tanmay.buddie.data.local.entity.SubmissionStatus
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

data class SubmissionFormState(
    val title: String = "",
    val subjectId: String? = null,
    val sessionType: SessionType? = null,
    val deadline: LocalDateTime = LocalDateTime.now().plusDays(1).withHour(23).withMinute(59),
    val roomOverride: String = "",
    val subjects: List<SubjectEntity> = emptyList(),
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false
)

@HiltViewModel
class AddEditSubmissionViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val subjectRepository: SubjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val submissionId: String? = savedStateHandle["submissionId"]
    
    private val _formState = MutableStateFlow(SubmissionFormState())
    
    val uiState: StateFlow<SubmissionFormState> = combine(
        _formState,
        subjectRepository.getAllSubjects()
    ) { state, subjects ->
        state.copy(subjects = subjects)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubmissionFormState())

    init {
        if (submissionId != null) {
            viewModelScope.launch {
                val submission = submissionRepository.getSubmissionById(submissionId)
                if (submission != null) {
                    _formState.value = _formState.value.copy(
                        title = submission.title,
                        subjectId = submission.subjectId,
                        sessionType = submission.sessionType,
                        deadline = submission.deadline,
                        roomOverride = submission.roomOverride ?: ""
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _formState.value = _formState.value.copy(title = title)
    }

    fun updateSubject(subjectId: String) {
        _formState.value = _formState.value.copy(subjectId = subjectId)
    }

    fun updateSessionType(type: SessionType?) {
        _formState.value = _formState.value.copy(sessionType = type)
    }

    fun updateDeadline(dateTime: LocalDateTime) {
        _formState.value = _formState.value.copy(deadline = dateTime)
    }

    fun updateRoomOverride(room: String) {
        _formState.value = _formState.value.copy(roomOverride = room)
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

    fun saveSubmission() {
        val state = _formState.value
        if (state.title.isBlank() || state.subjectId == null || state.isSaving || state.saveCompleted) return
        
        // Synchronously set isSaving = true immediately before launching coroutine to block concurrent taps
        _formState.value = state.copy(isSaving = true)
        
        viewModelScope.launch {
            try {
                // For new submissions, prevent inserting duplicate identical tasks
                if (submissionId == null) {
                    val allCurrent = submissionRepository.getAllSubmissions().firstOrNull() ?: emptyList()
                    val isDuplicate = allCurrent.any { existing ->
                        existing.source == SubmissionSource.MANUAL &&
                        existing.subjectId == state.subjectId &&
                        existing.title.trim().equals(state.title.trim(), ignoreCase = true) &&
                        Math.abs(ChronoUnit.MINUTES.between(existing.deadline, state.deadline)) <= 2
                    }
                    if (isDuplicate) {
                        _formState.value = _formState.value.copy(isSaving = false, saveCompleted = true)
                        return@launch
                    }
                }

                val entity = SubmissionEntity(
                    id = submissionId ?: UUID.randomUUID().toString(),
                    subjectId = state.subjectId,
                    sessionType = state.sessionType,
                    title = state.title.trim(),
                    deadline = state.deadline,
                    status = SubmissionStatus.NOT_STARTED,
                    roomOverride = state.roomOverride.takeIf { it.isNotBlank() }
                )
                
                if (submissionId == null) {
                    submissionRepository.insertSubmission(entity)
                } else {
                    val existing = submissionRepository.getSubmissionById(submissionId)
                    if (existing != null) {
                        submissionRepository.updateSubmission(
                            entity.copy(
                                status = existing.status,
                                source = existing.source,
                                moodleAssignmentId = existing.moodleAssignmentId,
                                filteredOut = existing.filteredOut,
                                moodleVanished = existing.moodleVanished
                            )
                        )
                    }
                }
                
                _formState.value = _formState.value.copy(isSaving = false, saveCompleted = true)
            } catch (_: Exception) {
                _formState.value = _formState.value.copy(isSaving = false)
            }
        }
    }
}
