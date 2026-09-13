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
    override suspend fun updateSubmission(submission: SubmissionEntity) = dao.updateSubmission(submission)
    override suspend fun deleteSubmission(submission: SubmissionEntity) = dao.deleteSubmission(submission)
}
