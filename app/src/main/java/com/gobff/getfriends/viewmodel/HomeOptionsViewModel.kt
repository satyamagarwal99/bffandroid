package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.LanguageOption
import com.gobff.getfriends.data.model.VibeOption
import com.gobff.getfriends.data.model.defaultLanguageOptions
import com.gobff.getfriends.data.model.defaultVibeOptions
import com.gobff.getfriends.data.model.toLanguageOption
import com.gobff.getfriends.data.model.toVibeOption
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.launch

data class HomeOptionsUiState(
    val isLoading: Boolean = false,
    val languageOptions: List<LanguageOption> = defaultLanguageOptions(),
    val vibeOptions: List<VibeOption> = defaultVibeOptions(),
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false
)

class HomeOptionsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var uiState by mutableStateOf(HomeOptionsUiState())
        private set

    fun loadHomeOptions(forceRefresh: Boolean = false) {
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
            runCatching { mainRepository.getHomeOptions(token) }
                .onSuccess { response ->
                    val body = response.body()
                    val languageOptions = body
                        ?.languages
                        .orEmpty()
                        .mapNotNull { it.toLanguageOption() }
                        .ifEmpty { defaultLanguageOptions() }
                    val vibeOptions = body
                        ?.vibes
                        .orEmpty()
                        .mapNotNull { it.toVibeOption() }
                        .ifEmpty { defaultVibeOptions() }

                    uiState = uiState.copy(
                        isLoading = false,
                        hasLoaded = true,
                        languageOptions = if (response.isSuccessful) languageOptions else uiState.languageOptions,
                        vibeOptions = if (response.isSuccessful) vibeOptions else uiState.vibeOptions,
                        errorMessage = if (response.isSuccessful) null else body?.message ?: "Unable to load home options"
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = error.message ?: "Unable to load home options"
                    )
                }
        }
    }
}
