package com.gobff.getfriends.utils

import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class InstallReferrerReporter(
    context: Context
) {
    private val appContext = context.applicationContext

    suspend fun fetchAndStoreIfAvailable() {
        val rawReferrer = fetchInstallReferrer() ?: return
        AttributionStore.saveInstallReferrer(appContext, rawReferrer)
    }

    private suspend fun fetchInstallReferrer(): String? = suspendCancellableCoroutine { continuation ->
        val client = InstallReferrerClient.newBuilder(appContext).build()
        continuation.invokeOnCancellation {
            runCatching { client.endConnection() }
        }

        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    val referrer = if (responseCode == InstallReferrerResponse.OK) {
                        client.installReferrer.installReferrer
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                    } else {
                        Log.d(TAG, "Install referrer unavailable responseCode=$responseCode")
                        null
                    }
                    if (continuation.isActive) {
                        continuation.resume(referrer)
                    }
                } catch (error: Exception) {
                    Log.w(TAG, "Unable to read install referrer", error)
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                } finally {
                    runCatching { client.endConnection() }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                Log.d(TAG, "Install referrer service disconnected")
            }
        })
    }

    private companion object {
        const val TAG = "InstallReferrerReporter"
    }
}
