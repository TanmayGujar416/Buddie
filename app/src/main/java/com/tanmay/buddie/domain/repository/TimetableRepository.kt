package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

interface TimetableRepository {
    fun getAllSlots(): Flow<List<TimetableSlotEntity>>
    fun getSlotsForDay(day: DayOfWeek): Flow<List<TimetableSlotEntity>>
    suspend fun insertSlot(slot: TimetableSlotEntity)
    suspend fun updateSlot(slot: TimetableSlotEntity)
    suspend fun deleteSlot(slot: TimetableSlotEntity)
    suspend fun deleteSlotById(id: String)
}
