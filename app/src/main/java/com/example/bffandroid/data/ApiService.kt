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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface ApiService {
    @GET("auth/countries/{countryIso}")
    suspend fun getCountryLoginConfig(
        @Path("countryIso") countryIso: String
    ): Response<CountryLoginConfig>

    @POST("auth/otp/request")
    suspend fun requestOtp(
        @Body body: OtpRequestBody
    ): Response<OtpRequestResponse>

    @POST("auth/otp/verify")
    suspend fun verifyOtp(
        @Body body: OtpVerifyBody
    ): Response<OtpVerifyResponse>

    @POST("auth/google")
    suspend fun authenticateWithGoogle(
        @Body body: GoogleAuthBody
    ): Response<GoogleAuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body body: RefreshTokenBody
    ): Response<RefreshTokenResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") bearerToken: String
    ): Response<LogoutResponse>

    @GET("app-version/{platform}/{appVersion}")
    suspend fun getAppVersion(
        @Path("platform") platform: String,
        @Path("appVersion") appVersion: String
    ): Response<AppVersionResponse>

    @GET("wallet/balance")
    suspend fun getWalletBalance(
        @Header("Authorization") bearerToken: String
    ): Response<WalletBalanceResponse>

    @GET("wallet/recharge/options")
    suspend fun getRechargeOptions(
        @Header("Authorization") bearerToken: String
    ): Response<RechargeOptionsResponse>

    @POST("wallet/recharge/quote")
    suspend fun getRechargeQuote(
        @Header("Authorization") bearerToken: String,
        @Body body: RechargeQuoteBody
    ): Response<RechargeQuoteResponse>

    @POST("wallet/recharge/purchase")
    suspend fun purchaseRecharge(
        @Header("Authorization") bearerToken: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: RechargePurchaseBody
    ): Response<RechargePurchaseResponse>

    @PUT("home/profile")
    suspend fun updateProfile(
        @Header("Authorization") bearerToken: String,
        @Body body: UpdateProfileBody
    ): Response<UpdateProfileResponse>

    @GET("home/profile")
    suspend fun getProfile(
        @Header("Authorization") bearerToken: String
    ): Response<UserProfileResponse>

    @GET("home/options")
    suspend fun getHomeOptions(
        @Header("Authorization") bearerToken: String
    ): Response<HomeOptionsResponse>

    @Multipart
    @POST("home/profile/voice-verification")
    suspend fun submitVoiceVerification(
        @Header("Authorization") bearerToken: String,
        @Part file: MultipartBody.Part
    ): Response<VoiceVerificationResponse>

    @GET("home/profile/voice-verification")
    suspend fun getVoiceVerificationStatus(
        @Header("Authorization") bearerToken: String
    ): Response<VoiceVerificationStatusResponse>
}
