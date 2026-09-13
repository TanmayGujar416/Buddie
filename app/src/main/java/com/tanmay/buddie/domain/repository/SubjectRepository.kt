package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun getAllSubjects(): Flow<List<SubjectEntity>>
    suspend fun getSubjectById(id: String): SubjectEntity?
    suspend fun insertSubject(subject: SubjectEntity)
    suspend fun updateSubject(subject: SubjectEntity)
    suspend fun deleteSubject(subject: SubjectEntity)
    suspend fun deleteSubjectById(id: String)
}
