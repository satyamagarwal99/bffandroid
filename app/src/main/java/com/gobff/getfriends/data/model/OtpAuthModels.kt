package com.gobff.getfriends.data.model

import android.content.Context
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import java.util.Locale
import com.google.gson.annotations.SerializedName

data class OtpRequestBody(
    @SerializedName("countryIso2")    val countryIso2: String,
    @SerializedName("phoneNumber")    val phoneNumber: String,
    @SerializedName("installationId") val installationId: String,
    @SerializedName("deviceType")     val deviceType: String
)

data class OtpRequestResponse(
    @SerializedName("phoneE164")  val phoneE164: String?,
    @SerializedName("expiresAt")  val expiresAt: String?
)

data class DeviceInfo(
    @SerializedName("installationId") val installationId: String,
    @SerializedName("platform")       val platform: String,
    @SerializedName("deviceBrand")    val deviceBrand: String,
    @SerializedName("deviceModel")    val deviceModel: String,
    @SerializedName("osVersion")      val osVersion: String,
    @SerializedName("appVersion")     val appVersion: String,
    @SerializedName("fcmToken")       val fcmToken: String? = null
)

data class OtpVerifyBody(
    @SerializedName("countryIso2")  val countryIso2: String,
    @SerializedName("phoneNumber")  val phoneNumber: String,
    @SerializedName("otp")          val otp: String,
    @SerializedName("device")       val device: DeviceInfo,
    @SerializedName("displayName")  val displayName: String,
    @SerializedName("dateOfBirth")  val dateOfBirth: String
)
data class OtpVerifyResponse(
    @SerializedName("userId")                 val userId: String?,
    @SerializedName("accessToken")            val accessToken: String?,
    @SerializedName("refreshToken")           val refreshToken: String?,
    @SerializedName("accessTokenExpiresAt")   val accessTokenExpiresAt: String?,
    @SerializedName("refreshTokenExpiresAt")  val refreshTokenExpiresAt: String?
)

data class GoogleAuthBody(
    @SerializedName("countryIso2") val countryIso2: String,
    @SerializedName("idToken") val idToken: String,
    @SerializedName("device") val device: DeviceInfo,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("dateOfBirth") val dateOfBirth: String
)

data class GoogleAuthResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("verified") val verified: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("accessTokenExpiresAt") val accessTokenExpiresAt: String?,
    @SerializedName("refreshTokenExpiresAt") val refreshTokenExpiresAt: String?
)

data class RefreshTokenBody(
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("installationId") val installationId: String
)

data class RefreshTokenResponse(
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("accessTokenExpiresAt") val accessTokenExpiresAt: String?,
    @SerializedName("refreshTokenExpiresAt") val refreshTokenExpiresAt: String?,
    @SerializedName("message") val message: String? = null
)

data class LogoutResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)

data class UpdateFcmTokenBody(
    @SerializedName("fcmToken") val fcmToken: String
)

data class AppVersionResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?
)

data class OtpRequestResult(
    val isSuccessful: Boolean,
    val debugOtp: String?,
    val message: String?
)

data class OtpVerifyResult(
    val isVerified: Boolean,
    val message: String?,
    val accessToken: String? = null
)

data class GoogleAuthResult(
    val isSuccessful: Boolean,
    val message: String?,
    val accessToken: String? = null
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
