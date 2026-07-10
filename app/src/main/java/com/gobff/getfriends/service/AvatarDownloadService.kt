package com.gobff.getfriends.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.gobff.getfriends.utils.AvatarCache
import com.gobff.getfriends.utils.AvatarGender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class AvatarDownloadService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isDownloading = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isDownloading) return START_NOT_STICKY

        if (AvatarCache.hasAllAvatars(applicationContext)) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        isDownloading = true
        serviceScope.launch {
            runCatching {
                downloadMissingAvatars()
            }.onFailure { error ->
                Log.w(TAG, "Avatar download failed", error)
            }
            isDownloading = false
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun downloadMissingAvatars() {
        AvatarGender.entries.forEach { gender ->
            val directory = AvatarCache.avatarDirectory(applicationContext, gender)
            if (!directory.exists()) directory.mkdirs()

            (1..gender.count).forEach { index ->
                val target = AvatarCache.avatarFile(applicationContext, gender, index)
                if (target.exists() && target.length() > 0L) return@forEach

                val url = AvatarCache.avatarUrl(gender, index)
                downloadAvatar(url = url, target = target)
                Log.d(TAG, "Downloaded ${gender.directoryName}/${target.name}")
            }
        }
    }

    private fun downloadAvatar(url: String, target: File) {
        val tempFile = File(target.parentFile, "${target.name}.tmp")
        var connection: HttpURLConnection? = null

        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 20_000
                requestMethod = "GET"
                doInput = true
            }

            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("HTTP ${connection.responseCode} for $url")
            }

            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (tempFile.length() <= 0L) {
                throw IllegalStateException("Empty avatar response for $url")
            }

            if (target.exists()) target.delete()
            if (!tempFile.renameTo(target)) {
                tempFile.copyTo(target, overwrite = true)
                tempFile.delete()
            }
        } catch (error: Throwable) {
            tempFile.delete()
            throw error
        } finally {
            connection?.disconnect()
        }
    }

    companion object {
        private const val TAG = "AvatarDownloadService"

        fun startIfNeeded(context: Context) {
            if (AvatarCache.hasAllAvatars(context)) return
            context.startService(Intent(context, AvatarDownloadService::class.java))
        }
    }
}
