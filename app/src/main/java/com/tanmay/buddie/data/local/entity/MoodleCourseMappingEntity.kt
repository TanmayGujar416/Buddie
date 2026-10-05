package com.tanmay.buddie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "moodle_course_mappings",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subjectId"])
    ]
)
data class MoodleCourseMappingEntity(
    @PrimaryKey val moodleCourseId: Long,
    val moodleCourseName: String,
    val subjectId: String? = null // null indicates marked as "Ignore"
)
