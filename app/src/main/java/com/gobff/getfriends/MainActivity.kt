package com.gobff.getfriends

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.gobff.getfriends.navigation.AppNavGraph
import com.gobff.getfriends.service.PresenceForegroundService
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.PresenceHeartbeat
import com.gobff.getfriends.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppSession.initialize(this)
        AppSession.logSnapshot("MainActivity.onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BffAndroidTheme {
                AppNavGraph()
            }
        }
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
}
