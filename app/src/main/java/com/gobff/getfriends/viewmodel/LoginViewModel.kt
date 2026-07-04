package com.gobff.getfriends.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.CountryIsoProvider
import com.gobff.getfriends.data.model.CountryLoginConfig
import com.gobff.getfriends.data.model.DeviceInfo
import com.gobff.getfriends.data.model.GoogleAuthBody
import com.gobff.getfriends.data.model.LoginMethod
import com.gobff.getfriends.data.model.LoginUiState
import com.gobff.getfriends.data.model.OtpRequestBody
import com.gobff.getfriends.data.model.OtpVerifyBody
import com.gobff.getfriends.data.model.UpdateFcmTokenBody
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.Constant
import com.gobff.getfriends.utils.OtpDeviceProvider
import com.gobff.getfriends.utils.TokenUtils
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()
    private val countryIsoProvider = CountryIsoProvider(application.applicationContext)
    private val otpDeviceProvider = OtpDeviceProvider(application.applicationContext)

    var uiState by mutableStateOf(createInitialState())
        private set

    init {
        Log.d(TAG, "LoginViewModel created with initial countryIso=${uiState.countryIso}, method=${uiState.loginCountry.loginMethod}")
        restoreSession()
    }

    fun onMobileNumberChange(mobileNumber: String) {
        uiState = uiState.copy(mobileNumber = mobileNumber)
    }

    fun onOtpCodeChange(otpCode: String) {
        uiState = uiState.copy(otpCode = otpCode.filter { it.isDigit() }.take(OTP_LENGTH))
    }

    fun onLoginClick() {
        Log.d(TAG, "Login clicked with method=${uiState.loginCountry.loginMethod}")
        if (uiState.loginCountry.loginMethod == LoginMethod.MobileNumber) {
            requestOtp()
        }
    }

    fun onContinueClick() {
        Log.d(TAG, "Continue clicked")
        verifyOtp()
    }

    fun onGoogleSignInClick() {
        Log.d(TAG, "Google sign-in clicked")
        authenticateWithGoogle()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            Log.d(TAG, "Loading country login config for ${uiState.countryIso}")
            uiState = uiState.copy(isCountryConfigLoading = true)
            val countryConfig = runCatching {
                val normalizedIso = uiState.countryIso.uppercase()
                val response = mainRepository.getCountryLoginConfig(normalizedIso)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    val countryIso = body.countryIso.ifBlank { normalizedIso }
                    val hasMobileLogin = body.loginMethods
                        ?.any { it.method.equals("MOBILE_NUMBER", ignoreCase = true) } == true
                    CountryLoginConfig(
                        countryIso = countryIso,
                        dialCode = body.dialCode,
                        exampleNationalNumber = body.exampleNationalNumber,
                        loginMethod = if (
                            countryIso.equals(DEFAULT_COUNTRY_ISO, ignoreCase = true) &&
                            hasMobileLogin
                        ) {
                            LoginMethod.MobileNumber
                        } else {
                            LoginMethod.Google
                        },
                        loginMethods = body.loginMethods
                    )
                } else {
                    fallbackCountryLoginConfig(normalizedIso)
                }
            }.getOrElse {
                fallbackCountryLoginConfig(uiState.countryIso.uppercase())
            }
            Log.d(TAG, "Country config loaded: countryIso=${countryConfig.countryIso}, method=${countryConfig.loginMethod}")
            uiState = uiState.copy(
                countryIso = countryConfig.countryIso,
                loginCountry = countryConfig,
                showOtp = uiState.showOtp && countryConfig.loginMethod == LoginMethod.MobileNumber,
                isCountryConfigLoading = false
            )
        }
    }


    private fun requestOtp() {
        val phoneNumber = uiState.mobileNumber.filter { it.isDigit() }
        if (phoneNumber.isBlank()) {
            uiState = uiState.copy(authStatusText = "Enter your phone number")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isOtpRequestLoading = true,
                authStatusText = null
            )

            val body = OtpRequestBody(
                countryIso2 = uiState.countryIso.uppercase(),
                phoneNumber = phoneNumber,
                installationId = otpDeviceProvider.installationId(),
                deviceType = Constant.DEVICE_PLATFORM
            )

            runCatching { mainRepository.requestOtp(body) }
                .onSuccess { response ->
                    if (response.isSuccessful && response.body() != null) {
                        uiState = uiState.copy(
                            isOtpRequestLoading = false,
                            showOtp = true,
                            authStatusText = "OTP sent to ${response.body()!!.phoneE164}"
                        )
                    } else {
                        uiState = uiState.copy(
                            isOtpRequestLoading = false,
                            showOtp = false,
                            authStatusText = "Unable to request OTP"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isOtpRequestLoading = false,
                        showOtp = false,
                        authStatusText = error.message ?: "Unable to request OTP"
                    )
                }
        }
    }
    private fun verifyOtp() {
        val otp = uiState.otpCode.filter { it.isDigit() }
        if (otp.length < OTP_LENGTH) {
            uiState = uiState.copy(authStatusText = "Enter the $OTP_LENGTH digit OTP")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isOtpVerifyLoading = true,
                authStatusText = null
            )
            val fcmToken = getCurrentFcmTokenOrNull()
            if (!fcmToken.isNullOrBlank()) {
                TokenUtils.recordFetchedFcmToken(fcmToken, "LoginViewModel.otp")
            }

            val body = OtpVerifyBody(
                countryIso2 = uiState.countryIso.uppercase(),
                phoneNumber = uiState.mobileNumber.filter { it.isDigit() },
                otp = otp,
                device = DeviceInfo(
                    installationId = otpDeviceProvider.installationId(),
                    platform = Constant.DEVICE_PLATFORM,
                    deviceBrand = Build.BRAND.orEmpty().ifBlank { "Android" },
                    deviceModel = Build.MODEL.orEmpty().ifBlank { "Device" },
                    osVersion = Build.VERSION.RELEASE.orEmpty().ifBlank { "unknown" },
                    appVersion = Constant.APP_VERSION,
                    fcmToken = fcmToken
                ),
                displayName = Constant.DEFAULT_DISPLAY_NAME,
                dateOfBirth = Constant.DEFAULT_DATE_OF_BIRTH
            )

            runCatching { mainRepository.verifyOtp(body) }
                .onSuccess { response ->
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        AppSession.putBoolean(Constant.IS_USER_LOGGED_IN, true)
                        TokenUtils.saveTokens(
                            accessToken = responseBody.accessToken,
                            refreshToken = responseBody.refreshToken,
                            accessTokenExpiresAt = responseBody.accessTokenExpiresAt,
                            refreshTokenExpiresAt = responseBody.refreshTokenExpiresAt,
                            installationId = otpDeviceProvider.installationId()
                        )
                        syncFcmTokenAfterLogin(responseBody.accessToken, fcmToken)
                        AppSession.logSnapshot("LoginViewModel.otp.success")
                        uiState = uiState.copy(
                            isOtpVerifyLoading = false,
                            isAuthenticated = true,
                            authStatusText = "OTP verified"
                        )
                    } else {
                        uiState = uiState.copy(
                            isOtpVerifyLoading = false,
                            isAuthenticated = false,
                            authStatusText = "OTP verification failed"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isOtpVerifyLoading = false,
                        isAuthenticated = false,
                        authStatusText = error.message ?: "OTP verification failed"
                    )
                }
        }
    }

    private fun authenticateWithGoogle() {
        val installationId = otpDeviceProvider.installationId()
        viewModelScope.launch {
            Log.d(TAG, "Starting Google auth")
            uiState = uiState.copy(
                isGoogleAuthLoading = true,
                authStatusText = null,
                isAuthenticated = false
            )
            val fcmToken = getCurrentFcmTokenOrNull()
            if (!fcmToken.isNullOrBlank()) {
                TokenUtils.recordFetchedFcmToken(fcmToken, "LoginViewModel.google")
            }
            val body = GoogleAuthBody(
                countryIso2 = uiState.countryIso.uppercase(),
                idToken = "dev-google:${otpDeviceProvider.installationId()}",
                device = DeviceInfo(
                    installationId = otpDeviceProvider.installationId(),
                    platform = Constant.DEVICE_PLATFORM,
                    deviceBrand = Build.BRAND.orEmpty().ifBlank { "Android" },
                    deviceModel = Build.MODEL.orEmpty().ifBlank { "Device" },
                    osVersion = Build.VERSION.RELEASE.orEmpty().ifBlank { "unknown" },
                    appVersion = Constant.APP_VERSION,
                    fcmToken = fcmToken
                ),
                displayName = Constant.DEFAULT_DISPLAY_NAME,
                dateOfBirth = Constant.DEFAULT_DATE_OF_BIRTH
            )
            runCatching { mainRepository.authenticateWithGoogle(body) }
                .onSuccess { response ->
                    val responseBody = response.body()
                    val isSuccessful = response.isSuccessful &&
                        responseBody != null &&
                        (responseBody.success == true || responseBody.verified == true || !responseBody.accessToken.isNullOrBlank())
                    if (isSuccessful) {
                        AppSession.putBoolean(Constant.IS_USER_LOGGED_IN, true)
                        TokenUtils.saveTokens(
                            accessToken = responseBody?.accessToken,
                            refreshToken = responseBody?.refreshToken,
                            accessTokenExpiresAt = responseBody?.accessTokenExpiresAt,
                            refreshTokenExpiresAt = responseBody?.refreshTokenExpiresAt,
                            installationId = installationId
                        )
                        syncFcmTokenAfterLogin(responseBody?.accessToken, fcmToken)
                        AppSession.logSnapshot("LoginViewModel.google.success")
                    }
                    uiState = uiState.copy(
                        isGoogleAuthLoading = false,
                        isAuthenticated = isSuccessful,
                        authStatusText = responseBody?.message ?: if (isSuccessful) {
                            "Signed in with Google"
                        } else {
                            "Google sign-in failed"
                        }
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isGoogleAuthLoading = false,
                        isAuthenticated = false,
                        authStatusText = error.message ?: "Google sign-in failed"
                    )
                }
        }
    }

    private fun createInitialState(): LoginUiState {
        val countryIso = countryIsoProvider.detectCountryIso2()
        return LoginUiState(
            countryIso = countryIso,
            loginCountry = CountryLoginConfig(
                countryIso = countryIso,
                dialCode = null,
                exampleNationalNumber = null,
                loginMethod = LoginMethod.MobileNumber
            ),
            isCountryConfigLoading = true
        )
    }

    private suspend fun syncFcmTokenAfterLogin(accessToken: String?, knownFcmToken: String? = null) {
        val bearerToken = accessToken
            ?.takeIf { it.isNotBlank() }
            ?.let { "Bearer $it" }
            ?: TokenUtils.getToken()
        if (bearerToken.isBlank()) {
            Log.w(TAG, "Skipping FCM token update: access token missing")
            return
        }

        val fcmToken = knownFcmToken ?: getCurrentFcmTokenOrNull()
        if (fcmToken.isNullOrBlank()) {
            Log.w(TAG, "Skipping FCM token update: FCM token missing")
            return
        }

        runCatching {
            mainRepository.updateFcmToken(
                bearerToken = bearerToken,
                body = UpdateFcmTokenBody(fcmToken = fcmToken)
            )
        }.onSuccess { response ->
            if (response.isSuccessful) {
                Log.d(TAG, "FCM token updated after login")
                TokenUtils.recordSyncedFcmToken(
                    token = fcmToken,
                    source = "LoginViewModel",
                    responseCode = response.code()
                )
            } else {
                Log.w(TAG, "FCM token update failed status=${response.code()}")
            }
        }.onFailure { error ->
            Log.w(TAG, "FCM token update failed", error)
        }
    }

    private suspend fun getCurrentFcmTokenOrNull(): String? {
        return runCatching { fetchFcmToken() }
            .onFailure { Log.w(TAG, "Unable to fetch FCM token", it) }
            .getOrNull()
    }

    private suspend fun fetchFcmToken(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (continuation.isActive) {
                    continuation.resume(token)
                }
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
    }

    private companion object {
        const val TAG = "LoginViewModel"
        const val DEFAULT_COUNTRY_ISO = "IN"
        const val OTP_LENGTH = 4
    }

    private fun fallbackCountryLoginConfig(countryIso: String): CountryLoginConfig {
        return CountryLoginConfig(
            countryIso = countryIso,
            dialCode = null,
            exampleNationalNumber = null,
            loginMethod = if (countryIso.equals(DEFAULT_COUNTRY_ISO, ignoreCase = true)) {
                LoginMethod.MobileNumber
            } else {
                LoginMethod.Google
            }
        )
    }
}
