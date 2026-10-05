package com.tanmay.buddie.data.local.entity

/**
 * Represents a batch or section filter code for a subject (e.g., "C1", "D3", "T2").
 *
 * @param id Room auto-generated or in-memory ID
 * @param subjectId The associated subject ID
 * @param code The token/code (e.g., "C1", "T2")
 * @param isMine If true, this code belongs to the user (allow-list). If false, explicitly excluded.
 */
data class FilterCode(
    val id: Long = 0,
    val subjectId: String,
    val code: String,
    val isMine: Boolean
)

typealias FilterCodeEntity = FilterCode
