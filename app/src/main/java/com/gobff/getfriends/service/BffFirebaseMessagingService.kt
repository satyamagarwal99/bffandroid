package com.gobff.getfriends.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.gobff.getfriends.AppForegroundState
import com.gobff.getfriends.CallEndedEvents
import com.gobff.getfriends.CallEndedPush
import com.gobff.getfriends.IncomingCallEvents
import com.gobff.getfriends.IncomingCallPush
import com.gobff.getfriends.MainActivity
import com.gobff.getfriends.R
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.data.model.UpdateFcmTokenBody
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.TokenUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BffFirebaseMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainRepository = MainRepository()

    override fun onCreate() {
        AppSession.initialize(applicationContext)
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed=${token}")
        Log.d(TAG, "FCM token refreshed length=${token.length}")
        TokenUtils.recordFetchedFcmToken(token, "FirebaseMessagingService")
        syncFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(
            TAG,
            buildString {
                append("FCM received from=${message.from.orEmpty()} ")
                append("messageId=${message.messageId.orEmpty()} ")
                append("collapseKey=${message.collapseKey.orEmpty()} ")
                append("sentTime=${message.sentTime} ")
                append("ttl=${message.ttl} ")
                append("priority=${message.priority} ")
                append("originalPriority=${message.originalPriority} ")
                append("notification=${message.notification != null} ")
                append("dataKeys=${message.data.keys} ")
                append("data=${redactedPayload(message.data)}")
                message.notification?.let { notification ->
                    append(" notificationTitle=${notification.title.orEmpty()}")
                    append(" notificationBody=${notification.body.orEmpty()}")
                }
            }
        )

        if (message.data["event"] == INCOMING_CALL_EVENT) {
            showIncomingCallNotification(message.data)
            return
        }

        if (message.data["event"] == CALL_ENDED_EVENT) {
            handleCallEnded(message.data)
            return
        }

        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: "You have a new update"

        showNotification(title = title, body = body)
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        ensureNotificationChannel(notificationManager)

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d(TAG, "FCM notification posted")
    }

    private fun handleCallEnded(data: Map<String, String>) {
        val roomId = data["roomId"].orEmpty().ifBlank { data["callId"].orEmpty() }
        if (roomId.isBlank()) {
            Log.w(TAG, "Skipping call_ended push: roomId missing")
            return
        }

        NotificationManagerCompat.from(this).cancel(roomId.hashCode())

        val push = CallEndedPush(
            callId = data["callId"].orEmpty().ifBlank { roomId },
            roomId = roomId,
            roomType = data["roomType"].orEmpty(),
            status = data["status"].orEmpty(),
            endedByUserId = data["endedByUserId"].orEmpty(),
            endedByName = data["endedByName"].orEmpty(),
            endedByAvatarUrl = data["endedByAvatarUrl"]?.takeIf { it.isNotBlank() },
            createdByUserId = data["createdByUserId"].orEmpty(),
            invitedUserId = data["invitedUserId"].orEmpty(),
            endedAt = data["endedAt"].orEmpty(),
            expiresAt = data["expiresAt"].orEmpty(),
            connected = data["connected"]?.toBooleanStrictOrNull()
        )

        if (AppForegroundState.isForeground) {
            CallEndedEvents.publish(push)
            Log.d(TAG, "Call ended delivered in-app roomId=$roomId connected=${push.connected}")
        } else {
            Log.d(TAG, "Call ended received in background roomId=$roomId connected=${push.connected}")
        }
    }

    private fun showIncomingCallNotification(data: Map<String, String>) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        ensureIncomingCallChannel(notificationManager)

        val roomId = data["roomId"] ?: data["callId"].orEmpty()
        val requestedRole = data["requestedRole"].orEmpty().ifBlank { "SPEAKER" }
        val callerName = data["callerName"].orEmpty().ifBlank { "Someone" }
        val callerAvatarUrl = data["callerAvatarUrl"].orEmpty().ifBlank { data["avatarUrl"].orEmpty() }
        val roomType = data["roomType"].orEmpty()
        val title = "Incoming call"
        val body = "$callerName is calling you"
        val notificationId = roomId.ifBlank { System.currentTimeMillis().toString() }.hashCode()

        val incomingCallPush = IncomingCallPush(
            roomId = roomId,
            requestedRole = requestedRole,
            callerName = callerName,
            callerAvatarUrl = callerAvatarUrl.takeIf { it.isNotBlank() },
            notificationId = notificationId
        )

        if (roomId.isNotBlank() && AppForegroundState.isForeground) {
            IncomingCallEvents.publish(incomingCallPush)
            Log.d(
                TAG,
                "Incoming call delivered in-app only roomId=$roomId caller=$callerName"
            )
            return
        }

        val answerIntent = PendingIntent.getActivity(
            this,
            notificationId,
            buildIncomingCallIntent(
                roomId = roomId,
                requestedRole = requestedRole,
                callerName = callerName,
                callerAvatarUrl = callerAvatarUrl,
                roomType = roomType,
                notificationId = notificationId
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = PendingIntent.getBroadcast(
            this,
            notificationId,
            Intent(this, IncomingCallActionReceiver::class.java).apply {
                action = ACTION_DECLINE_CALL
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val caller = Person.Builder()
            .setName(callerName)
            .setImportant(true)
            .build()

        val notificationBuilder = NotificationCompat.Builder(this, INCOMING_CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setTimeoutAfter(INCOMING_CALL_TIMEOUT_MS)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(answerIntent)
            .setFullScreenIntent(answerIntent, true)
            .setDeleteIntent(declineIntent)
            .setStyle(
                NotificationCompat.CallStyle.forIncomingCall(
                    caller,
                    declineIntent,
                    answerIntent
                )
            )
        val notification = notificationBuilder.build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
        Log.d(
            TAG,
            "Incoming call notification posted roomId=$roomId caller=$callerName " +
                "notificationsEnabled=${NotificationManagerCompat.from(this).areNotificationsEnabled()}"
        )
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.w(TAG, "FCM onDeletedMessages called; FCM may have dropped queued messages")
    }

    private fun buildIncomingCallIntent(
        roomId: String,
        requestedRole: String,
        callerName: String,
        callerAvatarUrl: String,
        roomType: String,
        notificationId: Int
    ): Intent {
        return Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_PUSH_EVENT, INCOMING_CALL_EVENT)
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_REQUESTED_ROLE, requestedRole)
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_CALLER_AVATAR_URL, callerAvatarUrl)
            putExtra(EXTRA_ROOM_TYPE, roomType)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
    }

    private fun syncFcmToken(token: String) {
        val bearerToken = TokenUtils.getToken()
        if (token.isBlank() || bearerToken.isBlank()) {
            Log.d(TAG, "Skipping refreshed FCM token sync: token/session missing")
            return
        }

        serviceScope.launch {
            runCatching {
                mainRepository.updateFcmToken(
                    bearerToken = bearerToken,
                    body = UpdateFcmTokenBody(fcmToken = token)
                )
            }.onSuccess { response ->
                Log.d(TAG, "Refreshed FCM token sync status=${response.code()}")
                TokenUtils.recordSyncedFcmToken(
                    token = token,
                    source = "FirebaseMessagingService",
                    responseCode = response.code()
                )
            }.onFailure { error ->
                Log.w(TAG, "Refreshed FCM token sync failed", error)
            }
        }
    }

    private fun ensureNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val existingChannel = notificationManager.getNotificationChannel(FCM_CHANNEL_ID)
        if (existingChannel != null) return

        val channel = NotificationChannel(
            FCM_CHANNEL_ID,
            "Messages",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "App messages and alerts"
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun ensureIncomingCallChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val existingChannel = notificationManager.getNotificationChannel(INCOMING_CALL_CHANNEL_ID)
        if (existingChannel != null) return

        val channel = NotificationChannel(
            INCOMING_CALL_CHANNEL_ID,
            "Incoming calls",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Incoming audio and video calls"
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            setSound(
                Uri.parse("content://settings/system/ringtone"),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val FCM_CHANNEL_ID = "bff_messages"
        const val INCOMING_CALL_CHANNEL_ID = "bff_incoming_calls"
        const val EXTRA_PUSH_EVENT = "push_event"
        const val EXTRA_ROOM_ID = "room_id"
        const val EXTRA_REQUESTED_ROLE = "requested_role"
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_CALLER_AVATAR_URL = "caller_avatar_url"
        const val EXTRA_ROOM_TYPE = "room_type"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val INCOMING_CALL_EVENT = "incoming_call"
        const val CALL_ENDED_EVENT = "call_ended"
        const val ACTION_DECLINE_CALL = "com.gobff.getfriends.action.DECLINE_CALL"
        private const val INCOMING_CALL_TIMEOUT_MS = 30_000L
        private const val TAG = "BffFirebaseMessaging"

        private fun redactedPayload(data: Map<String, String>): String {
            if (data.isEmpty()) return "{}"
            return data.entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
                val safeValue = when (key) {
                    "callerAvatarUrl" -> if (value.isBlank()) "missing" else "present"
                    "roomId", "callId" -> value.takeLast(6)
                    else -> value
                }
                "$key=$safeValue"
            }
        }

        fun createDefaultChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val existingChannel = notificationManager.getNotificationChannel(FCM_CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    FCM_CHANNEL_ID,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "App messages and alerts"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val existingCallChannel = notificationManager.getNotificationChannel(INCOMING_CALL_CHANNEL_ID)
            if (existingCallChannel == null) {
                val callChannel = NotificationChannel(
                    INCOMING_CALL_CHANNEL_ID,
                    "Incoming calls",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Incoming audio and video calls"
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    enableVibration(true)
                    setSound(
                        Uri.parse("content://settings/system/ringtone"),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
                notificationManager.createNotificationChannel(callChannel)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
