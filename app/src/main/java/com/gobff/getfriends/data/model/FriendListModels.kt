package com.gobff.getfriends.data.model

import com.google.gson.annotations.SerializedName

data class FriendListUserResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("online") val online: Boolean? = null,
    @SerializedName("isOnline") val isOnline: Boolean? = null,
    @SerializedName("favorite") val favorite: Boolean? = null,
    @SerializedName("isFavorite") val isFavorite: Boolean? = null,
    @SerializedName("lastOnlineAt") val lastOnlineAt: String? = null,
    @SerializedName("lastSeenAt") val lastSeenAt: String? = null,
    @SerializedName("lastTalkedAt") val lastTalkedAt: String? = null
)

