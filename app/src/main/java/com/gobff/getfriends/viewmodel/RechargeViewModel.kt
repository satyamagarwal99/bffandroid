package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.JuspayCheckoutData
import com.gobff.getfriends.data.model.RechargeOption
import com.gobff.getfriends.data.model.RechargeOptionsResponse
import com.gobff.getfriends.data.model.RechargePurchaseBody
import com.gobff.getfriends.data.model.RechargePurchaseResponse
import com.gobff.getfriends.data.model.RechargeQuoteBody
import com.gobff.getfriends.data.model.RechargeUiState
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.launch
import java.util.UUID

class RechargeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

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
                couponCode = couponCode,
                customerEmail = "test@mail.com",
                customerPhone = "9876543210",
                firstName = "John",
                lastName = "Wick",
                returnUrl = "https://shop.merchant.com",
                description = "Complete your payment"
            )
            val idempotencyKey = "wallet-recharge-${UUID.randomUUID()}"

            runCatching { mainRepository.purchaseRecharge(token, idempotencyKey, body) }
                .onSuccess { response ->
                    val responseBody = response.body()
                    uiState = uiState.copy(
                        isPurchaseLoading = false,
                        isPurchaseSuccessful = response.isSuccessful,
                        purchaseMessage = responseBody?.message ?: if (response.isSuccessful) {
                            "Complete your payment"
                        } else {
                            "Unable to start payment"
                        },
                        checkout = responseBody?.takeIf { response.isSuccessful }?.let(::parseJuspayCheckoutData)
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
    }

    fun markCheckoutLaunchFailed(message: String) {
        uiState = uiState.copy(
            isPurchaseLoading = false,
            isPurchaseSuccessful = false,
            purchaseMessage = message,
            checkout = null
        )
    }

    fun clearQuoteState() {
        uiState = uiState.copy(
            isQuoteLoading = false,
            isQuoteSuccessful = false,
            quoteMessage = null,
            isPurchaseLoading = false,
            isPurchaseSuccessful = false,
            purchaseMessage = null,
            checkout = null,
            launchedCheckoutKey = null
        )
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

    private fun parseJuspayCheckoutData(body: RechargePurchaseResponse): JuspayCheckoutData {
        return JuspayCheckoutData(
            orderId = body.orderId ?: body.order_id ?: body.juspayOrderId ?: body.paymentOrderId,
            paymentUrl = body.paymentUrl
                ?: body.payment_url
                ?: body.paymentLink
                ?: body.payment_link
                ?: body.redirectUrl
                ?: body.redirect_url
                ?: body.webUrl
                ?: body.web_url,
            sdkPayload = (body.sdkPayload
                ?: body.sdk_payload
                ?: body.juspayPayload
                ?: body.hyperSdkPayload
                ?: body.processPayload
                ?: body.paymentPagePayload)?.toString(),
            rawResponse = body.toString()
        )
    }
}
