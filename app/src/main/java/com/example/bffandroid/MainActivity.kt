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
import androidx.compose.ui.platform.LocalContext
import com.example.bffandroid.model.AuthSessionStore
import com.example.bffandroid.repository.AuthRepository
import com.example.bffandroid.screens.AudioScreen
import com.example.bffandroid.screens.CallScreen
import com.example.bffandroid.screens.FriendsListScreen
import com.example.bffandroid.screens.GenderScreen
import com.example.bffandroid.screens.HomeScreen
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
    val context = LocalContext.current
    val authSessionStore = remember { AuthSessionStore(context) }
    val authRepository = remember { AuthRepository() }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    var activeCallName by remember { mutableStateOf("Anshu") }

    LaunchedEffect(Unit) {
        authRepository.getAppVersion()
        delay(SPLASH_DURATION_MS)
        currentScreen = if (authSessionStore.isLoggedIn()) {
            AppScreen.Home
        } else {
            AppScreen.Login
        }
    }

    when (currentScreen) {
        AppScreen.Splash -> SplashScreen()
        AppScreen.Login -> LoginScreen(
            onSkipLogin = { currentScreen = AppScreen.Gender },
            onAuthenticated = { currentScreen = AppScreen.Gender }
        )
        AppScreen.Gender -> GenderScreen(
            onAudioStepRequested = { currentScreen = AppScreen.Audio }
        )
        AppScreen.Audio -> AudioScreen(
            onBack = { currentScreen = AppScreen.Gender },
            onDone = { currentScreen = AppScreen.Home }
        )
        AppScreen.Home -> HomeScreen(
            onLogout = {
                authSessionStore.setLoggedIn(false)
                currentScreen = AppScreen.Login
            },
            onCallRequested = { personName ->
                activeCallName = personName
                currentScreen = AppScreen.Call
            },
            onFriendsRequested = { currentScreen = AppScreen.Friends }
        )
        AppScreen.Friends -> FriendsListScreen(
            onBack = { currentScreen = AppScreen.Home }
        )
        AppScreen.Call -> CallScreen(
            personName = activeCallName,
            onBack = { currentScreen = AppScreen.Home }
        )
    }
}

private const val SPLASH_DURATION_MS = 2_000L

private enum class AppScreen {
    Splash,
    Login,
    Gender,
    Audio,
    Home,
    Friends,
    Call
}
