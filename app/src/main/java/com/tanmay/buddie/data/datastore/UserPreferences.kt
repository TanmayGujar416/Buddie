package com.tanmay.buddie.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class BubbleEdge {
    LEFT,
    RIGHT
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
open class UserPreferences(
    private val dataStoreProvider: (() -> DataStore<Preferences>)? = null
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this({ context.dataStore })

    private val dataStore: DataStore<Preferences>? by lazy {
        dataStoreProvider?.invoke()
    }

    companion object {
        val CARD_STYLE = stringPreferencesKey("card_style")
        val COMPANION_TYPE = stringPreferencesKey("companion_type")
        val BUBBLE_EDGE = stringPreferencesKey("bubble_edge")
        val BUBBLE_POSITION_Y = intPreferencesKey("bubble_position_y")
        val INCLUDE_SATURDAY = booleanPreferencesKey("include_saturday")
        val INCLUDE_SUNDAY = booleanPreferencesKey("include_sunday")
        val NOTIFICATION_HOURS_BEFORE = intPreferencesKey("notification_hours_before")
        val FLOATING_BUBBLE_ENABLED = booleanPreferencesKey("floating_bubble_enabled")
        val BATTERY_OPTIMIZATION_PROMPT_DISMISSED = booleanPreferencesKey("battery_optimization_prompt_dismissed")
        val BATTERY_SAVER_ENABLED = booleanPreferencesKey("battery_saver_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val MOODLE_SITE_URL = stringPreferencesKey("moodle_site_url")
    }

    val cardStyleFlow: Flow<String> = dataStore?.data?.map { it[CARD_STYLE] ?: "COMPANION_HEADER" } ?: kotlinx.coroutines.flow.emptyFlow()
    val companionTypeFlow: Flow<CompanionType> = dataStore?.data?.map { preferences ->
        val typeStr = preferences[COMPANION_TYPE]
        try {
            if (typeStr != null) {
                when {
                    typeStr.equals("CAT", ignoreCase = true) -> CompanionType.MOMO
                    typeStr.equals("CLOUD", ignoreCase = true) -> CompanionType.WISP
                    typeStr.equals("GHOST", ignoreCase = true) -> CompanionType.WISP
                    else -> CompanionType.valueOf(typeStr)
                }
            } else CompanionType.BUDDIE
        } catch (_: Exception) {
            CompanionType.BUDDIE
        }
    } ?: kotlinx.coroutines.flow.emptyFlow()
    val notificationHoursFlow: Flow<Int> = dataStore?.data?.map { it[NOTIFICATION_HOURS_BEFORE] ?: 24 } ?: kotlinx.coroutines.flow.emptyFlow()
    val floatingBubbleEnabledFlow: Flow<Boolean> = dataStore?.data?.map { it[FLOATING_BUBBLE_ENABLED] ?: false } ?: kotlinx.coroutines.flow.emptyFlow()
    val batteryOptimizationPromptDismissedFlow: Flow<Boolean> = dataStore?.data?.map { it[BATTERY_OPTIMIZATION_PROMPT_DISMISSED] ?: false } ?: kotlinx.coroutines.flow.emptyFlow()
    val batterySaverEnabledFlow: Flow<Boolean> = dataStore?.data?.map { it[BATTERY_SAVER_ENABLED] ?: false } ?: kotlinx.coroutines.flow.emptyFlow()
    val onboardingCompletedFlow: Flow<Boolean> = dataStore?.data?.map { it[ONBOARDING_COMPLETED] ?: false } ?: kotlinx.coroutines.flow.emptyFlow()
    open val moodleSiteUrlFlow: Flow<String?> = dataStore?.data?.map { it[MOODLE_SITE_URL] } ?: kotlinx.coroutines.flow.emptyFlow()

    val bubbleEdgeFlow: Flow<BubbleEdge> = dataStore?.data?.map { preferences ->
        val edgeStr = preferences[BUBBLE_EDGE]
        if (edgeStr == BubbleEdge.LEFT.name) BubbleEdge.LEFT else BubbleEdge.RIGHT
    } ?: kotlinx.coroutines.flow.emptyFlow()

    val bubblePositionYFlow: Flow<Int?> = dataStore?.data?.map { preferences ->
        preferences[BUBBLE_POSITION_Y]
    } ?: kotlinx.coroutines.flow.emptyFlow()

    val includeSaturdayFlow: Flow<Boolean> = dataStore?.data?.map { preferences ->
        preferences[INCLUDE_SATURDAY] ?: false
    } ?: kotlinx.coroutines.flow.emptyFlow()

    val includeSundayFlow: Flow<Boolean> = dataStore?.data?.map { preferences ->
        preferences[INCLUDE_SUNDAY] ?: false
    } ?: kotlinx.coroutines.flow.emptyFlow()

    suspend fun setCardStyle(style: String) {
        dataStore?.edit { it[CARD_STYLE] = style }
    }

    suspend fun setCompanionType(type: CompanionType) {
        dataStore?.edit { it[COMPANION_TYPE] = type.name }
    }

    suspend fun setNotificationHoursBefore(hours: Int) {
        dataStore?.edit { it[NOTIFICATION_HOURS_BEFORE] = hours }
    }

    suspend fun setFloatingBubbleEnabled(enabled: Boolean) {
        dataStore?.edit { it[FLOATING_BUBBLE_ENABLED] = enabled }
    }

    suspend fun setBatteryOptimizationPromptDismissed(dismissed: Boolean) {
        dataStore?.edit { it[BATTERY_OPTIMIZATION_PROMPT_DISMISSED] = dismissed }
    }

    suspend fun setBatterySaverEnabled(enabled: Boolean) {
        dataStore?.edit { it[BATTERY_SAVER_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore?.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setIncludeSaturday(include: Boolean) {
        dataStore?.edit { preferences ->
            preferences[INCLUDE_SATURDAY] = include
        }
    }

    suspend fun setIncludeSunday(include: Boolean) {
        dataStore?.edit { preferences ->
            preferences[INCLUDE_SUNDAY] = include
        }
    }

    suspend fun setBubblePosition(edge: BubbleEdge, positionY: Int) {
        dataStore?.edit { preferences ->
            preferences[BUBBLE_EDGE] = edge.name
            preferences[BUBBLE_POSITION_Y] = positionY
        }
    }

    open suspend fun setMoodleSiteUrl(url: String?) {
        dataStore?.edit { preferences ->
            if (url != null) {
                preferences[MOODLE_SITE_URL] = url
            } else {
                preferences.remove(MOODLE_SITE_URL)
            }
        }
    }
}
