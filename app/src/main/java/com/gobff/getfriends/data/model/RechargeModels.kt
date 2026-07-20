package com.gobff.getfriends.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class RechargeOption(
    val id: String,
    val packCode: String,
    val hearts: Int,
    val price: Int,
    val isPopular: Boolean = false
)

data class RechargeQuoteResult(
    val isSuccessful: Boolean,
    val message: String?,
    val rawResponse: String? = null
)

data class RechargePurchaseResult(
    val isSuccessful: Boolean,
    val message: String?,
    val checkout: CashfreeCheckoutData? = null,
    val rawResponse: String? = null
)

data class CashfreeCheckoutData(
    val orderId: String?,
    val paymentSessionId: String?,
    val environment: String?,
    val paymentUrl: String?,
    val sdkPayload: String?,
    val rawResponse: String
) {
    val hasCashfreeSession: Boolean
        get() = !orderId.isNullOrBlank() && !paymentSessionId.isNullOrBlank()

    val launchKey: String
        get() = orderId
            ?: paymentSessionId
            ?: paymentUrl
            ?: sdkPayload?.hashCode()?.toString()
            ?: rawResponse.hashCode().toString()
}

typealias JuspayCheckoutData = CashfreeCheckoutData

data class RechargeOptionsResult(
    val isSuccessful: Boolean,
    val options: List<RechargeOption>,
    val message: String?
)

data class RechargeUiState(
    val isLoading: Boolean = true,
    val options: List<RechargeOption> = emptyList(),
    val selectedOptionId: String? = null,
    val errorMessage: String? = null,
    val isQuoteLoading: Boolean = false,
    val isQuoteSuccessful: Boolean = false,
    val quoteMessage: String? = null,
    val isPurchaseLoading: Boolean = false,
    val isPurchaseSuccessful: Boolean = false,
    val purchaseMessage: String? = null,
    val checkout: CashfreeCheckoutData? = null,
    val launchedCheckoutKey: String? = null,
    val activeOrderId: String? = null,
    val isStatusPolling: Boolean = false,
    val paymentResolution: RechargePaymentResolution? = null
) {
    val selectedOption: RechargeOption?
        get() = options.firstOrNull { it.id == selectedOptionId }
}

enum class RechargePaymentResolution {
    Success,
    Failed,
    Pending
}

data class RechargeQuoteBody(
    @SerializedName("packCode") val packCode: String,
    @SerializedName("couponCode") val couponCode: String
)

data class RechargeQuoteResponse(
    @SerializedName("message") val message: String?
)

data class RechargePurchaseBody(
    @SerializedName("packCode") val packCode: String,
    @SerializedName("couponCode") val couponCode: String
)

data class RechargePurchaseResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("gatewayOrderId") val gatewayOrderId: String?,
    @SerializedName("orderId") val orderId: String?,
    @SerializedName("order_id") val order_id: String?,
    @SerializedName("cfOrderId") val cfOrderId: String?,
    @SerializedName("cf_order_id") val cf_order_id: String?,
    @SerializedName("juspayOrderId") val juspayOrderId: String?,
    @SerializedName("paymentOrderId") val paymentOrderId: String?,
    @SerializedName("paymentSessionId") val paymentSessionId: String?,
    @SerializedName("payment_session_id") val payment_session_id: String?,
    @SerializedName("cashfreePaymentSessionId") val cashfreePaymentSessionId: String?,
    @SerializedName("cashfree_payment_session_id") val cashfree_payment_session_id: String?,
    @SerializedName("environment") val environment: String?,
    @SerializedName("cashfreeEnvironment") val cashfreeEnvironment: String?,
    @SerializedName("cashfree_environment") val cashfree_environment: String?,
    @SerializedName("paymentUrl") val paymentUrl: String?,
    @SerializedName("payment_url") val payment_url: String?,
    @SerializedName("paymentLink") val paymentLink: String?,
    @SerializedName("payment_link") val payment_link: String?,
    @SerializedName("redirectUrl") val redirectUrl: String?,
    @SerializedName("redirect_url") val redirect_url: String?,
    @SerializedName("webUrl") val webUrl: String?,
    @SerializedName("web_url") val web_url: String?,
    @SerializedName("sdkPayload") val sdkPayload: JsonElement?,
    @SerializedName("sdk_payload") val sdk_payload: JsonElement?,
    @SerializedName("juspayPayload") val juspayPayload: JsonElement?,
    @SerializedName("hyperSdkPayload") val hyperSdkPayload: JsonElement?,
    @SerializedName("processPayload") val processPayload: JsonElement?,
    @SerializedName("paymentPagePayload") val paymentPagePayload: JsonElement?,
    @SerializedName("paymentSession") val paymentSession: RechargePaymentSession?
)

data class RechargePaymentSession(
    @SerializedName("webPaymentLink") val webPaymentLink: String?,
    @SerializedName("web_payment_link") val web_payment_link: String?,
    @SerializedName("sdkPayload") val sdkPayload: RechargePaymentSessionPayload?,
    @SerializedName("sdk_payload") val sdk_payload: RechargePaymentSessionPayload?
)

data class RechargePaymentSessionPayload(
    @SerializedName("orderId") val orderId: String?,
    @SerializedName("order_id") val order_id: String?,
    @SerializedName("paymentSessionId") val paymentSessionId: String?,
    @SerializedName("payment_session_id") val payment_session_id: String?
)

data class RechargeOrderStatusResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("gatewayStatus") val gatewayStatus: String?,
    @SerializedName("gateway_status") val gateway_status: String?,
    @SerializedName("credited") val credited: Boolean?,
    @SerializedName("orderStatus") val orderStatus: String?,
    @SerializedName("order_status") val order_status: String?,
    @SerializedName("paymentStatus") val paymentStatus: String?,
    @SerializedName("payment_status") val payment_status: String?,
    @SerializedName("cashfreeStatus") val cashfreeStatus: String?,
    @SerializedName("cashfree_status") val cashfree_status: String?,
    @SerializedName("transactionStatus") val transactionStatus: String?,
    @SerializedName("transaction_status") val transaction_status: String?,
    @SerializedName("data") val data: RechargeOrderStatusContainer?
)

data class RechargeOrderStatusContainer(
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("gatewayStatus") val gatewayStatus: String?,
    @SerializedName("gateway_status") val gateway_status: String?,
    @SerializedName("credited") val credited: Boolean?,
    @SerializedName("orderStatus") val orderStatus: String?,
    @SerializedName("order_status") val order_status: String?,
    @SerializedName("paymentStatus") val paymentStatus: String?,
    @SerializedName("payment_status") val payment_status: String?,
    @SerializedName("cashfreeStatus") val cashfreeStatus: String?,
    @SerializedName("cashfree_status") val cashfree_status: String?,
    @SerializedName("transactionStatus") val transactionStatus: String?,
    @SerializedName("transaction_status") val transaction_status: String?
)

data class RechargeOptionsResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("packs") val packs: List<RechargeOptionDto>?,
    @SerializedName("options") val options: List<RechargeOptionDto>?,
    @SerializedName("rechargeOptions") val rechargeOptions: List<RechargeOptionDto>?,
    @SerializedName("data") val data: RechargeOptionsContainer?,
    @SerializedName("wallet") val wallet: RechargeOptionsContainer?
)

data class RechargeOptionsContainer(
    @SerializedName("packs") val packs: List<RechargeOptionDto>?,
    @SerializedName("options") val options: List<RechargeOptionDto>?,
    @SerializedName("rechargeOptions") val rechargeOptions: List<RechargeOptionDto>?
)

data class RechargeOptionDto(
    @SerializedName("id") val id: String?,
    @SerializedName("code") val code: String?,
    @SerializedName("packCode") val packCode: String?,
    @SerializedName("pack_code") val pack_code: String?,
    @SerializedName("sku") val sku: String?,
    @SerializedName("hearts") val hearts: Int?,
    @SerializedName("coins") val coins: Int?,
    @SerializedName("value") val value: Int?,
    @SerializedName("price") val price: Int?,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("amountInr") val amountInr: Int?,
    @SerializedName("amountPaise") val amountPaise: Int?,
    @SerializedName("mrp") val mrp: Int?,
    @SerializedName("badge") val badge: String?,
    @SerializedName("isPopular") val isPopular: Boolean?,
    @SerializedName("popular") val popular: Boolean?,
    @SerializedName("recommended") val recommended: Boolean?
)
