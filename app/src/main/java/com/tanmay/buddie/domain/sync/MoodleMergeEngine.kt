package com.tanmay.buddie.domain.sync

import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.data.local.entity.SubmissionSource
import com.tanmay.buddie.data.local.entity.SubmissionStatus
import com.tanmay.buddie.domain.repository.SubmissionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class MergeResult(
    val insertedCount: Int,
    val updatedCount: Int,
    val vanishedCount: Int,
    val totalProcessed: Int
)

@Singleton
open class MoodleMergeEngine @Inject constructor(
    private val submissionRepository: SubmissionRepository
) {

    private val mergeMutex = Mutex()

    open suspend fun mergeFilteredItems(
        filteredItems: List<FilteredMoodleItem>
    ): MergeResult = withContext(Dispatchers.IO) {
        mergeMutex.withLock {
            // Deduplicate incoming items by cmid to ensure no item is processed twice in a single run
            val distinctItems = filteredItems.distinctBy { it.item.cmid }

            // Query ONLY Moodle-sourced submissions — MANUAL rows are never touched
            val existingMoodleSubmissions = submissionRepository.getAllMoodleSubmissions()

            // Group by moodleAssignmentId to detect and purge any existing duplicate records in DB
            val groupedByCmid = existingMoodleSubmissions
                .filter { it.moodleAssignmentId != null }
                .groupBy { it.moodleAssignmentId!! }

            val existingByCmid = mutableMapOf<Long, SubmissionEntity>()
            for ((cmid, list) in groupedByCmid) {
                if (list.size > 1) {
                    // Pick the most complete entry (prefer DONE status or first) and remove redundant rows
                    val keep = list.firstOrNull { it.status == SubmissionStatus.DONE } ?: list.first()
                    existingByCmid[cmid] = keep
                    list.filter { it.id != keep.id }.forEach { duplicate ->
                        try {
                            submissionRepository.deleteSubmission(duplicate)
                        } catch (_: Exception) {}
                    }
                } else {
                    existingByCmid[cmid] = list.first()
                }
            }

            val seenCmids = mutableSetOf<Long>()
            val insertedCmids = mutableSetOf<Long>()
            var inserted = 0
            var updated = 0
            val toInsert = mutableListOf<SubmissionEntity>()

            for (filteredItem in distinctItems) {
                val item = filteredItem.item
                val cmid = item.cmid
                seenCmids.add(cmid)

                val existing = existingByCmid[cmid]
                val deadline = convertEpochSecondsToLocalDateTime(item.dueDate)

                if (existing == null) {
                    // Not seen before and not already scheduled for insertion in this batch
                    if (cmid !in insertedCmids) {
                        insertedCmids.add(cmid)
                        toInsert.add(
                            SubmissionEntity(
                                id = UUID.randomUUID().toString(),
                                subjectId = item.mappedSubjectId,
                                sessionType = null,
                                title = item.title,
                                deadline = deadline,
                                status = SubmissionStatus.NOT_STARTED,
                                roomOverride = null,
                                source = SubmissionSource.MOODLE,
                                moodleAssignmentId = cmid,
                                moodleCourseId = item.courseId,
                                filteredOut = filteredItem.filteredOut,
                                needsReview = filteredItem.needsReview,
                                moodleVanished = false
                            )
                        )
                        inserted++
                    }
                } else {
                    // Already exists → update title, deadline, courseId, filteredOut, needsReview. NEVER touch status.
                    submissionRepository.updateMoodleSubmission(
                        id = existing.id,
                        title = item.title,
                        deadline = deadline,
                        courseId = item.courseId,
                        filteredOut = filteredItem.filteredOut,
                        needsReview = filteredItem.needsReview
                    )
                    updated++
                }
            }

            if (toInsert.isNotEmpty()) {
                submissionRepository.insertSubmissions(toInsert)
            }

            // Existed before but absent from this fetch → flag as moodleVanished, don't delete
            var vanished = 0
            for ((existingCmid, existingSub) in existingByCmid) {
                if (existingCmid !in seenCmids) {
                    if (!existingSub.moodleVanished) {
                        submissionRepository.markMoodleVanished(existingSub.id)
                        vanished++
                    }
                }
            }

            MergeResult(
                insertedCount = inserted,
                updatedCount = updated,
                vanishedCount = vanished,
                totalProcessed = distinctItems.size
            )
        }
    }

    fun convertEpochSecondsToLocalDateTime(epochSeconds: Long?): LocalDateTime {
        if (epochSeconds == null || epochSeconds <= 0L) {
            return LocalDateTime.now().plusDays(7).withHour(23).withMinute(59).withSecond(0).withNano(0)
        }
        return try {
            val instant = Instant.ofEpochSecond(epochSeconds)
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        } catch (_: Exception) {
            LocalDateTime.now().plusDays(7).withHour(23).withMinute(59).withSecond(0).withNano(0)
        }
    }
}
