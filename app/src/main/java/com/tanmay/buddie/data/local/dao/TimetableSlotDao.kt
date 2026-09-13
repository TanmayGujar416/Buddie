package com.tanmay.buddie.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface TimetableSlotDao {
    @Query("SELECT * FROM timetable_slots ORDER BY dayOfWeek, startTime")
    fun getAllSlots(): Flow<List<TimetableSlotEntity>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :day ORDER BY startTime")
    fun getSlotsForDay(day: DayOfWeek): Flow<List<TimetableSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: TimetableSlotEntity)

    @Update
    suspend fun updateSlot(slot: TimetableSlotEntity)

    @Delete
    suspend fun deleteSlot(slot: TimetableSlotEntity)

    @Query("DELETE FROM timetable_slots WHERE id = :id")
    suspend fun deleteSlotById(id: String)

    @Query("DELETE FROM timetable_slots WHERE subjectId = :subjectId")
    suspend fun deleteSlotsForSubject(subjectId: String)
}
