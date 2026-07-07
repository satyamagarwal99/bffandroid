package com.gobff.getfriends.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

object AppSession {

    private const val TAG = "AuthSessionStore"
    private const val PREFS_NAME = "bff_auth_session"
    private const val BACKUP_PREFS_NAME = "bff_auth_session_backup"

    private val lock = Any()
    private var sharedPref: SharedPreferences? = null
    private var backupPref: SharedPreferences? = null
    private var appContext: Context? = null
    private var isInitialized = false
    private var initializationFailed = false

    // ── Init ──────────────────────────────────────────────────────────────────

    @Synchronized
    fun initialize(context: Context) {
        synchronized(lock) {
            if (isInitialized) {
                Log.d(TAG, "AuthSessionStore already initialized")
                return
            }

            appContext = context.applicationContext
            backupPref = appContext!!.getSharedPreferences(BACKUP_PREFS_NAME, MODE_PRIVATE)

            try {
                val masterKey = MasterKey.Builder(appContext!!)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                sharedPref = EncryptedSharedPreferences.create(
                    appContext!!,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                isInitialized = true
                Log.d(TAG, "AuthSessionStore initialized with encryption")
                restorePrimaryFromBackupIfNeeded()
                logSnapshot("initialize.encrypted")

            } catch (e: Exception) {
                Log.e(TAG, "Encrypted init failed, falling back to plain prefs", e)
                try {
                    sharedPref = appContext!!.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    isInitialized = true
                    Log.w(TAG, "AuthSessionStore initialized with fallback (non-encrypted) SharedPreferences")
                    restorePrimaryFromBackupIfNeeded()
                    logSnapshot("initialize.fallback")
                } catch (fallback: Exception) {
                    initializationFailed = true
                    Log.e(TAG, "Fallback init also failed", fallback)
                    throw IllegalStateException("Could not initialize AuthSessionStore", fallback)
                }
            }
        }
    }

    private fun ensureInitialized() {
        synchronized(lock) {
            if (initializationFailed) {
                throw IllegalStateException("AuthSessionStore initialization failed previously")
            }
            if (!isInitialized) {
                if (appContext != null) {
                    initialize(appContext!!)
                } else {
                    throw IllegalStateException(
                        "AuthSessionStore is not initialized. Call AuthSessionStore.initialize(context) in your Application.onCreate()"
                    )
                }
            }
        }
    }


    fun putString(key: String, value: String?) {
        ensureInitialized()
        sharedPref!!.edit(commit = true) { putString(key, value) }
        if (isSessionKey(key)) {
            backupPref!!.edit(commit = true) { putString(key, value) }
        }
        Log.d(TAG, "putString key=$key ${redactedValue(value)}")
    }

    fun getString(key: String): String? {
        ensureInitialized()
        return sharedPref!!.getString(key, null)
    }

    fun putBoolean(key: String, value: Boolean) {
        ensureInitialized()
        sharedPref!!.edit(commit = true) { putBoolean(key, value) }
        if (isSessionKey(key)) {
            backupPref!!.edit(commit = true) { putBoolean(key, value) }
        }
        Log.d(TAG, "putBoolean key=$key value=$value")
    }

    fun getBoolean(key: String): Boolean {
        ensureInitialized()
        return sharedPref!!.getBoolean(key, false)
    }


    fun putInt(key: String, value: Int) {
        ensureInitialized()
        sharedPref!!.edit(commit = true) { putInt(key, value) }
    }

    fun getInt(key: String): Int {
        ensureInitialized()
        return sharedPref!!.getInt(key, 0)
    }


    fun putLong(key: String, value: Long) {
        ensureInitialized()
        sharedPref!!.edit(commit = true) { putLong(key, value) }
    }

    fun getLong(key: String): Long {
        ensureInitialized()
        return sharedPref!!.getLong(key, 0L)
    }


    fun remove(key: String) {
        ensureInitialized()
        sharedPref!!.edit(commit = true) { remove(key) }
        if (isSessionKey(key)) {
            backupPref!!.edit(commit = true) { remove(key) }
        }
        Log.d(TAG, "remove key=$key")
    }

    fun clear() {
        ensureInitialized()
        Log.w(TAG, "AuthSessionStore clear requested", Throwable("clear caller trace"))
        sharedPref!!.edit(commit = true) { clear() }
        backupPref!!.edit(commit = true) { clear() }
        Log.d(TAG, "AuthSessionStore cleared")
    }

    fun logSnapshot(reason: String) {
        ensureInitialized()
        val prefs = sharedPref!!
        val backup = backupPref!!
        Log.d(
            TAG,
            "snapshot[$reason] " +
                "loggedIn=${prefs.getBoolean(Constant.IS_USER_LOGGED_IN, false)} " +
                "access=${redactedValue(prefs.getString(Constant.ACCESS_TOKEN_KEY, null))} " +
                "refresh=${redactedValue(prefs.getString(Constant.REFRESH_TOKEN_KEY, null))} " +
                "accessExp=${prefs.getString(Constant.ACCESS_TOKEN_EXPIRES_AT_KEY, null).orEmpty()} " +
                "refreshExp=${prefs.getString(Constant.REFRESH_TOKEN_EXPIRES_AT_KEY, null).orEmpty()} " +
                "installation=${redactedValue(prefs.getString(Constant.INSTALLATION_ID_KEY, null))} " +
                "backupLoggedIn=${backup.getBoolean(Constant.IS_USER_LOGGED_IN, false)} " +
                "backupAccess=${redactedValue(backup.getString(Constant.ACCESS_TOKEN_KEY, null))} " +
                "backupRefresh=${redactedValue(backup.getString(Constant.REFRESH_TOKEN_KEY, null))}"
        )
    }

    private fun restorePrimaryFromBackupIfNeeded() {
        val primary = sharedPref ?: return
        val backup = backupPref ?: return
        val primaryHasSession = primary.getBoolean(Constant.IS_USER_LOGGED_IN, false) ||
            !primary.getString(Constant.ACCESS_TOKEN_KEY, null).isNullOrBlank() ||
            !primary.getString(Constant.REFRESH_TOKEN_KEY, null).isNullOrBlank()
        val backupHasSession = backup.getBoolean(Constant.IS_USER_LOGGED_IN, false) ||
            !backup.getString(Constant.ACCESS_TOKEN_KEY, null).isNullOrBlank() ||
            !backup.getString(Constant.REFRESH_TOKEN_KEY, null).isNullOrBlank()

        Log.d(
            TAG,
            "restore check primaryHasSession=$primaryHasSession backupHasSession=$backupHasSession"
        )
        if (primaryHasSession || !backupHasSession) return

        primary.edit(commit = true) {
            putBoolean(
                Constant.IS_USER_LOGGED_IN,
                backup.getBoolean(Constant.IS_USER_LOGGED_IN, false)
            )
            copyStringFromBackup(Constant.ACCESS_TOKEN_KEY, backup)
            copyStringFromBackup(Constant.REFRESH_TOKEN_KEY, backup)
            copyStringFromBackup(Constant.ACCESS_TOKEN_EXPIRES_AT_KEY, backup)
            copyStringFromBackup(Constant.REFRESH_TOKEN_EXPIRES_AT_KEY, backup)
            copyStringFromBackup(Constant.INSTALLATION_ID_KEY, backup)
        }
        Log.w(TAG, "Primary auth session was empty; restored values from backup prefs")
    }

    private fun SharedPreferences.Editor.copyStringFromBackup(
        key: String,
        backup: SharedPreferences
    ) {
        backup.getString(key, null)?.let { putString(key, it) }
    }

    private fun isSessionKey(key: String): Boolean = key == Constant.IS_USER_LOGGED_IN ||
        key == Constant.ACCESS_TOKEN_KEY ||
        key == Constant.REFRESH_TOKEN_KEY ||
        key == Constant.ACCESS_TOKEN_EXPIRES_AT_KEY ||
        key == Constant.REFRESH_TOKEN_EXPIRES_AT_KEY ||
        key == Constant.USER_ID_KEY ||
        key == Constant.INSTALLATION_ID_KEY

    private fun redactedValue(value: String?): String {
        if (value.isNullOrBlank()) return "missing"
        return "present(len=${value.length}, tail=${value.takeLast(4)})"
    }
}
