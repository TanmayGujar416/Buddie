package com.tanmay.buddie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tanmay.buddie.data.local.entity.MoodleCourseContentCacheEntity

@Dao
interface MoodleCourseContentCacheDao {
    @Query("SELECT * FROM moodle_course_content_cache WHERE courseId = :courseId")
    suspend fun getCachedContent(courseId: Long): MoodleCourseContentCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: MoodleCourseContentCacheEntity)

    @Query("DELETE FROM moodle_course_content_cache WHERE courseId = :courseId")
    suspend fun deleteByCourseId(courseId: Long)

    @Query("DELETE FROM moodle_course_content_cache")
    suspend fun clearAll()
}
