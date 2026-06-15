package com.example.bffandroid.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bffandroid.model.CountryIsoProvider
import com.example.bffandroid.model.CountryLoginConfig
import com.example.bffandroid.model.LoginMethod
import com.example.bffandroid.model.LoginUiState
import com.example.bffandroid.model.AuthSessionStore
import com.example.bffandroid.model.OtpDeviceProvider
import com.example.bffandroid.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val countryIsoProvider = CountryIsoProvider(application.applicationContext)
    private val otpDeviceProvider = OtpDeviceProvider(application.applicationContext)
    private val authSessionStore = AuthSessionStore(application.applicationContext)

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
            val countryConfig = authRepository.getCountryLoginConfig(uiState.countryIso)
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
            Log.d(TAG, "Starting OTP request")
            uiState = uiState.copy(
                isOtpRequestLoading = true,
                authStatusText = null,
                otpDebugText = null,
                isAuthenticated = false
            )
            val result = authRepository.requestOtp(
                countryIso = uiState.countryIso,
                phoneNumber = phoneNumber,
                installationId = otpDeviceProvider.installationId()
            )
            Log.d(TAG, "OTP request completed: debugOtp=${result.debugOtp}, message=${result.message}")
            uiState = uiState.copy(
                showOtp = result.isSuccessful,
                isOtpRequestLoading = false,
                otpDebugText = result.debugOtp?.let { "OTP: $it" },
                authStatusText = when {
                    result.debugOtp != null -> "OTP sent"
                    result.isSuccessful -> result.message ?: "OTP sent"
                    else -> result.message ?: "Unable to request OTP"
                }
            )
        }
    }

    private fun verifyOtp() {
        val otp = uiState.otpCode.filter { it.isDigit() }
        if (otp.length < OTP_LENGTH) {
            uiState = uiState.copy(authStatusText = "Enter the 4 digit OTP")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Starting OTP verify")
            uiState = uiState.copy(isOtpVerifyLoading = true, authStatusText = null)
            val result = authRepository.verifyOtp(
                countryIso = uiState.countryIso,
                phoneNumber = uiState.mobileNumber,
                otp = otp,
                installationId = otpDeviceProvider.installationId()
            )
            Log.d(TAG, "OTP verify completed: verified=${result.isVerified}, message=${result.message}")
            if (result.isVerified) {
                authSessionStore.setLoggedIn(true)
            }
            uiState = uiState.copy(
                isOtpVerifyLoading = false,
                isAuthenticated = result.isVerified,
                authStatusText = result.message ?: if (result.isVerified) {
                    "OTP verified"
                } else {
                    "OTP verification failed"
                }
            )
        }
    }

    private fun authenticateWithGoogle() {
        viewModelScope.launch {
            Log.d(TAG, "Starting Google auth")
            uiState = uiState.copy(
                isGoogleAuthLoading = true,
                authStatusText = null,
                isAuthenticated = false
            )
            val result = authRepository.authenticateWithGoogle(
                countryIso = uiState.countryIso,
                installationId = otpDeviceProvider.installationId()
            )
            Log.d(TAG, "Google auth completed: success=${result.isSuccessful}, message=${result.message}")
            if (result.isSuccessful) {
                authSessionStore.setLoggedIn(true)
            }
            uiState = uiState.copy(
                isGoogleAuthLoading = false,
                isAuthenticated = result.isSuccessful,
                authStatusText = result.message ?: if (result.isSuccessful) {
                    "Signed in with Google"
                } else {
                    "Google sign-in failed"
                }
            )
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

    private companion object {
        const val TAG = "LoginViewModel"
        const val DEFAULT_COUNTRY_ISO = "IN"
        const val OTP_LENGTH = 4
    }
}
