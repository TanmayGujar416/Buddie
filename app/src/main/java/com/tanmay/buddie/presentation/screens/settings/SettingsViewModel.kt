package com.tanmay.buddie.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val cardStyle: String = "SPEECH_BUBBLE",
    val notificationHours: Int = 24,
    val floatingBubbleEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.cardStyleFlow,
        userPreferences.notificationHoursFlow,
        userPreferences.floatingBubbleEnabledFlow
    ) { cardStyle, notificationHours, floatingBubbleEnabled ->
        SettingsUiState(
            cardStyle = cardStyle,
            notificationHours = notificationHours,
            floatingBubbleEnabled = floatingBubbleEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

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
}
