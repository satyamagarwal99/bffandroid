package com.gobff.getfriends.data.model

import com.google.gson.annotations.SerializedName

data class WalletBalanceResult(
    val isSuccessful: Boolean,
    val amountInr: Int,
    val message: String?
)

data class WalletUiState(
    val isLoading: Boolean = true,
    val coins: Int = 0,
    val hearts: Int = 0,
    val amountInr: Int = 0,
    val errorMessage: String? = null
)

data class WalletBalanceResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("hearts") val hearts: Int?,
    @SerializedName("coins") val coins: Int?,
    @SerializedName("coinBalance") val coinBalance: Int?,
    @SerializedName("coin_balance") val coin_balance: Int?,
    @SerializedName("withdrawableAmount") val withdrawableAmount: Int?,
    @SerializedName("withdrawableBalance") val withdrawableBalance: Int?,
    @SerializedName("rewardBalance") val rewardBalance: Int?,
    @SerializedName("cashBalance") val cashBalance: Int?,
    @SerializedName("amountInr") val amountInr: Int?,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("balance") val balance: Int?,
    @SerializedName("walletBalance") val walletBalance: Int?,
    @SerializedName("data") val data: WalletBalanceData?
)

data class WalletBalanceData(
    @SerializedName("message") val message: String?,
    @SerializedName("hearts") val hearts: Int?,
    @SerializedName("coins") val coins: Int?,
    @SerializedName("coinBalance") val coinBalance: Int?,
    @SerializedName("coin_balance") val coin_balance: Int?,
    @SerializedName("withdrawableAmount") val withdrawableAmount: Int?,
    @SerializedName("withdrawableBalance") val withdrawableBalance: Int?,
    @SerializedName("rewardBalance") val rewardBalance: Int?,
    @SerializedName("cashBalance") val cashBalance: Int?,
    @SerializedName("amountInr") val amountInr: Int?,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("balance") val balance: Int?,
    @SerializedName("walletBalance") val walletBalance: Int?
)
