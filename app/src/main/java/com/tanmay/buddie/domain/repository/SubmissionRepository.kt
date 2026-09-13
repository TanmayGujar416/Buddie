package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.SubmissionEntity
import kotlinx.coroutines.flow.Flow

interface SubmissionRepository {
    fun getAllSubmissions(): Flow<List<SubmissionEntity>>
    suspend fun getSubmissionById(id: String): SubmissionEntity?
    suspend fun insertSubmission(submission: SubmissionEntity)
    suspend fun updateSubmission(submission: SubmissionEntity)
    suspend fun deleteSubmission(submission: SubmissionEntity)
}
