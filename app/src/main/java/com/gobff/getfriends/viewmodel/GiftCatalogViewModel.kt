package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.GiftCatalogResponse
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.launch

data class GiftCatalogUiState(
    val isLoading: Boolean = false,
    val catalog: GiftCatalogResponse? = null,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false
)

class GiftCatalogViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var uiState by mutableStateOf(GiftCatalogUiState())
        private set

    fun loadGiftCatalog(forceRefresh: Boolean = false) {
        if (uiState.hasLoaded && !forceRefresh) return

        viewModelScope.launch {
            val token = TokenUtils.getToken()
            if (token.isBlank()) {
                uiState = uiState.copy(
                    isLoading = false,
                    hasLoaded = true,
                    errorMessage = "Login token missing"
                )
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)
            runCatching { mainRepository.getGiftCatalog(token) }
                .onSuccess { response ->
                    val body = response.body()
                    uiState = uiState.copy(
                        isLoading = false,
                        catalog = body?.takeIf { response.isSuccessful } ?: uiState.catalog,
                        hasLoaded = true,
                        errorMessage = if (response.isSuccessful) {
                            null
                        } else {
                            body?.message ?: "Unable to load gifts"
                        }
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = error.message ?: "Unable to load gifts"
                    )
                }
        }
    }
}
