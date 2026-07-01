package com.example.bffandroid.data.model

import com.google.gson.annotations.SerializedName


data class PresenceResponse(
    @SerializedName("online")       val online: Boolean?,
    @SerializedName("lastOnlineAt") val lastOnlineAt: String?
)

data class PresenceRequestBody(
    @SerializedName("online") val online: Boolean
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
