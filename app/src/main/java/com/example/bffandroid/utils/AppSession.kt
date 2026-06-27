package com.example.bffandroid.utils

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

    private val lock = Any()
    private var sharedPref: SharedPreferences? = null
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

            } catch (e: Exception) {
                Log.e(TAG, "Encrypted init failed, falling back to plain prefs", e)
                try {
                    sharedPref = appContext!!.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    isInitialized = true
                    Log.w(TAG, "AuthSessionStore initialized with fallback (non-encrypted) SharedPreferences")
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
        sharedPref!!.edit { putString(key, value) }
    }

    fun getString(key: String): String? {
        ensureInitialized()
        return sharedPref!!.getString(key, null)
    }

    fun putBoolean(key: String, value: Boolean) {
        ensureInitialized()
        sharedPref!!.edit { putBoolean(key, value) }
    }

    fun getBoolean(key: String): Boolean {
        ensureInitialized()
        return sharedPref!!.getBoolean(key, false)
    }


    fun putInt(key: String, value: Int) {
        ensureInitialized()
        sharedPref!!.edit { putInt(key, value) }
    }

    fun getInt(key: String): Int {
        ensureInitialized()
        return sharedPref!!.getInt(key, 0)
    }


    fun putLong(key: String, value: Long) {
        ensureInitialized()
        sharedPref!!.edit { putLong(key, value) }
    }

    fun getLong(key: String): Long {
        ensureInitialized()
        return sharedPref!!.getLong(key, 0L)
    }


    fun remove(key: String) {
        ensureInitialized()
        sharedPref!!.edit { remove(key) }
    }

    fun clear() {
        ensureInitialized()
        sharedPref!!.edit { clear() }
        Log.d(TAG, "AuthSessionStore cleared")
    }
}