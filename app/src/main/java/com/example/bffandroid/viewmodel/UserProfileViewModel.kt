package com.example.bffandroid.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bffandroid.data.MainRepository
import com.example.bffandroid.data.model.UpdateProfileBody
import com.example.bffandroid.data.model.UserProfileResponse
import com.example.bffandroid.data.model.UserProfileUiState
import com.example.bffandroid.utils.Constant
import com.example.bffandroid.utils.TokenUtils
import kotlinx.coroutines.launch

class UserProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var uiState by mutableStateOf(UserProfileUiState())
        private set

    suspend fun refreshProfile(): Boolean {
        val token = TokenUtils.getToken()
        if (token.isBlank()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "Login token missing"
            )
            return false
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)
        return runCatching { mainRepository.getProfile(token) }
            .fold(
                onSuccess = { response ->
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        applyProfile(body)
                        uiState = uiState.copy(isLoading = false, errorMessage = null)
                        hasRequiredProfileData()
                    } else {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "Unable to load profile"
                        )
                        false
                    }
                },
                onFailure = { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load profile"
                    )
                    false
                }
            )
    }

    fun loadProfile() {
        if (uiState.isLoading) return
        viewModelScope.launch {
            refreshProfile()
        }
    }

    fun saveLanguages(languages: Set<String>, onComplete: () -> Unit = {}) {
        updateProfileSelection(languages = languages, vibes = uiState.vibes, onComplete = onComplete)
    }

    fun saveVibes(vibes: Set<String>, onComplete: () -> Unit = {}) {
        updateProfileSelection(languages = uiState.languages, vibes = vibes, onComplete = onComplete)
    }

    fun saveIdentity(
        displayName: String = uiState.displayName.orEmpty(),
        avatarUrl: String? = uiState.avatarUrl,
        onComplete: () -> Unit = {}
    ) {
        updateProfile(
            displayName = displayName,
            avatarUrl = avatarUrl,
            languages = uiState.languages,
            vibes = uiState.vibes,
            onComplete = onComplete
        )
    }

    private fun updateProfileSelection(
        languages: Set<String>,
        vibes: Set<String>,
        onComplete: () -> Unit
    ) {
        updateProfile(
            displayName = uiState.displayName.orEmpty(),
            avatarUrl = uiState.avatarUrl,
            languages = languages,
            vibes = vibes,
            onComplete = onComplete
        )
    }

    private fun updateProfile(
        displayName: String,
        avatarUrl: String?,
        languages: Set<String>,
        vibes: Set<String>,
        onComplete: () -> Unit
    ) {
        if (uiState.isSaving) return

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(errorMessage = "Login token missing")
                return@launch
            }

            uiState = uiState.copy(
                isSaving = true,
                errorMessage = null,
                displayName = displayName.trim().ifBlank { Constant.DEFAULT_DISPLAY_NAME },
                avatarUrl = avatarUrl,
                languages = languages,
                vibes = vibes
            )

            val body = UpdateProfileBody(
                displayName = displayName.trim().ifBlank { Constant.DEFAULT_DISPLAY_NAME },
                gender = uiState.gender,
                avatarUrl = avatarUrl,
                bio = uiState.bio?.trim()?.takeIf { it.isNotBlank() },
                languages = languages.toList(),
                vibes = vibes.toList()
            )

            runCatching { mainRepository.updateProfile(token, body) }
                .onSuccess { response ->
                    val responseBody = response.body()
                    if (response.isSuccessful) {
                        responseBody?.let {
                            uiState = uiState.copy(
                                displayName = it.displayName ?: uiState.displayName,
                                gender = it.gender ?: uiState.gender,
                                avatarUrl = it.avatarUrl ?: uiState.avatarUrl,
                                bio = it.bio ?: uiState.bio,
                                languages = it.languages?.normalizedSet() ?: languages,
                                vibes = it.vibes?.normalizedSet() ?: vibes
                            )
                        }
                        uiState = uiState.copy(isSaving = false, errorMessage = null)
                        onComplete()
                    } else {
                        uiState = uiState.copy(
                            isSaving = false,
                            errorMessage = responseBody?.message ?: "Unable to update profile"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Unable to update profile"
                    )
                }
        }
    }

    private fun applyProfile(profile: UserProfileResponse) {
        uiState = uiState.copy(
            displayName = profile.displayName,
            gender = profile.gender,
            avatarUrl = profile.avatarUrl,
            bio = profile.bio,
            languages = profile.languages?.normalizedSet().orEmpty(),
            vibes = profile.vibes?.normalizedSet().orEmpty()
        )
    }

    private fun hasRequiredProfileData(): Boolean {
        return !uiState.displayName.isNullOrBlank() && !uiState.avatarUrl.isNullOrBlank()
    }
}

private fun List<String>.normalizedSet(): Set<String> {
    return mapNotNull { value ->
        value.trim().takeIf { it.isNotBlank() }?.uppercase()
    }.toSet()
}
