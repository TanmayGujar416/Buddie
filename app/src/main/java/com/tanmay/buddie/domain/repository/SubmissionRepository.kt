package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.SubmissionEntity
import kotlinx.coroutines.flow.Flow

interface SubmissionRepository {
    fun getAllSubmissions(): Flow<List<SubmissionEntity>>
    suspend fun getSubmissionById(id: String): SubmissionEntity?
    suspend fun insertSubmission(submission: SubmissionEntity)
    suspend fun insertSubmissions(submissions: List<SubmissionEntity>)
    suspend fun updateSubmission(submission: SubmissionEntity)
    suspend fun deleteSubmission(submission: SubmissionEntity)
    suspend fun getSubmissionByMoodleId(cmid: Long): SubmissionEntity?
    suspend fun getAllMoodleSubmissions(): List<SubmissionEntity>
    suspend fun updateMoodleSubmission(id: String, title: String, deadline: java.time.LocalDateTime, courseId: Long, filteredOut: Boolean, needsReview: Boolean)
    suspend fun updateFilteredOut(id: String, filteredOut: Boolean)
    suspend fun markMoodleVanished(id: String)
    suspend fun deleteMoodleSubmissionsByCourseId(courseId: Long): Int
    suspend fun deleteMoodleSubmissionsBySubjectId(subjectId: String): Int
}
