package com.gobff.getfriends.payment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.gobff.getfriends.data.model.JuspayCheckoutData
import org.json.JSONObject

object JuspayCheckoutLauncher {
    fun launch(activity: Activity, checkout: JuspayCheckoutData): LaunchResult {
        checkout.paymentUrl?.takeIf { it.isNotBlank() }?.let { paymentUrl ->
            return openPaymentUrl(activity, paymentUrl)
        }

        checkout.sdkPayload?.takeIf { it.isNotBlank() }?.let { sdkPayload ->
            return launchHyperSdkIfAvailable(activity, sdkPayload)
        }

        return LaunchResult.Failure("Payment response did not include checkout data")
    }

    private fun openPaymentUrl(activity: Activity, paymentUrl: String): LaunchResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
            activity.startActivity(intent)
            LaunchResult.Launched
        } catch (error: ActivityNotFoundException) {
            Log.e(TAG, "No app can open Juspay payment URL", error)
            LaunchResult.Failure("No app found to open payment page")
        }
    }

    private fun launchHyperSdkIfAvailable(activity: Activity, sdkPayload: String): LaunchResult {
        return runCatching {
            val payload = JSONObject(sdkPayload)
            val hyperServicesClass = Class.forName(HYPER_SERVICES_CLASS)
            val hyperServices = hyperServicesClass
                .getConstructor(Activity::class.java)
                .newInstance(activity)

            hyperServicesClass
                .getMethod("process", JSONObject::class.java)
                .invoke(hyperServices, payload)

            LaunchResult.Launched
        }.getOrElse { error ->
            Log.e(TAG, "Unable to launch Juspay HyperSDK", error)
            LaunchResult.Failure("Juspay SDK is not available in this build")
        }
    }

    sealed interface LaunchResult {
        data object Launched : LaunchResult
        data class Failure(val message: String) : LaunchResult
    }

    private const val TAG = "JuspayCheckoutLauncher"
    private const val HYPER_SERVICES_CLASS = "in.juspay.hypersdk.core.HyperServices"
}
