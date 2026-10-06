package com.tanmay.buddie

import com.tanmay.buddie.data.local.dao.MoodleCourseContentCacheDao
import com.tanmay.buddie.data.local.entity.MoodleCourseContentCacheEntity
import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity
import com.tanmay.buddie.data.local.entity.SignatureDecision
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.data.local.entity.SubmissionSource
import com.tanmay.buddie.data.local.entity.SubmissionStatus
import com.tanmay.buddie.data.local.entity.TokenSignatureDecision
import com.tanmay.buddie.data.remote.moodle.MoodleApiClient
import com.tanmay.buddie.data.remote.moodle.MoodleAssignment
import com.tanmay.buddie.data.remote.moodle.MoodleModule
import com.tanmay.buddie.data.remote.moodle.MoodleSection
import com.tanmay.buddie.domain.model.FetchedMoodleItem
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import com.tanmay.buddie.domain.repository.TokenSignatureRepository
import com.tanmay.buddie.domain.sync.FilteredMoodleItem
import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine
import com.tanmay.buddie.domain.sync.MoodleItemFilter
import com.tanmay.buddie.domain.sync.MoodleTokenScanner
import com.tanmay.buddie.domain.sync.MoodleMergeEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class MoodleSyncEngineTest {

    private fun createMockOkHttpClient(
        statusCode: Int = 200,
        responseJson: String = ""
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val responseBody = responseJson.toResponseBody("application/json".toMediaType())
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(statusCode)
                    .message(if (statusCode == 200) "OK" else "Error")
                    .body(responseBody)
                    .build()
            }
            .build()
    }

    @Test
    fun getCourseContents_parsesSectionsAndModulesSuccessfully() = runBlocking {
        val jsonResponse = """
            [
                {
                    "id": 1001,
                    "name": "Unit 1: Relational Algebra",
                    "visible": 1,
                    "summary": "",
                    "modules": [
                        {
                            "id": 5001,
                            "name": "Assignment 1: Schema Design",
                            "instance": 201,
                            "modname": "assign"
                        },
                        {
                            "id": 5002,
                            "name": "Lecture 1 Slides",
                            "instance": 301,
                            "modname": "resource"
                        }
                    ]
                },
                {
                    "id": 1002,
                    "name": "Unit 2: SQL & Normalization",
                    "visible": 1,
                    "summary": "",
                    "modules": [
                        {
                            "id": 5003,
                            "name": "Assignment 2: SQL Queries",
                            "instance": 202,
                            "modname": "assign"
                        }
                    ]
                }
            ]
        """.trimIndent()

        val mockClient = createMockOkHttpClient(200, jsonResponse)
        val tokenStorage = MoodleConnectionTest.MoodleTokenStorageFake().apply { saveToken("valid_token") }
        val userPrefs = MoodleConnectionTest.UserPreferencesFake().apply { setMoodleSiteUrl("https://moodle.spit.ac.in") }

        val client = MoodleApiClient(mockClient, tokenStorage, userPrefs)
        val result = client.getCourseContents(101L)

        assertTrue(result.isSuccess)
        val sections = result.getOrThrow()
        assertEquals(2, sections.size)

        val sec1 = sections[0]
        assertEquals(1001L, sec1.id)
        assertEquals("Unit 1: Relational Algebra", sec1.name)
        assertEquals(2, sec1.modules.size)
        assertEquals(5001L, sec1.modules[0].id)
        assertEquals("Assignment 1: Schema Design", sec1.modules[0].name)
        assertEquals("assign", sec1.modules[0].modname)
        assertEquals(201L, sec1.modules[0].instance)

        val sec2 = sections[1]
        assertEquals(1002L, sec2.id)
        assertEquals(1, sec2.modules.size)
        assertEquals(5003L, sec2.modules[2 - 2].id)
    }

    @Test
    fun getAssignments_parsesDueDatesAndCmidsSuccessfully() = runBlocking {
        val jsonResponse = """
            {
                "courses": [
                    {
                        "id": 101,
                        "fullname": "Database Management Systems",
                        "shortname": "DBMS",
                        "assignments": [
                            {
                                "id": 201,
                                "cmid": 5001,
                                "course": 101,
                                "name": "Assignment 1: Schema Design",
                                "duedate": 1775000000
                            },
                            {
                                "id": 202,
                                "cmid": 5003,
                                "course": 101,
                                "name": "Assignment 2: SQL Queries",
                                "duedate": 0
                            }
                        ]
                    }
                ],
                "warnings": []
            }
        """.trimIndent()

        val mockClient = createMockOkHttpClient(200, jsonResponse)
        val tokenStorage = MoodleConnectionTest.MoodleTokenStorageFake().apply { saveToken("valid_token") }
        val userPrefs = MoodleConnectionTest.UserPreferencesFake().apply { setMoodleSiteUrl("https://moodle.spit.ac.in") }

        val client = MoodleApiClient(mockClient, tokenStorage, userPrefs)
        val result = client.getAssignments(listOf(101L))

        assertTrue(result.isSuccess)
        val assignments = result.getOrThrow()
        assertEquals(2, assignments.size)

        val a1 = assignments[0]
        assertEquals(201L, a1.id)
        assertEquals(5001L, a1.cmid)
        assertEquals(101L, a1.courseId)
        assertEquals("Assignment 1: Schema Design", a1.name)
        assertEquals(1775000000L, a1.dueDate)

        val a2 = assignments[1]
        assertEquals(0L, a2.dueDate)
    }

    @Test
    fun syncEngine_correlatesByCmidAndSkipsIgnoredCourses() = runBlocking {
        val fakeApiClient = object : MoodleApiClient(
            OkHttpClient(),
            MoodleConnectionTest.MoodleTokenStorageFake(),
            MoodleConnectionTest.UserPreferencesFake()
        ) {
            override suspend fun getCourseContents(courseId: Long): Result<List<MoodleSection>> {
                if (courseId == 101L) {
                    return Result.success(
                        listOf(
                            MoodleSection(
                                id = 1,
                                name = "Module 1: Foundations",
                                modules = listOf(
                                    MoodleModule(id = 5001L, name = "Lab 1", modname = "assign", instance = 10),
                                    MoodleModule(id = 5002L, name = "PDF Notes", modname = "resource", instance = 11)
                                )
                            )
                        )
                    )
                }
                fail("Should not fetch course contents for unmapped or ignored courses: $courseId")
                return Result.failure(Exception("Not found"))
            }

            override suspend fun getAssignments(courseIds: List<Long>): Result<List<MoodleAssignment>> {
                assertEquals(listOf(101L), courseIds) // Only mapped course 101
                return Result.success(
                    listOf(
                        MoodleAssignment(
                            id = 10,
                            cmid = 5001L,
                            courseId = 101L,
                            name = "Lab 1: ER Diagrams",
                            dueDate = 1776000000L
                        )
                    )
                )
            }
        }

        val fakeMappingRepo = object : MoodleCourseMappingRepository {
            override fun getAllMappings(): Flow<List<MoodleCourseMappingEntity>> = flowOf(
                listOf(
                    MoodleCourseMappingEntity(101L, "Database Systems", "sub_dbms"),
                    MoodleCourseMappingEntity(102L, "Sports Activity", null) // Ignored course!
                )
            )
            override suspend fun getMappingByCourseId(courseId: Long) = null
            override suspend fun saveMapping(courseId: Long, courseName: String, subjectId: String?) {}
            override suspend fun deleteMappingByCourseId(courseId: Long) {}
            override suspend fun clearAllMappings() {}
        }

        val fakeSubjectRepo = FakeSubjectRepository(
            listOf(SubjectEntity(id = "sub_dbms", name = "DBMS", color = "#4285F4"))
        )

        val fakeCacheDao = FakeMoodleCourseContentCacheDao()

        val engine = MoodleContentSyncEngine(
            moodleApiClient = fakeApiClient,
            courseMappingRepository = fakeMappingRepo,
            subjectRepository = fakeSubjectRepo,
            contentCacheDao = fakeCacheDao,
            tokenSignatureRepository = FakeTokenSignatureRepository(),
            mergeEngine = MoodleMergeEngine(FakeSubmissionRepository())
        )

        val result = engine.fetchPreviewItems()
        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(1, items.size)

        val item = items[0]
        assertEquals(101L, item.courseId)
        assertEquals("Database Systems", item.courseName)
        assertEquals("sub_dbms", item.mappedSubjectId)
        assertEquals("DBMS", item.mappedSubjectName)
        assertEquals(5001L, item.cmid)
        assertEquals("Lab 1: ER Diagrams", item.title)
        assertEquals(1776000000L, item.dueDate)
        assertEquals(listOf("Module 1: Foundations"), item.ancestorHeadings)
    }

    @Test
    fun syncEngine_zeroDueDate_mapsToNull() = runBlocking {
        val fakeApiClient = object : MoodleApiClient(
            OkHttpClient(),
            MoodleConnectionTest.MoodleTokenStorageFake(),
            MoodleConnectionTest.UserPreferencesFake()
        ) {
            override suspend fun getCourseContents(courseId: Long): Result<List<MoodleSection>> {
                return Result.success(
                    listOf(
                        MoodleSection(
                            id = 1,
                            name = "General",
                            modules = listOf(
                                MoodleModule(id = 6001L, name = "Homework 1", modname = "assign", instance = 20)
                            )
                        )
                    )
                )
            }

            override suspend fun getAssignments(courseIds: List<Long>): Result<List<MoodleAssignment>> {
                return Result.success(
                    listOf(
                        MoodleAssignment(
                            id = 20,
                            cmid = 6001L,
                            courseId = 101L,
                            name = "Homework 1 (Ungraded)",
                            dueDate = 0L // No due date
                        )
                    )
                )
            }
        }

        val fakeMappingRepo = object : MoodleCourseMappingRepository {
            override fun getAllMappings(): Flow<List<MoodleCourseMappingEntity>> = flowOf(
                listOf(MoodleCourseMappingEntity(101L, "Mathematics", "sub_math"))
            )
            override suspend fun getMappingByCourseId(courseId: Long) = null
            override suspend fun saveMapping(courseId: Long, courseName: String, subjectId: String?) {}
            override suspend fun deleteMappingByCourseId(courseId: Long) {}
            override suspend fun clearAllMappings() {}
        }

        val fakeSubjectRepo = FakeSubjectRepository(
            listOf(SubjectEntity(id = "sub_math", name = "Math", color = null))
        )

        val engine = MoodleContentSyncEngine(
            moodleApiClient = fakeApiClient,
            courseMappingRepository = fakeMappingRepo,
            subjectRepository = fakeSubjectRepo,
            contentCacheDao = FakeMoodleCourseContentCacheDao(),
            tokenSignatureRepository = FakeTokenSignatureRepository(),
            mergeEngine = MoodleMergeEngine(FakeSubmissionRepository())
        )

        val items = engine.fetchPreviewItems().getOrThrow()
        assertEquals(1, items.size)
        assertNull(items[0].dueDate)
    }

    @Test
    fun syncEngine_cacheUnder24h_usesCachedSectionsWithoutCallingApi() = runBlocking {
        var apiContentsCallCount = 0

        val fakeApiClient = object : MoodleApiClient(
            OkHttpClient(),
            MoodleConnectionTest.MoodleTokenStorageFake(),
            MoodleConnectionTest.UserPreferencesFake()
        ) {
            override suspend fun getCourseContents(courseId: Long): Result<List<MoodleSection>> {
                apiContentsCallCount++
                return Result.success(
                    listOf(
                        MoodleSection(
                            id = 1,
                            name = "Section Fresh",
                            modules = listOf(MoodleModule(7001L, "Task Fresh", "assign", 1))
                        )
                    )
                )
            }

            override suspend fun getAssignments(courseIds: List<Long>): Result<List<MoodleAssignment>> {
                return Result.success(
                    listOf(MoodleAssignment(1, 7001L, 101L, "Task Fresh", 12345L))
                )
            }
        }

        val fakeMappingRepo = object : MoodleCourseMappingRepository {
            override fun getAllMappings(): Flow<List<MoodleCourseMappingEntity>> = flowOf(
                listOf(MoodleCourseMappingEntity(101L, "Course 101", "sub_1"))
            )
            override suspend fun getMappingByCourseId(courseId: Long) = null
            override suspend fun saveMapping(courseId: Long, courseName: String, subjectId: String?) {}
            override suspend fun deleteMappingByCourseId(courseId: Long) {}
            override suspend fun clearAllMappings() {}
        }

        val fakeSubjectRepo = FakeSubjectRepository(
            listOf(SubjectEntity("sub_1", "Sub 1", null))
        )

        val fakeCacheDao = FakeMoodleCourseContentCacheDao()

        val engine = MoodleContentSyncEngine(
            moodleApiClient = fakeApiClient,
            courseMappingRepository = fakeMappingRepo,
            subjectRepository = fakeSubjectRepo,
            contentCacheDao = fakeCacheDao,
            tokenSignatureRepository = FakeTokenSignatureRepository(),
            mergeEngine = MoodleMergeEngine(FakeSubmissionRepository())
        )

        // Seed cache with entry created 1 hour ago (< 24h)
        val oneHourAgo = System.currentTimeMillis() - (1 * 60 * 60 * 1000L)
        val cachedSections = listOf(
            MoodleSection(
                id = 99,
                name = "Cached Section",
                modules = listOf(MoodleModule(7001L, "Cached Task", "assign", 1))
            )
        )
        fakeCacheDao.insertOrUpdate(
            MoodleCourseContentCacheEntity(
                courseId = 101L,
                contentJson = engine.serializeSectionsToJson(cachedSections),
                fetchedAtMillis = oneHourAgo
            )
        )

        val items = engine.fetchPreviewItems(forceRefreshCourseStructure = false).getOrThrow()

        assertEquals(0, apiContentsCallCount) // API core_course_get_contents was NOT called
        assertEquals(1, items.size)
        assertEquals(listOf("Cached Section"), items[0].ancestorHeadings)
    }

    @Test
    fun syncEngine_cacheOver24h_fetchesFreshAndUpdatesCache() = runBlocking {
        var apiContentsCallCount = 0

        val fakeApiClient = object : MoodleApiClient(
            OkHttpClient(),
            MoodleConnectionTest.MoodleTokenStorageFake(),
            MoodleConnectionTest.UserPreferencesFake()
        ) {
            override suspend fun getCourseContents(courseId: Long): Result<List<MoodleSection>> {
                apiContentsCallCount++
                return Result.success(
                    listOf(
                        MoodleSection(
                            id = 1,
                            name = "Fresh Section",
                            modules = listOf(MoodleModule(8001L, "Fresh Module", "assign", 1))
                        )
                    )
                )
            }

            override suspend fun getAssignments(courseIds: List<Long>): Result<List<MoodleAssignment>> {
                return Result.success(
                    listOf(MoodleAssignment(1, 8001L, 101L, "Fresh Module", 99999L))
                )
            }
        }

        val fakeMappingRepo = object : MoodleCourseMappingRepository {
            override fun getAllMappings(): Flow<List<MoodleCourseMappingEntity>> = flowOf(
                listOf(MoodleCourseMappingEntity(101L, "Course 101", "sub_1"))
            )
            override suspend fun getMappingByCourseId(courseId: Long) = null
            override suspend fun saveMapping(courseId: Long, courseName: String, subjectId: String?) {}
            override suspend fun deleteMappingByCourseId(courseId: Long) {}
            override suspend fun clearAllMappings() {}
        }

        val fakeSubjectRepo = FakeSubjectRepository(
            listOf(SubjectEntity("sub_1", "Sub 1", null))
        )

        val fakeCacheDao = FakeMoodleCourseContentCacheDao()

        val engine = MoodleContentSyncEngine(
            moodleApiClient = fakeApiClient,
            courseMappingRepository = fakeMappingRepo,
            subjectRepository = fakeSubjectRepo,
            contentCacheDao = fakeCacheDao,
            tokenSignatureRepository = FakeTokenSignatureRepository(),
            mergeEngine = MoodleMergeEngine(FakeSubmissionRepository())
        )

        // Seed cache with entry created 25 hours ago (> 24h)
        val twentyFiveHoursAgo = System.currentTimeMillis() - (25 * 60 * 60 * 1000L)
        fakeCacheDao.insertOrUpdate(
            MoodleCourseContentCacheEntity(
                courseId = 101L,
                contentJson = "[]",
                fetchedAtMillis = twentyFiveHoursAgo
            )
        )

        val items = engine.fetchPreviewItems(forceRefreshCourseStructure = false).getOrThrow()

        assertEquals(1, apiContentsCallCount) // Fresh fetch triggered!
        assertEquals(1, items.size)
        assertEquals(listOf("Fresh Section"), items[0].ancestorHeadings)

        // Verify cache was updated
        val updatedCache = fakeCacheDao.getCachedContent(101L)
        assertNotNull(updatedCache)
        assertTrue(updatedCache!!.fetchedAtMillis > twentyFiveHoursAgo)
    }

    // --- Part A: TokenSignature Decisions & Filter Tests ---

    @Test
    fun computeSignature_sortsAndUppercasesTokensDeterministically() {
        val tokens1 = setOf("w1", "T1", "d3")
        val tokens2 = setOf("D3", "W1", "T1")
        val sig1 = MoodleItemFilter.computeSignature(tokens1)
        val sig2 = MoodleItemFilter.computeSignature(tokens2)

        assertEquals("D3,T1,W1", sig1)
        assertEquals("D3,T1,W1", sig2)
        assertEquals(sig1, sig2)
    }

    @Test
    fun applyDecisions_noTokensDetected_alwaysIncluded() {
        val item = FetchedMoodleItem(
            courseId = 1L,
            courseName = "DBMS",
            mappedSubjectId = "sub_1",
            mappedSubjectName = "DBMS",
            cmid = 101L,
            title = "General Course Assignment",
            dueDate = 1000L,
            ancestorHeadings = listOf("General")
        )

        val filtered = MoodleItemFilter.applyDecisions(listOf(item), emptyMap())
        assertEquals(1, filtered.size)
        assertFalse("No tokens must always be included", filtered[0].filteredOut)
        assertFalse("No tokens must not need review", filtered[0].needsReview)
        assertEquals("", filtered[0].signature)
    }

    @Test
    fun applyDecisions_tokenDetected_includeDecision_kept() {
        val item = FetchedMoodleItem(
            courseId = 1L,
            courseName = "DBMS",
            mappedSubjectId = "sub_1",
            mappedSubjectName = "DBMS",
            cmid = 101L,
            title = "Lab 1 (C1)",
            dueDate = 1000L,
            ancestorHeadings = emptyList()
        )

        val decisions = mapOf(
            "sub_1" to mapOf("C1" to SignatureDecision.INCLUDE)
        )

        val filtered = MoodleItemFilter.applyDecisions(listOf(item), decisions)
        assertEquals(1, filtered.size)
        assertFalse(filtered[0].filteredOut)
        assertFalse(filtered[0].needsReview)
        assertEquals("C1", filtered[0].signature)
    }

    @Test
    fun applyDecisions_tokenDetected_excludeDecision_filteredOut() {
        val item = FetchedMoodleItem(
            courseId = 1L,
            courseName = "DBMS",
            mappedSubjectId = "sub_1",
            mappedSubjectName = "DBMS",
            cmid = 101L,
            title = "Lab 1 (C2)",
            dueDate = 1000L,
            ancestorHeadings = emptyList()
        )

        val decisions = mapOf(
            "sub_1" to mapOf("C2" to SignatureDecision.EXCLUDE)
        )

        val filtered = MoodleItemFilter.applyDecisions(listOf(item), decisions)
        assertEquals(1, filtered.size)
        assertTrue(filtered[0].filteredOut)
        assertFalse(filtered[0].needsReview)
        assertEquals("C2", filtered[0].signature)
    }

    @Test
    fun applyDecisions_tokenDetected_noDecision_markedNeedsReview() {
        val item = FetchedMoodleItem(
            courseId = 1L,
            courseName = "DBMS",
            mappedSubjectId = "sub_1",
            mappedSubjectName = "DBMS",
            cmid = 101L,
            title = "Lab 1 (D3)",
            dueDate = 1000L,
            ancestorHeadings = emptyList()
        )

        val filtered = MoodleItemFilter.applyDecisions(listOf(item), emptyMap())
        assertEquals(1, filtered.size)
        assertFalse(filtered[0].filteredOut)
        assertTrue("Item with no configured codes is unresolved", MoodleItemFilter.isItemUnresolved(item, emptyList()))
        assertEquals("D3", filtered[0].signature)
    }

    // --- Part 5: Merge Tests ---

    @Test
    fun mergeEngine_insertsNewMoodleSubmission_withNotStartedStatus() = runBlocking {
        val repo = FakeSubmissionRepository()
        val engine = MoodleMergeEngine(repo)

        val item = FilteredMoodleItem(
            item = FetchedMoodleItem(
                courseId = 1L,
                courseName = "DBMS",
                mappedSubjectId = "sub_dbms",
                mappedSubjectName = "DBMS",
                cmid = 5001L,
                title = "SQL Lab 1",
                dueDate = 1775000000L,
                ancestorHeadings = listOf("Unit 1")
            ),
            filteredOut = false
        )

        val result = engine.mergeFilteredItems(listOf(item))
        assertEquals(1, result.insertedCount)
        assertEquals(0, result.updatedCount)
        assertEquals(0, result.vanishedCount)

        val allSubs = repo.getAllMoodleSubmissions()
        assertEquals(1, allSubs.size)
        val sub = allSubs[0]
        assertEquals("SQL Lab 1", sub.title)
        assertEquals(5001L, sub.moodleAssignmentId)
        assertEquals(SubmissionSource.MOODLE, sub.source)
        assertEquals(SubmissionStatus.NOT_STARTED, sub.status)
        assertFalse(sub.filteredOut)
        assertFalse(sub.moodleVanished)
    }

    @Test
    fun mergeEngine_updatesExistingMoodleSubmission_preservesUserStatus() = runBlocking {
        val existing = SubmissionEntity(
            id = "existing_sub_1",
            subjectId = "sub_dbms",
            sessionType = null,
            title = "Old Title",
            deadline = java.time.LocalDateTime.now().plusDays(2),
            status = SubmissionStatus.DONE, // User marked as DONE!
            roomOverride = null,
            source = SubmissionSource.MOODLE,
            moodleAssignmentId = 5001L,
            filteredOut = false,
            moodleVanished = false
        )

        val repo = FakeSubmissionRepository(listOf(existing))
        val engine = MoodleMergeEngine(repo)

        val updatedItem = FilteredMoodleItem(
            item = FetchedMoodleItem(
                courseId = 1L,
                courseName = "DBMS",
                mappedSubjectId = "sub_dbms",
                mappedSubjectName = "DBMS",
                cmid = 5001L,
                title = "Updated Title From Instructor",
                dueDate = 1776000000L,
                ancestorHeadings = listOf("Unit 1")
            ),
            filteredOut = true // Filter now flags as filteredOut
        )

        val result = engine.mergeFilteredItems(listOf(updatedItem))
        assertEquals(0, result.insertedCount)
        assertEquals(1, result.updatedCount)
        assertEquals(0, result.vanishedCount)

        val sub = repo.getSubmissionByMoodleId(5001L)!!
        assertEquals("Updated Title From Instructor", sub.title)
        assertEquals(SubmissionStatus.DONE, sub.status) // STATUS PRESERVED!
        assertTrue(sub.filteredOut)
        assertFalse(sub.moodleVanished)
    }

    @Test
    fun mergeEngine_itemMissingFromSync_markedVanished() = runBlocking {
        val existing = SubmissionEntity(
            id = "existing_sub_1",
            subjectId = "sub_dbms",
            sessionType = null,
            title = "Deleted Moodle Assignment",
            deadline = java.time.LocalDateTime.now().plusDays(2),
            status = SubmissionStatus.IN_PROGRESS,
            roomOverride = null,
            source = SubmissionSource.MOODLE,
            moodleAssignmentId = 5001L,
            filteredOut = false,
            moodleVanished = false
        )

        val repo = FakeSubmissionRepository(listOf(existing))
        val engine = MoodleMergeEngine(repo)

        // Sync arrives with NO items
        val result = engine.mergeFilteredItems(emptyList())
        assertEquals(0, result.insertedCount)
        assertEquals(0, result.updatedCount)
        assertEquals(1, result.vanishedCount)

        val sub = repo.getSubmissionByMoodleId(5001L)!!
        assertTrue(sub.moodleVanished) // Flagged vanished, NOT deleted!
        assertEquals(SubmissionStatus.IN_PROGRESS, sub.status) // Status still intact
    }

    @Test
    fun mergeEngine_vanishedItemReappearing_clearsVanishedFlag() = runBlocking {
        val existing = SubmissionEntity(
            id = "existing_sub_1",
            subjectId = "sub_dbms",
            sessionType = null,
            title = "Assignment",
            deadline = java.time.LocalDateTime.now().plusDays(2),
            status = SubmissionStatus.NOT_STARTED,
            roomOverride = null,
            source = SubmissionSource.MOODLE,
            moodleAssignmentId = 5001L,
            filteredOut = false,
            moodleVanished = true // previously vanished
        )

        val repo = FakeSubmissionRepository(listOf(existing))
        val engine = MoodleMergeEngine(repo)

        val item = FilteredMoodleItem(
            item = FetchedMoodleItem(
                courseId = 1L,
                courseName = "DBMS",
                mappedSubjectId = "sub_dbms",
                mappedSubjectName = "DBMS",
                cmid = 5001L,
                title = "Assignment",
                dueDate = 1775000000L,
                ancestorHeadings = listOf("Unit 1")
            ),
            filteredOut = false
        )

        engine.mergeFilteredItems(listOf(item))

        val sub = repo.getSubmissionByMoodleId(5001L)!!
        assertFalse(sub.moodleVanished) // Vanished flag cleared!
    }

    @Test
    fun mergeEngine_manualSubmissions_neverModifiedOrVanished() = runBlocking {
        val manualSub = SubmissionEntity(
            id = "manual_1",
            subjectId = "sub_dbms",
            sessionType = null,
            title = "Personal Homework",
            deadline = java.time.LocalDateTime.now().plusDays(3),
            status = SubmissionStatus.IN_PROGRESS,
            roomOverride = null,
            source = SubmissionSource.MANUAL,
            moodleAssignmentId = null,
            filteredOut = false,
            moodleVanished = false
        )

        val repo = FakeSubmissionRepository(listOf(manualSub))
        val engine = MoodleMergeEngine(repo)

        // Sync arrives with some Moodle item
        val moodleItem = FilteredMoodleItem(
            item = FetchedMoodleItem(
                courseId = 1L,
                courseName = "DBMS",
                mappedSubjectId = "sub_dbms",
                mappedSubjectName = "DBMS",
                cmid = 9999L,
                title = "Moodle Assignment",
                dueDate = 1775000000L
            ),
            filteredOut = false
        )

        engine.mergeFilteredItems(listOf(moodleItem))

        val manualAfterSync = repo.getSubmissionById("manual_1")!!
        assertEquals("Personal Homework", manualAfterSync.title)
        assertEquals(SubmissionStatus.IN_PROGRESS, manualAfterSync.status)
        assertEquals(SubmissionSource.MANUAL, manualAfterSync.source)
        assertFalse(manualAfterSync.moodleVanished)
    }

    @Test
    fun mergeEngine_duplicateIncomingItems_insertsOnlyOnce() = runBlocking {
        val repo = FakeSubmissionRepository()
        val engine = MoodleMergeEngine(repo)

        val item1 = FilteredMoodleItem(
            item = FetchedMoodleItem(
                courseId = 1L,
                courseName = "DBMS",
                mappedSubjectId = "sub_dbms",
                mappedSubjectName = "DBMS",
                cmid = 5555L,
                title = "Duplicate Item",
                dueDate = 1775000000L
            ),
            filteredOut = false
        )
        val item2 = item1.copy() // Duplicate item with same cmid!

        val result = engine.mergeFilteredItems(listOf(item1, item2))
        assertEquals(1, result.insertedCount)
        assertEquals(1, result.totalProcessed)

        val allSubs = repo.getAllMoodleSubmissions()
        assertEquals(1, allSubs.size)
        assertEquals(5555L, allSubs[0].moodleAssignmentId)
    }

    @Test
    fun mergeEngine_existingDuplicatesInDatabase_purgesRedundantRows() = runBlocking {
        val dup1 = SubmissionEntity(
            id = "dup_1",
            subjectId = "sub_dbms",
            sessionType = null,
            title = "Existing Duplicate 1",
            deadline = java.time.LocalDateTime.now().plusDays(2),
            status = SubmissionStatus.NOT_STARTED,
            source = SubmissionSource.MOODLE,
            moodleAssignmentId = 6666L
        )
        val dup2 = SubmissionEntity(
            id = "dup_2",
            subjectId = "sub_dbms",
            sessionType = null,
            title = "Existing Duplicate 2",
            deadline = java.time.LocalDateTime.now().plusDays(2),
            status = SubmissionStatus.DONE, // Has DONE status!
            source = SubmissionSource.MOODLE,
            moodleAssignmentId = 6666L
        )

        val repo = FakeSubmissionRepository(listOf(dup1, dup2))
        val engine = MoodleMergeEngine(repo)

        val incomingItem = FilteredMoodleItem(
            item = FetchedMoodleItem(
                courseId = 1L,
                courseName = "DBMS",
                mappedSubjectId = "sub_dbms",
                mappedSubjectName = "DBMS",
                cmid = 6666L,
                title = "Updated Title",
                dueDate = 1776000000L
            ),
            filteredOut = false
        )

        val result = engine.mergeFilteredItems(listOf(incomingItem))
        assertEquals(0, result.insertedCount)
        assertEquals(1, result.updatedCount)

        val remainingSubs = repo.getAllMoodleSubmissions().filter { it.moodleAssignmentId == 6666L }
        assertEquals(1, remainingSubs.size) // Redundant row purged!
        assertEquals("dup_2", remainingSubs[0].id) // Kept the completed one!
    }

    @Test
    fun tokenScanner_extractsRecurringTokens_filtersOutSingleOccurrencesAndStopWords() {
        val items = listOf(
            FetchedMoodleItem(
                courseId = 1L,
                courseName = "DBMS",
                mappedSubjectId = "sub_dbms",
                mappedSubjectName = "DBMS",
                cmid = 101L,
                title = "Lab 1 (C1)",
                dueDate = null,
                ancestorHeadings = listOf("Batch-A")
            ),
            FetchedMoodleItem(
                courseId = 1L,
                courseName = "DBMS",
                mappedSubjectId = "sub_dbms",
                mappedSubjectName = "DBMS",
                cmid = 102L,
                title = "Lab 2 [C1]",
                dueDate = null,
                ancestorHeadings = listOf("Batch-A", "PDF")
            ),
            FetchedMoodleItem(
                courseId = 1L,
                courseName = "DBMS",
                mappedSubjectId = "sub_dbms",
                mappedSubjectName = "DBMS",
                cmid = 103L,
                title = "Assignment 1 (UNIQUE_TOKEN_ONCE)",
                dueDate = null,
                ancestorHeadings = listOf("Submission")
            )
        )

        val tokens = MoodleTokenScanner.extractTokens(items)

        // "C1" and "Batch-A" appear in >=2 items, so they must be detected
        assertTrue("Expected C1 to be detected", tokens.contains("C1"))
        assertTrue("Expected Batch-A to be detected", tokens.contains("Batch-A"))

        // Single occurrences and stop words should NOT be in the result
        assertFalse("Unique single occurrence should not be detected", tokens.contains("UNIQUE_TOKEN_ONCE"))
        assertFalse("Stop word PDF should not be detected", tokens.contains("PDF"))
        assertFalse("Stop word Submission should not be detected", tokens.contains("Submission"))
    }

    @Test
    fun demappingCourse_deletesAssociatedMoodleSubmissionsOnly() = runBlocking {
        val subCourse101 = SubmissionEntity(
            id = "moodle_101_1",
            subjectId = "sub_dbms",
            sessionType = null,
            title = "DBMS Assignment 1",
            deadline = java.time.LocalDateTime.now().plusDays(2),
            status = SubmissionStatus.NOT_STARTED,
            roomOverride = null,
            source = SubmissionSource.MOODLE,
            moodleAssignmentId = 5001L,
            moodleCourseId = 101L,
            filteredOut = false,
            moodleVanished = false
        )
        val subCourse102 = SubmissionEntity(
            id = "moodle_102_1",
            subjectId = "sub_dbms",
            sessionType = null,
            title = "DBMS Lab Task 1",
            deadline = java.time.LocalDateTime.now().plusDays(4),
            status = SubmissionStatus.NOT_STARTED,
            roomOverride = null,
            source = SubmissionSource.MOODLE,
            moodleAssignmentId = 5002L,
            moodleCourseId = 102L,
            filteredOut = false,
            moodleVanished = false
        )
        val manualSub = SubmissionEntity(
            id = "manual_1",
            subjectId = "sub_dbms",
            sessionType = null,
            title = "Manual DBMS Notes",
            deadline = java.time.LocalDateTime.now().plusDays(5),
            status = SubmissionStatus.IN_PROGRESS,
            roomOverride = null,
            source = SubmissionSource.MANUAL,
            moodleAssignmentId = null,
            moodleCourseId = null,
            filteredOut = false,
            moodleVanished = false
        )

        val repo = FakeSubmissionRepository(listOf(subCourse101, subCourse102, manualSub))

        // De-map course 101
        repo.deleteMoodleSubmissionsByCourseId(101L)

        // Submissions for course 101 should be gone
        assertNull(repo.getSubmissionById("moodle_101_1"))

        // Submissions for other course 102 and manual submissions should remain intact
        assertNotNull(repo.getSubmissionById("moodle_102_1"))
        assertNotNull(repo.getSubmissionById("manual_1"))
    }

    @Test
    fun calibrate_deduplicatesItemsToUniqueTokenSets() {
        val rawItems = listOf(
            FetchedMoodleItem(1L, "DS", "sub_ds", "DS", 101L, "E2-C1-T2 Lab 1", null),
            FetchedMoodleItem(1L, "DS", "sub_ds", "DS", 102L, "E2-C1-T2 Lab 2", null),
            FetchedMoodleItem(1L, "DS", "sub_ds", "DS", 103L, "E2-C1-T2 Lab 3", null),
            FetchedMoodleItem(1L, "DS", "sub_ds", "DS", 201L, "E2-C2-T2 Lab 1", null),
            FetchedMoodleItem(1L, "DS", "sub_ds", "DS", 202L, "E2-C2-T2 Lab 2", null),
            FetchedMoodleItem(1L, "DS", "sub_ds", "DS", 301L, "E1-D3-T2 Lab 1", null),
            FetchedMoodleItem(1L, "DS", "sub_ds", "DS", 401L, "General Course Overview", null) // no tokens
        )

        val testItems = listOf(
            FetchedMoodleItem(1L, "Data Structures", "sub_ds", "Data Structures", 1001L, "E2-C1-T2(FM) — Application 2", null),
            FetchedMoodleItem(1L, "Data Structures", "sub_ds", "Data Structures", 1002L, "E2-C1-T2(FM) — Application 3", null),
            FetchedMoodleItem(1L, "Data Structures", "sub_ds", "Data Structures", 1003L, "E2-C2-T2(FM) — Application 2", null),
            FetchedMoodleItem(1L, "Data Structures", "sub_ds", "Data Structures", 1004L, "General Tree Theory Assignment", null)
        )

        val decisions = mapOf(
            "sub_ds" to mapOf(
                "C1,E2,FM,T2" to SignatureDecision.INCLUDE,
                "C2,E2,FM,T2" to SignatureDecision.EXCLUDE
            )
        )

        val filtered = MoodleItemFilter.applyDecisions(testItems, decisions)

        // Items with C1 signature resolved as INCLUDE
        val c1Items = filtered.filter { it.signature == "C1,E2,FM,T2" }
        assertEquals(2, c1Items.size)
        c1Items.forEach {
            assertFalse("C1 item must not be filtered out", it.filteredOut)
            assertFalse("C1 item must not need review", it.needsReview)
        }

        // Item with C2 signature resolved as EXCLUDE
        val c2Item = filtered.find { it.signature == "C2,E2,FM,T2" }!!
        assertTrue("C2 item must be filtered out", c2Item.filteredOut)
        assertFalse("C2 item must not need review", c2Item.needsReview)

        // Course-wide item with no tokens is always included
        val generalItem = filtered.find { it.item.cmid == 1004L }!!
        assertFalse("General item must be included", generalItem.filteredOut)
        assertFalse("General item must not need review", generalItem.needsReview)
    }

    @Test
    fun syncPreview_expandedSubjectIds_togglesIndependently() {
        var expandedIds = setOf("sub_math") // seeded on initial load

        fun toggle(subjectId: String) {
            expandedIds = if (expandedIds.contains(subjectId)) {
                expandedIds - subjectId
            } else {
                expandedIds + subjectId
            }
        }

        // Initially only math is expanded
        assertTrue(expandedIds.contains("sub_math"))
        assertFalse(expandedIds.contains("sub_phys"))

        // Expand phys -> both expanded
        toggle("sub_phys")
        assertTrue(expandedIds.contains("sub_math"))
        assertTrue(expandedIds.contains("sub_phys"))

        // Collapse math -> only phys expanded (math does NOT snap back open!)
        toggle("sub_math")
        assertFalse(expandedIds.contains("sub_math"))
        assertTrue(expandedIds.contains("sub_phys"))

        // Collapse phys -> both collapsed, empty set is valid and does NOT force fallback
        toggle("sub_phys")
        assertTrue(expandedIds.isEmpty())
    }

    open class FakeTokenSignatureRepository(
        private val decisions: MutableList<TokenSignatureDecision> = mutableListOf()
    ) : TokenSignatureRepository {
        override fun getDecisionsForSubject(subjectId: String): Flow<List<TokenSignatureDecision>> =
            flowOf(decisions.filter { it.subjectId == subjectId })
        override fun getAllDecisions(): Flow<List<TokenSignatureDecision>> = flowOf(decisions)
        override suspend fun getAllDecisionsSync(): List<TokenSignatureDecision> = decisions
        override suspend fun getDecision(subjectId: String, signature: String): TokenSignatureDecision? =
            decisions.find { it.subjectId == subjectId && it.signature == signature }
        override suspend fun saveDecision(subjectId: String, signature: String, decision: SignatureDecision): Long {
            val idx = decisions.indexOfFirst { it.subjectId == subjectId && it.signature == signature }
            return if (idx != -1) {
                decisions[idx] = decisions[idx].copy(decision = decision)
                decisions[idx].id
            } else {
                val newId = (decisions.size + 1).toLong()
                decisions.add(TokenSignatureDecision(id = newId, subjectId = subjectId, signature = signature, decision = decision))
                newId
            }
        }
        override suspend fun deleteDecision(subjectId: String, signature: String): Int {
            val before = decisions.size
            decisions.removeAll { it.subjectId == subjectId && it.signature == signature }
            return before - decisions.size
        }
        override suspend fun deleteDecisionById(id: Long): Int {
            val before = decisions.size
            decisions.removeAll { it.id == id }
            return before - decisions.size
        }
        override suspend fun deleteAllForSubject(subjectId: String): Int {
            val before = decisions.size
            decisions.removeAll { it.subjectId == subjectId }
            return before - decisions.size
        }
    }

    open class FakeSubmissionRepository(
        initialSubmissions: List<SubmissionEntity> = emptyList()
    ) : SubmissionRepository {
        val submissions = initialSubmissions.toMutableList()
        override fun getAllSubmissions(): Flow<List<SubmissionEntity>> = flowOf(submissions)
        override suspend fun getSubmissionById(id: String): SubmissionEntity? = submissions.find { it.id == id }
        override suspend fun insertSubmission(submission: SubmissionEntity) { submissions.add(submission) }
        override suspend fun insertSubmissions(newSubmissions: List<SubmissionEntity>) { submissions.addAll(newSubmissions) }
        override suspend fun updateSubmission(submission: SubmissionEntity) {
            val idx = submissions.indexOfFirst { it.id == submission.id }
            if (idx != -1) submissions[idx] = submission
        }
        override suspend fun deleteSubmission(submission: SubmissionEntity) { submissions.removeAll { it.id == submission.id } }
        override suspend fun getSubmissionByMoodleId(cmid: Long): SubmissionEntity? =
            submissions.find { it.moodleAssignmentId == cmid && it.source == SubmissionSource.MOODLE }
        override suspend fun getAllMoodleSubmissions(): List<SubmissionEntity> =
            submissions.filter { it.source == SubmissionSource.MOODLE }
        override suspend fun updateMoodleSubmission(id: String, title: String, deadline: java.time.LocalDateTime, courseId: Long, filteredOut: Boolean, needsReview: Boolean) {
            val idx = submissions.indexOfFirst { it.id == id }
            if (idx != -1) {
                submissions[idx] = submissions[idx].copy(
                    title = title,
                    deadline = deadline,
                    moodleCourseId = courseId,
                    filteredOut = filteredOut,
                    needsReview = needsReview,
                    moodleVanished = false
                )
            }
        }
        override suspend fun updateFilteredOut(id: String, filteredOut: Boolean) {
            val idx = submissions.indexOfFirst { it.id == id }
            if (idx != -1) submissions[idx] = submissions[idx].copy(filteredOut = filteredOut)
        }
        override suspend fun markMoodleVanished(id: String) {
            val idx = submissions.indexOfFirst { it.id == id }
            if (idx != -1) submissions[idx] = submissions[idx].copy(moodleVanished = true)
        }
        override suspend fun deleteMoodleSubmissionsByCourseId(courseId: Long): Int {
            val count = submissions.count { it.source == SubmissionSource.MOODLE && it.moodleCourseId == courseId }
            submissions.removeAll { it.source == SubmissionSource.MOODLE && it.moodleCourseId == courseId }
            return count
        }
        override suspend fun deleteMoodleSubmissionsBySubjectId(subjectId: String): Int {
            val count = submissions.count { it.source == SubmissionSource.MOODLE && it.subjectId == subjectId }
            submissions.removeAll { it.source == SubmissionSource.MOODLE && it.subjectId == subjectId }
            return count
        }
    }

    open class FakeSubjectRepository(
        private val subjects: List<SubjectEntity> = emptyList()
    ) : SubjectRepository {
        override fun getAllSubjects(): Flow<List<SubjectEntity>> = flowOf(subjects)
        override suspend fun getSubjectById(id: String): SubjectEntity? = subjects.find { it.id == id }
        override suspend fun insertSubject(subject: SubjectEntity) {}
        override suspend fun updateSubject(subject: SubjectEntity) {}
        override suspend fun deleteSubject(subject: SubjectEntity) {}
        override suspend fun deleteSubjectById(id: String) {}
    }

    class FakeMoodleCourseContentCacheDao : MoodleCourseContentCacheDao {
        private val cache = mutableMapOf<Long, MoodleCourseContentCacheEntity>()

        override suspend fun getCachedContent(courseId: Long): MoodleCourseContentCacheEntity? {
            return cache[courseId]
        }

        override suspend fun insertOrUpdate(entity: MoodleCourseContentCacheEntity) {
            cache[entity.courseId] = entity
        }

        override suspend fun deleteByCourseId(courseId: Long) {
            cache.remove(courseId)
        }

        override suspend fun clearAll() {
            cache.clear()
        }
    }
}
