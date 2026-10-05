package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.SubmissionDao
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.domain.repository.SubmissionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubmissionRepositoryImpl @Inject constructor(
    private val dao: SubmissionDao
) : SubmissionRepository {
    override fun getAllSubmissions(): Flow<List<SubmissionEntity>> = dao.getAllSubmissions()
    override suspend fun getSubmissionById(id: String): SubmissionEntity? = dao.getSubmissionById(id)
    override suspend fun insertSubmission(submission: SubmissionEntity) = dao.insertSubmission(submission)
    override suspend fun insertSubmissions(submissions: List<SubmissionEntity>) = dao.insertSubmissions(submissions)
    override suspend fun updateSubmission(submission: SubmissionEntity) = dao.updateSubmission(submission)
    override suspend fun deleteSubmission(submission: SubmissionEntity) = dao.deleteSubmission(submission)
    override suspend fun getSubmissionByMoodleId(cmid: Long): SubmissionEntity? = dao.getSubmissionByMoodleId(cmid)
    override suspend fun getAllMoodleSubmissions(): List<SubmissionEntity> = dao.getAllMoodleSubmissions()
    override suspend fun updateMoodleSubmission(id: String, title: String, deadline: java.time.LocalDateTime, courseId: Long, filteredOut: Boolean, needsReview: Boolean) =
        dao.updateMoodleSubmission(id, title, deadline, courseId, filteredOut, needsReview)
    override suspend fun updateFilteredOut(id: String, filteredOut: Boolean) = dao.updateFilteredOut(id, filteredOut)
    override suspend fun markMoodleVanished(id: String) = dao.markMoodleVanished(id)
    override suspend fun deleteMoodleSubmissionsByCourseId(courseId: Long): Int = dao.deleteMoodleSubmissionsByCourseId(courseId)
    override suspend fun deleteMoodleSubmissionsBySubjectId(subjectId: String): Int = dao.deleteMoodleSubmissionsBySubjectId(subjectId)
}
