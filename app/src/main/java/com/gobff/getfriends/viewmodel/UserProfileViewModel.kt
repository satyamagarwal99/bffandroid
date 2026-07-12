package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.UpdateProfileBody
import com.gobff.getfriends.data.model.UserProfileResponse
import com.gobff.getfriends.data.model.UserProfileUiState
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.Constant
import com.gobff.getfriends.utils.TokenUtils
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
        updateProfile(
            languages = languages,
            onComplete = onComplete
        )
    }

    fun saveVibes(vibes: Set<String>, onComplete: () -> Unit = {}) {
        updateProfile(
            vibes = vibes,
            onComplete = onComplete
        )
    }

    fun saveName(
        displayName: String,
        onComplete: () -> Unit = {}
    ) {
        updateProfile(
            displayName = displayName,
            onComplete = onComplete
        )
    }

    fun saveAvatar(
        avatarUrl: String,
        onComplete: () -> Unit = {}
    ) {
        updateProfile(
            avatarUrl = avatarUrl,
            onComplete = onComplete
        )
    }

    private fun updateProfile(
        displayName: String? = null,
        avatarUrl: String? = null,
        languages: Set<String>? = null,
        vibes: Set<String>? = null,
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
                errorMessage = null
            )

            val body = UpdateProfileBody(
                displayName = displayName?.trim()?.takeIf { it.isNotBlank() }?.let {
                    it
                } ?: if (displayName != null) Constant.DEFAULT_DISPLAY_NAME else null,
                avatarUrl = avatarUrl?.trim()?.takeIf { it.isNotBlank() },
                languages = languages?.toList(),
                vibes = vibes?.toList()
            )

            runCatching { mainRepository.updateProfile(token, body) }
                .onSuccess { response ->
                    val responseBody = response.body()
                    if (response.isSuccessful) {
                        val resolvedDisplayName =
                            responseBody?.displayName ?: displayName?.trim()?.takeIf { it.isNotBlank() } ?: uiState.displayName
                        val resolvedAvatarUrl =
                            responseBody?.avatarUrl ?: avatarUrl?.trim()?.takeIf { it.isNotBlank() } ?: uiState.avatarUrl
                        val resolvedGender = responseBody?.gender ?: uiState.gender
                        AppSession.setCurrentUserProfile(
                            displayName = resolvedDisplayName,
                            avatarUrl = resolvedAvatarUrl,
                            gender = resolvedGender
                        )
                        uiState = uiState.copy(
                            displayName = resolvedDisplayName,
                            gender = resolvedGender,
                            avatarUrl = resolvedAvatarUrl,
                            bio = responseBody?.bio ?: uiState.bio,
                            languages = responseBody?.languages?.normalizedSet() ?: (languages ?: uiState.languages),
                            vibes = responseBody?.vibes?.normalizedSet() ?: (vibes ?: uiState.vibes)
                        )
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
        AppSession.setCurrentUserProfile(
            displayName = profile.displayName,
            avatarUrl = profile.avatarUrl,
            gender = profile.gender
        )
        uiState = uiState.copy(
            displayName = profile.displayName,
            gender = profile.gender,
            avatarUrl = profile.avatarUrl,
            bio = profile.bio,
            voiceVerificationStatus = profile.voiceVerificationStatus,
            voiceVerificationRequired = profile.voiceVerificationRequired == true,
            voiceVerificationMethod = profile.voiceVerificationMethod,
            voiceVerificationCheckedAt = profile.voiceVerificationCheckedAt,
            voiceSampleReference = profile.voiceSampleReference,
            languages = profile.languages?.normalizedSet().orEmpty(),
            vibes = profile.vibes?.normalizedSet().orEmpty()
        )
    }

    fun shouldCompleteVoiceVerification(): Boolean {
        return uiState.voiceVerificationRequired && uiState.voiceVerificationStatus.isVoiceVerificationPending()
    }

    private fun hasRequiredProfileData(): Boolean {
        return !uiState.displayName.isNullOrBlank() && !uiState.avatarUrl.isNullOrBlank()
    }
}

private fun String?.isVoiceVerificationPending(): Boolean {
    return this?.trim()?.uppercase() == "PENDING"
}

private fun List<String>.normalizedSet(): Set<String> {
    return mapNotNull { value ->
        value.trim().takeIf { it.isNotBlank() }?.uppercase()
    }.toSet()
}
