package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.WalletBalanceResponse
import com.gobff.getfriends.data.model.WalletUiState
import com.gobff.getfriends.utils.TokenUtils
import com.gobff.getfriends.utils.userFacingMessage
import kotlinx.coroutines.launch

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
                            amountInr = body.withdrawableAmountInr() ?: 0
                        )
                    } else {
                        errorMessage = body?.message ?: "Unable to load wallet balance"
                    }
                }
                .onFailure { error ->
                    errorMessage = error.userFacingMessage("Unable to load wallet balance")
                    nextState = nextState.copy(amountInr = 0)
                }

            runCatching { mainRepository.getCoinSummary(token) }
                .onSuccess { response ->
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        nextState = nextState.copy(
                            coins = body.coinBalanceAmount() ?: nextState.coins
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
        }
    }

    private fun WalletBalanceResponse.heartBalanceAmount(): Int? {
        return hearts ?: data?.hearts
    }

    private fun WalletBalanceResponse.coinBalanceAmount(): Int? {
        return coinBalance
            ?: coin_balance
            ?: coins
            ?: data?.coinBalance
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
}
