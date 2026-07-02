package com.gobff.getfriends.utils

import android.util.Log
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.PresenceRequestBody

object PresenceHeartbeat {
    const val INTERVAL_MS = 45_000L

    suspend fun updateOnline(
        repository: MainRepository,
        online: Boolean,
        tag: String
    ): Boolean {
        if (!TokenUtils.hasStoredSession()) {
            Log.d(tag, "Presence update skipped: no stored session online=$online")
            return false
        }

        val accessToken = TokenUtils.getToken()
        if (accessToken.isBlank()) {
            Log.w(tag, "Presence update skipped: missing access token online=$online")
            return false
        }

        return runCatching {
            val response = repository.updatePresence(
                accessToken = accessToken,
                body = PresenceRequestBody(online = online)
            )
            if (response.isSuccessful) {
                Log.d(tag, "Presence updated online=$online code=${response.code()}")
                true
            } else {
                Log.w(
                    tag,
                    "Presence update failed online=$online code=${response.code()} " +
                        "error=${response.errorBody()?.string().orEmpty()}"
                )
                false
            }
        }.onFailure { error ->
            Log.e(tag, "Presence update failed online=$online", error)
        }.getOrDefault(false)
    }

    fun isAlwaysOnlineEnabled(): Boolean =
        AppSession.getBoolean(Constant.ALWAYS_ONLINE_FOR_CALLS_KEY)

    fun setAlwaysOnlineEnabled(enabled: Boolean) {
        AppSession.putBoolean(Constant.ALWAYS_ONLINE_FOR_CALLS_KEY, enabled)
    }
}
