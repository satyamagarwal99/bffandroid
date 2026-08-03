package com.gobff.getfriends.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gobff.getfriends.R
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.PresenceHeartbeat
import com.gobff.getfriends.utils.TokenUtils
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
            Log.d(TAG, "Service start ignored: always-online disabled startId=$startId")
            stopSelf()
            return START_NOT_STICKY
        }

        startAsForegroundService()
        Log.d(TAG, "Service started startId=$startId action=${intent?.action.orEmpty()}")
        startHeartbeat()
        return START_REDELIVER_INTENT
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (PresenceHeartbeat.isAlwaysOnlineEnabled() && TokenUtils.hasStoredSession()) {
            scheduleRestart()
            Log.d(TAG, "Task removed; scheduled presence service restart")
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        heartbeatJob?.cancel()
        if (PresenceHeartbeat.isAlwaysOnlineEnabled() && TokenUtils.hasStoredSession()) {
            scheduleRestart()
            Log.d(TAG, "Service destroyed while enabled; scheduled restart")
        }
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startHeartbeat() {
        if (heartbeatJob?.isActive == true) return

        heartbeatJob = serviceScope.launch {
            var tick = 0
            while (isActive && PresenceHeartbeat.isAlwaysOnlineEnabled()) {
                tick += 1
                Log.d(TAG, "Presence heartbeat tick=$tick")
                PresenceHeartbeat.updateOnline(repository, online = true, tag = TAG)
                delay(PresenceHeartbeat.INTERVAL_MS)
            }
            Log.d(TAG, "Presence heartbeat stopped enabled=${PresenceHeartbeat.isAlwaysOnlineEnabled()}")
            stopSelf()
        }
    }

    private fun startAsForegroundService() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun scheduleRestart() {
        val restartIntent = Intent(applicationContext, PresenceForegroundService::class.java).apply {
            action = ACTION_RESTART
        }
        val pendingIntent = PendingIntent.getService(
            applicationContext,
            RESTART_REQUEST_CODE,
            restartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(AlarmManager::class.java)
        val triggerAtMillis = System.currentTimeMillis() + RESTART_DELAY_MS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Available for calls")
            .setContentText("BFF is keeping you online for incoming calls.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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
        private const val ACTION_RESTART = "com.gobff.getfriends.PRESENCE_RESTART"
        private const val RESTART_REQUEST_CODE = 2102
        private const val RESTART_DELAY_MS = 5_000L

        fun start(context: Context) {
            val intent = Intent(context, PresenceForegroundService::class.java).apply {
                action = ACTION_RESTART
            }
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
