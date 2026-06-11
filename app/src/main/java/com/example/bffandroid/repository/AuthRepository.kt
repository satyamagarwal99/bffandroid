package com.example.bffandroid.repository

import android.os.Build
import android.util.Log
import com.example.bffandroid.model.CountryLoginConfig
import com.example.bffandroid.model.LoginMethod
import com.example.bffandroid.model.OtpRequestResult
import com.example.bffandroid.model.OtpVerifyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

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
                val deviceJson = JSONObject().apply {
                    put("installationId", installationId)
                    put("platform", DEVICE_PLATFORM)
                    put("deviceBrand", Build.BRAND.orEmpty().ifBlank { "Android" })
                    put("deviceModel", Build.MODEL.orEmpty().ifBlank { "Device" })
                    put("osVersion", Build.VERSION.RELEASE.orEmpty().ifBlank { "unknown" })
                    put("appVersion", APP_VERSION)
                }
                val requestBody = JSONObject().apply {
                    put("countryIso2", countryIso.normalizedCountryIso())
                    put("phoneNumber", phoneNumber.onlyDigits())
                    put("otp", otp.onlyDigits())
                    put("device", deviceJson)
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

    private fun executeJsonRequest(
        url: String,
        method: String,
        body: String? = null
    ): String {
        Log.d(TAG, "$method $url")
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
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
            message = parseMessage(response) ?: "OTP verified"
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
        const val OTP_VERIFY_ENDPOINT = "http://localhost:8080/api/v1/auth/otp/verify"
        const val DEFAULT_COUNTRY_ISO = "IN"
        const val DEFAULT_INSTALLATION_ID = "android-device-1"
        const val DEFAULT_DISPLAY_NAME = "Android User"
        const val DEVICE_PLATFORM = "ANDROID"
        const val APP_VERSION = "1.0.0"
    }
}
