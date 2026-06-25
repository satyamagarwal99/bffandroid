package com.example.bffandroid.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bffandroid.model.AuthSessionStore
import com.example.bffandroid.model.WalletUiState
import com.example.bffandroid.repository.AuthRepository
import kotlinx.coroutines.launch

class WalletViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val authSessionStore = AuthSessionStore(application.applicationContext)

    var uiState by mutableStateOf(WalletUiState())
        private set

    init {
        loadWalletBalance()
    }

    fun loadWalletBalance() {
        viewModelScope.launch {
            val accessToken = authSessionStore.getAccessToken()
            if (accessToken.isNullOrBlank()) {
                uiState = uiState.copy(
                    isLoading = false,
                    amountInr = 0,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)
            val result = authRepository.getWalletBalance(accessToken)
            uiState = uiState.copy(
                isLoading = false,
                amountInr = result.amountInr,
                errorMessage = if (result.isSuccessful) null else result.message
            )
        }
    }
}
