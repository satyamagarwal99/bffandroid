package com.gobff.getfriends.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.FriendListUserResponse
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.launch

data class FriendsListUiState(
    val isLoading: Boolean = false,
    val friends: List<FriendListUserResponse> = emptyList(),
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false
)

class FriendsListViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var uiState by mutableStateOf(FriendsListUiState())
        private set

    fun loadFriends(forceRefresh: Boolean = false) {
        if (uiState.hasLoaded && !forceRefresh) return

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isLoading = false,
                    friends = emptyList(),
                    hasLoaded = true,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching { mainRepository.getMyFriends(token) }
                .onSuccess { response ->
                    uiState = uiState.copy(
                        isLoading = false,
                        friends = if (response.isSuccessful) response.body().orEmpty() else emptyList(),
                        hasLoaded = true,
                        errorMessage = if (response.isSuccessful) null else "Unable to load friends"
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Friend list load failed", error)
                    uiState = uiState.copy(
                        isLoading = false,
                        friends = emptyList(),
                        hasLoaded = true,
                        errorMessage = error.message ?: "Unable to load friends"
                    )
                }
        }
    }

    private companion object {
        const val TAG = "FriendsListViewModel"
    }
}

