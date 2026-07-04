package com.gobff.getfriends.utils

import android.util.Log

object TokenUtils {
    private const val TAG = "TokenUtils"

    fun getAccessToken(): String = AppSession.getString(Constant.ACCESS_TOKEN_KEY).orEmpty()

    fun getToken(): String {
        val accessToken = getAccessToken()
        Log.d(TAG, "getToken accessToken=${redactedValue(accessToken)}")
        return if (accessToken.isEmpty()) {
            ""
        } else {
            "Bearer $accessToken"
        }
    }

    fun saveTokens(
        accessToken: String?,
        refreshToken: String?,
        accessTokenExpiresAt: String? = null,
        refreshTokenExpiresAt: String? = null,
        installationId: String? = null
    ) {
        Log.d(
            TAG,
            "saveTokens access=${redactedValue(accessToken)} " +
                "refresh=${redactedValue(refreshToken)} " +
                "accessExp=${accessTokenExpiresAt.orEmpty()} " +
                "refreshExp=${refreshTokenExpiresAt.orEmpty()} " +
                "installation=${redactedValue(installationId)}"
        )
        accessToken?.takeIf { it.isNotBlank() }?.let {
            AppSession.putString(Constant.ACCESS_TOKEN_KEY, it)
        }
        refreshToken?.takeIf { it.isNotBlank() }?.let {
            AppSession.putString(Constant.REFRESH_TOKEN_KEY, it)
        }
        accessTokenExpiresAt?.takeIf { it.isNotBlank() }?.let {
            AppSession.putString(Constant.ACCESS_TOKEN_EXPIRES_AT_KEY, it)
        }
        refreshTokenExpiresAt?.takeIf { it.isNotBlank() }?.let {
            AppSession.putString(Constant.REFRESH_TOKEN_EXPIRES_AT_KEY, it)
        }
        installationId?.takeIf { it.isNotBlank() }?.let {
            AppSession.putString(Constant.INSTALLATION_ID_KEY, it)
        }
        AppSession.logSnapshot("after-saveTokens")
    }

    fun hasStoredSession(): Boolean {
        val loggedIn = AppSession.getBoolean(Constant.IS_USER_LOGGED_IN)
        val accessToken = AppSession.getString(Constant.ACCESS_TOKEN_KEY).orEmpty()
        val refreshToken = AppSession.getString(Constant.REFRESH_TOKEN_KEY).orEmpty()
        val hasSession = loggedIn && (accessToken.isNotBlank() || refreshToken.isNotBlank())
        Log.d(
            TAG,
            "hasStoredSession=$hasSession loggedIn=$loggedIn " +
                "access=${redactedValue(accessToken)} refresh=${redactedValue(refreshToken)}"
        )
        return hasSession
    }

    fun getStoredFcmToken(): String = AppSession.getString(Constant.FCM_TOKEN_KEY).orEmpty()

    fun getLastSyncedFcmToken(): String = AppSession.getString(Constant.LAST_SYNCED_FCM_TOKEN_KEY).orEmpty()

    fun recordFetchedFcmToken(token: String, source: String) {
        val incomingToken = token.trim()
        val storedToken = getStoredFcmToken()
        Log.d(TAG, "FCM[$source] fetched.token=${incomingToken}")
        Log.d(TAG, "FCM[$source] stored.before=${redactedValue(storedToken)}")

        if (incomingToken.isBlank()) {
            Log.d(TAG, "FCM[$source] fetched.token is blank, skipping store")
            return
        }

        if (storedToken.isNotBlank() && storedToken != incomingToken) {
            Log.d(TAG, "FCM[$source] token.rotated old=${storedToken} new=${incomingToken}")
            AppSession.putString(Constant.PREVIOUS_FCM_TOKEN_KEY, storedToken)
        } else if (storedToken == incomingToken) {
            Log.d(TAG, "FCM[$source] token.unchanged current=${incomingToken}")
        } else {
            Log.d(TAG, "FCM[$source] token.firstSeen current=${incomingToken}")
        }

        AppSession.putString(Constant.FCM_TOKEN_KEY, incomingToken)
        Log.d(TAG, "FCM[$source] stored.after=${redactedValue(getStoredFcmToken())}")
        logFcmTokenState("after-fetch-$source")
    }

    fun recordSyncedFcmToken(token: String, source: String, responseCode: Int? = null) {
        val syncedToken = token.trim()
        if (syncedToken.isBlank()) {
            Log.d(TAG, "FCM[$source] sync skipped: blank token")
            return
        }

        val storedToken = getStoredFcmToken()
        val lastSyncedToken = getLastSyncedFcmToken()
        Log.d(TAG, "FCM[$source] sync.request=${syncedToken}")
        Log.d(TAG, "FCM[$source] sync.stored=${redactedValue(storedToken)}")
        Log.d(TAG, "FCM[$source] sync.lastSynced.before=${redactedValue(lastSyncedToken)}")

        AppSession.putString(Constant.LAST_SYNCED_FCM_TOKEN_KEY, syncedToken)
        if (storedToken.isNotBlank() && storedToken != syncedToken) {
            AppSession.putString(Constant.PREVIOUS_FCM_TOKEN_KEY, storedToken)
        }

        Log.d(
            TAG,
            "FCM[$source] sync.applied responseCode=${responseCode ?: -1} synced=${syncedToken}"
        )
        logFcmTokenState("after-sync-$source")
    }

    fun logFcmTokenState(reason: String) {
        val currentToken = getStoredFcmToken()
        val lastSyncedToken = getLastSyncedFcmToken()
        val previousToken = AppSession.getString(Constant.PREVIOUS_FCM_TOKEN_KEY).orEmpty()
        Log.d(TAG, "FCM[$reason] current=${currentToken}")
        Log.d(TAG, "FCM[$reason] lastSynced=${lastSyncedToken}")
        Log.d(TAG, "FCM[$reason] previous=${previousToken}")
    }

    private fun redactedValue(value: String?): String {
        if (value.isNullOrBlank()) return "missing"
        return "present(len=${value.length}, tail=${value.takeLast(4)})"
    }
}
