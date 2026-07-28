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
import com.gobff.getfriends.utils.userFacingMessage
import kotlinx.coroutines.launch

class OnboardingProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()


    var uiState by mutableStateOf(UpdateProfileUiState())
        private set

    fun saveGender(
        gender: String,
        onSuccess: () -> Unit
    ) {
        updateProfile(
            body = UpdateProfileBody(gender = gender),
            onSuccess = {
                AppSession.setCurrentUserProfile(
                    displayName = AppSession.getCurrentUserDisplayName(),
                    avatarUrl = AppSession.getCurrentUserAvatarUrl(),
                    gender = it.gender ?: gender
                )
                onSuccess()
            }
        )
    }

    fun saveDisplayName(
        displayName: String,
        gender: String,
        onSuccess: () -> Unit
    ) {
        val resolvedDisplayName = displayName.trim().ifBlank { Constant.DEFAULT_DISPLAY_NAME }
        updateProfile(
            body = UpdateProfileBody(displayName = resolvedDisplayName),
            onSuccess = { response ->
                Log.d(
                    ONLINE_ONBOARDING_TAG,
                    "profileUpdateSuccess gender=$gender markOffline=true"
                )
                AppSession.putUserPersistentBoolean(Constant.USER_UNAVAILABLE_FOR_CALLS_KEY, true)
                if (gender.equals("FEMALE", ignoreCase = true)) {
                    Log.d(ONLINE_ONBOARDING_TAG, "female profile created: marking pending")
                    AppSession.markFemaleOnlineOnboardingPending()
                } else {
                    Log.d(ONLINE_ONBOARDING_TAG, "non-female profile created: no onboarding")
                }
                AppSession.setCurrentUserProfile(
                    displayName = response.displayName ?: resolvedDisplayName,
                    avatarUrl = response.avatarUrl ?: AppSession.getCurrentUserAvatarUrl(),
                    gender = response.gender ?: AppSession.getCurrentUserGender()
                )
                onSuccess()
            }
        )
    }

    private fun updateProfile(
        body: UpdateProfileBody,
        onSuccess: (com.gobff.getfriends.data.model.UpdateProfileResponse) -> Unit
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

            runCatching { mainRepository.updateProfile(token, body) }
                .onSuccess { response ->
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        uiState = uiState.copy(isLoading = false, errorMessage = null)
                        onSuccess(responseBody)
                    } else {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = responseBody?.message ?: "Unable to update profile"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.userFacingMessage("Unable to update profile")
                    )
                }
        }
    }

    private companion object {
        const val ONLINE_ONBOARDING_TAG = "FemaleOnlineOnboarding"
    }
}
