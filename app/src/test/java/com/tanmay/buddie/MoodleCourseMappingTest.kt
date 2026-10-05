package com.tanmay.buddie

import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity
import com.tanmay.buddie.data.remote.moodle.MoodleApiClient
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class MoodleCourseMappingTest {

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
    fun getSiteInfo_success_parsesUserIdAndDetails() = runBlocking {
        val siteInfoJson = """
            {
                "sitename": "SPIT Moodle",
                "username": "student123",
                "firstname": "Tanmay",
                "lastname": "Gujar",
                "fullname": "Tanmay Gujar",
                "lang": "en",
                "userid": 4567,
                "userpictureurl": "https://moodle.spit.ac.in/pluginfile.php/...",
                "functions": []
            }
        """.trimIndent()

        val mockClient = createMockOkHttpClient(statusCode = 200, responseJson = siteInfoJson)
        val tokenStorage = MoodleConnectionTest.MoodleTokenStorageFake().apply { saveToken("valid_token") }
        val userPrefs = MoodleConnectionTest.UserPreferencesFake().apply { setMoodleSiteUrl("https://moodle.spit.ac.in") }

        val client = MoodleApiClient(mockClient, tokenStorage, userPrefs)
        val result = client.getSiteInfo()

        assertTrue(result.isSuccess)
        val siteInfo = result.getOrThrow()
        assertEquals(4567L, siteInfo.userId)
        assertEquals("Tanmay Gujar", siteInfo.fullName)
        assertEquals("SPIT Moodle", siteInfo.siteName)
    }

    @Test
    fun getSiteInfo_moodleException_returnsFailure() = runBlocking {
        val errorJson = """
            {
                "exception": "moodle_exception",
                "errorcode": "invalidtoken",
                "message": "Invalid token - token not found"
            }
        """.trimIndent()

        val mockClient = createMockOkHttpClient(statusCode = 200, responseJson = errorJson)
        val tokenStorage = MoodleConnectionTest.MoodleTokenStorageFake().apply { saveToken("expired_token") }
        val userPrefs = MoodleConnectionTest.UserPreferencesFake().apply { setMoodleSiteUrl("https://moodle.spit.ac.in") }

        val client = MoodleApiClient(mockClient, tokenStorage, userPrefs)
        val result = client.getSiteInfo()

        assertTrue(result.isFailure)
        assertEquals("Invalid token - token not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun getEnrolledCourses_success_parsesCoursesList() = runBlocking {
        val coursesJson = """
            [
                {
                    "id": 101,
                    "shortname": "DBMS",
                    "fullname": "Database Management Systems",
                    "displayname": "Database Management Systems",
                    "idnumber": "CS301"
                },
                {
                    "id": 102,
                    "shortname": "DBMS-LAB",
                    "fullname": "Database Management Systems Laboratory",
                    "displayname": "Database Management Systems Laboratory",
                    "idnumber": "CS301L"
                },
                {
                    "id": 103,
                    "shortname": "LIB",
                    "fullname": "Library Orientation and General Knowledge",
                    "displayname": "Library Orientation",
                    "idnumber": "GEN001"
                }
            ]
        """.trimIndent()

        val mockClient = createMockOkHttpClient(statusCode = 200, responseJson = coursesJson)
        val tokenStorage = MoodleConnectionTest.MoodleTokenStorageFake().apply { saveToken("valid_token") }
        val userPrefs = MoodleConnectionTest.UserPreferencesFake().apply { setMoodleSiteUrl("https://moodle.spit.ac.in") }

        val client = MoodleApiClient(mockClient, tokenStorage, userPrefs)
        val result = client.getEnrolledCourses(userId = 4567L)

        assertTrue(result.isSuccess)
        val courses = result.getOrThrow()
        assertEquals(3, courses.size)
        assertEquals(101L, courses[0].id)
        assertEquals("Database Management Systems", courses[0].fullName)
        assertEquals("DBMS", courses[0].shortName)
        assertEquals(102L, courses[1].id)
        assertEquals("Database Management Systems Laboratory", courses[1].fullName)
        assertEquals(103L, courses[2].id)
        assertEquals("Library Orientation and General Knowledge", courses[2].fullName)
    }

    @Test
    fun getEnrolledCourses_errorResponse_returnsFailure() = runBlocking {
        val errorJson = """
            {
                "exception": "required_capability_exception",
                "errorcode": "nopermissions",
                "message": "Sorry, but you do not currently have permissions to do that."
            }
        """.trimIndent()

        val mockClient = createMockOkHttpClient(statusCode = 200, responseJson = errorJson)
        val tokenStorage = MoodleConnectionTest.MoodleTokenStorageFake().apply { saveToken("valid_token") }
        val userPrefs = MoodleConnectionTest.UserPreferencesFake().apply { setMoodleSiteUrl("https://moodle.spit.ac.in") }

        val client = MoodleApiClient(mockClient, tokenStorage, userPrefs)
        val result = client.getEnrolledCourses(userId = 4567L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("permissions") == true)
    }

    @Test
    fun manyCoursesMappingToSameSubject_isSupported() {
        val dbmsSubjectId = "subject_dbms_uuid"

        // Theory course mapping
        val course1 = MoodleCourseMappingEntity(
            moodleCourseId = 101L,
            moodleCourseName = "Database Management Systems",
            subjectId = dbmsSubjectId
        )

        // Lab course mapping
        val course2 = MoodleCourseMappingEntity(
            moodleCourseId = 102L,
            moodleCourseName = "Database Management Systems Laboratory",
            subjectId = dbmsSubjectId
        )

        // Both courses reference the exact same subject ID
        assertEquals(course1.subjectId, course2.subjectId)
        assertEquals(dbmsSubjectId, course1.subjectId)
        assertEquals(dbmsSubjectId, course2.subjectId)
        assertNotEquals(course1.moodleCourseId, course2.moodleCourseId)
    }

    @Test
    fun ignoredCourse_hasNullSubjectId() {
        val ignoredCourse = MoodleCourseMappingEntity(
            moodleCourseId = 103L,
            moodleCourseName = "Library Orientation",
            subjectId = null // Ignored
        )

        assertNull(ignoredCourse.subjectId)
        assertEquals(103L, ignoredCourse.moodleCourseId)
    }

    @Test
    fun repository_handlesManyToOneMappingAndCascadeSimulation() = runBlocking {
        val repo = FakeMoodleCourseMappingRepository()

        val subjectMath = "sub_math"
        val subjectCs = "sub_cs"

        // Map two Moodle courses to CS (many-to-one)
        repo.saveMapping(101L, "CS Theory", subjectCs)
        repo.saveMapping(102L, "CS Lab", subjectCs)
        // Map one Moodle course to Math
        repo.saveMapping(103L, "Applied Mathematics", subjectMath)
        // Ignore one Moodle course
        repo.saveMapping(104L, "Physical Education", null)

        val allMappings = repo.getAllMappings().first()
        assertEquals(4, allMappings.size)

        val csMappings = allMappings.filter { it.subjectId == subjectCs }
        assertEquals(2, csMappings.size)
        assertTrue(csMappings.any { it.moodleCourseId == 101L })
        assertTrue(csMappings.any { it.moodleCourseId == 102L })

        val ignoredMappings = allMappings.filter { it.subjectId == null }
        assertEquals(1, ignoredMappings.size)
        assertEquals(104L, ignoredMappings[0].moodleCourseId)

        // Simulate Subject CASCADE delete: when CS subject is deleted,
        // all mappings referencing subjectCs should be removed, while others remain
        repo.simulateSubjectDeleted(subjectCs)

        val remainingMappings = repo.getAllMappings().first()
        assertEquals(2, remainingMappings.size)
        assertNull(remainingMappings.find { it.moodleCourseId == 101L })
        assertNull(remainingMappings.find { it.moodleCourseId == 102L })
        assertNotNull(remainingMappings.find { it.moodleCourseId == 103L }) // Math remains
        assertNotNull(remainingMappings.find { it.moodleCourseId == 104L }) // Ignored remains
    }

    class FakeMoodleCourseMappingRepository : MoodleCourseMappingRepository {
        private val mappingsMap = MutableStateFlow<Map<Long, MoodleCourseMappingEntity>>(emptyMap())

        override fun getAllMappings(): Flow<List<MoodleCourseMappingEntity>> {
            return mappingsMap.map { it.values.toList() }
        }

        override suspend fun getMappingByCourseId(courseId: Long): MoodleCourseMappingEntity? {
            return mappingsMap.value[courseId]
        }

        override suspend fun saveMapping(courseId: Long, courseName: String, subjectId: String?) {
            val updated = mappingsMap.value.toMutableMap()
            updated[courseId] = MoodleCourseMappingEntity(
                moodleCourseId = courseId,
                moodleCourseName = courseName,
                subjectId = subjectId
            )
            mappingsMap.value = updated
        }

        override suspend fun deleteMappingByCourseId(courseId: Long) {
            val updated = mappingsMap.value.toMutableMap()
            updated.remove(courseId)
            mappingsMap.value = updated
        }

        override suspend fun clearAllMappings() {
            mappingsMap.value = emptyMap()
        }

        fun simulateSubjectDeleted(deletedSubjectId: String) {
            val updated = mappingsMap.value.toMutableMap()
            val iterator = updated.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.subjectId == deletedSubjectId) {
                    iterator.remove()
                }
            }
            mappingsMap.value = updated
        }
    }
}
