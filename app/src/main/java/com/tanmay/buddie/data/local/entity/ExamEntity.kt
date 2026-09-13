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
    val allowedMaterials: String?,
    val roomOverride: String? = null
)
