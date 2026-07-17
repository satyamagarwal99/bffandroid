package com.gobff.getfriends.data

import com.gobff.getfriends.data.model.AppVersionResponse
import com.gobff.getfriends.data.model.CallHistoryItemResponse
import com.gobff.getfriends.data.model.CountryLoginConfig
import com.gobff.getfriends.data.model.ConnectUserResponse
import com.gobff.getfriends.data.model.CreateRoomBody
import com.gobff.getfriends.data.model.EndRoomResponse
import com.gobff.getfriends.data.model.FriendListUserResponse
import com.gobff.getfriends.data.model.GameCatalogItemDto
import com.gobff.getfriends.data.model.GiftCatalogResponse
import com.gobff.getfriends.data.model.SendGiftBody
import com.gobff.getfriends.data.model.SendGiftResponse
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
import com.gobff.getfriends.data.model.RechargeOrderStatusResponse
import com.gobff.getfriends.data.model.RechargePurchaseBody
import com.gobff.getfriends.data.model.RechargePurchaseResponse
import com.gobff.getfriends.data.model.RechargeQuoteBody
import com.gobff.getfriends.data.model.RechargeQuoteResponse
import com.gobff.getfriends.data.model.RefreshTokenBody
import com.gobff.getfriends.data.model.RefreshTokenResponse
import com.gobff.getfriends.data.model.RoomResponse
import com.gobff.getfriends.data.model.RoomFeedbackBody
import com.gobff.getfriends.data.model.RoomFeedbackResponse
import com.gobff.getfriends.data.model.RoomFeedbackStatusResponse
import com.gobff.getfriends.data.model.RoomMessageBody
import com.gobff.getfriends.data.model.RoomMessageResponse
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

    suspend fun deleteAccount(bearerToken: String): Response<LogoutResponse> =
        apiService.deleteAccount(bearerToken)

    suspend fun updateFcmToken(
        bearerToken: String,
        body: UpdateFcmTokenBody
    ): Response<Unit> = apiService.updateFcmToken(bearerToken, body)

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

    suspend fun getGiftCatalog(bearerToken: String): Response<GiftCatalogResponse> =
        apiService.getGiftCatalog(bearerToken)

    suspend fun sendGift(
        bearerToken: String,
        idempotencyKey: String,
        roomId: String,
        body: SendGiftBody
    ): Response<SendGiftResponse> =
        apiService.sendGift(bearerToken, idempotencyKey, roomId, body)

    suspend fun getGameCatalog(bearerToken: String): Response<List<GameCatalogItemDto>> =
        apiService.getGameCatalog(bearerToken)

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

    suspend fun getRechargeOrderStatus(
        bearerToken: String,
        orderId: String
    ): Response<RechargeOrderStatusResponse> =
        apiService.getRechargeOrderStatus(bearerToken, orderId)

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

    suspend fun endRoom(
        bearerToken: String,
        roomId: String
    ): Response<EndRoomResponse> = apiService.endRoom(bearerToken, roomId)

    suspend fun submitRoomFeedback(
        bearerToken: String,
        roomId: String,
        body: RoomFeedbackBody
    ): Response<RoomFeedbackResponse> = apiService.submitRoomFeedback(bearerToken, roomId, body)

    suspend fun getRoomFeedbackStatus(
        bearerToken: String,
        roomId: String
    ): Response<RoomFeedbackStatusResponse> = apiService.getRoomFeedbackStatus(bearerToken, roomId)

    suspend fun getRoomMessages(
        bearerToken: String,
        roomId: String
    ): Response<List<RoomMessageResponse>> = apiService.getRoomMessages(bearerToken, roomId)

    suspend fun sendRoomMessage(
        bearerToken: String,
        roomId: String,
        body: RoomMessageBody
    ): Response<RoomMessageResponse> = apiService.sendRoomMessage(bearerToken, roomId, body)

    suspend fun getRtcToken(
        bearerToken: String,
        roomId: String,
        body: RtcTokenBody
    ): Response<RtcTokenResponse> = apiService.getRtcToken(
        bearerToken = bearerToken,
        roomId = roomId,
        body = body
    )

    suspend fun getCallHistory(
        bearerToken: String,
        size: Int = 20
    ): Response<List<CallHistoryItemResponse>> = apiService.getCallHistory(bearerToken, size)

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

    suspend fun getConnectUsers(
        bearerToken: String,
        size: Int = 10
    ): Response<List<ConnectUserResponse>> = apiService.getConnectUsers(bearerToken, size)

    suspend fun getMyFriends(
        bearerToken: String
    ): Response<List<FriendListUserResponse>> = apiService.getMyFriends(bearerToken)

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
