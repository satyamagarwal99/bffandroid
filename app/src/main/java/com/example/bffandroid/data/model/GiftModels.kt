package com.example.bffandroid.data.model

import com.google.gson.annotations.SerializedName

data class GiftCatalogResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("heartBalance") val heartBalance: Int?,
    @SerializedName("categories") val categories: List<GiftCategoryDto>?
)

data class GiftCategoryDto(
    @SerializedName("code") val code: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("iconKey") val iconKey: String?,
    @SerializedName("items") val items: List<GiftItemDto>?
)

data class GiftItemDto(
    @SerializedName("code") val code: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("imageKey") val imageKey: String?,
    @SerializedName("heartPrice") val heartPrice: Int?,
    @SerializedName("originalHeartPrice") val originalHeartPrice: Int?
)
