package com.example.bffandroid.data.model

import com.google.gson.annotations.SerializedName


data class PresenceResponse(
    @SerializedName("online")       val online: Boolean?,
    @SerializedName("lastOnlineAt") val lastOnlineAt: String?
)

data class PresenceRequestBody(
    @SerializedName("online") val online: Boolean
)

data class ConnectUserResponse(
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("voiceVerified") val voiceVerified: Boolean? = null,
    @SerializedName("online") val online: Boolean? = null,
    @SerializedName("lastOnlineAt") val lastOnlineAt: String? = null,
    @SerializedName("languages") val languages: List<String>? = null,
    @SerializedName("vibes") val vibes: List<String>? = null,
    @SerializedName("friend") val friend: Boolean? = null,
    @SerializedName("lastTalkedAt") val lastTalkedAt: String? = null,
    @SerializedName("prompt") val prompt: String? = null
)

/*
data class RefreshTokenBody(
    @SerializedName("refreshToken")  val refreshToken: String,
    @SerializedName("installationId") val installationId: String
)

data class RefreshTokenResponse(
    @SerializedName("accessToken")           val accessToken: String?,
    @SerializedName("refreshToken")          val refreshToken: String?,
    @SerializedName("accessTokenExpiresAt")  val accessTokenExpiresAt: String?,
    @SerializedName("refreshTokenExpiresAt") val refreshTokenExpiresAt: String?
)*/
