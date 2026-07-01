package com.example.bffandroid.utils

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

    private fun redactedValue(value: String?): String {
        if (value.isNullOrBlank()) return "missing"
        return "present(len=${value.length}, tail=${value.takeLast(4)})"
    }
}
