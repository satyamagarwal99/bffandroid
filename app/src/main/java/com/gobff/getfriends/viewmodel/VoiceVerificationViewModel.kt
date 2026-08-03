package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.VoiceVerificationUiState
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.Constant
import com.gobff.getfriends.utils.TokenUtils
import com.gobff.getfriends.utils.userFacingMessage
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

    private fun markVoiceVerifiedIfNeeded(isVerified: Boolean) {
        if (isVerified) {
            AppSession.putUserPersistentBoolean(Constant.VOICE_VERIFICATION_COMPLETED_KEY, true)
        }
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
                    val isVerified = body?.verified == true || status.isVoiceVerificationSuccessful()
                    val isFailed = status.isVoiceVerificationFailed()
                    markVoiceVerifiedIfNeeded(isVerified)
                    uiState = uiState.copy(
                        isStatusLoading = false,
                        status = status,
                        isVoiceRecorded = body?.isVoiceRecorded == true,
                        isVerified = isVerified,
                        errorMessage = when {
                            !response.isSuccessful -> body?.message ?: "Unable to load voice status"
                            isVerified || status.isVoiceVerificationPending() -> null
                            isFailed -> body?.message ?: VOICE_VERIFICATION_FAILED_MESSAGE
                            else -> null
                        }
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isStatusLoading = false,
                        errorMessage = error.userFacingMessage("Unable to load voice status")
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
                        val isFailed = status.isVoiceVerificationFailed()
                        markVoiceVerifiedIfNeeded(isVerified)
                        uiState = uiState.copy(
                            isSubmitting = false,
                            status = status ?: uiState.status,
                            isVoiceRecorded = body?.isVoiceRecorded == true ||
                                uiState.isVoiceRecorded ||
                                status.isVoiceVerificationPending(),
                            isVerified = isVerified || uiState.isVerified,
                            errorMessage = when {
                                isVerified || status.isVoiceVerificationPending() -> null
                                isFailed -> body?.message ?: VOICE_VERIFICATION_FAILED_MESSAGE
                                else -> uiState.errorMessage
                            }
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
                        errorMessage = error.userFacingMessage("Unable to submit voice")
                    )
                    onResult(false)
                }
        }
    }

    fun refreshVoiceVerificationStatus(
        onVerified: () -> Unit = {},
        onFailed: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(errorMessage = "Login token missing")
                onFailed()
                return@launch
            }

            runCatching { mainRepository.getVoiceVerificationStatus(token) }
                .onSuccess { response ->
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        val status = body.status
                        val isVerified = body.verified == true || status.isVoiceVerificationSuccessful()
                        val isFailed = status.isVoiceVerificationFailed()
                        markVoiceVerifiedIfNeeded(isVerified)
                        uiState = uiState.copy(
                            status = status ?: uiState.status,
                            isVoiceRecorded = body.isVoiceRecorded == true || uiState.isVoiceRecorded,
                            isVerified = isVerified || uiState.isVerified,
                            errorMessage = when {
                                isVerified || status.isVoiceVerificationPending() -> null
                                isFailed -> body.message ?: VOICE_VERIFICATION_FAILED_MESSAGE
                                else -> uiState.errorMessage
                            }
                        )
                        when {
                            isVerified -> onVerified()
                            isFailed -> onFailed()
                        }
                    } else {
                        uiState = uiState.copy(
                            errorMessage = body?.message ?: "Unable to load voice status"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        errorMessage = error.userFacingMessage("Unable to load voice status")
                    )
                }
        }
    }

}

private fun String?.isVoiceVerificationPending(): Boolean {
    return this?.trim()?.uppercase() == "PENDING"
}

private fun String?.isVoiceVerificationFailed(): Boolean {
    return when (this?.trim()?.uppercase()) {
        "FAILED",
        "FAIL",
        "REJECTED",
        "DECLINED",
        "VOICE_VERIFICATION_FAILED",
        "VOICE_VERIFICATION_FAIL" -> true
        else -> false
    }
}

private fun String?.isVoiceVerificationSuccessful(): Boolean {
    return when (this?.trim()?.uppercase()) {
        "SUCCESS",
        "SUCCESSFUL",
        "COMPLETED",
        "COMPLETE",
        "PASSED",
        "PASS",
        "APPROVED",
        "ACCEPTED",
        "MATCHED",
        "VOICE_VERIFICATION_PASSED",
        "VOICE_VERIFICATION_PASS",
        "VERIFIED" -> true
        else -> false
    }
}

private const val VOICE_VERIFICATION_FAILED_MESSAGE = "We couldn't recognize your voice. Please try again."
