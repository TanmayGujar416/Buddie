package com.tanmay.buddie.presentation.screens.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.repository.ExamRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import com.tanmay.buddie.domain.usecase.GetMascotMoodUseCase
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

enum class ExamTab {
    UPCOMING,
    PAST
}

data class ExamWithSubject(
    val exam: ExamEntity,
    val subject: SubjectEntity?
)

data class ExamsUiState(
    val exams: List<ExamWithSubject> = emptyList(),
    val upcomingExams: List<ExamWithSubject> = emptyList(),
    val pastExams: List<ExamWithSubject> = emptyList(),
    val currentTab: ExamTab = ExamTab.UPCOMING,
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

    private val _currentTab = MutableStateFlow(ExamTab.UPCOMING)

    val uiState: StateFlow<ExamsUiState> = combine(
        examRepository.getAllExams(),
        subjectRepository.getAllSubjects(),
        getMascotMoodUseCase(),
        userPreferences.companionTypeFlow,
        _currentTab
    ) { exams, subjects, mood, companionType, tab ->
        val now = LocalDateTime.now()
        val allMapped = exams.map { exam ->
            ExamWithSubject(
                exam = exam,
                subject = subjects.find { it.id == exam.subjectId }
            )
        }

        val upcoming = allMapped
            .filter { it.exam.datetime.plusMinutes(it.exam.durationMinutes.toLong()).isAfter(now) }
            .sortedBy { it.exam.datetime }

        val past = allMapped
            .filter { !it.exam.datetime.plusMinutes(it.exam.durationMinutes.toLong()).isAfter(now) }
            .sortedByDescending { it.exam.datetime }

        ExamsUiState(
            exams = if (tab == ExamTab.UPCOMING) upcoming else past,
            upcomingExams = upcoming,
            pastExams = past,
            currentTab = tab,
            isLoading = false,
            mascotMood = mood,
            companionType = companionType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExamsUiState())

    fun setTab(tab: ExamTab) {
        _currentTab.value = tab
    }

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
