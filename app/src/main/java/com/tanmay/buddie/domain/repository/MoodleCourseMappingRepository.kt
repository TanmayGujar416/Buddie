package com.tanmay.buddie.domain.repository

import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity
import kotlinx.coroutines.flow.Flow

interface MoodleCourseMappingRepository {
    fun getAllMappings(): Flow<List<MoodleCourseMappingEntity>>
    suspend fun getMappingByCourseId(courseId: Long): MoodleCourseMappingEntity?
    suspend fun saveMapping(courseId: Long, courseName: String, subjectId: String?)
    suspend fun deleteMappingByCourseId(courseId: Long)
    suspend fun clearAllMappings()
}
