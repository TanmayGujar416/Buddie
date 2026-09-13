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
    val status: SubmissionStatus,
    val roomOverride: String? = null
)
