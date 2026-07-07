package com.gobff.getfriends.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.TokenUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class IncomingCallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BffFirebaseMessagingService.ACTION_DECLINE_CALL) return

        AppSession.initialize(context.applicationContext)

        val roomId = intent.getStringExtra(BffFirebaseMessagingService.EXTRA_ROOM_ID)
            ?.takeIf { it.isNotBlank() }
            ?: return
        val notificationId = intent.getIntExtra(
            BffFirebaseMessagingService.EXTRA_NOTIFICATION_ID,
            roomId.hashCode()
        )

        NotificationManagerCompat.from(context).cancel(notificationId)

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val token = TokenUtils.getToken()
                if (token.isBlank()) {
                    Log.w(TAG, "Skipping decline end call: token missing roomId=$roomId")
                    return@launch
                }

                val response = MainRepository().endRoom(token, roomId)
                Log.d(TAG, "Decline end call status=${response.code()} roomId=$roomId")
            } catch (error: Throwable) {
                Log.w(TAG, "Decline end call failed roomId=$roomId", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "IncomingCallActionReceiver"
    }
}
