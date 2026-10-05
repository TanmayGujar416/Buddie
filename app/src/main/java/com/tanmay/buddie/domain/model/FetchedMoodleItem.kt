package com.tanmay.buddie.domain.model

data class FetchedMoodleItem(
    val courseId: Long,
    val courseName: String,
    val mappedSubjectId: String,
    val mappedSubjectName: String,
    val cmid: Long,
    val title: String,
    val dueDate: Long?, // Unix timestamp in seconds, null if no due date
    val ancestorHeadings: List<String> = emptyList()
)
