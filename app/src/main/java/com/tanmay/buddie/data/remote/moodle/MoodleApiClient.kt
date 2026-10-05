package com.tanmay.buddie.data.remote.moodle

import android.util.Log
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.data.local.security.MoodleTokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray

sealed class MoodleAuthResult {
    data class Success(val token: String, val siteUrl: String) : MoodleAuthResult()
    data class Error(val message: String, val errorCode: String? = null) : MoodleAuthResult()
}

data class MoodleSiteInfo(
    val userId: Long,
    val fullName: String,
    val siteName: String
)

data class MoodleCourse(
    val id: Long,
    val fullName: String,
    val shortName: String
)

data class MoodleSection(
    val id: Long,
    val name: String,
    val modules: List<MoodleModule> = emptyList()
)

data class MoodleModule(
    val id: Long,        // course module id (cmid)
    val name: String,
    val modname: String, // "assign", "resource", etc.
    val instance: Long
)

data class MoodleAssignment(
    val id: Long,        // assignment instance id
    val cmid: Long,      // course module id (correlation key)
    val courseId: Long,
    val name: String,
    val dueDate: Long    // Unix timestamp (seconds), 0 if no due date
)

@Singleton
open class MoodleApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenStorage: MoodleTokenStorage,
    private val userPreferences: UserPreferences
) {
    companion object {
        const val SERVICE_NAME = "moodle_mobile_app"
        private const val TAG = "MoodleApiClient"
    }

    val isConnectedFlow: Flow<Boolean> = tokenStorage.hasTokenFlow

    val connectionStatusFlow: Flow<Pair<Boolean, String?>> = combine(
        tokenStorage.hasTokenFlow,
        userPreferences.moodleSiteUrlFlow
    ) { hasToken, siteUrl ->
        Pair(hasToken, siteUrl)
    }

    fun normalizeSiteUrl(inputUrl: String): String {
        var url = inputUrl.trim()
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            url = "https://$url"
        }
        return url.trimEnd('/')
    }

    suspend fun authenticate(
        siteUrl: String,
        username: String,
        password: String
    ): MoodleAuthResult = withContext(Dispatchers.IO) {
        val trimmedUrl = siteUrl.trim()
        val trimmedUsername = username.trim()

        if (trimmedUrl.isEmpty()) {
            return@withContext MoodleAuthResult.Error("Please enter your Moodle site URL")
        }
        if (trimmedUsername.isEmpty()) {
            return@withContext MoodleAuthResult.Error("Please enter your username")
        }
        if (password.isEmpty()) {
            return@withContext MoodleAuthResult.Error("Please enter your password")
        }

        val normalizedBaseUrl = normalizeSiteUrl(trimmedUrl)
        val tokenEndpoint = "$normalizedBaseUrl/login/token.php"

        val formBody = FormBody.Builder()
            .add("username", trimmedUsername)
            .add("password", password)
            .add("service", SERVICE_NAME)
            .build()

        val request = Request.Builder()
            .url(tokenEndpoint)
            .post(formBody)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            val json = try {
                if (responseBody.isNotBlank()) JSONObject(responseBody) else null
            } catch (_: Exception) {
                null
            }

            // Check if Moodle returned a JSON error (Moodle returns 200 or 4xx with JSON error)
            if (json != null && json.has("error")) {
                val errorMessage = json.getString("error")
                val errorCode = if (json.has("errorcode")) json.getString("errorcode") else null
                logWarning("Moodle authentication failed with error: $errorMessage ($errorCode)")
                return@withContext MoodleAuthResult.Error(
                    message = errorMessage,
                    errorCode = errorCode
                )
            }

            // Check if HTTP response failed and no JSON error was found
            if (!response.isSuccessful) {
                return@withContext MoodleAuthResult.Error(
                    "Unable to connect to Moodle: HTTP ${response.code} (${response.message})"
                )
            }

            // Check if Moodle returned a valid token
            if (json != null && json.has("token")) {
                val token = json.getString("token")
                // Store ONLY the token in EncryptedSharedPreferences
                tokenStorage.saveToken(token)
                // Store the site URL in plain DataStore
                userPreferences.setMoodleSiteUrl(normalizedBaseUrl)
                return@withContext MoodleAuthResult.Success(
                    token = token,
                    siteUrl = normalizedBaseUrl
                )
            }

            // If JSON was parsed but has neither token nor error, or HTML was returned
            return@withContext MoodleAuthResult.Error(
                "Invalid response from server. Please verify your Moodle site URL."
            )
        } catch (e: IOException) {
            logError("Network exception during Moodle authentication", e)
            return@withContext MoodleAuthResult.Error(
                "Network error: ${e.localizedMessage ?: "Unable to reach Moodle server"}"
            )
        } catch (e: Exception) {
            logError("Unexpected error during Moodle authentication", e)
            return@withContext MoodleAuthResult.Error(
                "An unexpected error occurred: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    private fun logWarning(message: String) {
        try {
            Log.w(TAG, message)
        } catch (_: Exception) {}
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        try {
            Log.e(TAG, message, throwable)
        } catch (_: Exception) {}
    }

    open suspend fun getSiteInfo(): Result<MoodleSiteInfo> = withContext(Dispatchers.IO) {
        val token = tokenStorage.getToken()
            ?: return@withContext Result.failure(Exception("Not authenticated with Moodle (missing token)"))
        val siteUrl = userPreferences.moodleSiteUrlFlow.firstOrNull()
            ?: return@withContext Result.failure(Exception("Moodle site URL is not configured"))

        val endpoint = "${normalizeSiteUrl(siteUrl)}/webservice/rest/server.php"
        val formBody = FormBody.Builder()
            .add("wstoken", token)
            .add("wsfunction", "core_webservice_get_site_info")
            .add("moodlewsrestformat", "json")
            .build()

        val request = Request.Builder()
            .url(endpoint)
            .post(formBody)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful && responseBody.isBlank()) {
                return@withContext Result.failure(Exception("Server returned HTTP ${response.code}: ${response.message}"))
            }

            val json = try {
                if (responseBody.isNotBlank()) JSONObject(responseBody) else null
            } catch (_: Exception) {
                null
            }

            if (json != null) {
                if (json.has("exception") || json.has("error")) {
                    val message = json.optString("message", json.optString("error", "Failed to retrieve site info"))
                    return@withContext Result.failure(Exception(message))
                }
                if (json.has("userid")) {
                    val userId = json.getLong("userid")
                    val fullName = json.optString("fullname", "")
                    val siteName = json.optString("sitename", "")
                    return@withContext Result.success(MoodleSiteInfo(userId, fullName, siteName))
                }
            }

            Result.failure(Exception("Invalid response received from Moodle server"))
        } catch (e: IOException) {
            logError("Network error while calling core_webservice_get_site_info", e)
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        } catch (e: Exception) {
            logError("Unexpected error in getSiteInfo", e)
            Result.failure(Exception(e.localizedMessage ?: "Unexpected error"))
        }
    }

    open suspend fun getEnrolledCourses(userId: Long? = null): Result<List<MoodleCourse>> = withContext(Dispatchers.IO) {
        val resolvedUserId = if (userId != null) {
            userId
        } else {
            val siteInfoResult = getSiteInfo()
            if (siteInfoResult.isFailure) {
                return@withContext Result.failure(siteInfoResult.exceptionOrNull() ?: Exception("Failed to retrieve user ID"))
            }
            siteInfoResult.getOrThrow().userId
        }

        val token = tokenStorage.getToken()
            ?: return@withContext Result.failure(Exception("Not authenticated with Moodle (missing token)"))
        val siteUrl = userPreferences.moodleSiteUrlFlow.firstOrNull()
            ?: return@withContext Result.failure(Exception("Moodle site URL is not configured"))

        val endpoint = "${normalizeSiteUrl(siteUrl)}/webservice/rest/server.php"
        val formBody = FormBody.Builder()
            .add("wstoken", token)
            .add("wsfunction", "core_enrol_get_users_courses")
            .add("moodlewsrestformat", "json")
            .add("userid", resolvedUserId.toString())
            .build()

        val request = Request.Builder()
            .url(endpoint)
            .post(formBody)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful && responseBody.isBlank()) {
                return@withContext Result.failure(Exception("Server returned HTTP ${response.code}: ${response.message}"))
            }

            // Check if Moodle returned an error JSON object
            val trimmedBody = responseBody.trim()
            if (trimmedBody.startsWith("{")) {
                val json = try { JSONObject(trimmedBody) } catch (_: Exception) { null }
                if (json != null && (json.has("exception") || json.has("error"))) {
                    val message = json.optString("message", json.optString("error", "Failed to retrieve courses"))
                    return@withContext Result.failure(Exception(message))
                }
            }

            // Parse JSON array of courses
            val jsonArray = try {
                JSONArray(trimmedBody)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Unexpected response format from Moodle: expected course array"))
            }

            val courses = mutableListOf<MoodleCourse>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.getLong("id")
                val fullName = item.getString("fullname")
                val shortName = item.optString("shortname", "")
                courses.add(MoodleCourse(id = id, fullName = fullName, shortName = shortName))
            }

            Result.success(courses)
        } catch (e: IOException) {
            logError("Network error while calling core_enrol_get_users_courses", e)
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        } catch (e: Exception) {
            logError("Unexpected error in getEnrolledCourses", e)
            Result.failure(Exception(e.localizedMessage ?: "Unexpected error"))
        }
    }

    open suspend fun getCourseContents(courseId: Long): Result<List<MoodleSection>> = withContext(Dispatchers.IO) {
        val token = tokenStorage.getToken()
            ?: return@withContext Result.failure(Exception("Not authenticated with Moodle (missing token)"))
        val siteUrl = userPreferences.moodleSiteUrlFlow.firstOrNull()
            ?: return@withContext Result.failure(Exception("Moodle site URL is not configured"))

        val endpoint = "${normalizeSiteUrl(siteUrl)}/webservice/rest/server.php"
        val formBody = FormBody.Builder()
            .add("wstoken", token)
            .add("wsfunction", "core_course_get_contents")
            .add("moodlewsrestformat", "json")
            .add("courseid", courseId.toString())
            .build()

        val request = Request.Builder()
            .url(endpoint)
            .post(formBody)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful && responseBody.isBlank()) {
                return@withContext Result.failure(Exception("Server returned HTTP ${response.code}: ${response.message}"))
            }

            val trimmed = responseBody.trim()
            if (trimmed.startsWith("{")) {
                val json = try { JSONObject(trimmed) } catch (_: Exception) { null }
                if (json != null && (json.has("exception") || json.has("error"))) {
                    val message = json.optString("message", json.optString("error", "Failed to retrieve course contents"))
                    return@withContext Result.failure(Exception(message))
                }
            }

            val jsonArray = try {
                JSONArray(trimmed)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Unexpected response format from Moodle: expected sections array"))
            }

            val sections = mutableListOf<MoodleSection>()
            for (i in 0 until jsonArray.length()) {
                val sectionObj = jsonArray.getJSONObject(i)
                val sectionId = sectionObj.optLong("id", 0L)
                val sectionName = sectionObj.optString("name", "Section $i")
                val modulesList = mutableListOf<MoodleModule>()

                val modulesArray = sectionObj.optJSONArray("modules")
                if (modulesArray != null) {
                    for (j in 0 until modulesArray.length()) {
                        val modObj = modulesArray.getJSONObject(j)
                        val modId = modObj.getLong("id") // cmid
                        val modName = modObj.optString("name", "")
                        val modType = modObj.optString("modname", "")
                        val modInstance = modObj.optLong("instance", 0L)
                        modulesList.add(
                            MoodleModule(
                                id = modId,
                                name = modName,
                                modname = modType,
                                instance = modInstance
                            )
                        )
                    }
                }

                sections.add(
                    MoodleSection(
                        id = sectionId,
                        name = sectionName,
                        modules = modulesList
                    )
                )
            }

            Result.success(sections)
        } catch (e: IOException) {
            logError("Network error while calling core_course_get_contents", e)
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        } catch (e: Exception) {
            logError("Unexpected error in getCourseContents", e)
            Result.failure(Exception(e.localizedMessage ?: "Unexpected error"))
        }
    }

    open suspend fun getAssignments(courseIds: List<Long>): Result<List<MoodleAssignment>> = withContext(Dispatchers.IO) {
        if (courseIds.isEmpty()) {
            return@withContext Result.success(emptyList())
        }

        val token = tokenStorage.getToken()
            ?: return@withContext Result.failure(Exception("Not authenticated with Moodle (missing token)"))
        val siteUrl = userPreferences.moodleSiteUrlFlow.firstOrNull()
            ?: return@withContext Result.failure(Exception("Moodle site URL is not configured"))

        val endpoint = "${normalizeSiteUrl(siteUrl)}/webservice/rest/server.php"
        val formBuilder = FormBody.Builder()
            .add("wstoken", token)
            .add("wsfunction", "mod_assign_get_assignments")
            .add("moodlewsrestformat", "json")

        courseIds.forEachIndexed { index, courseId ->
            formBuilder.add("courseids[$index]", courseId.toString())
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(formBuilder.build())
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful && responseBody.isBlank()) {
                return@withContext Result.failure(Exception("Server returned HTTP ${response.code}: ${response.message}"))
            }

            val trimmed = responseBody.trim()
            val json = try { JSONObject(trimmed) } catch (e: Exception) { null }

            if (json == null) {
                return@withContext Result.failure(Exception("Unexpected response format from Moodle for assignments"))
            }

            if (json.has("exception") || json.has("error")) {
                val message = json.optString("message", json.optString("error", "Failed to retrieve assignments"))
                return@withContext Result.failure(Exception(message))
            }

            val assignments = mutableListOf<MoodleAssignment>()
            val coursesArray = json.optJSONArray("courses")
            if (coursesArray != null) {
                for (i in 0 until coursesArray.length()) {
                    val courseObj = coursesArray.getJSONObject(i)
                    val courseId = courseObj.optLong("id", 0L)
                    val assignsArray = courseObj.optJSONArray("assignments")
                    if (assignsArray != null) {
                        for (j in 0 until assignsArray.length()) {
                            val assignObj = assignsArray.getJSONObject(j)
                            val assignId = assignObj.getLong("id")
                            val cmid = assignObj.optLong("cmid", 0L)
                            val course = assignObj.optLong("course", courseId)
                            val name = assignObj.optString("name", "")
                            val duedate = assignObj.optLong("duedate", 0L)

                            assignments.add(
                                MoodleAssignment(
                                    id = assignId,
                                    cmid = cmid,
                                    courseId = course,
                                    name = name,
                                    dueDate = duedate
                                )
                            )
                        }
                    }
                }
            }

            Result.success(assignments)
        } catch (e: IOException) {
            logError("Network error while calling mod_assign_get_assignments", e)
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unable to reach server"}"))
        } catch (e: Exception) {
            logError("Unexpected error in getAssignments", e)
            Result.failure(Exception(e.localizedMessage ?: "Unexpected error"))
        }
    }

    suspend fun disconnect() {
        tokenStorage.clearToken()
        userPreferences.setMoodleSiteUrl(null)
    }

    fun getStoredToken(): String? = tokenStorage.getToken()

    fun isConnected(): Boolean = tokenStorage.hasToken()
}
