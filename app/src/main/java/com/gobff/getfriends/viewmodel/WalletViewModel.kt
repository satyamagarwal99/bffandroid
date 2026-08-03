package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.model.CreateCoinWithdrawalBody
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.WalletBalanceResponse
import com.gobff.getfriends.data.model.WalletWithdrawalItem
import com.gobff.getfriends.data.model.WalletUiState
import com.gobff.getfriends.utils.TokenUtils
import com.gobff.getfriends.utils.userFacingMessage
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import java.util.UUID

class WalletViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var uiState by mutableStateOf(WalletUiState())
        private set

    init {
        loadWalletBalance()
    }

    fun loadWalletBalance() {
        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isLoading = false,
                    amountInr = 0,
                    amountPaise = 0,
                    currentCoinValuePaise = 0,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)
            var nextState = uiState
            var errorMessage: String? = null

            runCatching { mainRepository.getWalletBalance(token) }
                .onSuccess { response ->
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        nextState = nextState.copy(
                            hearts = body.heartBalanceAmount() ?: nextState.hearts,
                            coins = body.coinBalanceAmount() ?: nextState.coins,
                            amountInr = body.withdrawableAmountInr() ?: 0,
                            amountPaise = body.withdrawableAmountPaise() ?: 0,
                            currentCoinValuePaise = body.currentCoinValuePaiseAmount()
                                ?: nextState.currentCoinValuePaise
                        )
                    } else {
                        errorMessage = body?.message ?: "Unable to load wallet balance"
                    }
                }
                .onFailure { error ->
                    errorMessage = error.userFacingMessage("Unable to load wallet balance")
                    nextState = nextState.copy(amountInr = 0, amountPaise = 0)
                }

            runCatching { mainRepository.getCoinSummary(token) }
                .onSuccess { response ->
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        nextState = nextState.copy(
                            coins = body.coinBalanceAmount() ?: nextState.coins,
                            amountInr = body.withdrawableAmountInr() ?: nextState.amountInr,
                            amountPaise = body.withdrawableAmountPaise() ?: nextState.amountPaise,
                            currentCoinValuePaise = body.currentCoinValuePaiseAmount()
                                ?: nextState.currentCoinValuePaise
                        )
                    } else if (errorMessage == null) {
                        errorMessage = body?.message ?: "Unable to load coin balance"
                    }
                }
                .onFailure { error ->
                    if (errorMessage == null) {
                        errorMessage = error.userFacingMessage("Unable to load coin balance")
                    }
                }

            uiState = nextState.copy(
                isLoading = false,
                errorMessage = errorMessage
            )
            loadWithdrawals()
        }
    }

    fun loadWithdrawals() {
        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isWithdrawalsLoading = false,
                    withdrawalErrorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isWithdrawalsLoading = true, withdrawalErrorMessage = null)
            runCatching { mainRepository.getCoinWithdrawals(token) }
                .onSuccess { response ->
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        uiState = uiState.copy(
                            isWithdrawalsLoading = false,
                            withdrawals = body.toWithdrawalItems(),
                            withdrawalErrorMessage = null
                        )
                    } else {
                        uiState = uiState.copy(
                            isWithdrawalsLoading = false,
                            withdrawalErrorMessage = "Unable to load withdrawals"
                        )
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isWithdrawalsLoading = false,
                        withdrawalErrorMessage = error.userFacingMessage("Unable to load withdrawals")
                    )
                }
        }
    }

    fun createWithdrawal(
        name: String,
        pan: String,
        upiId: String,
        coinAmount: Int,
        onSuccess: () -> Unit = {}
    ) {
        if (uiState.isSubmittingWithdrawal) return
        val token = TokenUtils.getToken()
        if (token.isBlank()) {
            uiState = uiState.copy(withdrawalSubmitMessage = "Login token missing")
            return
        }
        if (coinAmount <= MIN_WITHDRAWAL_COINS) {
            uiState = uiState.copy(withdrawalSubmitMessage = "Minimum 101 coins required to redeem")
            return
        }

        viewModelScope.launch {
            val estimatedAmountPaise = coinAmount * uiState.currentCoinValuePaise
            uiState = uiState.copy(
                isSubmittingWithdrawal = true,
                withdrawalSubmitMessage = null,
                lastWithdrawalAmountPaise = estimatedAmountPaise
            )

            runCatching {
                mainRepository.createCoinWithdrawal(
                    bearerToken = token,
                    idempotencyKey = UUID.randomUUID().toString(),
                    body = CreateCoinWithdrawalBody(
                        name = name.trim(),
                        pan = pan.trim().uppercase(),
                        upiId = upiId.trim(),
                        coinAmount = coinAmount
                    )
                )
            }.onSuccess { response ->
                val body = response.body()
                if (response.isSuccessful) {
                    val amountPaise = body?.findFirstObject()
                        ?.amountPaiseFromObject()
                        ?: estimatedAmountPaise
                    uiState = uiState.copy(
                        isSubmittingWithdrawal = false,
                        withdrawalSubmitMessage = null,
                        lastWithdrawalAmountPaise = amountPaise
                    )
                    loadWalletBalance()
                    loadWithdrawals()
                    onSuccess()
                } else {
                    uiState = uiState.copy(
                        isSubmittingWithdrawal = false,
                        withdrawalSubmitMessage = response.errorBody()?.string()
                            ?.takeIf { it.isNotBlank() }
                            ?.extractJsonMessage()
                            ?: "Unable to create withdrawal"
                    )
                }
            }.onFailure { error ->
                uiState = uiState.copy(
                    isSubmittingWithdrawal = false,
                    withdrawalSubmitMessage = error.userFacingMessage("Unable to create withdrawal")
                )
            }
        }
    }

    private fun WalletBalanceResponse.heartBalanceAmount(): Int? {
        return hearts ?: data?.hearts
    }

    private fun WalletBalanceResponse.coinBalanceAmount(): Int? {
        return coinBalance
            ?: flowerBalance
            ?: coin_balance
            ?: coins
            ?: data?.coinBalance
            ?: data?.flowerBalance
            ?: data?.coin_balance
            ?: data?.coins
            ?: balance
            ?: walletBalance
            ?: data?.balance
            ?: data?.walletBalance
    }

    private fun WalletBalanceResponse.withdrawableAmountInr(): Int? {
        return withdrawableAmount
            ?: withdrawableBalance
            ?: rewardBalance
            ?: cashBalance
            ?: amountInr
            ?: amount
            ?: data?.withdrawableAmount
            ?: data?.withdrawableBalance
            ?: data?.rewardBalance
            ?: data?.cashBalance
            ?: data?.amountInr
            ?: data?.amount
    }

    private fun WalletBalanceResponse.withdrawableAmountPaise(): Int? {
        return estimatedPayoutPaise
            ?: data?.estimatedPayoutPaise
            ?: withdrawableAmount?.times(100)
            ?: withdrawableBalance?.times(100)
            ?: rewardBalance?.times(100)
            ?: cashBalance?.times(100)
            ?: amountInr?.times(100)
            ?: amount?.times(100)
            ?: data?.withdrawableAmount?.times(100)
            ?: data?.withdrawableBalance?.times(100)
            ?: data?.rewardBalance?.times(100)
            ?: data?.cashBalance?.times(100)
            ?: data?.amountInr?.times(100)
            ?: data?.amount?.times(100)
    }

    private fun WalletBalanceResponse.currentCoinValuePaiseAmount(): Int? {
        return currentCoinValuePaise
            ?: data?.currentCoinValuePaise
            ?: currentFlowerValuePaise
            ?: data?.currentFlowerValuePaise
    }

    private fun JsonElement.toWithdrawalItems(): List<WalletWithdrawalItem> {
        val array = when {
            isJsonArray -> asJsonArray
            isJsonObject -> {
                val obj = asJsonObject
                when {
                    obj.get("withdrawals")?.isJsonArray == true -> obj.getAsJsonArray("withdrawals")
                    obj.get("data")?.isJsonArray == true -> obj.getAsJsonArray("data")
                    obj.get("items")?.isJsonArray == true -> obj.getAsJsonArray("items")
                    obj.get("results")?.isJsonArray == true -> obj.getAsJsonArray("results")
                    else -> null
                }
            }
            else -> null
        } ?: return emptyList()

        return array.mapIndexedNotNull { index, element ->
            val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapIndexedNotNull null
            val coinAmount = obj.firstInt("coinAmount", "coins", "flowerAmount", "flowerBalance", "amountCoins") ?: 0
            WalletWithdrawalItem(
                id = obj.firstString("id", "withdrawalId", "_id") ?: index.toString(),
                status = obj.firstString("status", "state") ?: "PENDING",
                createdAt = obj.firstString("createdAt", "requestedAt", "submittedAt", "updatedAt"),
                amountPaise = obj.amountPaiseFromObject(),
                coinAmount = coinAmount
            )
        }
    }

    private fun JsonElement.findFirstObject(): JsonObject? {
        return when {
            isJsonObject -> {
                val obj = asJsonObject
                obj.get("data")?.takeIf { it.isJsonObject }?.asJsonObject
                    ?: obj.get("withdrawal")?.takeIf { it.isJsonObject }?.asJsonObject
                    ?: obj
            }
            else -> null
        }
    }

    private fun JsonObject.amountPaiseFromObject(): Int {
        firstInt("amountPaise", "estimatedPayoutPaise", "payoutPaise", "withdrawablePaise")?.let {
            return it
        }
        firstInt("amountInr", "amount", "payoutAmount", "withdrawableAmount")?.let {
            return it * 100
        }
        return 0
    }

    private fun JsonObject.firstString(vararg keys: String): String? {
        return keys.firstNotNullOfOrNull { key ->
            get(key)?.takeIf { !it.isJsonNull }?.asString?.trim()?.takeIf { it.isNotBlank() }
        }
    }

    private fun JsonObject.firstInt(vararg keys: String): Int? {
        return keys.firstNotNullOfOrNull { key ->
            get(key)?.takeIf { !it.isJsonNull }?.let { value ->
                runCatching { value.asInt }.getOrNull()
            }
        }
    }

    private fun String.extractJsonMessage(): String {
        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(this)
            ?.groupValues
            ?.getOrNull(1)
            ?: this
    }

    private companion object {
        const val MIN_WITHDRAWAL_COINS = 100
    }
}
