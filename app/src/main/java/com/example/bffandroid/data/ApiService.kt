package com.example.bffandroid.data

import com.example.bffandroid.data.model.AppVersionResponse
import com.example.bffandroid.data.model.CountryLoginConfig
import com.example.bffandroid.data.model.GoogleAuthBody
import com.example.bffandroid.data.model.GoogleAuthResponse
import com.example.bffandroid.data.model.OtpRequestBody
import com.example.bffandroid.data.model.OtpRequestResponse
import com.example.bffandroid.data.model.OtpVerifyBody
import com.example.bffandroid.data.model.OtpVerifyResponse
import com.example.bffandroid.data.model.RechargeOptionsResponse
import com.example.bffandroid.data.model.RechargePurchaseBody
import com.example.bffandroid.data.model.RechargePurchaseResponse
import com.example.bffandroid.data.model.RechargeQuoteBody
import com.example.bffandroid.data.model.RechargeQuoteResponse
import com.example.bffandroid.data.model.WalletBalanceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

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
}
