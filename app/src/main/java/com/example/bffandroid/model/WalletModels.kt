package com.example.bffandroid.model

data class WalletBalanceResult(
    val isSuccessful: Boolean,
    val amountInr: Int,
    val message: String?
)

data class WalletUiState(
    val isLoading: Boolean = true,
    val amountInr: Int = 0,
    val errorMessage: String? = null
)
