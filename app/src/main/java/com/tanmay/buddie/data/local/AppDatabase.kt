package com.tanmay.buddie.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.data.local.entity.MoodleCourseContentCacheEntity
import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import com.tanmay.buddie.data.local.dao.SubjectDao
import com.tanmay.buddie.data.local.dao.TimetableSlotDao
import com.tanmay.buddie.data.local.dao.SubmissionDao
import com.tanmay.buddie.data.local.dao.ExamDao
import com.tanmay.buddie.data.local.dao.MoodleCourseMappingDao
import com.tanmay.buddie.data.local.dao.MoodleCourseContentCacheDao
import com.tanmay.buddie.data.local.entity.TokenSignatureDecision
import com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao

@Database(
    entities = [
        SubjectEntity::class,
        TimetableSlotEntity::class,
        SubmissionEntity::class,
        ExamEntity::class,
        MoodleCourseMappingEntity::class,
        MoodleCourseContentCacheEntity::class,
        TokenSignatureDecision::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val subjectDao: SubjectDao
    abstract val timetableSlotDao: TimetableSlotDao
    abstract val submissionDao: SubmissionDao
    abstract val examDao: ExamDao
    abstract val moodleCourseMappingDao: MoodleCourseMappingDao
    abstract val moodleCourseContentCacheDao: MoodleCourseContentCacheDao
    abstract val tokenSignatureDecisionDao: TokenSignatureDecisionDao
}
