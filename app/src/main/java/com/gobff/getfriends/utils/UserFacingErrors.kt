package com.gobff.getfriends.utils

fun Throwable.userFacingMessage(fallback: String): String {
    val rawMessage = message?.takeIf { it.isNotBlank() } ?: return fallback
    val isInternalError = rawMessage.contains("java.lang.") ||
        rawMessage.contains("kotlin.") ||
        rawMessage.contains("retrofit2.") ||
        rawMessage.contains("cannot be cast", ignoreCase = true)

    return if (isInternalError) fallback else rawMessage
}
