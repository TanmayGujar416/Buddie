package com.tanmay.buddie.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.data.local.entity.SubmissionSource
import com.tanmay.buddie.data.local.entity.SubmissionStatus
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.repository.ExamRepository
import com.tanmay.buddie.domain.repository.FilterCodeRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import com.tanmay.buddie.domain.repository.TimetableRepository
import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine
import com.tanmay.buddie.domain.sync.MoodleItemFilter
import com.tanmay.buddie.domain.usecase.GetMascotMoodUseCase
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.screens.timetable.SlotWithSubject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

enum class TaskFilter(val label: String) {
    ALL("All"),
    DUE_TODAY("Due Today"),
    THIS_WEEK("This Week"),
    URGENT("Urgent"),
    MOODLE("Moodle")
}

data class HomeUiState(
    val submissions: List<SubmissionWithSubject> = emptyList(),
    val allActiveSubmissions: List<SubmissionWithSubject> = emptyList(),
    val todaysClasses: List<SlotWithSubject> = emptyList(),
    val upcomingExamsCount: Int = 0,
    val selectedFilter: TaskFilter = TaskFilter.ALL,
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
    private val syncEngine: MoodleContentSyncEngine,
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    // Cache the previous status before marking done, ensuring undo preserves
    // the exact original state (e.g. IN_PROGRESS or NOT_STARTED).
    private val previousStatuses = ConcurrentHashMap<String, SubmissionStatus>()
    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)

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

    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    val uiState: StateFlow<HomeUiState> = combine(
        submissionRepository.getAllSubmissions(),
        subjectRepository.getAllSubjects(),
        getMascotMoodUseCase(),
        userPreferences.companionTypeFlow,
        timetableRepository.getAllSlots(),
        examRepository.getAllExams(),
        _selectedFilter
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val submissions = args[0] as List<SubmissionEntity>
        @Suppress("UNCHECKED_CAST")
        val subjects = args[1] as List<SubjectEntity>
        val mood = args[2] as MascotMood
        val companionType = args[3] as CompanionType
        @Suppress("UNCHECKED_CAST")
        val slots = args[4] as List<com.tanmay.buddie.data.local.entity.TimetableSlotEntity>
        @Suppress("UNCHECKED_CAST")
        val exams = args[5] as List<com.tanmay.buddie.data.local.entity.ExamEntity>
        val currentFilter = args[6] as TaskFilter

        val now = LocalDateTime.now()
        val todayDate = now.toLocalDate()
        val currentDayOfWeek = todayDate.dayOfWeek

        val activeSubmissions = submissions.filter { it.status != SubmissionStatus.DONE && !it.filteredOut && !it.needsReview }

        // Deduplicate submissions defensively so duplicates are NEVER shown on screen
        val seenKeys = mutableSetOf<String>()
        val distinctSubmissions = mutableListOf<SubmissionEntity>()
        val duplicateSubmissions = mutableListOf<SubmissionEntity>()

        for (sub in activeSubmissions) {
            val key = if (sub.source == SubmissionSource.MOODLE && sub.moodleAssignmentId != null) {
                "moodle_${sub.moodleAssignmentId}"
            } else {
                val normTitle = sub.title.trim().lowercase()
                val deadlineMinute = "${sub.deadline.year}-${sub.deadline.dayOfYear}-${sub.deadline.hour}-${sub.deadline.minute}"
                "manual_${sub.subjectId}_${normTitle}_$deadlineMinute"
            }
            if (seenKeys.add(key)) {
                distinctSubmissions.add(sub)
            } else {
                duplicateSubmissions.add(sub)
            }
        }

        // Auto-heal: delete redundant duplicate rows asynchronously so DB remains clean
        if (duplicateSubmissions.isNotEmpty()) {
            cleanUpDuplicates(duplicateSubmissions)
        }

        val allMapped = distinctSubmissions
            .map { sub ->
                SubmissionWithSubject(
                    submission = sub,
                    subject = subjects.find { it.id == sub.subjectId }
                )
            }
            .sortedBy { it.submission.deadline } // Most urgent first

        val filtered = when (currentFilter) {
            TaskFilter.ALL -> allMapped
            TaskFilter.DUE_TODAY -> allMapped.filter { it.submission.deadline.toLocalDate() == todayDate }
            TaskFilter.THIS_WEEK -> allMapped.filter {
                val days = ChronoUnit.DAYS.between(todayDate, it.submission.deadline.toLocalDate())
                days in 0..7
            }
            TaskFilter.URGENT -> allMapped.filter {
                val hours = ChronoUnit.HOURS.between(now, it.submission.deadline)
                hours in 0..24
            }
            TaskFilter.MOODLE -> allMapped.filter { it.submission.source == SubmissionSource.MOODLE }
        }

        // Today's classes sorted by start time
        val todaysClasses = slots
            .filter { it.dayOfWeek == currentDayOfWeek }
            .sortedBy { it.startTime }
            .map { slot ->
                SlotWithSubject(
                    slot = slot,
                    subject = subjects.find { it.id == slot.subjectId }
                )
            }

        // Upcoming exams in the next 14 days
        val upcomingExamsCount = exams.count {
            !it.datetime.plusMinutes(it.durationMinutes.toLong().coerceAtLeast(30L)).isBefore(now)
        }

        HomeUiState(
            submissions = filtered,
            allActiveSubmissions = allMapped,
            todaysClasses = todaysClasses,
            upcomingExamsCount = upcomingExamsCount,
            selectedFilter = currentFilter,
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

    private fun cleanUpDuplicates(duplicates: List<SubmissionEntity>) {
        viewModelScope.launch {
            for (dup in duplicates) {
                try {
                    submissionRepository.deleteSubmission(dup)
                } catch (_: Exception) {}
            }
        }
    }
}
