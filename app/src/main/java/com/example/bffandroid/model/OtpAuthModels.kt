package com.example.bffandroid.model

import android.content.Context
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import java.util.Locale

data class OtpRequestResult(
    val isSuccessful: Boolean,
    val debugOtp: String?,
    val message: String?
)

data class OtpVerifyResult(
    val isVerified: Boolean,
    val message: String?
)

data class GoogleAuthResult(
    val isSuccessful: Boolean,
    val message: String?
)

data class AppVersionResult(
    val isSuccessful: Boolean,
    val message: String?,
    val rawResponse: String?
)

class CountryIsoProvider(private val context: Context) {
    fun detectCountryIso2(): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val simCountryIso = telephonyManager
            ?.simCountryIso
            ?.takeIf { it.isNotBlank() }
            ?.uppercase(Locale.US)
        val networkCountryIso = telephonyManager
            ?.networkCountryIso
            ?.takeIf { it.isNotBlank() }
            ?.uppercase(Locale.US)
        val localeCountryIso = Locale.getDefault()
            .country
            .takeIf { it.isNotBlank() }
            ?.uppercase(Locale.US)

        val resolvedCountryIso = simCountryIso
            ?: networkCountryIso
            ?: localeCountryIso
            ?: DEFAULT_COUNTRY_ISO

        Log.d(
            TAG,
            "Country ISO resolved=$resolvedCountryIso, sim=$simCountryIso, network=$networkCountryIso, locale=$localeCountryIso"
        )
        return resolvedCountryIso
    }

    private companion object {
        const val TAG = "CountryIsoProvider"
        const val DEFAULT_COUNTRY_ISO = "IN"
    }
}

class OtpDeviceProvider(private val context: Context) {
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

class AuthSessionStore(private val context: Context) {
    fun isLoggedIn(): Boolean {
        return preferences().getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        preferences()
            .edit()
            .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            .apply()
    }

    private fun preferences() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private companion object {
        const val PREFS_NAME = "auth_session"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}
