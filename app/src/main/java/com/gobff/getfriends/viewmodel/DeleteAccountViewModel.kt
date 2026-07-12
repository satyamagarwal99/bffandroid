package com.gobff.getfriends.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.launch

class DeleteAccountViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    var isDeleting by mutableStateOf(false)
        private set

    fun deleteAccount(onDeleted: () -> Unit) {
        if (isDeleting) return

        viewModelScope.launch {
            isDeleting = true
            val token = TokenUtils.getToken()
            if (token.isNotBlank()) {
                val success = runCatching { mainRepository.deleteAccount(token) }
                    .getOrNull()
                    ?.isSuccessful == true

                if (success) {
                    AppSession.clear()
                    isDeleting = false
                    onDeleted()
                    return@launch
                }
            }

            isDeleting = false
        }
    }
}
