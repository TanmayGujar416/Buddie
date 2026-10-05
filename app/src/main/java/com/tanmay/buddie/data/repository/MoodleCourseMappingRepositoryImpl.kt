package com.tanmay.buddie.data.repository

import com.tanmay.buddie.data.local.dao.MoodleCourseMappingDao
import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodleCourseMappingRepositoryImpl @Inject constructor(
    private val dao: MoodleCourseMappingDao
) : MoodleCourseMappingRepository {

    override fun getAllMappings(): Flow<List<MoodleCourseMappingEntity>> = dao.getAllMappings()

    override suspend fun getMappingByCourseId(courseId: Long): MoodleCourseMappingEntity? =
        dao.getMappingByCourseId(courseId)

    override suspend fun saveMapping(courseId: Long, courseName: String, subjectId: String?) {
        dao.insertOrUpdateMapping(
            MoodleCourseMappingEntity(
                moodleCourseId = courseId,
                moodleCourseName = courseName,
                subjectId = subjectId
            )
        )
    }

    override suspend fun deleteMappingByCourseId(courseId: Long) {
        dao.deleteMappingByCourseId(courseId)
    }

    override suspend fun clearAllMappings() {
        dao.deleteAllMappings()
    }
}
