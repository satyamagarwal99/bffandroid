package com.gobff.getfriends.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gobff.getfriends.R
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.PresenceHeartbeat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PresenceForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = MainRepository()
    private var heartbeatJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        AppSession.initialize(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!PresenceHeartbeat.isAlwaysOnlineEnabled()) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        startHeartbeat()
        return START_STICKY
    }

    override fun onDestroy() {
        heartbeatJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startHeartbeat() {
        if (heartbeatJob?.isActive == true) return

        heartbeatJob = serviceScope.launch {
            while (isActive && PresenceHeartbeat.isAlwaysOnlineEnabled()) {
                PresenceHeartbeat.updateOnline(repository, online = true, tag = TAG)
                delay(PresenceHeartbeat.INTERVAL_MS)
            }
            stopSelf()
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Available for calls")
            .setContentText("BFF is keeping you online for incoming calls.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Call availability",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when BFF keeps you online for incoming calls."
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "PresenceForegroundSvc"
        private const val CHANNEL_ID = "call_availability"
        private const val NOTIFICATION_ID = 2101

        fun start(context: Context) {
            val intent = Intent(context, PresenceForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PresenceForegroundService::class.java))
        }
    }
}
