package com.gobff.getfriends.data

import com.gobff.getfriends.data.model.AppVersionResponse
import com.gobff.getfriends.data.model.CallHistoryItemResponse
import com.gobff.getfriends.data.model.CountryLoginConfig
import com.gobff.getfriends.data.model.ConnectUserResponse
import com.gobff.getfriends.data.model.CreateRoomBody
import com.gobff.getfriends.data.model.EndRoomResponse
import com.gobff.getfriends.data.model.GameCatalogItemDto
import com.gobff.getfriends.data.model.GiftCatalogResponse
import com.gobff.getfriends.data.model.GoogleAuthBody
import com.gobff.getfriends.data.model.GoogleAuthResponse
import com.gobff.getfriends.data.model.HomeOptionsResponse
import com.gobff.getfriends.data.model.JoinRoomBody
import com.gobff.getfriends.data.model.LogoutResponse
import com.gobff.getfriends.data.model.OtpRequestBody
import com.gobff.getfriends.data.model.OtpRequestResponse
import com.gobff.getfriends.data.model.OtpVerifyBody
import com.gobff.getfriends.data.model.OtpVerifyResponse
import com.gobff.getfriends.data.model.PresenceRequestBody
import com.gobff.getfriends.data.model.PresenceResponse
import com.gobff.getfriends.data.model.RechargeOptionsResponse
import com.gobff.getfriends.data.model.RechargePurchaseBody
import com.gobff.getfriends.data.model.RechargePurchaseResponse
import com.gobff.getfriends.data.model.RechargeQuoteBody
import com.gobff.getfriends.data.model.RechargeQuoteResponse
import com.gobff.getfriends.data.model.RefreshTokenBody
import com.gobff.getfriends.data.model.RefreshTokenResponse
import com.gobff.getfriends.data.model.RoomResponse
import com.gobff.getfriends.data.model.RtcTokenBody
import com.gobff.getfriends.data.model.RtcTokenResponse
import com.gobff.getfriends.data.model.UpdateProfileBody
import com.gobff.getfriends.data.model.UpdateProfileResponse
import com.gobff.getfriends.data.model.UpdateFcmTokenBody
import com.gobff.getfriends.data.model.UserProfileResponse
import com.gobff.getfriends.data.model.VideoUpgradeStatusResponse
import com.gobff.getfriends.data.model.VoiceVerificationResponse
import com.gobff.getfriends.data.model.VoiceVerificationStatusResponse
import com.gobff.getfriends.data.model.WalletBalanceResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @PUT("auth/device/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") bearerToken: String,
        @Body body: UpdateFcmTokenBody
    ): Response<Unit>

    @GET("app-version/{platform}/{appVersion}")
    suspend fun getAppVersion(
        @Path("platform") platform: String,
        @Path("appVersion") appVersion: String
    ): Response<AppVersionResponse>

    @PUT("home/presence")
    suspend fun updatePresence(
        @Header("Authorization") bearerToken: String,
        @Body body: PresenceRequestBody
    ): Response<PresenceResponse>

    @GET("wallet/balance")
    suspend fun getWalletBalance(
        @Header("Authorization") bearerToken: String
    ): Response<WalletBalanceResponse>

    @GET("wallet/recharge/options")
    suspend fun getRechargeOptions(
        @Header("Authorization") bearerToken: String
    ): Response<RechargeOptionsResponse>

    @GET("gifts/catalog")
    suspend fun getGiftCatalog(
        @Header("Authorization") bearerToken: String
    ): Response<GiftCatalogResponse>

    @GET("games/catalog")
    suspend fun getGameCatalog(
        @Header("Authorization") bearerToken: String
    ): Response<List<GameCatalogItemDto>>

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

    @POST("rooms")
    suspend fun createRoom(
        @Header("Authorization") bearerToken: String,
        @Body body: CreateRoomBody
    ): Response<RoomResponse>

    @GET("rooms/{roomId}")
    suspend fun getRoom(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String
    ): Response<RoomResponse>

    @POST("rooms/{roomId}/join")
    suspend fun joinRoom(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String,
        @Body body: JoinRoomBody
    ): Response<RoomResponse>

    @POST("rooms/{roomId}/leave")
    suspend fun leaveRoom(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String
    ): Response<RoomResponse>

    @POST("rooms/{roomId}/close")
    suspend fun closeRoom(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String
    ): Response<RoomResponse>

    @POST("rooms/{roomId}/end")
    suspend fun endRoom(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String
    ): Response<EndRoomResponse>

    @POST("rooms/{roomId}/rtc-token")
    suspend fun getRtcToken(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String,
        @Body body: RtcTokenBody
    ): Response<RtcTokenResponse>

    @GET("rooms/call-history")
    suspend fun getCallHistory(
        @Header("Authorization") bearerToken: String,
        @Query("size") size: Int = 20
    ): Response<List<CallHistoryItemResponse>>

    @GET("rooms/{roomId}/video-upgrade")
    suspend fun getVideoUpgradeStatus(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String
    ): Response<VideoUpgradeStatusResponse>

    @POST("rooms/{roomId}/video-upgrade/request")
    suspend fun requestVideoUpgrade(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String
    ): Response<VideoUpgradeStatusResponse>

    @POST("rooms/{roomId}/video-upgrade/accept")
    suspend fun acceptVideoUpgrade(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String
    ): Response<VideoUpgradeStatusResponse>

    @POST("rooms/{roomId}/video-upgrade/decline")
    suspend fun declineVideoUpgrade(
        @Header("Authorization") bearerToken: String,
        @Path("roomId") roomId: String
    ): Response<VideoUpgradeStatusResponse>

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

    @GET("home/connect/users")
    suspend fun getConnectUsers(
        @Header("Authorization") bearerToken: String,
        @Query("size") size: Int = 10
    ): Response<List<ConnectUserResponse>>

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
