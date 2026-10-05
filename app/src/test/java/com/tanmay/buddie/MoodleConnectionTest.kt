package com.tanmay.buddie

import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.data.local.security.MoodleTokenStorage
import com.tanmay.buddie.data.remote.moodle.MoodleApiClient
import com.tanmay.buddie.data.remote.moodle.MoodleAuthResult
import com.tanmay.buddie.presentation.screens.settings.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test

class MoodleConnectionTest {

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
    fun urlNormalization_prependsHttpsAndStripsTrailingSlash() {
        val client = MoodleApiClient(
            okHttpClient = OkHttpClient(),
            tokenStorage = MoodleTokenStorageFake(),
            userPreferences = UserPreferencesFake()
        )

        assertEquals("https://moodle.spit.ac.in", client.normalizeSiteUrl("moodle.spit.ac.in"))
        assertEquals("https://moodle.spit.ac.in", client.normalizeSiteUrl("moodle.spit.ac.in/"))
        assertEquals("https://moodle.spit.ac.in", client.normalizeSiteUrl("https://moodle.spit.ac.in"))
        assertEquals("https://moodle.spit.ac.in", client.normalizeSiteUrl("https://moodle.spit.ac.in/"))
        assertEquals("http://moodle.internal.org", client.normalizeSiteUrl("http://moodle.internal.org/"))
        assertEquals("https://college.edu/moodle", client.normalizeSiteUrl("college.edu/moodle/"))
    }

    @Test
    fun formBodyEncoding_preservesSpecialCharactersInPasswords() {
        val rawPassword = "P@ss#word+123&test=val?%#"
        val formBody = FormBody.Builder()
            .add("username", "student_user")
            .add("password", rawPassword)
            .add("service", "moodle_mobile_app")
            .build()

        val buffer = Buffer()
        formBody.writeTo(buffer)
        val encodedString = buffer.readUtf8()

        assertTrue(encodedString.contains("username=student_user"))
        assertTrue(encodedString.contains("service=moodle_mobile_app"))
        assertTrue(encodedString.contains("%23"))
        assertTrue(encodedString.contains("%2B"))
        assertTrue(encodedString.contains("%26"))
        assertFalse(encodedString.contains("password=P@ss#"))
    }

    @Test
    fun authenticate_successfulToken_returnsSuccessAndSavesData() = runBlocking {
        val mockToken = "472e3895e7c80521e42cba23948abcd"
        val jsonResponse = """{"token":"$mockToken","privatetoken":"secret987"}"""
        val mockClient = createMockOkHttpClient(statusCode = 200, responseJson = jsonResponse)

        val testTokenStorage = MoodleTokenStorageFake()
        val testUserPrefs = UserPreferencesFake()

        val client = MoodleApiClient(mockClient, testTokenStorage, testUserPrefs)
        val result = client.authenticate("moodle.spit.ac.in", "student", "secret#pass")

        assertTrue(result is MoodleAuthResult.Success)
        val success = result as MoodleAuthResult.Success
        assertEquals(mockToken, success.token)
        assertEquals("https://moodle.spit.ac.in", success.siteUrl)
        assertEquals(mockToken, testTokenStorage.getToken())
        assertEquals("https://moodle.spit.ac.in", testUserPrefs.savedUrl)
    }

    @Test
    fun authenticate_invalidLogin_returnsMoodleRealErrorMessage() = runBlocking {
        val expectedError = "Invalid login, please check your username and password"
        val jsonResponse = """{"error":"$expectedError","errorcode":"invalidlogin"}"""
        val mockClient = createMockOkHttpClient(statusCode = 200, responseJson = jsonResponse)

        val client = MoodleApiClient(mockClient, MoodleTokenStorageFake(), UserPreferencesFake())
        val result = client.authenticate("moodle.spit.ac.in", "student", "wrong_pass")

        assertTrue(result is MoodleAuthResult.Error)
        val error = result as MoodleAuthResult.Error
        assertEquals(expectedError, error.message)
        assertEquals("invalidlogin", error.errorCode)
    }

    @Test
    fun authenticate_webServicesDisabled_returnsMoodleRealErrorMessage() = runBlocking {
        val expectedError = "The site does not support mobile access"
        val jsonResponse = """{"error":"$expectedError","errorcode":"enablews"}"""
        val mockClient = createMockOkHttpClient(statusCode = 200, responseJson = jsonResponse)

        val client = MoodleApiClient(mockClient, MoodleTokenStorageFake(), UserPreferencesFake())
        val result = client.authenticate("moodle.spit.ac.in", "student", "password")

        assertTrue(result is MoodleAuthResult.Error)
        val error = result as MoodleAuthResult.Error
        assertEquals(expectedError, error.message)
        assertEquals("enablews", error.errorCode)
    }

    @Test
    fun authenticate_validation_detectsEmptyFieldsWithoutNetwork() = runBlocking {
        val client = MoodleApiClient(OkHttpClient(), MoodleTokenStorageFake(), UserPreferencesFake())

        val emptyUrlResult = client.authenticate("", "student", "password")
        assertTrue(emptyUrlResult is MoodleAuthResult.Error)
        assertTrue((emptyUrlResult as MoodleAuthResult.Error).message.contains("site URL"))

        val emptyUserResult = client.authenticate("moodle.spit.ac.in", "", "password")
        assertTrue(emptyUserResult is MoodleAuthResult.Error)
        assertTrue((emptyUserResult as MoodleAuthResult.Error).message.contains("username"))

        val emptyPassResult = client.authenticate("moodle.spit.ac.in", "student", "")
        assertTrue(emptyPassResult is MoodleAuthResult.Error)
        assertTrue((emptyPassResult as MoodleAuthResult.Error).message.contains("password"))
    }

    @Test
    fun disconnect_clearsTokenAndSiteUrl() = runBlocking {
        val testTokenStorage = MoodleTokenStorageFake()
        val testUserPrefs = UserPreferencesFake()
        testTokenStorage.saveToken("initial_token")
        testUserPrefs.setMoodleSiteUrl("https://moodle.spit.ac.in")

        val client = MoodleApiClient(OkHttpClient(), testTokenStorage, testUserPrefs)
        assertTrue(client.isConnected())

        client.disconnect()

        assertFalse(client.isConnected())
        assertNull(testTokenStorage.getToken())
        assertFalse(testTokenStorage.hasTokenFlow.value)
        assertNull(testUserPrefs.savedUrl)
    }

    @Test
    fun corruptedKeystoreRecovery_explicitlyResetsHasTokenFlowToFalse() {
        val storage = MoodleTokenStorageFake()
        storage.saveToken("test_token")
        assertTrue(storage.hasTokenFlow.value)

        // Fix 3 verification: Trigger Keystore recovery simulation
        storage.simulateCorruptedKeystoreRecovery()
        assertFalse(storage.hasTokenFlow.value)
    }

    @Test
    fun settingsUiState_containsMoodleConnectionState() {
        val disconnectedState = SettingsUiState(isMoodleConnected = false, moodleSiteUrl = null)
        assertFalse(disconnectedState.isMoodleConnected)
        assertNull(disconnectedState.moodleSiteUrl)

        val connectedState = SettingsUiState(
            isMoodleConnected = true,
            moodleSiteUrl = "https://moodle.spit.ac.in"
        )
        assertTrue(connectedState.isMoodleConnected)
        assertEquals("https://moodle.spit.ac.in", connectedState.moodleSiteUrl)
    }

    open class MoodleTokenStorageFake : MoodleTokenStorage(contextProvider = null) {
        private var storedToken: String? = null

        override fun saveToken(token: String) {
            this.storedToken = token
            _hasTokenFlow.value = true
        }

        override fun getToken(): String? = storedToken

        override fun clearToken() {
            storedToken = null
            _hasTokenFlow.value = false
        }

        override fun hasToken(): Boolean = !storedToken.isNullOrBlank()
    }

    open class UserPreferencesFake : UserPreferences(dataStoreProvider = null) {
        var savedUrl: String? = null
        private val _siteUrlFlow = MutableStateFlow<String?>(null)
        override val moodleSiteUrlFlow = _siteUrlFlow

        override suspend fun setMoodleSiteUrl(url: String?) {
            savedUrl = url
            _siteUrlFlow.value = url
        }
    }
}
