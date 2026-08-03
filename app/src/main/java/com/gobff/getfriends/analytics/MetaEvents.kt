package com.gobff.getfriends.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.facebook.appevents.AppEventsLogger

object MetaEvents {
    private const val TAG = "MetaAppEvents"

    private const val EVENT_VIEW_CONTENT = "fb_mobile_content_view"
    private const val EVENT_COMPLETE_REGISTRATION = "fb_mobile_complete_registration"
    private const val EVENT_INITIATE_CHECKOUT = "fb_mobile_initiated_checkout"
    private const val EVENT_PURCHASE = "fb_mobile_purchase"

    private const val PARAM_CONTENT_ID = "fb_content_id"
    private const val PARAM_CONTENT_TYPE = "fb_content_type"
    private const val PARAM_REGISTRATION_METHOD = "fb_registration_method"
    private const val PARAM_CURRENCY = "fb_currency"
    private const val PARAM_ORDER_ID = "order_id"
    private const val PARAM_NUM_ITEMS = "fb_num_items"

    fun logRechargeView(context: Context) {
        logMetaEvent(
            context = context,
            eventName = EVENT_VIEW_CONTENT,
            parameters = Bundle().apply {
                putString(PARAM_CONTENT_ID, "recharge")
                putString(PARAM_CONTENT_TYPE, "wallet_recharge")
            }
        )
    }

    fun logCompleteRegistration(context: Context, method: String) {
        logMetaEvent(
            context = context,
            eventName = EVENT_COMPLETE_REGISTRATION,
            parameters = Bundle().apply {
                putString(PARAM_REGISTRATION_METHOD, method)
            }
        )
    }

    fun logInitiateCheckout(
        context: Context,
        packCode: String,
        hearts: Int,
        amountInr: Double,
        currencyCode: String = "INR"
    ) {
        logMetaEvent(
            context = context,
            eventName = EVENT_INITIATE_CHECKOUT,
            valueToSum = amountInr,
            parameters = Bundle().apply {
                putString(PARAM_CONTENT_ID, packCode)
                putString(PARAM_CONTENT_TYPE, "hearts_recharge")
                putString(PARAM_CURRENCY, currencyCode)
                putInt(PARAM_NUM_ITEMS, hearts)
            }
        )
    }

    fun logPurchase(
        context: Context,
        packCode: String,
        hearts: Int,
        amountInr: Double,
        currencyCode: String = "INR",
        orderId: String? = null
    ) {
        logMetaEvent(
            context = context,
            eventName = EVENT_PURCHASE,
            valueToSum = amountInr,
            parameters = Bundle().apply {
                putString(PARAM_CONTENT_ID, packCode)
                putString(PARAM_CONTENT_TYPE, "hearts_recharge")
                putString(PARAM_CURRENCY, currencyCode)
                putInt(PARAM_NUM_ITEMS, hearts)
                orderId?.takeIf { it.isNotBlank() }?.let { putString(PARAM_ORDER_ID, it) }
            }
        )
    }

    private fun logMetaEvent(
        context: Context,
        eventName: String,
        valueToSum: Double? = null,
        parameters: Bundle
    ) {
        Log.d(
            TAG,
            "Logging event=$eventName valueToSum=${valueToSum ?: "none"} params=${parameters.toDebugString()}"
        )

        runCatching {
            if (valueToSum == null) {
                logger(context).logEvent(eventName, parameters)
            } else {
                logger(context).logEvent(eventName, valueToSum, parameters)
            }
        }.onSuccess {
            Log.d(TAG, "Queued event=$eventName")
        }.onFailure { error ->
            Log.e(TAG, "Failed to queue event=$eventName", error)
        }
    }

    private fun logger(context: Context): AppEventsLogger =
        AppEventsLogger.newLogger(context.applicationContext)

    private fun Bundle.toDebugString(): String =
        keySet()
            .sorted()
            .joinToString(prefix = "{", postfix = "}") { key -> "$key=${get(key)}" }
}
