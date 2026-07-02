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
                    val status = body?.status
                    uiState = uiState.copy(
                        isStatusLoading = false,
                        status = status,
                        isVoiceRecorded = body?.isVoiceRecorded == true,
                        isVerified = body?.verified == true || status.isVoiceVerificationSuccessful(),
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
        onResult: (Boolean) -> Unit
    ) {
        if (uiState.isSubmitting) return
        if (file == null || !file.exists()) {
            uiState = uiState.copy(errorMessage = "Please record your voice again")
            onResult(false)
            return
        }

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isSubmitting = false,
                    errorMessage = "Login token missing"
                )
                onResult(false)
                return@launch
            }

            uiState = uiState.copy(isSubmitting = true, errorMessage = null)

            val requestBody = file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = file.nameWithoutExtension + ".mp3",
                body = requestBody
            )

            runCatching { mainRepository.submitVoiceVerification(token, filePart) }
                .onSuccess { response ->
                    val body = response.body()
                    if (response.isSuccessful) {
                        val status = body?.status
                        val isVerified = body?.verified == true || status.isVoiceVerificationSuccessful()
                        uiState = uiState.copy(
                            isSubmitting = false,
                            status = status ?: uiState.status,
                            isVoiceRecorded = body?.isVoiceRecorded == true || uiState.isVoiceRecorded,
                            isVerified = isVerified || uiState.isVerified,
                            errorMessage = null
                        )
                        onResult(isVerified)
                    } else {
                        uiState = uiState.copy(
                            isSubmitting = false,
                            errorMessage = body?.message ?: "Unable to submit voice"
                        )
                        onResult(false)
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Unable to submit voice"
                    )
                    onResult(false)
                }
        }
    }
}

private fun String?.isVoiceVerificationSuccessful(): Boolean {
    return when (this?.trim()?.uppercase()) {
        "SUCCESS",
        "SUCCESSFUL",
        "COMPLETED",
        "COMPLETE",
        "VERIFIED" -> true
        else -> false
    }
}
