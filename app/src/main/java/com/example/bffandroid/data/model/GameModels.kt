package com.example.bffandroid.data.model

import com.google.gson.annotations.SerializedName

data class GameCatalogItemDto(
    @SerializedName("code") val code: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("iconKey") val iconKey: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("minPlayers") val minPlayers: Int?,
    @SerializedName("maxPlayers") val maxPlayers: Int?
)
