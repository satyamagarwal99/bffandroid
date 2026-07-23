package com.gobff.getfriends.payment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.cashfree.pg.api.CFPaymentGatewayService
import com.cashfree.pg.core.api.CFSession
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback
import com.cashfree.pg.core.api.utils.CFErrorResponse
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment
import com.cashfree.pg.ui.api.upi.intent.CFUPIIntentCheckout
import com.cashfree.pg.ui.api.upi.intent.CFUPIIntentCheckoutPayment
import com.gobff.getfriends.BuildConfig
import com.gobff.getfriends.data.model.CashfreeCheckoutData

object CashfreePaymentLauncher {
    fun installedUpiApps(activity: Activity): List<UpiPaymentApp> {
        val packageManager = activity.packageManager
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay"))
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        val installed = resolveInfos
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
                UpiPaymentApp(
                    label = info.loadLabel(packageManager)?.toString()?.takeIf { it.isNotBlank() }
                        ?: packageName,
                    packageName = packageName
                )
            }
            .distinctBy { it.packageName }

        val cashfreeOrder = CFUPIIntentCheckout.CFUPIApps.values()
            .map { it.appID }
            .withIndex()
            .associate { it.value to it.index }

        return installed.sortedWith(
            compareBy<UpiPaymentApp> { cashfreeOrder[it.packageName] ?: Int.MAX_VALUE }
                .thenBy { it.label.lowercase() }
        )
    }

    fun launchUpiIntent(
        activity: Activity,
        checkout: CashfreeCheckoutData,
        selectedPackageName: String,
        onReturn: (orderId: String) -> Unit,
        onFailure: (orderId: String?, message: String) -> Unit
    ): LaunchResult {
        Log.d(
            TAG,
            "Launching Cashfree UPI intent orderId=${checkout.orderId.orEmpty()} " +
                "selectedPackage=$selectedPackageName hasSession=${checkout.hasCashfreeSession}"
        )
        if (!checkout.hasCashfreeSession) {
            Log.d(TAG, "Cashfree session missing; using fallback checkout orderId=${checkout.orderId.orEmpty()}")
            return fallback(activity, checkout, onReturn, onFailure)
        }

        return runCatching {
            initialize(activity, onReturn, onFailure)
            val session = checkout.session()
            val upiCheckout = CFUPIIntentCheckout.CFUPIIntentBuilder()
                .setOrderUsingPackageName(
                    orderedPackageNames(selectedPackageName)
                )
                .build()
            val payment = CFUPIIntentCheckoutPayment.CFUPIIntentPaymentBuilder()
                .setSession(session)
                .setCfUPIIntentCheckout(upiCheckout)
                .build()

            CFPaymentGatewayService.getInstance().doPayment(activity, payment)
            Log.d(TAG, "Cashfree UPI intent launched orderId=${checkout.orderId.orEmpty()}")
            LaunchResult.Launched
        }.getOrElse { error ->
            Log.e(TAG, "Unable to launch Cashfree UPI intent", error)
            fallback(activity, checkout, onReturn, onFailure)
        }
    }

    fun fallback(
        activity: Activity,
        checkout: CashfreeCheckoutData,
        onReturn: (orderId: String) -> Unit,
        onFailure: (orderId: String?, message: String) -> Unit
    ): LaunchResult {
        Log.d(
            TAG,
            "Launching Cashfree fallback orderId=${checkout.orderId.orEmpty()} " +
                "hasSession=${checkout.hasCashfreeSession} hasPaymentUrl=${!checkout.paymentUrl.isNullOrBlank()}"
        )
        if (checkout.hasCashfreeSession) {
            runCatching {
                initialize(activity, onReturn, onFailure)
                val payment = CFWebCheckoutPayment.CFWebCheckoutPaymentBuilder()
                    .setSession(checkout.session())
                    .build()

                CFPaymentGatewayService.getInstance().doPayment(activity, payment)
                Log.d(TAG, "Cashfree web checkout launched orderId=${checkout.orderId.orEmpty()}")
                return LaunchResult.Launched
            }.onFailure { error ->
                Log.e(TAG, "Unable to launch Cashfree web checkout", error)
            }
        }

        checkout.paymentUrl?.takeIf { it.isNotBlank() }?.let { paymentUrl ->
            return try {
                Log.d(TAG, "Opening external payment URL orderId=${checkout.orderId.orEmpty()}")
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl)))
                LaunchResult.Launched
            } catch (error: ActivityNotFoundException) {
                Log.e(TAG, "No app can open payment URL", error)
                LaunchResult.Failure("No app found to open payment page")
            }
        }

        return LaunchResult.Failure("Payment response did not include checkout data")
    }

    private fun initialize(
        activity: Activity,
        onReturn: (orderId: String) -> Unit,
        onFailure: (orderId: String?, message: String) -> Unit
    ) {
        CFPaymentGatewayService.initialize(activity.applicationContext)
        CFPaymentGatewayService.getInstance().setCheckoutCallback(
            object : CFCheckoutResponseCallback {
                override fun onPaymentVerify(orderID: String) {
                    Log.d(TAG, "Cashfree verify callback orderId=$orderID")
                    onReturn(orderID)
                }

                override fun onPaymentFailure(cfErrorResponse: CFErrorResponse, orderID: String) {
                    val message = cfErrorResponse.message ?: "Payment could not be completed"
                    Log.d(TAG, "Cashfree failure callback orderId=$orderID message=$message")
                    onFailure(orderID, message)
                }
            }
        )
    }

    private fun CashfreeCheckoutData.session(): CFSession {
        return CFSession.CFSessionBuilder()
            .setOrderId(requireNotNull(orderId))
            .setPaymentSessionID(requireNotNull(paymentSessionId))
            .setEnvironment(cashfreeEnvironment(environment))
            .build()
    }

    private fun cashfreeEnvironment(environment: String?): CFSession.Environment {
        return when {
            environment.equals("PRODUCTION", ignoreCase = true) ||
                environment.equals("PROD", ignoreCase = true) -> CFSession.Environment.PRODUCTION
            environment.equals("SANDBOX", ignoreCase = true) ||
                environment.equals("TEST", ignoreCase = true) -> CFSession.Environment.SANDBOX
            BuildConfig.DEBUG -> CFSession.Environment.SANDBOX
            else -> CFSession.Environment.PRODUCTION
        }
    }

    private fun orderedPackageNames(selectedPackageName: String): List<String> {
        val defaults = CFUPIIntentCheckout.CFUPIApps.values().map { it.appID }
        return (listOf(selectedPackageName) + defaults).distinct()
    }

    data class UpiPaymentApp(
        val label: String,
        val packageName: String
    )

    sealed interface LaunchResult {
        data object Launched : LaunchResult
        data class Failure(val message: String) : LaunchResult
    }

    private const val TAG = "CashfreePaymentLauncher"
}
