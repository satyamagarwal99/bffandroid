package com.gobff.getfriends.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import com.gobff.getfriends.data.model.MarketingContext
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone

class MarketingContextProvider(
    context: Context
) {
    private val appContext = context.applicationContext

    suspend fun currentContext(): MarketingContext = withContext(Dispatchers.IO) {
        val advertisingInfo = getAdvertisingInfo()
        val displayMetrics = appContext.resources.displayMetrics
        val locale = Locale.getDefault()
        val timezone = TimeZone.getDefault()
        val packageInfo = appContext.packageManager.getPackageInfoCompat(appContext.packageName)

        MarketingContext(
            platform = "ANDROID",
            advertiserTrackingEnabled = advertisingInfo?.advertiserTrackingEnabled == true,
            mobileAdvertisingId = advertisingInfo?.advertisingId,
            idfv = null,
            appId = appContext.packageName,
            appVersion = packageInfo.versionName.orEmpty(),
            appBuild = packageInfo.longVersionCodeCompat().toString(),
            osVersion = Build.VERSION.RELEASE.orEmpty(),
            deviceModel = listOf(Build.MANUFACTURER, Build.MODEL)
                .mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }
                .distinctBy { it.lowercase(Locale.US) }
                .joinToString(" ")
                .ifBlank { Build.DEVICE.orEmpty() },
            locale = locale.toLanguageTag().replace('-', '_'),
            timezone = timezone.id,
            timezoneAbbreviation = timezone.getDisplayName(false, TimeZone.SHORT, locale),
            carrier = getCarrierName(),
            screenWidth = displayMetrics.widthPixels,
            screenHeight = displayMetrics.heightPixels,
            screenDensity = displayMetrics.density.toString(),
            cpuCores = Runtime.getRuntime().availableProcessors().takeIf { it > 0 }
        )
    }

    private fun getAdvertisingInfo(): AdvertisingInfo? {
        return runCatching {
            val info = AdvertisingIdClient.getAdvertisingIdInfo(appContext)
            val advertisingId = info.id
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?.takeUnless { it == ZERO_ADVERTISING_ID }

            if (advertisingId != null && !info.isLimitAdTrackingEnabled) {
                AdvertisingInfo(
                    advertiserTrackingEnabled = true,
                    advertisingId = advertisingId
                )
            } else {
                AdvertisingInfo(
                    advertiserTrackingEnabled = false,
                    advertisingId = null
                )
            }
        }.getOrElse {
            AdvertisingInfo(
                advertiserTrackingEnabled = false,
                advertisingId = null
            )
        }
    }

    private fun getCarrierName(): String? {
        val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return null
        return runCatching {
            telephonyManager.networkOperatorName
                ?.trim()
                ?.takeIf(String::isNotBlank)
        }.getOrNull()
    }

    private data class AdvertisingInfo(
        val advertiserTrackingEnabled: Boolean,
        val advertisingId: String?
    )

    private companion object {
        const val ZERO_ADVERTISING_ID = "00000000-0000-0000-0000-000000000000"
    }
}

private fun PackageManager.getPackageInfoCompat(packageName: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo(packageName, 0)
    }

private fun android.content.pm.PackageInfo.longVersionCodeCompat(): Long =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode
    } else {
        @Suppress("DEPRECATION")
        versionCode.toLong()
    }
