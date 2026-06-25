package com.example.bffandroid.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bffandroid.model.AuthSessionStore
import com.example.bffandroid.model.RechargeUiState
import com.example.bffandroid.repository.AuthRepository
import kotlinx.coroutines.launch

class RechargeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val authSessionStore = AuthSessionStore(application.applicationContext)

    var uiState by mutableStateOf(RechargeUiState())
        private set

    init {
        loadRechargeOptions()
    }

    fun loadRechargeOptions() {
        viewModelScope.launch {
            val accessToken = authSessionStore.getAccessToken()
            if (accessToken.isNullOrBlank()) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Login token missing",
                    options = emptyList(),
                    selectedOptionId = null
                )
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)
            val result = authRepository.getRechargeOptions(accessToken)
            uiState = uiState.copy(
                isLoading = false,
                options = result.options,
                selectedOptionId = uiState.selectedOptionId
                    ?.takeIf { selectedId -> result.options.any { it.id == selectedId } }
                    ?: result.options.firstOrNull()?.id,
                errorMessage = if (result.isSuccessful) null else result.message
            )
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
            val accessToken = authSessionStore.getAccessToken()
            if (accessToken.isNullOrBlank()) {
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

            val result = authRepository.getRechargeQuote(
                accessToken = accessToken,
                packCode = selectedOption.packCode,
                couponCode = couponCode
            )

            uiState = uiState.copy(
                isQuoteLoading = false,
                isQuoteSuccessful = result.isSuccessful,
                quoteMessage = result.message
            )
        }
    }

    fun clearQuoteState() {
        uiState = uiState.copy(
            isQuoteLoading = false,
            isQuoteSuccessful = false,
            quoteMessage = null
        )
    }
}
