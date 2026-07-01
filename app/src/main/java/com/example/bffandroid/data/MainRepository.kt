package com.example.bffandroid.data

import com.example.bffandroid.data.model.AppVersionResponse
import com.example.bffandroid.data.model.CountryLoginConfig
import com.example.bffandroid.data.model.CreateRoomBody
import com.example.bffandroid.data.model.GoogleAuthBody
import com.example.bffandroid.data.model.GoogleAuthResponse
import com.example.bffandroid.data.model.HomeOptionsResponse
import com.example.bffandroid.data.model.JoinRoomBody
import com.example.bffandroid.data.model.LogoutResponse
import com.example.bffandroid.data.model.OtpRequestBody
import com.example.bffandroid.data.model.OtpRequestResponse
import com.example.bffandroid.data.model.OtpVerifyBody
import com.example.bffandroid.data.model.OtpVerifyResponse
import com.example.bffandroid.data.model.PresenceRequestBody
import com.example.bffandroid.data.model.PresenceResponse
import com.example.bffandroid.data.model.RechargeOptionsResponse
import com.example.bffandroid.data.model.RechargePurchaseBody
import com.example.bffandroid.data.model.RechargePurchaseResponse
import com.example.bffandroid.data.model.RechargeQuoteBody
import com.example.bffandroid.data.model.RechargeQuoteResponse
import com.example.bffandroid.data.model.RefreshTokenBody
import com.example.bffandroid.data.model.RefreshTokenResponse
import com.example.bffandroid.data.model.RoomResponse
import com.example.bffandroid.data.model.RtcTokenBody
import com.example.bffandroid.data.model.RtcTokenResponse
import com.example.bffandroid.data.model.UpdateProfileBody
import com.example.bffandroid.data.model.UpdateProfileResponse
import com.example.bffandroid.data.model.UserProfileResponse
import com.example.bffandroid.data.model.VideoUpgradeStatusResponse
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

    suspend fun updatePresence(
        accessToken: String,
        body: PresenceRequestBody
    ): Response<PresenceResponse> = apiService.updatePresence(accessToken, body)

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

    suspend fun createRoom(
        bearerToken: String,
        body: CreateRoomBody
    ): Response<RoomResponse> = apiService.createRoom(bearerToken, body)

    suspend fun getRoom(
        bearerToken: String,
        roomId: String
    ): Response<RoomResponse> = apiService.getRoom(bearerToken, roomId)

    suspend fun joinRoom(
        bearerToken: String,
        roomId: String,
        body: JoinRoomBody
    ): Response<RoomResponse> = apiService.joinRoom(bearerToken, roomId, body)

    suspend fun leaveRoom(
        bearerToken: String,
        roomId: String
    ): Response<RoomResponse> = apiService.leaveRoom(bearerToken, roomId)

    suspend fun closeRoom(
        bearerToken: String,
        roomId: String
    ): Response<RoomResponse> = apiService.closeRoom(bearerToken, roomId)

    suspend fun getRtcToken(
        bearerToken: String,
        roomId: String,
        appPlatform: String,
        deviceId: String,
        appAttestation: String,
        body: RtcTokenBody
    ): Response<RtcTokenResponse> = apiService.getRtcToken(
        bearerToken = bearerToken,
        appPlatform = appPlatform,
        deviceId = deviceId,
        appAttestation = appAttestation,
        roomId = roomId,
        body = body
    )

    suspend fun getVideoUpgradeStatus(
        bearerToken: String,
        roomId: String
    ): Response<VideoUpgradeStatusResponse> =
        apiService.getVideoUpgradeStatus(bearerToken, roomId)

    suspend fun requestVideoUpgrade(
        bearerToken: String,
        roomId: String
    ): Response<VideoUpgradeStatusResponse> =
        apiService.requestVideoUpgrade(bearerToken, roomId)

    suspend fun acceptVideoUpgrade(
        bearerToken: String,
        roomId: String
    ): Response<VideoUpgradeStatusResponse> =
        apiService.acceptVideoUpgrade(bearerToken, roomId)

    suspend fun declineVideoUpgrade(
        bearerToken: String,
        roomId: String
    ): Response<VideoUpgradeStatusResponse> =
        apiService.declineVideoUpgrade(bearerToken, roomId)

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
