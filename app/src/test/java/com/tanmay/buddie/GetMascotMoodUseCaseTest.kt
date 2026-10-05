package com.tanmay.buddie

import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.domain.repository.ExamRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import com.tanmay.buddie.domain.usecase.GetMascotMoodUseCase
import com.tanmay.buddie.presentation.components.mascot.MascotMood
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMascotMoodUseCaseTest {

    private class FakeSubmissionRepository : SubmissionRepository {
        val submissionsFlow = MutableStateFlow<List<SubmissionEntity>>(emptyList())
        override fun getAllSubmissions(): Flow<List<SubmissionEntity>> = submissionsFlow
        override suspend fun getSubmissionById(id: String): SubmissionEntity? = null
        override suspend fun insertSubmission(submission: SubmissionEntity) {}
        override suspend fun insertSubmissions(submissions: List<SubmissionEntity>) {}
        override suspend fun updateSubmission(submission: SubmissionEntity) {}
        override suspend fun deleteSubmission(submission: SubmissionEntity) {}
        override suspend fun getSubmissionByMoodleId(cmid: Long): SubmissionEntity? = null
        override suspend fun getAllMoodleSubmissions(): List<SubmissionEntity> = emptyList()
        override suspend fun updateMoodleSubmission(id: String, title: String, deadline: java.time.LocalDateTime, courseId: Long, filteredOut: Boolean, needsReview: Boolean) {}
        override suspend fun updateFilteredOut(id: String, filteredOut: Boolean) {}
        override suspend fun markMoodleVanished(id: String) {}
        override suspend fun deleteMoodleSubmissionsByCourseId(courseId: Long): Int = 0
        override suspend fun deleteMoodleSubmissionsBySubjectId(subjectId: String): Int = 0
    }

    private class FakeExamRepository : ExamRepository {
        val examsFlow = MutableStateFlow<List<ExamEntity>>(emptyList())
        override fun getAllExams(): Flow<List<ExamEntity>> = examsFlow
        override suspend fun getExamById(id: String): ExamEntity? = null
        override suspend fun insertExam(exam: ExamEntity) {}
        override suspend fun updateExam(exam: ExamEntity) {}
        override suspend fun deleteExam(exam: ExamEntity) {}
    }

    @Test
    fun testEmptyDataEmitsCalm() = runBlocking {
        val subRepo = FakeSubmissionRepository()
        val examRepo = FakeExamRepository()
        val useCase = GetMascotMoodUseCase(subRepo, examRepo)

        val mood = useCase().first()
        assertEquals(MascotMood.CALM, mood)
    }
}
