package com.example.bffandroid.utils

object TokenUtils {
    fun getAccessToken(): String = AppSession.getString(Constant.ACCESS_TOKEN_KEY).orEmpty()

    fun getToken(): String {
        val accessToken = getAccessToken()
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
    }
}
