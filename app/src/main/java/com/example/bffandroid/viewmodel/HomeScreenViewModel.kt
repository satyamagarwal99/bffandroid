package com.example.bffandroid.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bffandroid.data.MainRepository
import com.example.bffandroid.data.model.PresenceRequestBody
import com.example.bffandroid.utils.TokenUtils
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val mainRepository = MainRepository()

    fun markOnline() {
        updatePresence(online = true)
    }

    fun markOffline() {
        updatePresence(online = false)
    }

    private fun updatePresence(online: Boolean) {
        val accessToken = TokenUtils.getToken()
        viewModelScope.launch {
            runCatching {
                mainRepository.updatePresence(
                    accessToken = accessToken,
                    body = PresenceRequestBody(online = online)
                )
            }.onFailure { error ->
                Log.e(TAG, "Presence update failed (online=$online)", error)
            }
        }
    }

    private companion object {
        const val TAG = "HomeScreenViewModel"
    }
}
