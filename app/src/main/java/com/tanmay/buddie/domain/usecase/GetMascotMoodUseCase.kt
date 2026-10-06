package com.tanmay.buddie.domain.usecase

import com.tanmay.buddie.data.local.entity.SubmissionStatus
import com.tanmay.buddie.domain.repository.ExamRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import com.tanmay.buddie.presentation.components.mascot.MascotMoodHelper
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the Mascot Mood across the entire app (Home, Exams, and Floating Overlay).
 * Continuously observes active submissions and exams from Room, recalculating mood live on data changes.
 * Debounced by 300ms to coalesce rapid successive database updates.
 */
@Singleton
class GetMascotMoodUseCase @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val examRepository: ExamRepository
) {
    @OptIn(FlowPreview::class)
    operator fun invoke(): Flow<MascotMood> {
        return combine(
            submissionRepository.getAllSubmissions(),
            examRepository.getAllExams()
        ) { submissions, exams ->
            val now = java.time.LocalDateTime.now()
            val activeSubmissions = submissions.filter { it.status != SubmissionStatus.DONE && !it.filteredOut && !it.needsReview }
            // Exclude exams that have already concluded so finished exams don't permanently trigger OVERDUE
            val upcomingOrActiveExams = exams.filter {
                !it.datetime.plusMinutes(it.durationMinutes.toLong().coerceAtLeast(30L)).isBefore(now)
            }
            val allDeadlines = activeSubmissions.map { it.deadline } + upcomingOrActiveExams.map { it.datetime }
            MascotMoodHelper.calculateMood(allDeadlines, now)
        }.debounce(300L)
    }
}
