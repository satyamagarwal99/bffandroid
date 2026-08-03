package com.gobff.getfriends.data.model

import com.google.gson.annotations.SerializedName

data class WalletBalanceResult(
    val isSuccessful: Boolean,
    val amountInr: Int,
    val message: String?
)

data class WalletUiState(
    val isLoading: Boolean = true,
    val isWithdrawalsLoading: Boolean = false,
    val isSubmittingWithdrawal: Boolean = false,
    val coins: Int = 0,
    val hearts: Int = 0,
    val amountInr: Int = 0,
    val amountPaise: Int = 0,
    val currentCoinValuePaise: Int = 0,
    val withdrawals: List<WalletWithdrawalItem> = emptyList(),
    val withdrawalErrorMessage: String? = null,
    val withdrawalSubmitMessage: String? = null,
    val lastWithdrawalAmountPaise: Int = 0,
    val errorMessage: String? = null
)

data class CreateCoinWithdrawalBody(
    @SerializedName("name") val name: String,
    @SerializedName("pan") val pan: String,
    @SerializedName("upiId") val upiId: String,
    @SerializedName("coinAmount") val coinAmount: Int
)

data class WalletWithdrawalItem(
    val id: String,
    val title: String = "Withdrawal",
    val status: String,
    val createdAt: String?,
    val amountPaise: Int,
    val coinAmount: Int
)

data class WalletBalanceResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("hearts") val hearts: Int?,
    @SerializedName("coins") val coins: Int?,
    @SerializedName("flowerBalance") val flowerBalance: Int?,
    @SerializedName("currentCoinValuePaise") val currentCoinValuePaise: Int?,
    @SerializedName("currentFlowerValuePaise") val currentFlowerValuePaise: Int?,
    @SerializedName("coinBalance") val coinBalance: Int?,
    @SerializedName("coin_balance") val coin_balance: Int?,
    @SerializedName("withdrawableAmount") val withdrawableAmount: Int?,
    @SerializedName("withdrawableBalance") val withdrawableBalance: Int?,
    @SerializedName("rewardBalance") val rewardBalance: Int?,
    @SerializedName("cashBalance") val cashBalance: Int?,
    @SerializedName("amountInr") val amountInr: Int?,
    @SerializedName("estimatedPayoutPaise") val estimatedPayoutPaise: Int?,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("balance") val balance: Int?,
    @SerializedName("walletBalance") val walletBalance: Int?,
    @SerializedName("data") val data: WalletBalanceData?
)

data class WalletBalanceData(
    @SerializedName("message") val message: String?,
    @SerializedName("hearts") val hearts: Int?,
    @SerializedName("coins") val coins: Int?,
    @SerializedName("flowerBalance") val flowerBalance: Int?,
    @SerializedName("currentCoinValuePaise") val currentCoinValuePaise: Int?,
    @SerializedName("currentFlowerValuePaise") val currentFlowerValuePaise: Int?,
    @SerializedName("coinBalance") val coinBalance: Int?,
    @SerializedName("coin_balance") val coin_balance: Int?,
    @SerializedName("withdrawableAmount") val withdrawableAmount: Int?,
    @SerializedName("withdrawableBalance") val withdrawableBalance: Int?,
    @SerializedName("rewardBalance") val rewardBalance: Int?,
    @SerializedName("cashBalance") val cashBalance: Int?,
    @SerializedName("amountInr") val amountInr: Int?,
    @SerializedName("estimatedPayoutPaise") val estimatedPayoutPaise: Int?,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("balance") val balance: Int?,
    @SerializedName("walletBalance") val walletBalance: Int?
)
