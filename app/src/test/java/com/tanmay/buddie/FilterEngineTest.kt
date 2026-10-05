package com.tanmay.buddie

import com.tanmay.buddie.data.local.dao.MoodleCourseContentCacheDao
import com.tanmay.buddie.data.local.entity.FilterCode
import com.tanmay.buddie.data.local.entity.MoodleCourseContentCacheEntity
import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity
import com.tanmay.buddie.data.local.entity.SignatureDecision
import com.tanmay.buddie.data.local.entity.SubjectEntity
import com.tanmay.buddie.data.local.entity.SubmissionEntity
import com.tanmay.buddie.data.local.entity.SubmissionSource
import com.tanmay.buddie.data.local.entity.SubmissionStatus
import com.tanmay.buddie.data.local.entity.TokenSignatureDecision
import com.tanmay.buddie.data.remote.moodle.MoodleAssignment
import com.tanmay.buddie.data.remote.moodle.MoodleApiClient
import com.tanmay.buddie.data.remote.moodle.MoodleModule
import com.tanmay.buddie.data.remote.moodle.MoodleSection
import com.tanmay.buddie.domain.model.FetchedMoodleItem
import com.tanmay.buddie.domain.repository.FilterCodeRepository
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import com.tanmay.buddie.domain.repository.TokenSignatureRepository
import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine
import com.tanmay.buddie.domain.sync.MoodleItemFilter
import com.tanmay.buddie.domain.sync.MoodleMergeEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class FilterEngineTest {

    // =========================================================================
    // 1. Tokenizer Tests Against Varied Real Formats
    // =========================================================================

    @Test
    fun tokenizer_hyphenatedFormat() {
        val tokens = MoodleItemFilter.tokenize("E4-T2-D4(SB)")
        assertEquals(setOf("E4", "T2", "D4", "SB"), tokens)
    }

    @Test
    fun tokenizer_concatenatedFormats() {
        val tokens1 = MoodleItemFilter.tokenize("Exp2C3T1")
        assertEquals(setOf("EXP2", "C3", "T1"), tokens1)

        val tokens2 = MoodleItemFilter.tokenize("E4C3T1")
        assertEquals(setOf("E4", "C3", "T1"), tokens2)
    }

    @Test
    fun tokenizer_reorderedFormats_produceIdenticalTokenSets() {
        val tokensOriginal = MoodleItemFilter.tokenize("E4-T2-D4(SB)")
        val tokensReordered = MoodleItemFilter.tokenize("E4-D4-T2(SB)")

        assertEquals(setOf("E4", "T2", "D4", "SB"), tokensOriginal)
        assertEquals(tokensOriginal, tokensReordered)
    }

    @Test
    fun tokenizer_dsExpDivCStyle() {
        val tokens = MoodleItemFilter.tokenize("DS-Exp1-DivC-C3-T1")
        assertEquals(setOf("DS", "EXP1", "DIV", "C3", "T1"), tokens)
    }

    @Test
    fun tokenizer_inconsistentSpacingFormats() {
        val tokensSpaced = MoodleItemFilter.tokenize("E3-T2 - B3 (AN)")
        assertEquals(setOf("E3", "T2", "B3", "AN"), tokensSpaced)

        val tokensTight = MoodleItemFilter.tokenize("E3-T1-B2(AM)")
        assertEquals(setOf("E3", "T1", "B2", "AM"), tokensTight)
    }

    @Test
    fun tokenizer_appliesToAncestorHeadingsAndTitleBlob() {
        val item = FetchedMoodleItem(
            courseId = 101L,
            courseName = "DSA",
            mappedSubjectId = "sub_dsa",
            mappedSubjectName = "Data Structures",
            cmid = 501L,
            title = "Exp2C3T1 Sorting Lab",
            dueDate = null,
            ancestorHeadings = listOf("Unit 1: Fundamentals", "DS-Exp1-DivC-C3-T1")
        )

        val tokens = MoodleItemFilter.tokenizeItem(item)
        // Ancestors contain UNIT, FUNDAMENTALS, DS, EXP1, DIV, C3, T1; title contains EXP2, C3, T1, SORTING, LAB
        assertTrue(tokens.contains("EXP2"))
        assertTrue(tokens.contains("C3"))
        assertTrue(tokens.contains("T1"))
        assertTrue(tokens.contains("EXP1"))
        assertTrue(tokens.contains("DIV"))
        assertTrue(tokens.contains("SORTING"))
        assertTrue(tokens.contains("LAB"))
    }

    // =========================================================================
    // 2. Allow-List Mode: Single isMine=true Code Filtering
    // =========================================================================

    @Test
    fun allowListMode_configuringOnlyOneIsMineCode_filtersSiblingBatchItems_keepsCourseWide() {
        // User configures only "D3" as isMine = true
        val configuredCodes = listOf(
            FilterCode(id = 1L, subjectId = "sub_os", code = "D3", isMine = true)
        )

        val itemMine = FetchedMoodleItem(
            courseId = 1L,
            courseName = "Operating Systems",
            mappedSubjectId = "sub_os",
            mappedSubjectName = "OS",
            cmid = 101L,
            title = "Lab 1 (D3)",
            dueDate = null
        )

        val itemSiblingC1 = FetchedMoodleItem(
            courseId = 1L,
            courseName = "Operating Systems",
            mappedSubjectId = "sub_os",
            mappedSubjectName = "OS",
            cmid = 102L,
            title = "Lab 1 (C1)",
            dueDate = null
        )

        val itemSiblingC2 = FetchedMoodleItem(
            courseId = 1L,
            courseName = "Operating Systems",
            mappedSubjectId = "sub_os",
            mappedSubjectName = "OS",
            cmid = 103L,
            title = "Lab 1 (C2)",
            dueDate = null
        )

        val itemConcatenated = FetchedMoodleItem(
            courseId = 1L,
            courseName = "Operating Systems",
            mappedSubjectId = "sub_os",
            mappedSubjectName = "OS",
            cmid = 104L,
            title = "E4C3T1 Assignment",
            dueDate = null
        )

        val itemDivC = FetchedMoodleItem(
            courseId = 1L,
            courseName = "Operating Systems",
            mappedSubjectId = "sub_os",
            mappedSubjectName = "OS",
            cmid = 105L,
            title = "DS-Exp1-DivC-C3-T1",
            dueDate = null
        )

        val itemCourseWide = FetchedMoodleItem(
            courseId = 1L,
            courseName = "Operating Systems",
            mappedSubjectId = "sub_os",
            mappedSubjectName = "OS",
            cmid = 106L,
            title = "Course Syllabus and Semester Overview",
            dueDate = null
        )

        val itemLongWithBatchCode = FetchedMoodleItem(
            courseId = 1L,
            courseName = "Operating Systems",
            mappedSubjectId = "sub_os",
            mappedSubjectName = "OS",
            cmid = 107L,
            title = "A1-Application-4-Supermarket Point-of-Sale (POS) Terminal-AVN",
            dueDate = null
        )

        val allItems = listOf(
            itemMine,
            itemSiblingC1,
            itemSiblingC2,
            itemConcatenated,
            itemDivC,
            itemCourseWide,
            itemLongWithBatchCode
        )

        val filtered = MoodleItemFilter.applyFilterCodes(
            items = allItems,
            filterCodesBySubjectId = mapOf("sub_os" to configuredCodes)
        )

        // 1. D3 item matches isMine=true -> KEPT
        val resMine = filtered.find { it.item.cmid == 101L }!!
        assertFalse("User's own batch code D3 must be kept", resMine.filteredOut)

        // 2. Sibling codes without D3 -> FILTERED OUT
        val resC1 = filtered.find { it.item.cmid == 102L }!!
        assertTrue("Sibling batch code C1 must be filtered out without enumerating it", resC1.filteredOut)

        val resC2 = filtered.find { it.item.cmid == 103L }!!
        assertTrue("Sibling batch code C2 must be filtered out without enumerating it", resC2.filteredOut)

        val resConcat = filtered.find { it.item.cmid == 104L }!!
        assertTrue("Concatenated E4C3T1 must be filtered out", resConcat.filteredOut)

        val resDivC = filtered.find { it.item.cmid == 105L }!!
        assertTrue("DivC-C3-T1 must be filtered out", resDivC.filteredOut)

        // 3. Course-wide item with no batch tokens -> KEPT
        val resCourseWide = filtered.find { it.item.cmid == 106L }!!
        assertFalse("Course-wide item must be kept", resCourseWide.filteredOut)

        // 4. Long descriptive title with batch token A1 -> has batch-shaped token A1, not D3 -> FILTERED OUT
        val resLong = filtered.find { it.item.cmid == 107L }!!
        assertTrue("Item with A1 batch token must not leak as course-wide", resLong.filteredOut)
    }

    // =========================================================================
    // 3. Detection Mode: Zero isMine Codes Configured
    // =========================================================================

    @Test
    fun detectionMode_zeroIsMineCodes_keepsEverything() {
        val items = listOf(
            FetchedMoodleItem(1L, "Math", "sub_math", "Math", 201L, "Calculus Lab 1 (C1)", null),
            FetchedMoodleItem(1L, "Math", "sub_math", "Math", 202L, "Calculus Lab 1 (C2)", null),
            FetchedMoodleItem(1L, "Math", "sub_math", "Math", 203L, "Calculus Lab 1 (D3)", null),
            FetchedMoodleItem(1L, "Math", "sub_math", "Math", 204L, "General Math Overview", null)
        )

        // Subject has zero isMine=true codes configured
        val filtered = MoodleItemFilter.applyFilterCodes(
            items = items,
            filterCodesBySubjectId = emptyMap()
        )

        assertEquals(4, filtered.size)
        filtered.forEach {
            assertFalse("In detection mode with zero isMine codes, nothing is hidden", it.filteredOut)
        }
    }

    // =========================================================================
    // 4. Calibration Flow Proposes Correct Codes
    // =========================================================================

    @Test
    fun calibrationFlow_proposesCorrectCodesFromSmallLabeledYesNoSample() {
        // Sample labeled items for a student in batch C1, tutorial T2
        val item1Tokens = setOf("E2", "C1", "T2") // YES (mine)
        val item2Tokens = setOf("E2", "C1", "T2") // YES (mine)
        val item3Tokens = setOf("E2", "C2", "T2") // NO (not mine)
        val item4Tokens = setOf("E1", "D3", "T2") // NO (not mine)

        val labeledSample = listOf(
            item1Tokens to true,
            item2Tokens to true,
            item3Tokens to false,
            item4Tokens to false
        )

        val proposal = MoodleItemFilter.computeCalibrationProposals(labeledSample)

        // C1 is in every YES item and in NO "no" item -> propose as isMine = true
        assertEquals(listOf("C1"), proposal.proposedMineCodes)

        // C2, D3, E1 are in "no" items and absent from ALL "yes" items -> propose as isMine = false
        assertEquals(listOf("C2", "D3", "E1"), proposal.proposedNotMineCodes)
    }

    // =========================================================================
    // 5. Hide / Unhide Round-Trip With Zero Network Calls
    // =========================================================================

    @Test
    fun hideUnhideRoundTrip_updatesFilteredOutWithZeroNetworkCalls() = runBlocking {
        var networkCallsCount = 0

        val fakeApiClient = object : MoodleApiClient(
            okhttp3.OkHttpClient(),
            MoodleConnectionTest.MoodleTokenStorageFake(),
            MoodleConnectionTest.UserPreferencesFake()
        ) {
            override suspend fun getAssignments(courseIds: List<Long>): Result<List<MoodleAssignment>> {
                networkCallsCount++
                return Result.success(emptyList())
            }
            override suspend fun getCourseContents(courseId: Long): Result<List<MoodleSection>> {
                networkCallsCount++
                return Result.success(emptyList())
            }
        }

        val subjectRepo = FakeSubjectRepo(listOf(SubjectEntity(id = "sub_ds", name = "Data Structures", color = "#FF0000")))
        val mappingRepo = FakeMappingRepo(listOf(MoodleCourseMappingEntity(moodleCourseId = 100L, moodleCourseName = "DSA", subjectId = "sub_ds")))
        val subRepo = FakeSubRepo()
        val filterRepo = FakeFilterCodeRepo()
        val cacheDao = FakeCacheDao()
        val mergeEngine = MoodleMergeEngine(subRepo)

        val syncEngine = MoodleContentSyncEngine(
            moodleApiClient = fakeApiClient,
            courseMappingRepository = mappingRepo,
            subjectRepository = subjectRepo,
            contentCacheDao = cacheDao,
            tokenSignatureRepository = filterRepo,
            mergeEngine = mergeEngine
        )

        // Seed sync engine with cached preview item
        val initialItem = FetchedMoodleItem(
            courseId = 100L,
            courseName = "DSA",
            mappedSubjectId = "sub_ds",
            mappedSubjectName = "Data Structures",
            cmid = 9001L,
            title = "Binary Tree Lab (C2)",
            dueDate = 1775000000L
        )

        // Set the cached items directly via reflection / initial state
        val field = MoodleContentSyncEngine::class.java.getDeclaredField("lastFetchedItems")
        field.isAccessible = true
        field.set(syncEngine, listOf(initialItem))

        // Step 1: Initial merge without any filter codes
        val initResult = syncEngine.reapplyFiltersAndMerge()
        assertTrue(initResult.isSuccess)
        assertEquals(0, networkCallsCount) // ZERO network calls!

        val subBefore = subRepo.getSubmissionByMoodleId(9001L)
        assertNotNull(subBefore)
        assertFalse("Initially item is not filtered out", subBefore!!.filteredOut)

        // Step 2: User clicks "not mine, hide this" -> saves C2 as isMine = false
        filterRepo.saveFilterCode(subjectId = "sub_ds", code = "C2", isMine = false)
        val hideResult = syncEngine.reapplyFiltersAndMerge()
        assertTrue(hideResult.isSuccess)
        assertEquals(0, networkCallsCount) // ZERO network calls!

        val subAfterHide = subRepo.getSubmissionByMoodleId(9001L)
        assertNotNull(subAfterHide)
        assertTrue("After hide action, item is filteredOut = true", subAfterHide!!.filteredOut)

        // Step 3: User unhides -> removes C2 blocking code
        filterRepo.deleteFilterCode(subjectId = "sub_ds", code = "C2")
        val unhideResult = syncEngine.reapplyFiltersAndMerge()
        assertTrue(unhideResult.isSuccess)
        assertEquals(0, networkCallsCount) // ZERO network calls!

        val subAfterUnhide = subRepo.getSubmissionByMoodleId(9001L)
        assertNotNull(subAfterUnhide)
        assertFalse("After unhide action, item is filteredOut = false", subAfterUnhide!!.filteredOut)
    }

    // =========================================================================
    // 6. Retargeted Warning Badge
    // =========================================================================

    @Test
    fun warningBadge_retargetedToUnresolvedItems() {
        val item1 = FetchedMoodleItem(1L, "Algo", "sub_algo", "Algo", 1001L, "Lab 1 (C1)", null)
        val item2 = FetchedMoodleItem(1L, "Algo", "sub_algo", "Algo", 1002L, "Lab 2 (D3)", null)
        val itemCourseWide = FetchedMoodleItem(1L, "Algo", "sub_algo", "Algo", 1003L, "General Final Project Guide", null)

        val items = listOf(item1, item2, itemCourseWide)

        // Case A: User only configured C1 (isMine = true). D3 has no matching code!
        val codesWithOnlyC1 = listOf(FilterCode(1L, "sub_algo", "C1", isMine = true))
        val unresolvedCountA = MoodleItemFilter.countUnresolvedItems(items, codesWithOnlyC1)
        assertEquals(1, unresolvedCountA) // D3 is unresolved, triggers badge!
        assertTrue(MoodleItemFilter.isItemUnresolved(item2, codesWithOnlyC1))
        assertFalse(MoodleItemFilter.isItemUnresolved(item1, codesWithOnlyC1))
        assertFalse(MoodleItemFilter.isItemUnresolved(itemCourseWide, codesWithOnlyC1)) // Course-wide doesn't need code

        // Case B: User also configured D3 (either isMine=true or isMine=false)
        val codesWithC1AndD3 = listOf(
            FilterCode(1L, "sub_algo", "C1", isMine = true),
            FilterCode(2L, "sub_algo", "D3", isMine = false)
        )
        val unresolvedCountB = MoodleItemFilter.countUnresolvedItems(items, codesWithC1AndD3)
        assertEquals(0, unresolvedCountB) // Both resolved -> no warning badge!
    }

    // =========================================================================
    // Helper Test Fakes
    // =========================================================================

    class FakeFilterCodeRepo : FilterCodeRepository, TokenSignatureRepository {
        val codes = mutableListOf<FilterCode>()

        override fun getFilterCodesForSubject(subjectId: String): Flow<List<FilterCode>> =
            flowOf(codes.filter { it.subjectId == subjectId })

        override fun getAllFilterCodes(): Flow<List<FilterCode>> = flowOf(codes)

        override suspend fun getAllFilterCodesSync(): List<FilterCode> = codes

        override suspend fun getFilterCode(subjectId: String, code: String): FilterCode? =
            codes.find { it.subjectId == subjectId && it.code.equals(code, ignoreCase = true) }

        override suspend fun saveFilterCode(subjectId: String, code: String, isMine: Boolean): Long {
            val upper = code.trim().uppercase()
            val idx = codes.indexOfFirst { it.subjectId == subjectId && it.code == upper }
            return if (idx != -1) {
                codes[idx] = codes[idx].copy(isMine = isMine)
                codes[idx].id
            } else {
                val newId = (codes.size + 1).toLong()
                codes.add(FilterCode(id = newId, subjectId = subjectId, code = upper, isMine = isMine))
                newId
            }
        }

        override suspend fun deleteFilterCode(subjectId: String, code: String): Int {
            val upper = code.trim().uppercase()
            val before = codes.size
            codes.removeAll { it.subjectId == subjectId && it.code == upper }
            return before - codes.size
        }

        override suspend fun deleteFilterCodeById(id: Long): Int {
            val before = codes.size
            codes.removeAll { it.id == id }
            return before - codes.size
        }

        override suspend fun deleteAllForSubject(subjectId: String): Int {
            val before = codes.size
            codes.removeAll { it.subjectId == subjectId }
            return before - codes.size
        }

        // TokenSignatureRepository implementation
        override fun getDecisionsForSubject(subjectId: String): Flow<List<TokenSignatureDecision>> =
            flowOf(codes.filter { it.subjectId == subjectId }.map { it.toTokenSignatureDecision() })

        override fun getAllDecisions(): Flow<List<TokenSignatureDecision>> =
            flowOf(codes.map { it.toTokenSignatureDecision() })

        override suspend fun getAllDecisionsSync(): List<TokenSignatureDecision> =
            codes.map { it.toTokenSignatureDecision() }

        override suspend fun getDecision(subjectId: String, signature: String): TokenSignatureDecision? =
            getFilterCode(subjectId, signature)?.toTokenSignatureDecision()

        override suspend fun saveDecision(subjectId: String, signature: String, decision: SignatureDecision): Long =
            saveFilterCode(subjectId, signature, decision == SignatureDecision.INCLUDE)

        override suspend fun deleteDecision(subjectId: String, signature: String): Int =
            deleteFilterCode(subjectId, signature)

        override suspend fun deleteDecisionById(id: Long): Int =
            deleteFilterCodeById(id)

        private fun FilterCode.toTokenSignatureDecision(): TokenSignatureDecision =
            TokenSignatureDecision(id, subjectId, code, if (isMine) SignatureDecision.INCLUDE else SignatureDecision.EXCLUDE)
    }

    class FakeSubRepo : SubmissionRepository {
        val subs = mutableListOf<SubmissionEntity>()

        override fun getAllSubmissions(): Flow<List<SubmissionEntity>> = flowOf(subs)
        override suspend fun getSubmissionById(id: String): SubmissionEntity? = subs.find { it.id == id }
        override suspend fun insertSubmission(submission: SubmissionEntity) { subs.add(submission) }
        override suspend fun insertSubmissions(submissions: List<SubmissionEntity>) { subs.addAll(submissions) }
        override suspend fun updateSubmission(submission: SubmissionEntity) {
            val idx = subs.indexOfFirst { it.id == submission.id }
            if (idx != -1) subs[idx] = submission
        }
        override suspend fun deleteSubmission(submission: SubmissionEntity) { subs.removeAll { it.id == submission.id } }
        override suspend fun getSubmissionByMoodleId(cmid: Long): SubmissionEntity? =
            subs.find { it.moodleAssignmentId == cmid && it.source == SubmissionSource.MOODLE }
        override suspend fun getAllMoodleSubmissions(): List<SubmissionEntity> =
            subs.filter { it.source == SubmissionSource.MOODLE }
        override suspend fun updateMoodleSubmission(
            id: String,
            title: String,
            deadline: LocalDateTime,
            courseId: Long,
            filteredOut: Boolean,
            needsReview: Boolean
        ) {
            val idx = subs.indexOfFirst { it.id == id }
            if (idx != -1) {
                subs[idx] = subs[idx].copy(
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
            val idx = subs.indexOfFirst { it.id == id }
            if (idx != -1) subs[idx] = subs[idx].copy(filteredOut = filteredOut)
        }
        override suspend fun markMoodleVanished(id: String) {
            val idx = subs.indexOfFirst { it.id == id }
            if (idx != -1) subs[idx] = subs[idx].copy(moodleVanished = true)
        }
        override suspend fun deleteMoodleSubmissionsByCourseId(courseId: Long): Int = 0
        override suspend fun deleteMoodleSubmissionsBySubjectId(subjectId: String): Int = 0
    }

    class FakeSubjectRepo(private val subjects: List<SubjectEntity>) : SubjectRepository {
        override fun getAllSubjects(): Flow<List<SubjectEntity>> = flowOf(subjects)
        override suspend fun getSubjectById(id: String): SubjectEntity? = subjects.find { it.id == id }
        override suspend fun insertSubject(subject: SubjectEntity) {}
        override suspend fun updateSubject(subject: SubjectEntity) {}
        override suspend fun deleteSubject(subject: SubjectEntity) {}
        override suspend fun deleteSubjectById(id: String) {}
    }

    class FakeMappingRepo(private val mappings: List<MoodleCourseMappingEntity>) : MoodleCourseMappingRepository {
        override fun getAllMappings(): Flow<List<MoodleCourseMappingEntity>> = flowOf(mappings)
        override suspend fun getMappingByCourseId(courseId: Long): MoodleCourseMappingEntity? =
            mappings.find { it.moodleCourseId == courseId }
        override suspend fun saveMapping(courseId: Long, courseName: String, subjectId: String?) {}
        override suspend fun deleteMappingByCourseId(courseId: Long) {}
        override suspend fun clearAllMappings() {}
    }

    class FakeCacheDao : MoodleCourseContentCacheDao {
        override suspend fun getCachedContent(courseId: Long): MoodleCourseContentCacheEntity? = null
        override suspend fun insertOrUpdate(entity: MoodleCourseContentCacheEntity) {}
        override suspend fun deleteByCourseId(courseId: Long) {}
        override suspend fun clearAll() {}
    }
}
