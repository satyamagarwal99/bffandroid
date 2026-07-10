package com.gobff.getfriends.utils

import android.content.Context
import java.io.File

enum class AvatarGender(
    val directoryName: String,
    val remoteSegment: String,
    val count: Int
) {
    Female("Female", "Female", 100),
    Male("Male", "Male", 50)
}

object AvatarCache {
    private const val BASE_URL = "https://goentertainment.blr1.cdn.digitaloceanspaces.com/Avatars"

    fun avatarDirectory(context: Context, gender: AvatarGender): File {
        return File(context.cacheDir, gender.directoryName)
    }

    fun avatarFile(context: Context, gender: AvatarGender, index: Int): File {
        return File(avatarDirectory(context, gender), avatarFileName(index))
    }

    fun avatarUrl(gender: AvatarGender, index: Int): String {
        return "$BASE_URL/${gender.remoteSegment}/${avatarFileName(index)}.png"
    }

    fun hasAllAvatars(context: Context): Boolean {
        return AvatarGender.entries.all { gender ->
            (1..gender.count).all { index ->
                avatarFile(context, gender, index).let { it.exists() && it.length() > 0L }
            }
        }
    }

    fun avatarFileName(index: Int): String {
        return "Avatar${index.toString().padStart(2, '0')}"
    }

    fun parseAvatarKey(avatarUrl: String?): AvatarCacheKey? {
        val normalized = avatarUrl?.trim()?.lowercase().orEmpty()
        val gender = when {
            normalized.startsWith("women_avatar") -> AvatarGender.Female
            normalized.startsWith("female_avatar") -> AvatarGender.Female
            normalized.startsWith("man_avatar") -> AvatarGender.Male
            normalized.startsWith("male_avatar") -> AvatarGender.Male
            else -> return null
        }
        val index = normalized.takeLastWhile { it.isDigit() }.toIntOrNull() ?: return null
        return AvatarCacheKey(gender = gender, index = index).takeIf { index in 1..gender.count }
    }

    fun parseAvatarKey(avatarUrl: String?, gender: String?): AvatarCacheKey? {
        val normalized = avatarUrl?.trim().orEmpty()
        val explicitKey = parseAvatarKey(normalized)
        if (explicitKey != null) return explicitKey

        if (!normalized.startsWith("Avatar", ignoreCase = true)) return null
        val avatarGender = gender.toAvatarGender() ?: return null
        val index = normalized.takeLastWhile { it.isDigit() }.toIntOrNull() ?: return null
        return AvatarCacheKey(gender = avatarGender, index = index).takeIf { index in 1..avatarGender.count }
    }

    fun avatarValue(index: Int): String {
        return avatarFileName(index)
    }

    fun normalizeAvatarValue(avatarUrl: String?): String? {
        val trimmed = avatarUrl?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val index = parseAvatarKey(trimmed)?.index
            ?: trimmed.takeIf { it.startsWith("Avatar", ignoreCase = true) }
                ?.takeLastWhile { it.isDigit() }
                ?.toIntOrNull()
        return index?.let(::avatarValue)
    }
}

data class AvatarCacheKey(
    val gender: AvatarGender,
    val index: Int
)

fun String?.toAvatarGender(): AvatarGender? {
    return when (this?.trim()?.uppercase()) {
        "FEMALE", "WOMAN", "WOMEN" -> AvatarGender.Female
        "MALE", "MAN", "MEN" -> AvatarGender.Male
        else -> null
    }
}
