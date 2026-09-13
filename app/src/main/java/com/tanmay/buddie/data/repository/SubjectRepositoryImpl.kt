package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.SubjectDao
import com.tanmay.buddie.data.local.dao.TimetableSlotDao
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val dao: SubjectDao,
    private val slotDao: TimetableSlotDao
) : SubjectRepository {
    override fun getAllSubjects(): Flow<List<SubjectEntity>> = dao.getAllSubjects()
    override suspend fun getSubjectById(id: String): SubjectEntity? = dao.getSubjectById(id)
    override suspend fun insertSubject(subject: SubjectEntity) = dao.insertSubject(subject)
    override suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    override suspend fun deleteSubject(subject: SubjectEntity) {
        slotDao.deleteSlotsForSubject(subject.id)
        dao.deleteSubject(subject)
    }
    override suspend fun deleteSubjectById(id: String) {
        slotDao.deleteSlotsForSubject(id)
        dao.deleteSubjectById(id)
    }
}
