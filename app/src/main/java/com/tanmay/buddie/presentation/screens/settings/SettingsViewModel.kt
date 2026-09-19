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

data class SettingsUiState(
    val cardStyle: String = "COMPANION_HEADER",
    val companionType: CompanionType = CompanionType.BUDDIE,
    val notificationHours: Int = 24,
    val floatingBubbleEnabled: Boolean = false,
    val batteryOptimizationPromptDismissed: Boolean = false,
    val batterySaverEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.cardStyleFlow,
        userPreferences.companionTypeFlow,
        userPreferences.notificationHoursFlow,
        userPreferences.floatingBubbleEnabledFlow,
        combine(
            userPreferences.batteryOptimizationPromptDismissedFlow,
            userPreferences.batterySaverEnabledFlow
        ) { opt, saver -> opt to saver }
    ) { cardStyle, companionType, notificationHours, floatingBubbleEnabled, (batteryOptDismissed, batterySaverEnabled) ->
        SettingsUiState(
            cardStyle = cardStyle,
            companionType = companionType,
            notificationHours = notificationHours,
            floatingBubbleEnabled = floatingBubbleEnabled,
            batteryOptimizationPromptDismissed = batteryOptDismissed,
            batterySaverEnabled = batterySaverEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

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
}
