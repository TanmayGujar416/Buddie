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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<SubmissionEntity>)

    @Update
    suspend fun updateSubmission(submission: SubmissionEntity)

    @Delete
    suspend fun deleteSubmission(submission: SubmissionEntity)

    @Query("SELECT * FROM submissions WHERE moodleAssignmentId = :cmid AND source = 'MOODLE' LIMIT 1")
    suspend fun getSubmissionByMoodleId(cmid: Long): SubmissionEntity?

    @Query("SELECT * FROM submissions WHERE source = 'MOODLE'")
    suspend fun getAllMoodleSubmissions(): List<SubmissionEntity>

    @Query("UPDATE submissions SET title = :title, deadline = :deadline, moodleCourseId = :courseId, filteredOut = :filteredOut, needsReview = :needsReview, moodleVanished = 0 WHERE id = :id")
    suspend fun updateMoodleSubmission(id: String, title: String, deadline: java.time.LocalDateTime, courseId: Long, filteredOut: Boolean, needsReview: Boolean)

    @Query("UPDATE submissions SET filteredOut = :filteredOut WHERE id = :id")
    suspend fun updateFilteredOut(id: String, filteredOut: Boolean)

    @Query("UPDATE submissions SET moodleVanished = 1 WHERE id = :id")
    suspend fun markMoodleVanished(id: String)

    @Query("DELETE FROM submissions WHERE source = 'MOODLE' AND moodleCourseId = :courseId")
    suspend fun deleteMoodleSubmissionsByCourseId(courseId: Long): Int

    @Query("DELETE FROM submissions WHERE source = 'MOODLE' AND subjectId = :subjectId")
    suspend fun deleteMoodleSubmissionsBySubjectId(subjectId: String): Int
}
