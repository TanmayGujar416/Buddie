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
