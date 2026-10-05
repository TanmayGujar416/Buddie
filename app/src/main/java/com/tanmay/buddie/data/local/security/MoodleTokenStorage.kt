package com.tanmay.buddie.data.local.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class MoodleTokenStorage(
    private val contextProvider: (() -> Context)?
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this({ context })

    companion object {
        private const val PREFS_FILE_NAME = "moodle_secure_prefs"
        private const val KEY_TOKEN = "moodle_auth_token"
        private const val TAG = "MoodleTokenStorage"
    }

    protected val _hasTokenFlow = MutableStateFlow(false)
    open val hasTokenFlow: StateFlow<Boolean> = _hasTokenFlow.asStateFlow()

    @Volatile
    private var securePrefs: SharedPreferences? = null

    init {
        if (contextProvider != null) {
            securePrefs = initEncryptedSharedPreferences()
            _hasTokenFlow.value = !getToken().isNullOrBlank()
        }
    }

    private fun getContext(): Context {
        return contextProvider?.invoke() ?: throw IllegalStateException("Context not provided")
    }

    private fun initEncryptedSharedPreferences(): SharedPreferences? {
        return try {
            createEncryptedPrefs()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, resetting corrupted file", e)
            try {
                // Delete potentially corrupted shared preferences file if Keystore key mismatch/corruption occurred
                val context = getContext()
                val file = File(context.filesDir.parent, "shared_prefs/$PREFS_FILE_NAME.xml")
                if (file.exists()) {
                    file.delete()
                }
                // Explicitly set hasTokenFlow to false when the key is reset
                _hasTokenFlow.value = false
                createEncryptedPrefs()
            } catch (recoveryException: Exception) {
                Log.e(TAG, "Critical failure creating EncryptedSharedPreferences after reset", recoveryException)
                _hasTokenFlow.value = false
                null
            }
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val context = getContext()
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    open fun saveToken(token: String) {
        getPrefs()?.edit()?.putString(KEY_TOKEN, token)?.apply()
        _hasTokenFlow.value = true
    }

    open fun getToken(): String? {
        return try {
            getPrefs()?.getString(KEY_TOKEN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading token from EncryptedSharedPreferences, triggering recovery", e)
            initEncryptedSharedPreferences()
            null
        }
    }

    open fun clearToken() {
        try {
            getPrefs()?.edit()?.remove(KEY_TOKEN)?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error removing token from EncryptedSharedPreferences", e)
        } finally {
            _hasTokenFlow.value = false
        }
    }

    open fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }

    // Exposed for testing Keystore recovery behavior directly
    fun simulateCorruptedKeystoreRecovery() {
        try {
            val context = getContext()
            val file = File(context.filesDir.parent, "shared_prefs/$PREFS_FILE_NAME.xml")
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        _hasTokenFlow.value = false
    }

    private fun getPrefs(): SharedPreferences? {
        if (securePrefs == null && contextProvider != null) {
            securePrefs = initEncryptedSharedPreferences()
        }
        return securePrefs
    }
}
