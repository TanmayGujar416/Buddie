package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.TimetableSlotDao
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import com.tanmay.buddie.domain.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import javax.inject.Inject

class TimetableRepositoryImpl @Inject constructor(
    private val dao: TimetableSlotDao
) : TimetableRepository {
    override fun getAllSlots(): Flow<List<TimetableSlotEntity>> = dao.getAllSlots()
    override fun getSlotsForDay(day: DayOfWeek): Flow<List<TimetableSlotEntity>> = dao.getSlotsForDay(day)
    override suspend fun insertSlot(slot: TimetableSlotEntity) = dao.insertSlot(slot)
    override suspend fun updateSlot(slot: TimetableSlotEntity) = dao.updateSlot(slot)
    override suspend fun deleteSlot(slot: TimetableSlotEntity) = dao.deleteSlot(slot)
    override suspend fun deleteSlotById(id: String) = dao.deleteSlotById(id)
}
