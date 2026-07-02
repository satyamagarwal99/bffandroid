package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.GameCatalogItemDto
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.launch

data class GameCatalogUiState(
    val isLoading: Boolean = false,
    val games: List<GameCatalogItemDto> = emptyList(),
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false
)

class GameCatalogViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var uiState by mutableStateOf(GameCatalogUiState())
        private set

    fun loadGameCatalog(forceRefresh: Boolean = false) {
        if (uiState.hasLoaded && !forceRefresh) return

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isLoading = false,
                    hasLoaded = true,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching { mainRepository.getGameCatalog(token) }
                .onSuccess { response ->
                    val body = response.body().orEmpty()
                    uiState = uiState.copy(
                        isLoading = false,
                        games = if (response.isSuccessful && body.isNotEmpty()) body else uiState.games,
                        hasLoaded = true,
                        errorMessage = if (response.isSuccessful) null else "Unable to load games"
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = error.message ?: "Unable to load games"
                    )
                }
        }
    }
}
