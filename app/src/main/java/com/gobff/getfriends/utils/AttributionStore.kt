package com.gobff.getfriends.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AttributionStore {
    private const val PREFS_NAME = "bff_attribution"
    private const val KEY_CAPTURED_AT_MILLIS = "captured_at_millis"
    private const val KEY_RAW_REFERRER = "raw_referrer"
    private const val KEY_UTM_SOURCE = "utm_source"
    private const val KEY_UTM_MEDIUM = "utm_medium"
    private const val KEY_UTM_CAMPAIGN = "utm_campaign"
    private const val KEY_UTM_TERM = "utm_term"
    private const val KEY_UTM_CONTENT = "utm_content"
    private const val KEY_LANDING_URL = "landing_url"
    private const val TTL_MILLIS = 30L * 24L * 60L * 60L * 1000L

    fun saveInstallReferrer(context: Context, rawReferrer: String) {
        val normalizedReferrer = rawReferrer.trim().takeIf { it.isNotBlank() } ?: return
        val values = parseReferrer(normalizedReferrer)
        val landingUrl = buildPlayLandingUrl(context, normalizedReferrer)

        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit(commit = true) {
                putLong(KEY_CAPTURED_AT_MILLIS, System.currentTimeMillis())
                putString(KEY_RAW_REFERRER, normalizedReferrer)
                putString(KEY_LANDING_URL, landingUrl)
                putOptionalString(KEY_UTM_SOURCE, values["utm_source"])
                putOptionalString(KEY_UTM_MEDIUM, values["utm_medium"])
                putOptionalString(KEY_UTM_CAMPAIGN, values["utm_campaign"])
                putOptionalString(KEY_UTM_TERM, values["utm_term"])
                putOptionalString(KEY_UTM_CONTENT, values["utm_content"])
            }
    }

    fun attributionHeaders(context: Context): Map<String, String> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val capturedAtMillis = prefs.getLong(KEY_CAPTURED_AT_MILLIS, 0L)
        if (capturedAtMillis <= 0L) return emptyMap()
        if (System.currentTimeMillis() - capturedAtMillis > TTL_MILLIS) {
            prefs.edit(commit = true) { clear() }
            return emptyMap()
        }

        return buildMap {
            putHeader("X-Utm-Source", prefs.getString(KEY_UTM_SOURCE, null))
            putHeader("X-Utm-Medium", prefs.getString(KEY_UTM_MEDIUM, null))
            putHeader("X-Utm-Campaign", prefs.getString(KEY_UTM_CAMPAIGN, null))
            putHeader("X-Utm-Term", prefs.getString(KEY_UTM_TERM, null))
            putHeader("X-Utm-Content", prefs.getString(KEY_UTM_CONTENT, null))
            putHeader("X-Landing-Url", prefs.getString(KEY_LANDING_URL, null))
            putHeader("X-Referrer", prefs.getString(KEY_RAW_REFERRER, null))
        }
    }

    private fun parseReferrer(rawReferrer: String): Map<String, String> {
        val decodedOnce = decode(rawReferrer)
        val candidates = listOf(rawReferrer, decodedOnce).distinct()
        return candidates
            .asSequence()
            .map(::parseQueryValues)
            .firstOrNull { it.isNotEmpty() }
            ?: emptyMap()
    }

    private fun parseQueryValues(value: String): Map<String, String> {
        val query = when {
            value.contains("://") -> Uri.parse(value).encodedQuery.orEmpty()
            value.startsWith("?") -> value.drop(1)
            else -> value
        }
        if (query.isBlank()) return emptyMap()

        return query.split("&")
            .mapNotNull { part ->
                val index = part.indexOf("=")
                if (index <= 0) return@mapNotNull null
                val key = decode(part.substring(0, index)).trim()
                val parsedValue = decode(part.substring(index + 1)).trim()
                if (key.isBlank() || parsedValue.isBlank()) null else key to parsedValue
            }
            .toMap()
    }

    private fun buildPlayLandingUrl(context: Context, rawReferrer: String): String {
        val encodedReferrer = URLEncoder.encode(rawReferrer, StandardCharsets.UTF_8.name())
        return "https://play.google.com/store/apps/details?id=${context.packageName}&referrer=$encodedReferrer"
    }

    private fun decode(value: String): String =
        runCatching { URLDecoder.decode(value, StandardCharsets.UTF_8.name()) }.getOrDefault(value)

    private fun MutableMap<String, String>.putHeader(name: String, value: String?) {
        value?.trim()?.takeIf { it.isNotBlank() }?.let { put(name, it) }
    }

    private fun android.content.SharedPreferences.Editor.putOptionalString(key: String, value: String?) {
        value?.trim()?.takeIf { it.isNotBlank() }?.let { putString(key, it) } ?: remove(key)
    }
}
