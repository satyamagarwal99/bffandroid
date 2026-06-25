package com.example.bffandroid.model

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
    val quoteMessage: String? = null
) {
    val selectedOption: RechargeOption?
        get() = options.firstOrNull { it.id == selectedOptionId }
}
