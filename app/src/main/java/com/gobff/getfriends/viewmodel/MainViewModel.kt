package com.gobff.getfriends.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.RefreshTokenBody
import com.gobff.getfriends.service.PresenceForegroundService
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.Constant
import com.gobff.getfriends.utils.OtpDeviceProvider
import com.gobff.getfriends.utils.PresenceHeartbeat
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val mainRepository = MainRepository()
    private val otpDeviceProvider = OtpDeviceProvider(application.applicationContext)
    private var presenceJob: Job? = null
    var userAvailableForCalls by mutableStateOf(true)
        private set

    fun onAppOpen() {
        AppSession.logSnapshot("MainViewModel.onAppOpen")
        userAvailableForCalls = true
        if (!TokenUtils.hasStoredSession()) {
            Log.d(TAG, "onAppOpen skipped: no stored session")
            return
        }
        if (PresenceHeartbeat.isAlwaysOnlineEnabled()) {
            stopForegroundHeartbeat()
            PresenceForegroundService.start(appContext)
            Log.d(TAG, "Foreground heartbeat skipped: always-online service is enabled")
            return
        }
        PresenceForegroundService.stop(appContext)
        startForegroundHeartbeat()
    }

    fun onAppClose() {
        AppSession.logSnapshot("MainViewModel.onAppClose")
        if (!TokenUtils.hasStoredSession()) {
            Log.d(TAG, "onAppClose skipped: no stored session")
            return
        }
        stopForegroundHeartbeat(markOffline = !PresenceHeartbeat.isAlwaysOnlineEnabled())
    }

    private fun startForegroundHeartbeat() {
        if (presenceJob?.isActive == true) return
        presenceJob = viewModelScope.launch {
            while (
                isActive &&
                userAvailableForCalls &&
                !PresenceHeartbeat.isAlwaysOnlineEnabled()
            ) {
                PresenceHeartbeat.updateOnline(mainRepository, online = true, tag = TAG)
                delay(PresenceHeartbeat.INTERVAL_MS)
            }
        }
    }

    fun updateUserAvailableForCalls(available: Boolean) {
        userAvailableForCalls = available
        if (!TokenUtils.hasStoredSession()) {
            Log.d(TAG, "Availability changed locally only: no stored session available=$available")
            return
        }

        if (available) {
            viewModelScope.launch {
                PresenceHeartbeat.updateOnline(mainRepository, online = true, tag = TAG)
            }
            if (PresenceHeartbeat.isAlwaysOnlineEnabled()) {
                stopForegroundHeartbeat()
                PresenceForegroundService.start(appContext)
            } else {
                PresenceForegroundService.stop(appContext)
                startForegroundHeartbeat()
            }
        } else {
            stopForegroundHeartbeat()
            viewModelScope.launch {
                PresenceHeartbeat.updateOnline(mainRepository, online = false, tag = TAG)
            }
        }
    }

    fun stopForegroundHeartbeat(markOffline: Boolean = false) {
        presenceJob?.cancel()
        presenceJob = null
        if (markOffline) {
            viewModelScope.launch {
                PresenceHeartbeat.updateOnline(mainRepository, online = false, tag = TAG)
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
