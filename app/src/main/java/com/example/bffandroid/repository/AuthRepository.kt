package com.example.bffandroid.repository

import android.os.Build
import android.util.Log
import com.example.bffandroid.model.AppVersionResult
import com.example.bffandroid.model.CountryLoginConfig
import com.example.bffandroid.model.GoogleAuthResult
import com.example.bffandroid.model.LoginMethod
import com.example.bffandroid.model.OtpRequestResult
import com.example.bffandroid.model.RechargeQuoteResult
import com.example.bffandroid.model.RechargeOption
import com.example.bffandroid.model.RechargeOptionsResult
import com.example.bffandroid.model.OtpVerifyResult
import com.example.bffandroid.model.WalletBalanceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

class AuthRepository {
    suspend fun getCountryLoginConfig(countryIso: String): CountryLoginConfig {
        return withContext(Dispatchers.IO) {
            val normalizedIso = countryIso.normalizedCountryIso()
            runCatching {
                Log.d(TAG, "Fetching country login config for ISO=$normalizedIso")
                val response = executeJsonRequest(
                    url = "$COUNTRIES_ENDPOINT/$normalizedIso",
                    method = "GET"
                )
                parseCountryLoginConfig(response, normalizedIso)
            }.getOrElse { error ->
                Log.e(TAG, "Country login config failed for ISO=$normalizedIso", error)
                fallbackCountryLoginConfig(normalizedIso)
            }
        }
    }

    suspend fun requestOtp(
        countryIso: String,
        phoneNumber: String,
        installationId: String = DEFAULT_INSTALLATION_ID
    ): OtpRequestResult {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = JSONObject().apply {
                    put("countryIso2", countryIso.normalizedCountryIso())
                    put("phoneNumber", phoneNumber.onlyDigits())
                    put("deviceType", DEVICE_PLATFORM)
                    put("installationId", installationId)
                }

                Log.d(TAG, "Requesting OTP for ISO=${countryIso.normalizedCountryIso()} body=$requestBody")
                val response = executeJsonRequest(
                    url = OTP_REQUEST_ENDPOINT,
                    method = "POST",
                    body = requestBody.toString()
                )
                parseOtpRequestResult(response)
            }.getOrElse { error ->
                Log.e(TAG, "OTP request failed", error)
                OtpRequestResult(
                    isSuccessful = false,
                    debugOtp = null,
                    message = error.message ?: "Unable to request OTP"
                )
            }
        }
    }

    suspend fun verifyOtp(
        countryIso: String,
        phoneNumber: String,
        otp: String,
        installationId: String = DEFAULT_INSTALLATION_ID
    ): OtpVerifyResult {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = JSONObject().apply {
                    put("countryIso2", countryIso.normalizedCountryIso())
                    put("phoneNumber", phoneNumber.onlyDigits())
                    put("otp", otp.onlyDigits())
                    put("device", buildDeviceJson(installationId))
                    put("displayName", DEFAULT_DISPLAY_NAME)
                }

                Log.d(TAG, "Verifying OTP for ISO=${countryIso.normalizedCountryIso()}")
                val response = executeJsonRequest(
                    url = OTP_VERIFY_ENDPOINT,
                    method = "POST",
                    body = requestBody.toString()
                )
                parseOtpVerifyResult(response)
            }.getOrElse { error ->
                Log.e(TAG, "OTP verify failed", error)
                OtpVerifyResult(
                    isVerified = false,
                    message = error.message ?: "Unable to verify OTP"
                )
            }
        }
    }

    suspend fun authenticateWithGoogle(
        countryIso: String,
        installationId: String = DEFAULT_INSTALLATION_ID
    ): GoogleAuthResult {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = JSONObject().apply {
                    put("countryIso2", countryIso.normalizedCountryIso())
                    put("idToken", "dev-google:$installationId")
                    put("device", buildDeviceJson(installationId))
                    put("displayName", DEFAULT_DISPLAY_NAME)
                    put("dateOfBirth", DEFAULT_DATE_OF_BIRTH)
                }

                Log.d(TAG, "Authenticating with Google for ISO=${countryIso.normalizedCountryIso()}")
                val response = executeJsonRequest(
                    url = GOOGLE_AUTH_ENDPOINT,
                    method = "POST",
                    body = requestBody.toString()
                )
                parseGoogleAuthResult(response)
            }.getOrElse { error ->
                Log.e(TAG, "Google auth failed", error)
                GoogleAuthResult(
                    isSuccessful = false,
                    message = error.message ?: "Unable to sign in with Google"
                )
            }
        }
    }

    suspend fun getAppVersion(
        platform: String = DEVICE_PLATFORM,
        appVersion: String = APP_VERSION
    ): AppVersionResult {
        return withContext(Dispatchers.IO) {
            runCatching {
                val normalizedPlatform = platform.uppercase(Locale.US)
                val response = executeJsonRequest(
                    url = "$APP_VERSION_ENDPOINT/$normalizedPlatform/$appVersion",
                    method = "GET"
                )
                AppVersionResult(
                    isSuccessful = true,
                    message = parseMessage(response),
                    rawResponse = response
                )
            }.getOrElse { error ->
                Log.e(TAG, "App version check failed", error)
                AppVersionResult(
                    isSuccessful = false,
                    message = error.message ?: "Unable to check app version",
                    rawResponse = null
                )
            }
        }
    }

    suspend fun getRechargeOptions(accessToken: String): RechargeOptionsResult {
        return withContext(Dispatchers.IO) {
            runCatching {
                val response = executeJsonRequest(
                    url = RECHARGE_OPTIONS_ENDPOINT,
                    method = "GET",
                    bearerToken = accessToken
                )
                val options = parseRechargeOptions(response)
                RechargeOptionsResult(
                    isSuccessful = true,
                    options = options,
                    message = parseMessage(response)
                )
            }.getOrElse { error ->
                Log.e(TAG, "Recharge options failed", error)
                RechargeOptionsResult(
                    isSuccessful = false,
                    options = emptyList(),
                    message = error.message ?: "Unable to load recharge options"
                )
            }
        }
    }

    suspend fun getRechargeQuote(
        accessToken: String,
        packCode: String,
        couponCode: String
    ): RechargeQuoteResult {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = JSONObject().apply {
                    put("packCode", packCode)
                    put("couponCode", couponCode)
                }
                val response = executeJsonRequest(
                    url = RECHARGE_QUOTE_ENDPOINT,
                    method = "POST",
                    body = requestBody.toString(),
                    bearerToken = accessToken
                )
                RechargeQuoteResult(
                    isSuccessful = true,
                    message = parseMessage(response) ?: "Recharge quote created",
                    rawResponse = response
                )
            }.getOrElse { error ->
                Log.e(TAG, "Recharge quote failed", error)
                RechargeQuoteResult(
                    isSuccessful = false,
                    message = error.message ?: "Unable to create recharge quote",
                    rawResponse = null
                )
            }
        }
    }

    suspend fun getWalletBalance(accessToken: String): WalletBalanceResult {
        return withContext(Dispatchers.IO) {
            runCatching {
                val response = executeJsonRequest(
                    url = WALLET_BALANCE_ENDPOINT,
                    method = "GET",
                    bearerToken = accessToken
                )
                WalletBalanceResult(
                    isSuccessful = true,
                    amountInr = parseWalletBalanceAmount(response),
                    message = parseMessage(response)
                )
            }.getOrElse { error ->
                Log.e(TAG, "Wallet balance failed", error)
                WalletBalanceResult(
                    isSuccessful = false,
                    amountInr = 0,
                    message = error.message ?: "Unable to load wallet balance"
                )
            }
        }
    }

    private fun buildDeviceJson(installationId: String): JSONObject {
        return JSONObject().apply {
            put("installationId", installationId)
            put("platform", DEVICE_PLATFORM)
            put("deviceBrand", Build.BRAND.orEmpty().ifBlank { "Android" })
            put("deviceModel", Build.MODEL.orEmpty().ifBlank { "Device" })
            put("osVersion", Build.VERSION.RELEASE.orEmpty().ifBlank { "unknown" })
            put("appVersion", APP_VERSION)
        }
    }

    private fun executeJsonRequest(
        url: String,
        method: String,
        body: String? = null,
        bearerToken: String? = null
    ): String {
        Log.d(TAG, "$method $url")
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            if (!bearerToken.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $bearerToken")
            }
            doInput = true
            doOutput = body != null
        }

        try {
            if (body != null) {
                connection.outputStream.bufferedWriter().use { it.write(body) }
            }

            val responseStream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val response = responseStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "$method $url -> HTTP ${connection.responseCode}: $response")
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException(parseMessage(response) ?: "Request failed")
            }
            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun parseCountryLoginConfig(
        response: String,
        fallbackCountryIso: String
    ): CountryLoginConfig {
        val json = JSONObject(response)
        val countryIso = json.optString("countryIso2", fallbackCountryIso)
        val loginMethods = json.optJSONArray("loginMethods")
        val hasMobileLogin = (0 until (loginMethods?.length() ?: 0)).any { index ->
            loginMethods
                ?.optJSONObject(index)
                ?.optString("method")
                .equals("MOBILE_NUMBER", ignoreCase = true)
        }

        return CountryLoginConfig(
            countryIso = countryIso,
            dialCode = json.optInt("dialCode").takeIf { it != 0 },
            exampleNationalNumber = json.optString("exampleNationalNumber").takeIf { it.isNotBlank() },
            loginMethod = if (countryIso.equals(DEFAULT_COUNTRY_ISO, ignoreCase = true) && hasMobileLogin) {
                LoginMethod.MobileNumber
            } else {
                LoginMethod.Google
            }
        )
    }

    private fun fallbackCountryLoginConfig(countryIso: String): CountryLoginConfig {
        return CountryLoginConfig(
            countryIso = countryIso,
            dialCode = null,
            exampleNationalNumber = null,
            loginMethod = if (countryIso.equals(DEFAULT_COUNTRY_ISO, ignoreCase = true)) {
                LoginMethod.MobileNumber
            } else {
                LoginMethod.Google
            }
        )
    }

    private fun parseOtpRequestResult(response: String): OtpRequestResult {
        val json = JSONObject(response)
        return OtpRequestResult(
            isSuccessful = true,
            debugOtp = findStringValue(json, "otp", "code", "debugOtp", "verificationCode"),
            message = parseMessage(response)
        )
    }

    private fun parseOtpVerifyResult(response: String): OtpVerifyResult {
        val json = JSONObject(response)
        return OtpVerifyResult(
            isVerified = json.optBoolean("verified", true) || json.optBoolean("success", false),
            message = parseMessage(response) ?: "OTP verified",
            accessToken = parseAccessToken(json)
        )
    }

    private fun parseGoogleAuthResult(response: String): GoogleAuthResult {
        val json = JSONObject(response)
        return GoogleAuthResult(
            isSuccessful = json.optBoolean("success", true) || json.optBoolean("verified", false),
            message = parseMessage(response) ?: "Signed in with Google",
            accessToken = parseAccessToken(json)
        )
    }

    private fun parseRechargeOptions(response: String): List<RechargeOption> {
        val root = JSONObject(response)
        val candidates = sequenceOf(
            root.optJSONArray("options"),
            root.optJSONArray("rechargeOptions"),
            root.optJSONArray("data"),
            root.optJSONObject("data")?.optJSONArray("options"),
            root.optJSONObject("data")?.optJSONArray("rechargeOptions"),
            root.optJSONObject("wallet")?.optJSONArray("rechargeOptions")
        ).filterNotNull().firstOrNull()

        return parseRechargeOptionArray(candidates)
            .ifEmpty { fallbackRechargeOptions() }
    }

    private fun parseRechargeOptionArray(array: JSONArray?): List<RechargeOption> {
        if (array == null) return emptyList()

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val hearts = item.optInt("hearts")
                    .takeIf { it > 0 }
                    ?: item.optInt("coins").takeIf { it > 0 }
                    ?: item.optInt("value").takeIf { it > 0 }
                    ?: continue
                val price = item.optInt("price")
                    .takeIf { it > 0 }
                    ?: item.optInt("amount").takeIf { it > 0 }
                    ?: item.optInt("amountInr").takeIf { it > 0 }
                    ?: item.optInt("mrp").takeIf { it > 0 }
                    ?: continue
                val id = item.optString("id")
                    .takeIf { it.isNotBlank() }
                    ?: item.optString("code").takeIf { it.isNotBlank() }
                    ?: "recharge_$index"
                val packCode = item.optString("packCode")
                    .takeIf { it.isNotBlank() }
                    ?: item.optString("code").takeIf { it.isNotBlank() }
                    ?: item.optString("pack_code").takeIf { it.isNotBlank() }
                    ?: item.optString("sku").takeIf { it.isNotBlank() }
                    ?: "HEARTS_$hearts"

                add(
                    RechargeOption(
                        id = id,
                        packCode = packCode,
                        hearts = hearts,
                        price = price,
                        isPopular = item.optBoolean("isPopular") ||
                            item.optBoolean("popular") ||
                            item.optBoolean("recommended")
                    )
                )
            }
        }
    }

    private fun fallbackRechargeOptions(): List<RechargeOption> {
        return listOf(
            RechargeOption(id = "fallback_100", packCode = "HEARTS_100", hearts = 100, price = 99),
            RechargeOption(id = "fallback_250", packCode = "HEARTS_250", hearts = 250, price = 199),
            RechargeOption(id = "fallback_600", packCode = "HEARTS_600", hearts = 600, price = 499, isPopular = true),
            RechargeOption(id = "fallback_1200", packCode = "HEARTS_1200", hearts = 1200, price = 999),
            RechargeOption(id = "fallback_2500", packCode = "HEARTS_2500", hearts = 2500, price = 1499),
            RechargeOption(id = "fallback_4000", packCode = "HEARTS_4000", hearts = 4000, price = 1999)
        )
    }

    private fun parseWalletBalanceAmount(response: String): Int {
        val json = JSONObject(response)
        return findIntValue(
            json,
            "withdrawableAmount",
            "withdrawableBalance",
            "rewardBalance",
            "cashBalance",
            "amountInr",
            "amount",
            "balance",
            "walletBalance"
        ) ?: 0
    }

    private fun parseAccessToken(json: JSONObject): String? {
        return findStringValue(
            json,
            "accessToken",
            "access_token",
            "token",
            "jwt",
            "authToken"
        )
    }

    private fun parseMessage(response: String): String? {
        val json = runCatching { JSONObject(response) }.getOrNull() ?: return null
        return findStringValue(json, "message", "error", "detail")
    }

    private fun findStringValue(json: JSONObject, vararg keys: String): String? {
        keys.forEach { key ->
            val value = json.optString(key).takeIf { it.isNotBlank() }
            if (value != null) return value
        }

        json.keys().forEach { key ->
            val nested = json.optJSONObject(key) ?: return@forEach
            val value = findStringValue(nested, *keys)
            if (value != null) return value
        }

        return null
    }

    private fun findIntValue(json: JSONObject, vararg keys: String): Int? {
        keys.forEach { key ->
            if (json.has(key) && !json.isNull(key)) {
                val value = json.opt(key)
                when (value) {
                    is Number -> return value.toDouble().roundToInt()
                    is String -> value.toDoubleOrNull()?.let { return it.roundToInt() }
                }
            }
        }

        json.keys().forEach { key ->
            val nested = json.optJSONObject(key) ?: return@forEach
            val value = findIntValue(nested, *keys)
            if (value != null) return value
        }

        return null
    }

    private fun String.normalizedCountryIso(): String {
        return ifBlank { DEFAULT_COUNTRY_ISO }.uppercase(Locale.US)
    }

    private fun String.onlyDigits(): String {
        return filter { it.isDigit() }
    }

    private companion object {
        const val TAG = "AuthRepository"
        const val COUNTRIES_ENDPOINT = "https://api.gobff.app/api/v1/auth/countries"
        const val OTP_REQUEST_ENDPOINT = "https://api.gobff.app/api/v1/auth/otp/request"
        const val OTP_VERIFY_ENDPOINT = "https://api.gobff.app/api/v1/auth/otp/verify"
        const val GOOGLE_AUTH_ENDPOINT = "https://api.gobff.app/api/v1/auth/google"
        const val APP_VERSION_ENDPOINT = "https://api.gobff.app/api/v1/app-version"
        const val RECHARGE_OPTIONS_ENDPOINT = "https://api.gobff.app/api/v1/wallet/recharge/options"
        const val RECHARGE_QUOTE_ENDPOINT = "https://api.gobff.app/api/v1/wallet/recharge/quote"
        const val WALLET_BALANCE_ENDPOINT = "https://api.gobff.app/api/v1/wallet/balance"
        const val DEFAULT_COUNTRY_ISO = "IN"
        const val DEFAULT_INSTALLATION_ID = "android-device-1"
        const val DEFAULT_DISPLAY_NAME = "Android User"
        const val DEFAULT_DATE_OF_BIRTH = "1995-01-01"
        const val DEVICE_PLATFORM = "ANDROID"
        const val APP_VERSION = "1.0.0"
    }
}
