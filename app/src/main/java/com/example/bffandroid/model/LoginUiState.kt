package com.example.bffandroid.model

data class LoginUiState(
    val countryIso: String = "IN",
    val loginCountry: CountryLoginConfig = CountryLoginConfig(
        countryIso = countryIso,
        dialCode = null,
        exampleNationalNumber = null,
        loginMethod = LoginMethod.MobileNumber
    ),
    val mobileNumber: String = "",
    val otpCode: String = "",
    val showOtp: Boolean = false,
    val isCountryConfigLoading: Boolean = true,
    val isOtpRequestLoading: Boolean = false,
    val isOtpVerifyLoading: Boolean = false,
    val otpDebugText: String? = null,
    val authStatusText: String? = null
)
