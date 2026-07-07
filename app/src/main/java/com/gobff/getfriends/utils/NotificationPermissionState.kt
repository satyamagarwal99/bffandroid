package com.gobff.getfriends.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

enum class NotificationPermissionAction {
    RequestPermission,
    OpenSettings,
    None
}

data class NotificationPermissionUiState(
    val showBanner: Boolean,
    val action: NotificationPermissionAction
)

object NotificationPermissionState {

    fun hasNotificationAccess(context: Context): Boolean {
        return hasRuntimeNotificationPermission(context) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun hasRuntimeNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasRequestedRuntimePermission(): Boolean {
        return AppSession.getBoolean(Constant.NOTIFICATION_PERMISSION_REQUESTED_KEY)
    }

    fun markRuntimePermissionRequested() {
        AppSession.putBoolean(Constant.NOTIFICATION_PERMISSION_REQUESTED_KEY, true)
    }

    fun markRuntimePermissionDenied() {
        AppSession.putBoolean(Constant.NOTIFICATION_PERMISSION_DENIED_KEY, true)
    }

    fun clearRuntimePermissionDenied() {
        AppSession.putBoolean(Constant.NOTIFICATION_PERMISSION_DENIED_KEY, false)
    }

    fun shouldRequestOnAppStart(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasRuntimeNotificationPermission(context) &&
            !hasRequestedRuntimePermission()
    }

    fun currentUiState(context: Context, activity: Activity?): NotificationPermissionUiState {
        if (hasNotificationAccess(context)) {
            return NotificationPermissionUiState(
                showBanner = false,
                action = NotificationPermissionAction.None
            )
        }

        val runtimePermissionMissing = !hasRuntimeNotificationPermission(context)
        val runtimePermissionRequested = hasRequestedRuntimePermission()
        val runtimePermissionDenied = AppSession.getBoolean(Constant.NOTIFICATION_PERMISSION_DENIED_KEY)
        val appNotificationsDisabled = !NotificationManagerCompat.from(context).areNotificationsEnabled()
        val shouldShowBanner = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && runtimePermissionMissing ->
                runtimePermissionRequested || runtimePermissionDenied
            else -> appNotificationsDisabled
        }

        return NotificationPermissionUiState(
            showBanner = shouldShowBanner,
            action = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    runtimePermissionMissing &&
                    canRequestRuntimePermission() -> NotificationPermissionAction.RequestPermission

                else -> NotificationPermissionAction.OpenSettings
            }
        )
    }

    fun openAppNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    private fun canRequestRuntimePermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasRequestedRuntimePermission()
    }
}
