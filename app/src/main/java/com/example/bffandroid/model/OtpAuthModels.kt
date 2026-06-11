package com.example.bffandroid.model

import android.content.Context
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
