package com.tanmay.buddie.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tanmay.buddie.data.local.entity.ExamEntity
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity
import com.tanmay.buddie.data.local.dao.SubjectDao
import com.tanmay.buddie.data.local.dao.TimetableSlotDao
import com.tanmay.buddie.data.local.dao.SubmissionDao
import com.tanmay.buddie.data.local.dao.ExamDao

@Database(
    entities = [
        SubjectEntity::class,
        TimetableSlotEntity::class,
        SubmissionEntity::class,
        ExamEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val subjectDao: SubjectDao
    abstract val timetableSlotDao: TimetableSlotDao
    abstract val submissionDao: SubmissionDao
    abstract val examDao: ExamDao
}
