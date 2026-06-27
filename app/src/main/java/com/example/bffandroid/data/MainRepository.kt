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
}
