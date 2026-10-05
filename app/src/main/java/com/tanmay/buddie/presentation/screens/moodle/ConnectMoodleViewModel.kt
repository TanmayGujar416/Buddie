package com.tanmay.buddie.presentation.screens.moodle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanmay.buddie.data.remote.moodle.MoodleApiClient
import com.tanmay.buddie.data.remote.moodle.MoodleAuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectMoodleUiState(
    val siteUrl: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class ConnectMoodleEvent {
    object ConnectionSuccess : ConnectMoodleEvent()
}

@HiltViewModel
class ConnectMoodleViewModel @Inject constructor(
    private val moodleApiClient: MoodleApiClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectMoodleUiState())
    val uiState: StateFlow<ConnectMoodleUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ConnectMoodleEvent>()
    val events: SharedFlow<ConnectMoodleEvent> = _events.asSharedFlow()

    fun onSiteUrlChange(url: String) {
        _uiState.value = _uiState.value.copy(siteUrl = url, errorMessage = null)
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, errorMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun connect(siteUrl: String, username: String, password: String) {
        if (siteUrl.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your Moodle site URL")
            return
        }
        if (username.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your username")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = moodleApiClient.authenticate(
                siteUrl = siteUrl,
                username = username,
                password = password
            )

            when (result) {
                is MoodleAuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ConnectMoodleEvent.ConnectionSuccess)
                }
                is MoodleAuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
