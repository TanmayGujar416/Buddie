package com.tanmay.buddie.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.local.entity.SubmissionEntity
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
import java.util.concurrent.ConcurrentHashMap
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import javax.inject.Inject

import com.tanmay.buddie.domain.repository.FilterCodeRepository
import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine
import com.tanmay.buddie.domain.sync.MoodleItemFilter

data class HomeUiState(
    val submissions: List<SubmissionWithSubject> = emptyList(),
    val isLoading: Boolean = true,
    val mascotMood: MascotMood = MascotMood.CALM,
    val companionType: CompanionType = CompanionType.BUDDIE
)

data class SubmissionWithSubject(
    val submission: SubmissionEntity,
    val subject: SubjectEntity?
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val subjectRepository: SubjectRepository,
    private val examRepository: ExamRepository,
    private val getMascotMoodUseCase: GetMascotMoodUseCase,
    private val userPreferences: UserPreferences,
    private val filterCodeRepository: FilterCodeRepository,
    private val syncEngine: MoodleContentSyncEngine
) : ViewModel() {

    // Cache the previous status before marking done, ensuring undo preserves
    // the exact original state (e.g. IN_PROGRESS or NOT_STARTED).
    private val previousStatuses = ConcurrentHashMap<String, SubmissionStatus>()

    init {
        syncMoodleOnOpen()
    }

    fun syncMoodleOnOpen() {
        viewModelScope.launch {
            try {
                syncEngine.syncIfStale(staleThresholdMillis = 5 * 60 * 1000L)
            } catch (_: Exception) {
                // Background sync failure handled gracefully without UI disruption
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        submissionRepository.getAllSubmissions(),
        subjectRepository.getAllSubjects(),
        getMascotMoodUseCase(),
        userPreferences.companionTypeFlow
    ) { submissions, subjects, mood, companionType ->
        val activeSubmissions = submissions.filter { it.status != SubmissionStatus.DONE && !it.filteredOut && !it.needsReview }
        val mapped = activeSubmissions
            .map { sub ->
                SubmissionWithSubject(
                    submission = sub,
                    subject = subjects.find { it.id == sub.subjectId }
                )
            }
            .sortedBy { it.submission.deadline } // Most urgent first

        HomeUiState(
            submissions = mapped,
            isLoading = false,
            mascotMood = mood,
            companionType = companionType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
    
    fun markAsDone(submission: SubmissionEntity) {
        previousStatuses[submission.id] = submission.status
        viewModelScope.launch {
            submissionRepository.updateSubmission(
                submission.copy(status = SubmissionStatus.DONE)
            )
        }
    }

    fun undoMarkAsDone(submission: SubmissionEntity) {
        val restoredStatus = previousStatuses.remove(submission.id)
            ?: if (submission.status != SubmissionStatus.DONE) submission.status else SubmissionStatus.NOT_STARTED
        viewModelScope.launch {
            submissionRepository.updateSubmission(
                submission.copy(status = restoredStatus)
            )
        }
    }

    fun deleteSubmission(submission: SubmissionEntity) {
        viewModelScope.launch {
            submissionRepository.deleteSubmission(submission)
        }
    }

    fun restoreSubmission(submission: SubmissionEntity) {
        viewModelScope.launch {
            submissionRepository.insertSubmission(submission)
        }
    }

    /**
     * Hides a Moodle assignment by adding its batch-shaped token(s) as an EXCLUDE filter code (isMine = false)
     * and reapplying filters and merge with zero network calls.
     */
    fun hideMoodleSubmission(submission: SubmissionEntity) {
        viewModelScope.launch {
            val tokens = MoodleItemFilter.tokenize(submission.title)
            val batchTokens = tokens.filter { MoodleItemFilter.isBatchShapedToken(it) }.ifEmpty { tokens }
            for (token in batchTokens) {
                filterCodeRepository.saveFilterCode(
                    subjectId = submission.subjectId,
                    code = token,
                    isMine = false
                )
            }
            syncEngine.reapplyFiltersAndMerge()
        }
    }

    /**
     * Unhides a previously hidden Moodle assignment by deleting the blocking code
     * (or setting isMine = true) and reapplying filters and merge with zero network calls.
     */
    fun unhideMoodleSubmission(submission: SubmissionEntity) {
        viewModelScope.launch {
            val tokens = MoodleItemFilter.tokenize(submission.title)
            val batchTokens = tokens.filter { MoodleItemFilter.isBatchShapedToken(it) }.ifEmpty { tokens }
            for (token in batchTokens) {
                filterCodeRepository.deleteFilterCode(
                    subjectId = submission.subjectId,
                    code = token
                )
            }
            syncEngine.reapplyFiltersAndMerge()
        }
    }
}
