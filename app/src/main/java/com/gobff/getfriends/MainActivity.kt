package com.gobff.getfriends

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.UpdateFcmTokenBody
import com.gobff.getfriends.navigation.AppNavGraph
import com.gobff.getfriends.service.BffFirebaseMessagingService
import com.gobff.getfriends.service.PresenceForegroundService
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.PresenceHeartbeat
import com.gobff.getfriends.utils.TokenUtils
import com.gobff.getfriends.viewmodel.MainViewModel
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private var incomingCallPush by mutableStateOf<IncomingCallPush?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        AppSession.initialize(this)
        BffFirebaseMessagingService.createDefaultChannel(this)
        syncCurrentFcmToken()
        AppSession.logSnapshot("MainActivity.onCreate")
        super.onCreate(savedInstanceState)
        incomingCallPush = intent?.toIncomingCallPush()
        enableEdgeToEdge()
        setContent {
            BffAndroidTheme {
                AppNavGraph(
                    incomingCallPush = incomingCallPush,
                    onIncomingCallPushHandled = { incomingCallPush = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingCallPush = intent.toIncomingCallPush()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        AppSession.logSnapshot("MainActivity.onStart")
        if (PresenceHeartbeat.isAlwaysOnlineEnabled()) {
            mainViewModel.stopForegroundHeartbeat()
            PresenceForegroundService.start(this)
        } else {
            PresenceForegroundService.stop(this)
            mainViewModel.onAppOpen()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        AppSession.logSnapshot("MainActivity.onStop")
        if (PresenceHeartbeat.isAlwaysOnlineEnabled()) {
            PresenceForegroundService.start(this)
            mainViewModel.stopForegroundHeartbeat()
        } else {
            mainViewModel.onAppClose()
        }
    }

    private companion object {
        const val TAG = "MainActivity"
    }

    private fun syncCurrentFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { fcmToken ->
                Log.d(TAG, "FCM token fetch success")
                Log.d(TAG, "FCM token fetched=${fcmToken}")
                Log.d(TAG, "FCM token length=${fcmToken.length}")
                TokenUtils.recordFetchedFcmToken(fcmToken, "MainActivity")
                val bearerToken = TokenUtils.getToken()
                if (fcmToken.isBlank() || bearerToken.isBlank()) {
                    Log.d(TAG, "Skipping startup FCM token sync: token/session missing")
                    return@addOnSuccessListener
                }

                lifecycleScope.launch {
                    runCatching {
                        MainRepository().updateFcmToken(
                            bearerToken = bearerToken,
                            body = UpdateFcmTokenBody(fcmToken = fcmToken)
                        )
                    }.onSuccess { response ->
                        Log.d(TAG, "Startup FCM token sync status=${response.code()}")
                        TokenUtils.recordSyncedFcmToken(
                            token = fcmToken,
                            source = "MainActivity",
                            responseCode = response.code()
                        )
                    }.onFailure { error ->
                        Log.w(TAG, "Startup FCM token sync failed", error)
                    }
                }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Current FCM token fetch failed", error)
            }
    }
}

data class IncomingCallPush(
    val roomId: String,
    val requestedRole: String,
    val callerName: String
)

private fun android.content.Intent.toIncomingCallPush(): IncomingCallPush? {
    val event = getStringExtra(BffFirebaseMessagingService.EXTRA_PUSH_EVENT)
    if (event != BffFirebaseMessagingService.INCOMING_CALL_EVENT) return null

    val roomId = getStringExtra(BffFirebaseMessagingService.EXTRA_ROOM_ID)
        ?.takeIf { it.isNotBlank() }
        ?: return null
    return IncomingCallPush(
        roomId = roomId,
        requestedRole = getStringExtra(BffFirebaseMessagingService.EXTRA_REQUESTED_ROLE)
            ?.takeIf { it.isNotBlank() }
            ?: "SPEAKER",
        callerName = getStringExtra(BffFirebaseMessagingService.EXTRA_CALLER_NAME)
            ?.takeIf { it.isNotBlank() }
            ?: "Caller"
    )
}
