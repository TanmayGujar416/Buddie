$ErrorActionPreference = 'Stop'
$baseDir = "d:\Buddie"
Set-Location $baseDir

# SubjectEntity.kt
@"
package com.tanmay.buddie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String?
)
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/entity/SubjectEntity.kt" -Encoding UTF8

# TimetableSlotEntity.kt
@"
package com.tanmay.buddie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime

enum class SessionType { LECTURE, LAB, TUTORIAL }

@Entity(
    tableName = "timetable_slots",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class TimetableSlotEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val sessionType: SessionType,
    val room: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/entity/TimetableSlotEntity.kt" -Encoding UTF8

# SubmissionEntity.kt
@"
package com.tanmay.buddie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class SubmissionStatus { NOT_STARTED, IN_PROGRESS, DONE }

@Entity(
    tableName = "submissions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class SubmissionEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val sessionType: SessionType?,
    val title: String,
    val deadline: LocalDateTime,
    val status: SubmissionStatus
)
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/entity/SubmissionEntity.kt" -Encoding UTF8

# ExamEntity.kt
@"
package com.tanmay.buddie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class ExamEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val title: String,
    val datetime: LocalDateTime,
    val durationMinutes: Int,
    val hall: String,
    val seatNumber: String?,
    val syllabusScope: String?,
    val allowedMaterials: String?
)
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/entity/ExamEntity.kt" -Encoding UTF8

# Converters.kt
@"
package com.tanmay.buddie.data.local

import androidx.room.TypeConverter
import com.tanmay.buddie.data.local.entity.SessionType
import com.tanmay.buddie.data.local.entity.SubmissionStatus
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.format(DateTimeFormatter.ISO_LOCAL_TIME)

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? = timeString?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? = dateTimeString?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }

    @TypeConverter
    fun fromDayOfWeek(day: DayOfWeek?): String? = day?.name

    @TypeConverter
    fun toDayOfWeek(dayString: String?): DayOfWeek? = dayString?.let { DayOfWeek.valueOf(it) }

    @TypeConverter
    fun fromSessionType(type: SessionType?): String? = type?.name

    @TypeConverter
    fun toSessionType(typeString: String?): SessionType? = typeString?.let { SessionType.valueOf(it) }

    @TypeConverter
    fun fromSubmissionStatus(status: SubmissionStatus?): String? = status?.name

    @TypeConverter
    fun toSubmissionStatus(statusString: String?): SubmissionStatus? = statusString?.let { SubmissionStatus.valueOf(it) }
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/Converters.kt" -Encoding UTF8

# AppDatabase.kt
@"
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
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val subjectDao: SubjectDao
    abstract val timetableSlotDao: TimetableSlotDao
    abstract val submissionDao: SubmissionDao
    abstract val examDao: ExamDao
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/AppDatabase.kt" -Encoding UTF8

# DAOs
@"
package com.tanmay.buddie.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tanmay.buddie.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/dao/SubjectDao.kt" -Encoding UTF8

@"
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
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/dao/TimetableSlotDao.kt" -Encoding UTF8

@"
package com.tanmay.buddie.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubmissionDao {
    @Query("SELECT * FROM submissions ORDER BY deadline ASC")
    fun getAllSubmissions(): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE id = :id")
    suspend fun getSubmissionById(id: String): SubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: SubmissionEntity)

    @Update
    suspend fun updateSubmission(submission: SubmissionEntity)

    @Delete
    suspend fun deleteSubmission(submission: SubmissionEntity)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/dao/SubmissionDao.kt" -Encoding UTF8

@"
package com.tanmay.buddie.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tanmay.buddie.data.local.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY datetime ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getExamById(id: String): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)
}
"@ | Out-File -FilePath "app/src/main/java/com/tanmay/buddie/data/local/dao/ExamDao.kt" -Encoding UTF8

Write-Host "Database Entities and DAOs Created"
