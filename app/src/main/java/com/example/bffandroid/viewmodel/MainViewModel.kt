package com.example.bffandroid.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bffandroid.data.MainRepository
import com.example.bffandroid.data.model.PresenceRequestBody
import com.example.bffandroid.data.model.RefreshTokenBody
import com.example.bffandroid.utils.AppSession
import com.example.bffandroid.utils.Constant
import com.example.bffandroid.utils.OtpDeviceProvider
import com.example.bffandroid.utils.TokenUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val mainRepository = MainRepository()
    private val otpDeviceProvider = OtpDeviceProvider(application.applicationContext)
    private var presenceJob: Job? = null
    private var lastPresenceState: Boolean? = null

    fun onAppOpen() {
        AppSession.logSnapshot("MainViewModel.onAppOpen")
        if (!TokenUtils.hasStoredSession()) {
            Log.d(TAG, "onAppOpen skipped: no stored session")
            return
        }
        markOnline()
    }

    fun onAppClose() {
        AppSession.logSnapshot("MainViewModel.onAppClose")
        if (!TokenUtils.hasStoredSession()) {
            Log.d(TAG, "onAppClose skipped: no stored session")
            return
        }
        markOffline()
    }

    private fun markOnline() = updatePresence(online = true)

    private fun markOffline() = updatePresence(online = false)

    private fun updatePresence(online: Boolean) {
        if (lastPresenceState == online && presenceJob?.isActive == true) {
            Log.d(TAG, "Presence update skipped: already sending online=$online")
            return
        }

        val accessToken = TokenUtils.getToken()
        if (accessToken.isBlank()) {
            Log.w(TAG, "Presence update skipped: missing access token online=$online")
            return
        }

        presenceJob = viewModelScope.launch {
            runCatching {
                val response = mainRepository.updatePresence(
                    accessToken = accessToken,
                    body = PresenceRequestBody(online = online)
                )
                if (response.isSuccessful) {
                    lastPresenceState = online
                    Log.d(TAG, "Presence updated online=$online code=${response.code()} body=${response.body()}")
                } else {
                    Log.w(
                        TAG,
                        "Presence update failed online=$online code=${response.code()} " +
                            "error=${response.errorBody()?.string().orEmpty()}"
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "Presence update failed online=$online", error)
                AppSession.logSnapshot("MainViewModel.presence.failure")
            }
        }
    }

    private fun refreshToken() {
        val refreshToken = AppSession.getString(Constant.REFRESH_TOKEN_KEY) ?: run {
            Log.d(TAG, "Skipping token refresh: no refresh token found")
            AppSession.logSnapshot("MainViewModel.refreshToken.no-refresh")
            return
        }
        val installationId = otpDeviceProvider.installationId()
        Log.d(TAG, "Refreshing token installationIdPresent=${installationId.isNotBlank()}")

        viewModelScope.launch {
            runCatching {
                val response = mainRepository.refreshToken(
                    body = RefreshTokenBody(
                        refreshToken = refreshToken,
                        installationId = installationId
                    )
                )
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    TokenUtils.saveTokens(
                        accessToken = body.accessToken,
                        refreshToken = body.refreshToken,
                        accessTokenExpiresAt = body.accessTokenExpiresAt,
                        refreshTokenExpiresAt = body.refreshTokenExpiresAt,
                        installationId = installationId
                    )
                    AppSession.putBoolean(Constant.IS_USER_LOGGED_IN, true)
                    Log.d(TAG, "Token refreshed successfully")
                } else {
                    Log.w(TAG, "Token refresh failed: code=${response.code()} bodyPresent=${body != null}")
                    AppSession.logSnapshot("MainViewModel.refreshToken.unsuccessful")
                }
            }.onFailure { error ->
                Log.e(TAG, "Token refresh failed", error)
                AppSession.logSnapshot("MainViewModel.refreshToken.failure")
            }
        }
    }

    private companion object {
        const val TAG = "MainViewModel"
    }
}
