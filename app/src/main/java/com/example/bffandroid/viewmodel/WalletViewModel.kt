package com.example.bffandroid.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bffandroid.data.model.WalletUiState
import com.example.bffandroid.data.MainRepository
import com.example.bffandroid.utils.TokenUtils
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
            runCatching { mainRepository.getWalletBalance(token) }
                .onSuccess { response ->
                    val body = response.body()
                    uiState = uiState.copy(
                        isLoading = false,
                        hearts = if (response.isSuccessful && body != null) {
                            body.hearts ?: uiState.hearts
                        } else {
                            uiState.hearts
                        },
                        amountInr = if (response.isSuccessful && body != null) {
                            body.withdrawableAmount
                                ?: body.withdrawableBalance
                                ?: body.rewardBalance
                                ?: body.cashBalance
                                ?: body.amountInr
                                ?: body.amount
                                ?: body.balance
                                ?: body.walletBalance
                                ?: 0
                        } else {
                            0
                        },
                        errorMessage = if (response.isSuccessful) null else body?.message ?: "Unable to load wallet balance"
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        amountInr = 0,
                        errorMessage = error.message ?: "Unable to load wallet balance"
                    )
                }
        }
    }
}
