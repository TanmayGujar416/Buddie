package com.tanmay.buddie.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodleCourseMappingDao {
    @Query("SELECT * FROM moodle_course_mappings")
    fun getAllMappings(): Flow<List<MoodleCourseMappingEntity>>

    @Query("SELECT * FROM moodle_course_mappings WHERE moodleCourseId = :courseId")
    suspend fun getMappingByCourseId(courseId: Long): MoodleCourseMappingEntity?

    @Query("SELECT * FROM moodle_course_mappings WHERE subjectId = :subjectId")
    suspend fun getMappingsBySubjectId(subjectId: String): List<MoodleCourseMappingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMapping(mapping: MoodleCourseMappingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMappings(mappings: List<MoodleCourseMappingEntity>)

    @Delete
    suspend fun deleteMapping(mapping: MoodleCourseMappingEntity)

    @Query("DELETE FROM moodle_course_mappings WHERE moodleCourseId = :courseId")
    suspend fun deleteMappingByCourseId(courseId: Long)

    @Query("DELETE FROM moodle_course_mappings")
    suspend fun deleteAllMappings()
}
