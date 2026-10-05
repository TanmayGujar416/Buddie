package com.tanmay.buddie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moodle_course_content_cache")
data class MoodleCourseContentCacheEntity(
    @PrimaryKey val courseId: Long,
    val contentJson: String,
    val fetchedAtMillis: Long
)
