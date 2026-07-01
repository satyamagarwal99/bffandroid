package com.example.bffandroid.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

class OtpDeviceProvider(private val context: Context) {
    @SuppressLint("HardwareIds")
    fun installationId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return androidId
            ?.takeIf { it.isNotBlank() }
            ?.let { "android-$it" }
            ?: DEFAULT_INSTALLATION_ID
    }

    private companion object {
        const val DEFAULT_INSTALLATION_ID = "android-device"
    }
}
