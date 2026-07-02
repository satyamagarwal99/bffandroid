package com.gobff.getfriends.data.model

import com.gobff.getfriends.R
import com.google.gson.annotations.SerializedName

data class HomeOptionsResponse(
    @SerializedName("languages")
    val languages: List<HomeLanguageOptionResponse>? = null,
    @SerializedName("vibes")
    val vibes: List<HomeVibeOptionResponse>? = null,
    @SerializedName("message")
    val message: String? = null
)

data class HomeLanguageOptionResponse(
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("nativeLabel")
    val nativeLabel: String? = null
)

data class HomeVibeOptionResponse(
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("label")
    val label: String? = null
)

data class LanguageOption(
    val id: String,
    val title: String,
    val subtitle: String
)

data class VibeOption(
    val id: String,
    val iconRes: Int,
    val label: String
)

fun defaultLanguageOptions(): List<LanguageOption> = listOf(
    LanguageOption("ENGLISH", "English", "English"),
    LanguageOption("MALAYALAM", "Malayalam", "മലയാളം"),
    LanguageOption("TAMIL", "Tamil", "தமிழ்"),
    LanguageOption("HINDI", "Hindi", "हिन्दी"),
    LanguageOption("MARATHI", "Marathi", "मराठी"),
    LanguageOption("PUNJABI", "Punjabi", "ਪੰਜਾਬੀ"),
    LanguageOption("BENGALI", "Bengali", "বাংলা"),
    LanguageOption("KANNADA", "Kannada", "ಕನ್ನಡ"),
    LanguageOption("GUJARATI", "Gujarati", "ગુજરાતી"),
    LanguageOption("TELUGU", "Telugu", "తెలుగు"),
    LanguageOption("URDU", "Urdu", "اردو"),
    LanguageOption("ODIA", "Odia", "ଓଡ଼ିଆ")
)

fun defaultVibeOptions(): List<VibeOption> = listOf(
    VibeOption("FRIENDS", R.drawable.profile_screen_friend_sqaud, "Friends"),
    VibeOption("DATING", R.drawable.vibe_dating, "Dating"),
    VibeOption("ADVICE", R.drawable.vibe_advice, "Advice"),
    VibeOption("LATE_NIGHT", R.drawable.vibe_late_night, "Late Night"),
    VibeOption("BREAKUP", R.drawable.vibe_breakup, "Breakup"),
    VibeOption("DEEP_TALKS", R.drawable.vibe_deep_talk, "Deep Talks"),
    VibeOption("MOVIES", R.drawable.vibe_movie, "Movies"),
    VibeOption("TIMEPASS", R.drawable.vibe_timepass, "Timepass"),
    VibeOption("GAMING", R.drawable.vibe_gaming, "Gaming"),
    VibeOption("ASTROLOGY", R.drawable.vibe_astrology, "Astrology"),
    VibeOption("GOSSIP", R.drawable.vibe_gossip, "Gossip"),
    VibeOption("ANTAKSHARI", R.drawable.vibe_antakshari, "Antakshari")
)

fun HomeLanguageOptionResponse.toLanguageOption(): LanguageOption? {
    val optionCode = code?.takeIf { it.isNotBlank() } ?: label?.takeIf { it.isNotBlank() }
    val optionLabel = label?.takeIf { it.isNotBlank() } ?: optionCode
    if (optionCode == null || optionLabel == null) return null

    return LanguageOption(
        id = optionCode.uppercase(),
        title = optionLabel,
        subtitle = nativeLabel?.takeIf { it.isNotBlank() } ?: optionLabel
    )
}

fun HomeVibeOptionResponse.toVibeOption(): VibeOption? {
    val optionCode = code?.takeIf { it.isNotBlank() } ?: label?.takeIf { it.isNotBlank() }
    val optionLabel = label?.takeIf { it.isNotBlank() } ?: optionCode
    if (optionCode == null || optionLabel == null) return null

    val normalizedCode = optionCode.uppercase()
    return VibeOption(
        id = normalizedCode,
        iconRes = vibeIconForCode(normalizedCode),
        label = optionLabel
    )
}

private fun vibeIconForCode(code: String): Int {
    return when (code) {
        "FRIENDS" -> R.drawable.profile_screen_friend_sqaud
        "DATING" -> R.drawable.vibe_dating
        "ADVICE" -> R.drawable.vibe_advice
        "LATE_NIGHT" -> R.drawable.vibe_late_night
        "BREAKUP" -> R.drawable.vibe_breakup
        "DEEP_TALKS", "DEEP_TALK" -> R.drawable.vibe_deep_talk
        "MOVIES", "MOVIE" -> R.drawable.vibe_movie
        "TIMEPASS" -> R.drawable.vibe_timepass
        "GAMING" -> R.drawable.vibe_gaming
        "ASTROLOGY" -> R.drawable.vibe_astrology
        "GOSSIP" -> R.drawable.vibe_gossip
        "ANTAKSHARI" -> R.drawable.vibe_antakshari
        else -> R.drawable.profile_screen_friend_sqaud
    }
}
