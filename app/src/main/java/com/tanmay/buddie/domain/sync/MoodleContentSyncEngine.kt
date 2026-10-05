package com.tanmay.buddie.domain.sync

import com.tanmay.buddie.data.local.dao.MoodleCourseContentCacheDao
import com.tanmay.buddie.data.local.entity.MoodleCourseContentCacheEntity
import com.tanmay.buddie.data.remote.moodle.MoodleApiClient
import com.tanmay.buddie.data.remote.moodle.MoodleAssignment
import com.tanmay.buddie.data.remote.moodle.MoodleModule
import com.tanmay.buddie.data.remote.moodle.MoodleSection
import com.tanmay.buddie.domain.model.FetchedMoodleItem
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.tanmay.buddie.domain.repository.TokenSignatureRepository
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class MoodleContentSyncEngine @Inject constructor(
    private val moodleApiClient: MoodleApiClient,
    private val courseMappingRepository: MoodleCourseMappingRepository,
    private val subjectRepository: SubjectRepository,
    private val contentCacheDao: MoodleCourseContentCacheDao,
    private val tokenSignatureRepository: TokenSignatureRepository,
    private val mergeEngine: MoodleMergeEngine
) {
    companion object {
        const val CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L // 24 hours
    }

    @Volatile
    private var lastFetchedItems: List<FetchedMoodleItem>? = null

    open fun getLastFetchedItems(): List<FetchedMoodleItem> = lastFetchedItems.orEmpty()

    open fun getDetectedTokensForSubject(subjectId: String): List<String> {
        val itemsForSubject = lastFetchedItems?.filter { it.mappedSubjectId == subjectId }.orEmpty()
        return MoodleTokenScanner.extractTokens(itemsForSubject)
    }

    @Volatile
    private var lastSyncTimestampMillis: Long = 0L

    open fun getLastSyncTimestamp(): Long = lastSyncTimestampMillis

    /**
     * Executes the full pipeline:
     * 1. Fetches raw assignments & course sections (using 24h cache for sections unless forced).
     * 2. Evaluates FilterCodes for each Subject (marks filteredOut = true/false).
     * 3. Merges into Submissions table (idempotent, preserves user status, flags vanished).
     */
    open suspend fun runFullSync(
        forceRefreshCourseStructure: Boolean = false
    ): Result<MergeResult> = withContext(Dispatchers.IO) {
        val previewResult = fetchPreviewItems(forceRefreshCourseStructure)
        if (previewResult.isFailure) {
            return@withContext Result.failure(
                previewResult.exceptionOrNull() ?: Exception("Failed to fetch Moodle items")
            )
        }
        val items = previewResult.getOrThrow()
        val mergeResult = applyFiltersAndMerge(items)
        if (mergeResult.isSuccess) {
            lastSyncTimestampMillis = System.currentTimeMillis()
        }
        mergeResult
    }

    /**
     * Syncs fresh items from Moodle if more than [staleThresholdMillis] has elapsed
     * since the last successful sync. Returns null if data is already fresh.
     */
    open suspend fun syncIfStale(
        staleThresholdMillis: Long = 5 * 60 * 1000L
    ): Result<MergeResult>? {
        val now = System.currentTimeMillis()
        if (now - lastSyncTimestampMillis >= staleThresholdMillis) {
            return runFullSync(forceRefreshCourseStructure = false)
        }
        return null
    }

    /**
     * Re-runs filter evaluation and merge using cached FetchedMoodleItems without making network calls.
     * Perfect for updating Submissions immediately after a user adds, edits, or toggles FilterCodes.
     */
    open suspend fun reapplyFiltersAndMerge(): Result<MergeResult> = withContext(Dispatchers.IO) {
        val cached = lastFetchedItems
        if (cached != null) {
            applyFiltersAndMerge(cached)
        } else {
            runFullSync(forceRefreshCourseStructure = false)
        }
    }

    private suspend fun applyFiltersAndMerge(items: List<FetchedMoodleItem>): Result<MergeResult> {
        return try {
            val allDecisions = tokenSignatureRepository.getAllDecisions().first()
            val decisionsBySubjectId = allDecisions
                .groupBy { it.subjectId }
                .mapValues { (_, list) -> list.associate { it.signature to it.decision } }
            val filtered = MoodleItemFilter.applyDecisions(items, decisionsBySubjectId)
            val result = mergeEngine.mergeFilteredItems(filtered)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches and correlates content for all courses mapped to an app Subject.
     * Ignored courses (subjectId == null) are skipped.
     *
     * @param forceRefreshCourseStructure if true, bypasses the 24h cache for course sections.
     * Assignment due dates are ALWAYS fetched fresh regardless of this parameter.
     */
    open suspend fun fetchPreviewItems(
        forceRefreshCourseStructure: Boolean = false
    ): Result<List<FetchedMoodleItem>> = withContext(Dispatchers.IO) {
        try {
            // 1. Retrieve all mappings and filter only mapped courses (skip ignored ones)
            val allMappings = courseMappingRepository.getAllMappings().first()
            val mappedCourses = allMappings.filter { it.subjectId != null }

            if (mappedCourses.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            // 2. Resolve Subject names
            val allSubjects = subjectRepository.getAllSubjects().first()
            val subjectNameById = allSubjects.associate { it.id to it.name }

            val mappedCourseIds = mappedCourses.map { it.moodleCourseId }

            // 3. Always re-fetch assignments fresh on every sync
            val assignmentsResult = moodleApiClient.getAssignments(mappedCourseIds)
            if (assignmentsResult.isFailure) {
                return@withContext Result.failure(
                    assignmentsResult.exceptionOrNull()
                        ?: Exception("Failed to retrieve assignment data from Moodle")
                )
            }
            val assignments = assignmentsResult.getOrThrow()
            val assignmentByCmid = assignments.associateBy { it.cmid }

            val resultItems = mutableListOf<FetchedMoodleItem>()

            // 4. For each mapped course, retrieve contents (using 24h cache unless expired or forced)
            for (courseMapping in mappedCourses) {
                val courseId = courseMapping.moodleCourseId
                val courseName = courseMapping.moodleCourseName
                val subjectName = courseMapping.subjectId?.let { subjectNameById[it] } ?: "Unknown Subject"

                val sectionsResult = getCourseSectionsWithCache(courseId, forceRefreshCourseStructure)
                if (sectionsResult.isFailure) {
                    return@withContext Result.failure(
                        sectionsResult.exceptionOrNull()
                            ?: Exception("Failed to retrieve course sections for '$courseName'")
                    )
                }

                val sections = sectionsResult.getOrThrow()
                val addedCmidsForCourse = mutableSetOf<Long>()

                // Walk whatever structure core_course_get_contents returns and collect every heading
                for (section in sections) {
                    val sectionHeading = section.name.trim()
                    val ancestorHeadings = if (sectionHeading.isNotBlank()) listOf(sectionHeading) else emptyList()

                    for (module in section.modules) {
                        val isAssignModule = module.modname.equals("assign", ignoreCase = true)
                        val correlatedAssignment = assignmentByCmid[module.id]

                        if (isAssignModule || correlatedAssignment != null) {
                            val title = correlatedAssignment?.name?.takeIf { it.isNotBlank() }
                                ?: module.name.takeIf { it.isNotBlank() }
                                ?: "Assignment"

                            val rawDueDate = correlatedAssignment?.dueDate ?: 0L
                            val dueDate = if (rawDueDate > 0L) rawDueDate else null

                            resultItems.add(
                                FetchedMoodleItem(
                                    courseId = courseId,
                                    courseName = courseName,
                                    mappedSubjectId = courseMapping.subjectId ?: "",
                                    mappedSubjectName = subjectName,
                                    cmid = module.id,
                                    title = title,
                                    dueDate = dueDate,
                                    ancestorHeadings = ancestorHeadings
                                )
                            )
                            addedCmidsForCourse.add(module.id)
                        }
                    }
                }

                // Fallback: Check if there are assignments returned by mod_assign_get_assignments
                // that were not referenced inside the course contents modules (e.g. special course formats)
                val orphanAssignments = assignments.filter {
                    it.courseId == courseId && it.cmid !in addedCmidsForCourse
                }
                for (orphan in orphanAssignments) {
                    val dueDate = if (orphan.dueDate > 0L) orphan.dueDate else null
                    resultItems.add(
                        FetchedMoodleItem(
                            courseId = courseId,
                            courseName = courseName,
                            mappedSubjectId = courseMapping.subjectId ?: "",
                            mappedSubjectName = subjectName,
                            cmid = orphan.cmid,
                            title = orphan.name.ifBlank { "Assignment" },
                            dueDate = dueDate,
                            ancestorHeadings = listOf("Assignments")
                        )
                    )
                }
            }

            lastFetchedItems = resultItems
            Result.success(resultItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getCourseSectionsWithCache(
        courseId: Long,
        forceRefresh: Boolean
    ): Result<List<MoodleSection>> {
        val now = System.currentTimeMillis()
        val cached = if (!forceRefresh) contentCacheDao.getCachedContent(courseId) else null

        if (cached != null && (now - cached.fetchedAtMillis) < CACHE_TTL_MILLIS) {
            try {
                val sections = parseSectionsFromJson(cached.contentJson)
                return Result.success(sections)
            } catch (_: Exception) {
                // Stale or corrupted cache JSON, fall through to fresh fetch
            }
        }

        // Cache miss or expired: fetch fresh from MoodleApiClient
        val remoteResult = moodleApiClient.getCourseContents(courseId)
        if (remoteResult.isSuccess) {
            val sections = remoteResult.getOrThrow()
            try {
                val json = serializeSectionsToJson(sections)
                contentCacheDao.insertOrUpdate(
                    MoodleCourseContentCacheEntity(
                        courseId = courseId,
                        contentJson = json,
                        fetchedAtMillis = now
                    )
                )
            } catch (_: Exception) {
                // Non-fatal if caching fails
            }
            return Result.success(sections)
        } else {
            // If remote call fails but we have stale cache, use stale cache as best-effort fallback
            if (cached != null) {
                try {
                    val sections = parseSectionsFromJson(cached.contentJson)
                    return Result.success(sections)
                } catch (_: Exception) {}
            }
            return remoteResult
        }
    }

    fun serializeSectionsToJson(sections: List<MoodleSection>): String {
        val arr = JSONArray()
        for (sec in sections) {
            val sObj = JSONObject()
            sObj.put("id", sec.id)
            sObj.put("name", sec.name)
            val mArr = JSONArray()
            for (mod in sec.modules) {
                val mObj = JSONObject()
                mObj.put("id", mod.id)
                mObj.put("name", mod.name)
                mObj.put("modname", mod.modname)
                mObj.put("instance", mod.instance)
                mArr.put(mObj)
            }
            sObj.put("modules", mArr)
            arr.put(sObj)
        }
        return arr.toString()
    }

    fun parseSectionsFromJson(jsonStr: String): List<MoodleSection> {
        val arr = JSONArray(jsonStr)
        val sections = mutableListOf<MoodleSection>()
        for (i in 0 until arr.length()) {
            val sObj = arr.getJSONObject(i)
            val sId = sObj.optLong("id", 0L)
            val sName = sObj.optString("name", "")
            val mArr = sObj.optJSONArray("modules")
            val modules = mutableListOf<MoodleModule>()
            if (mArr != null) {
                for (j in 0 until mArr.length()) {
                    val mObj = mArr.getJSONObject(j)
                    modules.add(
                        MoodleModule(
                            id = mObj.getLong("id"),
                            name = mObj.optString("name", ""),
                            modname = mObj.optString("modname", ""),
                            instance = mObj.optLong("instance", 0L)
                        )
                    )
                }
            }
            sections.add(MoodleSection(id = sId, name = sName, modules = modules))
        }
        return sections
    }
}
