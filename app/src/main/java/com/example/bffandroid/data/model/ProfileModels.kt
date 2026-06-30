package com.example.bffandroid.data.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileBody(
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("languages") val languages: List<String>? = null,
    @SerializedName("vibes") val vibes: List<String>? = null
)

data class UpdateProfileResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("languages") val languages: List<String>? = null,
    @SerializedName("vibes") val vibes: List<String>? = null
)

data class UserProfileResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("languages") val languages: List<String>? = null,
    @SerializedName("vibes") val vibes: List<String>? = null
)

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val displayName: String? = null,
    val gender: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val languages: Set<String> = emptySet(),
    val vibes: Set<String> = emptySet()
)

data class UpdateProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class VoiceVerificationResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("verified") val verified: Boolean?,
    @SerializedName("submittedAt") val submittedAt: String?
)

data class VoiceVerificationStatusResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("verified") val verified: Boolean?,
    @SerializedName("submittedAt") val submittedAt: String?,
    @SerializedName("verifiedAt") val verifiedAt: String?
)

data class VoiceVerificationUiState(
    val isStatusLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val status: String? = null,
    val isVerified: Boolean = false,
    val errorMessage: String? = null
)
