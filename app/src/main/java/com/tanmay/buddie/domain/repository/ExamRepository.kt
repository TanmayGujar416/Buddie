package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

interface ExamRepository {
    fun getAllExams(): Flow<List<ExamEntity>>
    suspend fun getExamById(id: String): ExamEntity?
    suspend fun insertExam(exam: ExamEntity)
    suspend fun updateExam(exam: ExamEntity)
    suspend fun deleteExam(exam: ExamEntity)
}
