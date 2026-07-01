package com.example.bffandroid.data

import com.example.bffandroid.data.model.RefreshTokenResponse
import com.example.bffandroid.utils.AppSession
import com.example.bffandroid.utils.Constant
import com.example.bffandroid.utils.TokenUtils
import com.google.gson.Gson
import android.util.Log
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val TAG = "RetrofitInstance"
    private const val BASE_URL = "https://api.gobff.app/api/v1/"
    private const val TIMEOUT_SECONDS = 5L
    private const val AUTHORIZATION_HEADER = "Authorization"
    private const val BEARER_PREFIX = "Bearer "
    private val jsonMediaType = "application/json".toMediaType()
    private val gson = Gson()
    private val refreshLock = Any()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val token = TokenUtils.getAccessToken()
        val shouldAttachToken = token.isNotBlank() &&
            request.header(AUTHORIZATION_HEADER).isNullOrBlank() &&
            !request.url.encodedPath.contains("/auth/")

        val authorizedRequest = if (shouldAttachToken) {
            request.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$token")
                .build()
        } else {
            request
        }
        chain.proceed(authorizedRequest)
    }

    private val tokenAuthenticator = Authenticator { _, response ->
        if (responseCount(response) >= 2 || response.request.url.encodedPath.contains("/auth/refresh")) {
            Log.w(TAG, "Authenticator refusing retry path=${response.request.url.encodedPath} count=${responseCount(response)}; clearing session")
            AppSession.logSnapshot("authenticator-before-clear")
            AppSession.clear()
            return@Authenticator null
        }

        val requestToken = response.request.header(AUTHORIZATION_HEADER)?.removePrefix(BEARER_PREFIX)
        val refreshedToken = refreshAccessToken(requestToken)
        if (refreshedToken.isNullOrBlank()) {
            null
        } else {
            response.request.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$refreshedToken")
                .build()
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun refreshAccessToken(requestToken: String?): String? {
        synchronized(refreshLock) {
            val currentToken = TokenUtils.getAccessToken()
            if (currentToken.isNotBlank() && currentToken != requestToken) {
                Log.d(TAG, "Using token refreshed by another request")
                return currentToken
            }

            val refreshToken = AppSession.getString(Constant.REFRESH_TOKEN_KEY).orEmpty()
            val installationId = AppSession.getString(Constant.INSTALLATION_ID_KEY).orEmpty()
            if (refreshToken.isBlank() || installationId.isBlank()) {
                Log.w(
                    TAG,
                    "Cannot refresh access token: refreshTokenMissing=${refreshToken.isBlank()} " +
                        "installationIdMissing=${installationId.isBlank()}; clearing session"
                )
                AppSession.logSnapshot("refreshAccessToken-missing-data")
                AppSession.clear()
                return null
            }

            val body = gson.toJson(
                mapOf(
                    "refreshToken" to refreshToken,
                    "installationId" to installationId
                )
            ).toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url("${BASE_URL}auth/refresh")
                .post(body)
                .header("Content-Type", "application/json")
                .build()

            return runCatching {
                refreshClient.newCall(request).execute().use { refreshResponse ->
                    val responseBody = refreshResponse.body?.string().orEmpty()
                    if (!refreshResponse.isSuccessful) {
                        Log.w(TAG, "Refresh failed status=${refreshResponse.code}; clearing session")
                        AppSession.logSnapshot("refreshAccessToken-http-${refreshResponse.code}")
                        if (refreshResponse.code == 400 || refreshResponse.code == 401 || refreshResponse.code == 403) {
                            AppSession.clear()
                        }
                        return@use null
                    }

                    val tokenResponse = gson.fromJson(responseBody, RefreshTokenResponse::class.java)
                    val accessToken = tokenResponse.accessToken?.takeIf { it.isNotBlank() }
                    if (accessToken == null) {
                        Log.w(TAG, "Refresh response missing access token; clearing session")
                        AppSession.logSnapshot("refreshAccessToken-missing-access")
                        AppSession.clear()
                        return@use null
                    }

                    TokenUtils.saveTokens(
                        accessToken = accessToken,
                        refreshToken = tokenResponse.refreshToken,
                        accessTokenExpiresAt = tokenResponse.accessTokenExpiresAt,
                        refreshTokenExpiresAt = tokenResponse.refreshTokenExpiresAt,
                        installationId = installationId
                    )
                    AppSession.putBoolean(Constant.IS_USER_LOGGED_IN, true)
                    AppSession.logSnapshot("refreshAccessToken-success")
                    accessToken
                }
            }.getOrElse {
                Log.e(TAG, "Refresh request failed; keeping existing session for retry", it)
                AppSession.logSnapshot("refreshAccessToken-exception-keep-session")
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var currentResponse: Response? = response
        var count = 1
        while (currentResponse?.priorResponse != null) {
            count++
            currentResponse = currentResponse.priorResponse
        }
        return count
    }
}
