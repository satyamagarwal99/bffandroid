package com.gobff.getfriends.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.UpdateProfileBody
import com.gobff.getfriends.data.model.UpdateProfileUiState
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.Constant
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.launch

class OnboardingProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var uiState by mutableStateOf(UpdateProfileUiState())
        private set

    fun updateProfile(
        displayName: String,
        gender: String,
        avatarUrl: String,
        bio: String? = null,
        onSuccess: () -> Unit
    ) {
        if (uiState.isLoading) return

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val body = UpdateProfileBody(
                displayName = displayName.trim().ifBlank { Constant.DEFAULT_DISPLAY_NAME },
                gender = gender,
                avatarUrl = avatarUrl,
                bio = bio?.trim()?.takeIf { it.isNotBlank() }
            )

            runCatching { mainRepository.updateProfile(token, body) }
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        Log.d(
                            ONLINE_ONBOARDING_TAG,
                            "profileUpdateSuccess gender=$gender markOffline=true"
                        )
                        AppSession.putBoolean(Constant.USER_UNAVAILABLE_FOR_CALLS_KEY, true)
                        if (gender.equals("FEMALE", ignoreCase = true)) {
                            Log.d(ONLINE_ONBOARDING_TAG, "female profile created: marking pending")
                            AppSession.markFemaleOnlineOnboardingPending()
                        } else {
                            Log.d(ONLINE_ONBOARDING_TAG, "non-female profile created: no onboarding")
                        }
                        uiState = uiState.copy(isLoading = false, errorMessage = null)
                        onSuccess()
                    } else {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = response.body()?.message ?: "Unable to update profile"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to update profile"
                    )
                }
        }
    }

    private companion object {
        const val ONLINE_ONBOARDING_TAG = "FemaleOnlineOnboarding"
    }
}
