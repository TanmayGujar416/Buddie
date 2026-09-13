package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.ExamDao
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.domain.repository.ExamRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExamRepositoryImpl @Inject constructor(
    private val dao: ExamDao
) : ExamRepository {
    override fun getAllExams(): Flow<List<ExamEntity>> = dao.getAllExams()
    override suspend fun getExamById(id: String): ExamEntity? = dao.getExamById(id)
    override suspend fun insertExam(exam: ExamEntity) = dao.insertExam(exam)
    override suspend fun updateExam(exam: ExamEntity) = dao.updateExam(exam)
    override suspend fun deleteExam(exam: ExamEntity) = dao.deleteExam(exam)
}
