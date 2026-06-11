package com.example.bffandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.bffandroid.screens.LoginScreen
import com.example.bffandroid.screens.SplashScreen
import com.example.bffandroid.ui.theme.BffAndroidTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BffAndroidTheme {
                AppEntry()
            }
        }
    }
}

@Composable
private fun AppEntry() {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        LoginScreen()
    }
}

private const val SPLASH_DURATION_MS = 2_000L
