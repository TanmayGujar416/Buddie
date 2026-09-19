package com.tanmay.buddie.presentation.screens.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.data.local.entity.SubmissionStatus
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.repository.ExamRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import com.tanmay.buddie.domain.usecase.GetMascotMoodUseCase
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import javax.inject.Inject

data class ExamWithSubject(
    val exam: ExamEntity,
    val subject: SubjectEntity?
)

data class ExamsUiState(
    val exams: List<ExamWithSubject> = emptyList(),
    val isLoading: Boolean = true,
    val mascotMood: MascotMood = MascotMood.CALM,
    val companionType: CompanionType = CompanionType.BUDDIE
)

@HiltViewModel
class ExamsViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val subjectRepository: SubjectRepository,
    private val submissionRepository: SubmissionRepository,
    private val getMascotMoodUseCase: GetMascotMoodUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<ExamsUiState> = combine(
        examRepository.getAllExams(),
        subjectRepository.getAllSubjects(),
        getMascotMoodUseCase(),
        userPreferences.companionTypeFlow
    ) { exams, subjects, mood, companionType ->
        val mapped = exams
            .filter { it.datetime.isAfter(java.time.LocalDateTime.now().minusDays(1)) } // Filter out old exams
            .map { exam ->
                ExamWithSubject(
                    exam = exam,
                    subject = subjects.find { it.id == exam.subjectId }
                )
            }
            .sortedBy { it.exam.datetime } // Closest first

        ExamsUiState(
            exams = mapped,
            isLoading = false,
            mascotMood = mood,
            companionType = companionType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExamsUiState())

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            examRepository.deleteExam(exam)
        }
    }

    fun restoreExam(exam: ExamEntity) {
        viewModelScope.launch {
            examRepository.insertExam(exam)
        }
    }
}
