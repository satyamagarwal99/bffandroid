package com.example.bffandroid.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bffandroid.data.MainRepository
import com.example.bffandroid.data.model.VoiceVerificationUiState
import com.example.bffandroid.utils.TokenUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class VoiceVerificationViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var uiState by mutableStateOf(VoiceVerificationUiState())
        private set

    init {
        loadVoiceVerificationStatus()
    }

    fun loadVoiceVerificationStatus() {
        if (uiState.isStatusLoading) return

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isStatusLoading = false,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isStatusLoading = true, errorMessage = null)
            runCatching { mainRepository.getVoiceVerificationStatus(token) }
                .onSuccess { response ->
                    val body = response.body()
                    uiState = uiState.copy(
                        isStatusLoading = false,
                        status = body?.status,
                        isVerified = body?.verified == true,
                        errorMessage = if (response.isSuccessful) {
                            null
                        } else {
                            body?.message ?: "Unable to load voice status"
                        }
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isStatusLoading = false,
                        errorMessage = error.message ?: "Unable to load voice status"
                    )
                }
        }
    }

    fun submitVoiceVerification(
        file: File?,
        onSuccess: () -> Unit
    ) {
        if (uiState.isSubmitting) return
        if (file == null || !file.exists()) {
            uiState = uiState.copy(errorMessage = "Please record your voice again")
            return
        }

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isSubmitting = false,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isSubmitting = true, errorMessage = null)

            val requestBody = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = file.name,
                body = requestBody
            )

            runCatching { mainRepository.submitVoiceVerification(token, filePart) }
                .onSuccess { response ->
                    val body = response.body()
                    if (response.isSuccessful) {
                        uiState = uiState.copy(
                            isSubmitting = false,
                            status = body?.status ?: uiState.status,
                            isVerified = body?.verified == true || uiState.isVerified,
                            errorMessage = null
                        )
                        onSuccess()
                    } else {
                        uiState = uiState.copy(
                            isSubmitting = false,
                            errorMessage = body?.message ?: "Unable to submit voice"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Unable to submit voice"
                    )
                }
        }
    }
}
