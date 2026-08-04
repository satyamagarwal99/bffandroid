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
    val coinAmount: Int,
    val name: String = "",
    val maskedPan: String = "",
    val maskedUpiId: String = "",
    val rejectionReason: String? = null
)

data class CoinWithdrawalsResponse(
    @SerializedName("transactions") val transactions: List<CoinWithdrawalDto>?,
    @SerializedName("withdrawals") val withdrawals: List<CoinWithdrawalDto>?,
    @SerializedName("data") val data: List<CoinWithdrawalDto>?,
    @SerializedName("items") val items: List<CoinWithdrawalDto>?,
    @SerializedName("results") val results: List<CoinWithdrawalDto>?
)

data class CoinWithdrawalDto(
    @SerializedName("withdrawalId") val withdrawalId: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("_id") val _id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("maskedPan") val maskedPan: String?,
    @SerializedName("maskedUpiId") val maskedUpiId: String?,
    @SerializedName("coinAmount") val coinAmount: Int?,
    @SerializedName("coins") val coins: Int?,
    @SerializedName("flowerAmount") val flowerAmount: Int?,
    @SerializedName("coinValuePaise") val coinValuePaise: Int?,
    @SerializedName("payoutAmountPaise") val payoutAmountPaise: Int?,
    @SerializedName("amountPaise") val amountPaise: Int?,
    @SerializedName("estimatedPayoutPaise") val estimatedPayoutPaise: Int?,
    @SerializedName("payoutPaise") val payoutPaise: Int?,
    @SerializedName("amountInr") val amountInr: Int?,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("payoutAmount") val payoutAmount: Int?,
    @SerializedName("currencyCode") val currencyCode: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("rejectionReason") val rejectionReason: String?,
    @SerializedName("requestedAt") val requestedAt: String?,
    @SerializedName("processedAt") val processedAt: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("submittedAt") val submittedAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
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
