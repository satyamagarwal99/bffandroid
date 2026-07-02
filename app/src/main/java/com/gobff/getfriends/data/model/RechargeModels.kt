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
    val checkout: JuspayCheckoutData? = null,
    val rawResponse: String? = null
)

data class JuspayCheckoutData(
    val orderId: String?,
    val paymentUrl: String?,
    val sdkPayload: String?,
    val rawResponse: String
) {
    val launchKey: String
        get() = orderId ?: paymentUrl ?: sdkPayload?.hashCode()?.toString() ?: rawResponse.hashCode().toString()
}

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
    val checkout: JuspayCheckoutData? = null,
    val launchedCheckoutKey: String? = null
) {
    val selectedOption: RechargeOption?
        get() = options.firstOrNull { it.id == selectedOptionId }
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
    @SerializedName("couponCode") val couponCode: String,
    @SerializedName("customerEmail") val customerEmail: String,
    @SerializedName("customerPhone") val customerPhone: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("returnUrl") val returnUrl: String,
    @SerializedName("description") val description: String
)

data class RechargePurchaseResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("orderId") val orderId: String?,
    @SerializedName("order_id") val order_id: String?,
    @SerializedName("juspayOrderId") val juspayOrderId: String?,
    @SerializedName("paymentOrderId") val paymentOrderId: String?,
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
    @SerializedName("paymentPagePayload") val paymentPagePayload: JsonElement?
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
