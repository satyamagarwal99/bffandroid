package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.CallHistoryItemResponse
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CallHistoryUiState(
    val isLoading: Boolean = false,
    val calls: List<CallHistoryItemResponse> = emptyList(),
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false
)

class CallHistoryViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var uiState by mutableStateOf(CallHistoryUiState())
        private set

    fun loadCallHistory(size: Int = 20, forceRefresh: Boolean = false) {
        if (uiState.hasLoaded && !forceRefresh) return

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isLoading = false,
                    calls = emptyList(),
                    hasLoaded = true,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching { mainRepository.getCallHistory(token, size) }
                .onSuccess { response ->
                    val body = response.body()
                    uiState = uiState.copy(
                        isLoading = false,
                        calls = if (response.isSuccessful && body != null) body else emptyList(),
                        hasLoaded = true,
                        errorMessage = if (response.isSuccessful) null else "Unable to load call history"
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        calls = emptyList(),
                        hasLoaded = true,
                        errorMessage = error.message ?: "Unable to load call history"
                    )
                }
        }
    }
}

val CallHistoryItemResponse.displayCallerName: String
    get() = displayName?.takeIf { it.isNotBlank() } ?: "Unknown"

val CallHistoryItemResponse.displayTimestamp: String
    get() = startedAt.toDisplayDate()

val CallHistoryItemResponse.displayDuration: String
    get() {
        val seconds = durationSeconds ?: return "--"
        if (seconds <= 0) return "0s"

        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return when {
            minutes > 0 && remainingSeconds > 0 -> "${minutes}m ${remainingSeconds}s"
            minutes > 0 -> "${minutes}m"
            else -> "${remainingSeconds}s"
        }
    }

val CallHistoryItemResponse.callTypeLabel: String
    get() = when (roomType) {
        "ONE_TO_ONE_AUDIO_CALL" -> "Audio call"
        "ONE_TO_ONE_VIDEO_CALL" -> "Video call"
        "GROUP_AUDIO_ROOM" -> "Audio room"
        "GROUP_VIDEO_ROOM" -> "Video room"
        else -> "Call"
    }

private val callHistoryDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM, h:mm a", Locale.ENGLISH)

private fun String?.toDisplayDate(): String {
    if (isNullOrBlank()) return ""
    return runCatching {
        Instant.parse(this)
            .atZone(ZoneId.systemDefault())
            .format(callHistoryDateFormatter)
    }.getOrElse {
        this
    }
}
