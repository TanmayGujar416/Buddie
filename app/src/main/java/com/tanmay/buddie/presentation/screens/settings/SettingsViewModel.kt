package com.tanmay.buddie.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.datastore.UserPreferences
import com.tanmay.buddie.presentation.components.mascot.CompanionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine
import kotlinx.coroutines.flow.MutableStateFlow

data class SettingsUiState(
    val cardStyle: String = "COMPANION_HEADER",
    val companionType: CompanionType = CompanionType.BUDDIE,
    val notificationHours: Int = 24,
    val floatingBubbleEnabled: Boolean = false,
    val batteryOptimizationPromptDismissed: Boolean = false,
    val batterySaverEnabled: Boolean = false,
    val isMoodleConnected: Boolean = false,
    val moodleSiteUrl: String? = null,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val moodleApiClient: com.tanmay.buddie.data.remote.moodle.MoodleApiClient,
    private val moodleSyncEngine: MoodleContentSyncEngine
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _syncMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.cardStyleFlow,
        userPreferences.companionTypeFlow,
        userPreferences.notificationHoursFlow,
        userPreferences.floatingBubbleEnabledFlow,
        combine(
            userPreferences.batteryOptimizationPromptDismissedFlow,
            userPreferences.batterySaverEnabledFlow,
            moodleApiClient.connectionStatusFlow,
            _isSyncing,
            _syncMessage
        ) { opt, saver, moodleStatus, syncing, message ->
            CombinedMoodleSyncState(opt, saver, moodleStatus, syncing, message)
        }
    ) { cardStyle, companionType, notificationHours, floatingBubbleEnabled, syncState ->
        SettingsUiState(
            cardStyle = cardStyle,
            companionType = companionType,
            notificationHours = notificationHours,
            floatingBubbleEnabled = floatingBubbleEnabled,
            batteryOptimizationPromptDismissed = syncState.batteryOptDismissed,
            batterySaverEnabled = syncState.batterySaverEnabled,
            isMoodleConnected = syncState.moodleStatus.first,
            moodleSiteUrl = syncState.moodleStatus.second,
            isSyncing = syncState.isSyncing,
            syncMessage = syncState.syncMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    private data class CombinedMoodleSyncState(
        val batteryOptDismissed: Boolean,
        val batterySaverEnabled: Boolean,
        val moodleStatus: Pair<Boolean, String?>,
        val isSyncing: Boolean,
        val syncMessage: String?
    )

    fun runSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = null
            val result = moodleSyncEngine.runFullSync()
            if (result.isSuccess) {
                val r = result.getOrThrow()
                _syncMessage.value = "Synced: ${r.insertedCount} new, ${r.updatedCount} updated, ${r.vanishedCount} vanished"
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Sync failed"
                _syncMessage.value = "Sync error: $err"
            }
            _isSyncing.value = false
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun updateCompanionType(type: CompanionType) {
        viewModelScope.launch {
            userPreferences.setCompanionType(type)
        }
    }

    fun updateCardStyle(style: String) {
        viewModelScope.launch {
            userPreferences.setCardStyle(style)
        }
    }

    fun updateNotificationHours(hours: Int) {
        viewModelScope.launch {
            userPreferences.setNotificationHoursBefore(hours)
            // Note: Trigger WorkManager to reschedule all notifications based on new preference
        }
    }

    fun setFloatingBubbleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setFloatingBubbleEnabled(enabled)
        }
    }

    fun dismissBatteryOptimizationPrompt() {
        viewModelScope.launch {
            userPreferences.setBatteryOptimizationPromptDismissed(true)
        }
    }

    fun setBatterySaverEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setBatterySaverEnabled(enabled)
        }
    }

    fun disconnectMoodle() {
        viewModelScope.launch {
            moodleApiClient.disconnect()
        }
    }
}
