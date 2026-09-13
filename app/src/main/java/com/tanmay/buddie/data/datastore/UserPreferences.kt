package com.tanmay.buddie.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val CARD_STYLE = stringPreferencesKey("card_style")
        val BUBBLE_EDGE = stringPreferencesKey("bubble_edge")
        val BUBBLE_POSITION_Y = stringPreferencesKey("bubble_position_y") // Stored as string or float
        val INCLUDE_SATURDAY = booleanPreferencesKey("include_saturday")
        val INCLUDE_SUNDAY = booleanPreferencesKey("include_sunday")
        val NOTIFICATION_HOURS_BEFORE = androidx.datastore.preferences.core.intPreferencesKey("notification_hours_before")
    }

    val cardStyleFlow: Flow<String> = dataStore.data.map { it[CARD_STYLE] ?: "SPEECH_BUBBLE" }
    val notificationHoursFlow: Flow<Int> = dataStore.data.map { it[NOTIFICATION_HOURS_BEFORE] ?: 24 }

    val includeSaturdayFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[INCLUDE_SATURDAY] ?: false
    }

    val includeSundayFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[INCLUDE_SUNDAY] ?: false
    }

    suspend fun setCardStyle(style: String) {
        dataStore.edit { it[CARD_STYLE] = style }
    }

    suspend fun setNotificationHoursBefore(hours: Int) {
        dataStore.edit { it[NOTIFICATION_HOURS_BEFORE] = hours }
    }

    suspend fun setIncludeSaturday(include: Boolean) {
        dataStore.edit { preferences ->
            preferences[INCLUDE_SATURDAY] = include
        }
    }

    suspend fun setIncludeSunday(include: Boolean) {
        dataStore.edit { preferences ->
            preferences[INCLUDE_SUNDAY] = include
        }
    }
}
