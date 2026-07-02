package com.gobff.getfriends.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.ConnectUserResponse
import com.gobff.getfriends.data.model.PresenceRequestBody
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.launch

data class ConnectUsersUiState(
    val isLoading: Boolean = false,
    val users: List<ConnectUserResponse> = emptyList(),
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false
)

class HomeScreenViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var connectUsersUiState by mutableStateOf(ConnectUsersUiState())
        private set

    fun markOnline() {
        updatePresence(online = true)
    }

    fun markOffline() {
        updatePresence(online = false)
    }

    private fun updatePresence(online: Boolean) {
        val accessToken = TokenUtils.getToken()
        viewModelScope.launch {
            runCatching {
                mainRepository.updatePresence(
                    accessToken = accessToken,
                    body = PresenceRequestBody(online = online)
                )
            }.onFailure { error ->
                Log.e(TAG, "Presence update failed (online=$online)", error)
            }
        }
    }

    fun loadConnectUsers(size: Int = 10, forceRefresh: Boolean = false) {
        if (connectUsersUiState.hasLoaded && !forceRefresh) return

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                connectUsersUiState = connectUsersUiState.copy(
                    isLoading = false,
                    users = emptyList(),
                    hasLoaded = true,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            connectUsersUiState = connectUsersUiState.copy(isLoading = true, errorMessage = null)
            runCatching { mainRepository.getConnectUsers(token, size) }
                .onSuccess { response ->
                    connectUsersUiState = connectUsersUiState.copy(
                        isLoading = false,
                        users = if (response.isSuccessful) response.body().orEmpty() else emptyList(),
                        hasLoaded = true,
                        errorMessage = if (response.isSuccessful) null else "Unable to load connect users"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Connect users load failed", error)
                    connectUsersUiState = connectUsersUiState.copy(
                        isLoading = false,
                        users = emptyList(),
                        hasLoaded = true,
                        errorMessage = error.message ?: "Unable to load connect users"
                    )
                }
        }
    }

    private companion object {
        const val TAG = "HomeScreenViewModel"
    }
}
