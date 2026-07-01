package com.example.bffandroid.data.model

import com.google.gson.annotations.SerializedName

data class CreateRoomBody(
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("maxParticipants") val maxParticipants: Int? = null,
    @SerializedName("invitedUserId") val invitedUserId: String? = null
)

data class JoinRoomBody(
    @SerializedName("requestedRole") val requestedRole: String
)

data class RtcTokenBody(
    @SerializedName("requestedRole") val requestedRole: String
)

data class VideoUpgradeStatusResponse(
    @SerializedName("roomId") val roomId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("requestedByUserId") val requestedByUserId: String?,
    @SerializedName("requestedAt") val requestedAt: String?,
    @SerializedName("respondedAt") val respondedAt: String?
)

data class RoomResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("channelName") val channelName: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("createdByUserId") val createdByUserId: String?,
    @SerializedName("invitedUserId") val invitedUserId: String?,
    @SerializedName("maxParticipants") val maxParticipants: Int?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("closedAt") val closedAt: String?,
    @SerializedName("participants") val participants: List<RoomParticipant>?
)

data class RoomParticipant(
    @SerializedName("userId") val userId: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("agoraUid") val agoraUid: Int?,
    @SerializedName("joinedAt") val joinedAt: String?,
    @SerializedName("leftAt") val leftAt: String?
)

data class CallRoomUiState(
    val isCreatingRoom: Boolean = false,
    val isJoiningRoom: Boolean = false,
    val isFetchingRtcToken: Boolean = false,
    val isJoiningRtc: Boolean = false,
    val isRtcJoined: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerEnabled: Boolean = true,
    val remoteAudioUserIds: List<Int> = emptyList(),
    val room: RoomResponse? = null,
    val rtcToken: RtcTokenResponse? = null,
    val errorMessage: String? = null
)

object RoomType {
    const val GroupAudioRoom = "GROUP_AUDIO_ROOM"
    const val GroupVideoRoom = "GROUP_VIDEO_ROOM"
    const val OneToOneAudioCall = "ONE_TO_ONE_AUDIO_CALL"
    const val OneToOneVideoCall = "ONE_TO_ONE_VIDEO_CALL"
}

object RoomRole {
    const val Speaker = "SPEAKER"
    const val Listener = "LISTENER"
}

object AppAttestation {
    const val DevAndroid = "dev-android"
}

data class RtcTokenResponse(
    @SerializedName("appId") val appId: String?,
    @SerializedName("channelName") val channelName: String?,
    @SerializedName("uid") val uid: Long?,
    @SerializedName("token") val token: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("roomType") val roomType: String?,
    @SerializedName("canPublishAudio") val canPublishAudio: Boolean?,
    @SerializedName("canPublishVideo") val canPublishVideo: Boolean?,
    @SerializedName("expiresAt") val expiresAt: String?
)
