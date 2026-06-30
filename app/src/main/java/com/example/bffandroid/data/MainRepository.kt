package com.example.bffandroid.data

import com.example.bffandroid.data.model.AppVersionResponse
import com.example.bffandroid.data.model.CountryLoginConfig
import com.example.bffandroid.data.model.GoogleAuthBody
import com.example.bffandroid.data.model.GoogleAuthResponse
import com.example.bffandroid.data.model.HomeOptionsResponse
import com.example.bffandroid.data.model.LogoutResponse
import com.example.bffandroid.data.model.OtpRequestBody
import com.example.bffandroid.data.model.OtpRequestResponse
import com.example.bffandroid.data.model.OtpVerifyBody
import com.example.bffandroid.data.model.OtpVerifyResponse
import com.example.bffandroid.data.model.RechargeOptionsResponse
import com.example.bffandroid.data.model.RechargePurchaseBody
import com.example.bffandroid.data.model.RechargePurchaseResponse
import com.example.bffandroid.data.model.RechargeQuoteBody
import com.example.bffandroid.data.model.RechargeQuoteResponse
import com.example.bffandroid.data.model.RefreshTokenBody
import com.example.bffandroid.data.model.RefreshTokenResponse
import com.example.bffandroid.data.model.UpdateProfileBody
import com.example.bffandroid.data.model.UpdateProfileResponse
import com.example.bffandroid.data.model.UserProfileResponse
import com.example.bffandroid.data.model.VoiceVerificationResponse
import com.example.bffandroid.data.model.VoiceVerificationStatusResponse
import com.example.bffandroid.data.model.WalletBalanceResponse
import okhttp3.MultipartBody
import retrofit2.Response

class MainRepository(
    private val apiService: ApiService = RetrofitInstance.apiService
) {
    suspend fun getCountryLoginConfig(countryIso: String): Response<CountryLoginConfig> =
        apiService.getCountryLoginConfig(countryIso)

    suspend fun requestOtp(body: OtpRequestBody): Response<OtpRequestResponse> =
        apiService.requestOtp(body)

    suspend fun verifyOtp(body: OtpVerifyBody): Response<OtpVerifyResponse> =
        apiService.verifyOtp(body)

    suspend fun authenticateWithGoogle(body: GoogleAuthBody): Response<GoogleAuthResponse> =
        apiService.authenticateWithGoogle(body)

    suspend fun refreshToken(body: RefreshTokenBody): Response<RefreshTokenResponse> =
        apiService.refreshToken(body)

    suspend fun logout(bearerToken: String): Response<LogoutResponse> =
        apiService.logout(bearerToken)

    suspend fun getAppVersion(platform: String, appVersion: String): Response<AppVersionResponse> =
        apiService.getAppVersion(platform, appVersion)

    suspend fun getWalletBalance(bearerToken: String): Response<WalletBalanceResponse> =
        apiService.getWalletBalance(bearerToken)

    suspend fun getRechargeOptions(bearerToken: String): Response<RechargeOptionsResponse> =
        apiService.getRechargeOptions(bearerToken)

    suspend fun getRechargeQuote(
        bearerToken: String,
        body: RechargeQuoteBody
    ): Response<RechargeQuoteResponse> = apiService.getRechargeQuote(bearerToken, body)

    suspend fun purchaseRecharge(
        bearerToken: String,
        idempotencyKey: String,
        body: RechargePurchaseBody
    ): Response<RechargePurchaseResponse> =
        apiService.purchaseRecharge(bearerToken, idempotencyKey, body)

    suspend fun updateProfile(
        bearerToken: String,
        body: UpdateProfileBody
    ): Response<UpdateProfileResponse> = apiService.updateProfile(bearerToken, body)

    suspend fun getProfile(
        bearerToken: String
    ): Response<UserProfileResponse> = apiService.getProfile(bearerToken)

    suspend fun getHomeOptions(
        bearerToken: String
    ): Response<HomeOptionsResponse> = apiService.getHomeOptions(bearerToken)

    suspend fun submitVoiceVerification(
        bearerToken: String,
        file: MultipartBody.Part
    ): Response<VoiceVerificationResponse> =
        apiService.submitVoiceVerification(bearerToken, file)

    suspend fun getVoiceVerificationStatus(
        bearerToken: String
    ): Response<VoiceVerificationStatusResponse> =
        apiService.getVoiceVerificationStatus(bearerToken)
}
