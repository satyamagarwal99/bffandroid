package com.gobff.getfriends.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.CashfreeCheckoutData
import com.gobff.getfriends.data.model.RechargeOption
import com.gobff.getfriends.data.model.RechargeOrderStatusResponse
import com.gobff.getfriends.data.model.RechargePaymentResolution
import com.gobff.getfriends.data.model.RechargeOptionsResponse
import com.gobff.getfriends.data.model.RechargePurchaseBody
import com.gobff.getfriends.data.model.RechargePurchaseResponse
import com.gobff.getfriends.data.model.RechargeQuoteBody
import com.gobff.getfriends.data.model.RechargeUiState
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class RechargeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()
    private var statusPollingJob: Job? = null

    var uiState by mutableStateOf(RechargeUiState())
        private set

    init {
        loadRechargeOptions()
    }

    fun loadRechargeOptions() {
        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Login token missing",
                    options = emptyList(),
                    selectedOptionId = null
                )
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching { mainRepository.getRechargeOptions(token) }
                .onSuccess { response ->
                    val body = response.body()
                    val options = if (response.isSuccessful && body != null) {
                        parseRechargeOptions(body)
                    } else {
                        emptyList()
                    }
                    uiState = uiState.copy(
                        isLoading = false,
                        options = options,
                        selectedOptionId = uiState.selectedOptionId
                            ?.takeIf { selectedId -> options.any { it.id == selectedId } }
                            ?: options.firstOrNull()?.id,
                        errorMessage = if (response.isSuccessful) null else body?.message ?: "Unable to load recharge options"
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        options = emptyList(),
                        selectedOptionId = null,
                        errorMessage = error.message ?: "Unable to load recharge options"
                    )
                }
        }
    }

    fun selectOption(optionId: String) {
        uiState = uiState.copy(selectedOptionId = optionId)
    }

    fun requestRechargeQuote(couponCode: String = "") {
        val selectedOption = uiState.selectedOption ?: run {
            uiState = uiState.copy(
                isQuoteLoading = false,
                isQuoteSuccessful = false,
                quoteMessage = "Select a recharge pack"
            )
            return
        }

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isQuoteLoading = false,
                    isQuoteSuccessful = false,
                    quoteMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(
                isQuoteLoading = true,
                isQuoteSuccessful = false,
                quoteMessage = null
            )

            val body = RechargeQuoteBody(
                packCode = selectedOption.packCode,
                couponCode = couponCode
            )
            runCatching { mainRepository.getRechargeQuote(token, body) }
                .onSuccess { response ->
                    uiState = uiState.copy(
                        isQuoteLoading = false,
                        isQuoteSuccessful = response.isSuccessful,
                        quoteMessage = response.body()?.message ?: if (response.isSuccessful) {
                            "Recharge quote created"
                        } else {
                            "Unable to create recharge quote"
                        }
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isQuoteLoading = false,
                        isQuoteSuccessful = false,
                        quoteMessage = error.message ?: "Unable to create recharge quote"
                    )
                }
        }
    }

    fun purchaseRecharge(couponCode: String = "") {
        val selectedOption = uiState.selectedOption ?: run {
            uiState = uiState.copy(
                isPurchaseLoading = false,
                isPurchaseSuccessful = false,
                purchaseMessage = "Select a recharge pack",
                checkout = null
            )
            return
        }

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isPurchaseLoading = false,
                    isPurchaseSuccessful = false,
                    purchaseMessage = "Login token missing",
                    checkout = null
                )
                return@launch
            }

            uiState = uiState.copy(
                isPurchaseLoading = true,
                isPurchaseSuccessful = false,
                purchaseMessage = null,
                checkout = null
            )

            val body = RechargePurchaseBody(
                packCode = selectedOption.packCode,
                couponCode = couponCode
            )
            val idempotencyKey = UUID.randomUUID().toString()
            Log.d(
                TAG,
                "Purchase recharge request packCode=${body.packCode} couponCode=${body.couponCode} " +
                    "idempotencyKey=$idempotencyKey"
            )

            runCatching { mainRepository.purchaseRecharge(token, idempotencyKey, body) }
                .onSuccess { response ->
                    val responseBody = response.body()
                    val errorBody = if (response.isSuccessful) null else response.errorBody()?.string()
                    if (!response.isSuccessful) {
                        Log.w(
                            TAG,
                            "Purchase recharge failed status=${response.code()} errorBody=${errorBody.orEmpty()}"
                        )
                    }
                    uiState = uiState.copy(
                        isPurchaseLoading = false,
                        isPurchaseSuccessful = response.isSuccessful,
                        purchaseMessage = responseBody?.message ?: errorBody?.takeIf { it.isNotBlank() } ?: if (response.isSuccessful) {
                            "Complete your payment"
                        } else {
                            "Unable to start payment"
                        },
                        checkout = responseBody?.takeIf { response.isSuccessful }?.let(::parseCashfreeCheckoutData),
                        activeOrderId = responseBody?.takeIf { response.isSuccessful }?.let(::parseOrderId)
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isPurchaseLoading = false,
                        isPurchaseSuccessful = false,
                        purchaseMessage = error.message ?: "Unable to start payment",
                        checkout = null
                    )
                }
        }
    }

    fun markCheckoutLaunched(launchKey: String) {
        uiState = uiState.copy(
            launchedCheckoutKey = launchKey,
            purchaseMessage = "Complete your payment to add hearts."
        )
        uiState.activeOrderId?.takeIf { it.isNotBlank() }?.let { orderId ->
            Log.d(TAG, "Checkout launched; starting status polling for orderId=$orderId")
            pollRechargeStatus(orderId)
        }
    }

    fun markCheckoutLaunchFailed(message: String) {
        uiState = uiState.copy(
            isPurchaseLoading = false,
            isPurchaseSuccessful = false,
            purchaseMessage = message,
            checkout = null
        )
    }

    fun markPaymentReturned(orderId: String?) {
        val resolvedOrderId = resolveRechargeOrderId(orderId)
        if (resolvedOrderId.isNullOrBlank()) {
            uiState = uiState.copy(
                purchaseMessage = "Confirming payment...",
                paymentResolution = RechargePaymentResolution.Pending
            )
            return
        }
        pollRechargeStatus(resolvedOrderId)
    }

    fun markPaymentReturnFailed(orderId: String?, message: String) {
        val resolvedOrderId = resolveRechargeOrderId(orderId)
        if (resolvedOrderId.isNullOrBlank()) {
            uiState = uiState.copy(
                isStatusPolling = false,
                purchaseMessage = message,
                paymentResolution = RechargePaymentResolution.Failed
            )
            return
        }
        pollRechargeStatus(resolvedOrderId)
    }

    private fun resolveRechargeOrderId(callbackOrderId: String?): String? {
        val activeOrderId = uiState.activeOrderId?.takeIf { it.isNotBlank() }
        val cashfreeOrderId = callbackOrderId?.takeIf { it.isNotBlank() }
        if (activeOrderId != null && cashfreeOrderId != null && activeOrderId != cashfreeOrderId) {
            Log.d(TAG, "Using recharge order id=$activeOrderId for status; Cashfree callback id=$cashfreeOrderId")
        }
        return activeOrderId ?: cashfreeOrderId
    }

    fun clearQuoteState() {
        statusPollingJob?.cancel()
        statusPollingJob = null
        uiState = uiState.copy(
            isQuoteLoading = false,
            isQuoteSuccessful = false,
            quoteMessage = null,
            isPurchaseLoading = false,
            isPurchaseSuccessful = false,
            purchaseMessage = null,
            checkout = null,
            launchedCheckoutKey = null,
            activeOrderId = null,
            isStatusPolling = false,
            paymentResolution = null
        )
    }

    private fun pollRechargeStatus(orderId: String) {
        if (uiState.isStatusPolling && uiState.activeOrderId == orderId && statusPollingJob?.isActive == true) {
            return
        }
        statusPollingJob?.cancel()

        statusPollingJob = viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isStatusPolling = false,
                    purchaseMessage = "Login token missing",
                    paymentResolution = RechargePaymentResolution.Pending
                )
                return@launch
            }

            uiState = uiState.copy(
                activeOrderId = orderId,
                isStatusPolling = true,
                purchaseMessage = "Confirming payment...",
                paymentResolution = null
            )

            val deadline = System.currentTimeMillis() + STATUS_POLL_TIMEOUT_MS
            while (System.currentTimeMillis() < deadline) {
                val resolution = runCatching {
                    val response = mainRepository.getRechargeOrderStatus(token, orderId)
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Recharge status failed orderId=$orderId status=${response.code()}")
                        null
                    } else {
                        val body = response.body()
                        Log.d(
                            TAG,
                            "Recharge status orderId=$orderId status=${body?.status} " +
                                "gatewayStatus=${body?.gatewayStatus} credited=${body?.credited}"
                        )
                        body?.let(::parsePaymentResolution)
                    }
                }.getOrElse { error ->
                    Log.e(TAG, "Recharge status request failed orderId=$orderId", error)
                    null
                }

                when (resolution) {
                    RechargePaymentResolution.Success -> {
                        uiState = uiState.copy(
                            isStatusPolling = false,
                            isPurchaseSuccessful = true,
                            purchaseMessage = "Recharge successful",
                            paymentResolution = RechargePaymentResolution.Success
                        )
                        loadRechargeOptions()
                        statusPollingJob = null
                        return@launch
                    }
                    RechargePaymentResolution.Failed -> {
                        uiState = uiState.copy(
                            isStatusPolling = false,
                            isPurchaseSuccessful = false,
                            purchaseMessage = "Payment failed. Please try again or choose another payment method.",
                            paymentResolution = RechargePaymentResolution.Failed
                        )
                        statusPollingJob = null
                        return@launch
                    }
                    RechargePaymentResolution.Pending,
                    null -> {
                        delay(STATUS_POLL_INTERVAL_MS)
                    }
                }
            }

            uiState = uiState.copy(
                isStatusPolling = false,
                isPurchaseSuccessful = false,
                purchaseMessage = "Payment is pending. We'll notify you once it is confirmed.",
                paymentResolution = RechargePaymentResolution.Pending
            )
            statusPollingJob = null
        }
    }

    private fun parseRechargeOptions(body: RechargeOptionsResponse): List<RechargeOption> {
        val items = body.packs
            ?: body.options
            ?: body.rechargeOptions
            ?: body.data?.packs
            ?: body.data?.options
            ?: body.data?.rechargeOptions
            ?: body.wallet?.packs
            ?: body.wallet?.options
            ?: body.wallet?.rechargeOptions
            ?: emptyList()

        return items.mapIndexedNotNull { index, item ->
            val hearts = (item.hearts ?: item.coins ?: item.value)?.takeIf { it > 0 }
                ?: return@mapIndexedNotNull null
            val price = (
                item.price
                    ?: item.amount
                    ?: item.amountInr
                    ?: item.amountPaise?.let { paise -> paise / 100 }
                    ?: item.mrp
                )?.takeIf { it > 0 }
                ?: return@mapIndexedNotNull null
            val id = item.id?.takeIf { it.isNotBlank() }
                ?: item.code?.takeIf { it.isNotBlank() }
                ?: "recharge_$index"
            val packCode = item.packCode?.takeIf { it.isNotBlank() }
                ?: item.code?.takeIf { it.isNotBlank() }
                ?: item.pack_code?.takeIf { it.isNotBlank() }
                ?: item.sku?.takeIf { it.isNotBlank() }
                ?: "HEARTS_$hearts"

            RechargeOption(
                id = id,
                packCode = packCode,
                hearts = hearts,
                price = price,
                isPopular = item.isPopular == true ||
                    item.popular == true ||
                    item.recommended == true ||
                    item.badge.equals("POPULAR", ignoreCase = true)
            )
        }
    }

    private fun parseCashfreeCheckoutData(body: RechargePurchaseResponse): CashfreeCheckoutData {
        val nestedPayload = body.paymentSession?.sdkPayload ?: body.paymentSession?.sdk_payload
        return CashfreeCheckoutData(
            orderId = parseOrderId(body),
            paymentSessionId = body.paymentSessionId
                ?: body.payment_session_id
                ?: body.cashfreePaymentSessionId
                ?: body.cashfree_payment_session_id
                ?: nestedPayload?.paymentSessionId
                ?: nestedPayload?.payment_session_id,
            environment = body.environment
                ?: body.cashfreeEnvironment
                ?: body.cashfree_environment,
            paymentUrl = body.paymentUrl
                ?: body.payment_url
                ?: body.paymentLink
                ?: body.payment_link
                ?: body.redirectUrl
                ?: body.redirect_url
                ?: body.webUrl
                ?: body.web_url
                ?: body.paymentSession?.webPaymentLink
                ?: body.paymentSession?.web_payment_link,
            sdkPayload = (body.sdkPayload
                ?: body.sdk_payload
                ?: body.juspayPayload
                ?: body.hyperSdkPayload
                ?: body.processPayload
                ?: body.paymentPagePayload)?.toString(),
            rawResponse = body.toString()
        )
    }

    private fun parseOrderId(body: RechargePurchaseResponse): String? {
        return body.orderId
            ?: body.order_id
            ?: body.cfOrderId
            ?: body.cf_order_id
            ?: body.paymentSession?.sdkPayload?.orderId
            ?: body.paymentSession?.sdkPayload?.order_id
            ?: body.paymentSession?.sdk_payload?.orderId
            ?: body.paymentSession?.sdk_payload?.order_id
            ?: body.juspayOrderId
            ?: body.paymentOrderId
    }

    private fun parsePaymentResolution(body: RechargeOrderStatusResponse): RechargePaymentResolution {
        if (body.credited == true || body.data?.credited == true) {
            return RechargePaymentResolution.Success
        }

        val status = listOfNotNull(
            body.status,
            body.gatewayStatus,
            body.gateway_status,
            body.orderStatus,
            body.order_status,
            body.paymentStatus,
            body.payment_status,
            body.cashfreeStatus,
            body.cashfree_status,
            body.transactionStatus,
            body.transaction_status,
            body.data?.status,
            body.data?.gatewayStatus,
            body.data?.gateway_status,
            body.data?.orderStatus,
            body.data?.order_status,
            body.data?.paymentStatus,
            body.data?.payment_status,
            body.data?.cashfreeStatus,
            body.data?.cashfree_status,
            body.data?.transactionStatus,
            body.data?.transaction_status
        ).firstOrNull { it.isNotBlank() }?.uppercase() ?: return RechargePaymentResolution.Pending

        return when (status) {
            "PAID", "SUCCESS", "SUCCESSFUL", "COMPLETED", "CHARGED", "CAPTURED" ->
                RechargePaymentResolution.Success
            "FAILED", "FAILURE", "CANCELLED", "CANCELED", "EXPIRED", "VOID", "TERMINATED" ->
                RechargePaymentResolution.Failed
            "ACTIVE", "PENDING", "PAYMENT_PENDING", "PAYMENT_SESSION_CREATED", "CREATED" ->
                RechargePaymentResolution.Pending
            else -> RechargePaymentResolution.Pending
        }
    }

    private companion object {
        const val TAG = "RechargeViewModel"
        const val STATUS_POLL_INTERVAL_MS = 3_000L
        const val STATUS_POLL_TIMEOUT_MS = 600_000L
    }
}
