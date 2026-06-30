package com.example.bffandroid.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bffandroid.data.MainRepository
import com.example.bffandroid.utils.AppSession
import com.example.bffandroid.utils.TokenUtils
import kotlinx.coroutines.launch

class LogoutViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var isLoggingOut by mutableStateOf(false)
        private set

    fun logout(onLoggedOut: () -> Unit) {
        if (isLoggingOut) return

        viewModelScope.launch {
            isLoggingOut = true
            val token = TokenUtils.getToken()
            if (token.isNotBlank()) {
                runCatching { mainRepository.logout(token) }
            }
            AppSession.clear()
            isLoggingOut = false
            onLoggedOut()
        }
    }
}
