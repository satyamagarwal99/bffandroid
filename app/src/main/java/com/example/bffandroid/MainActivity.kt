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
import com.example.bffandroid.screens.ChatScreen
import com.example.bffandroid.screens.FriendsListScreen
import com.example.bffandroid.screens.GameScreen
import com.example.bffandroid.screens.GenderScreen
import com.example.bffandroid.screens.GiftVibeScreen
import com.example.bffandroid.screens.HistoryScreen
import com.example.bffandroid.screens.HomeScreen
import com.example.bffandroid.screens.LoginScreen
import com.example.bffandroid.screens.PersonalChatScreen
import com.example.bffandroid.screens.ProfileScreen
import com.example.bffandroid.screens.RechargeScreen
import com.example.bffandroid.screens.SettingsScreen
import com.example.bffandroid.screens.SplashScreen
import com.example.bffandroid.screens.TruthDareScreen
import com.example.bffandroid.screens.WalletScreen
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
    var activeChatName by remember { mutableStateOf("Anshu") }
    var activeChatAvatar by remember { mutableStateOf(R.drawable.women_avatar3) }

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
                authSessionStore.setAccessToken(null)
                currentScreen = AppScreen.Login
            },
            onCallRequested = { personName ->
                activeCallName = personName
                currentScreen = AppScreen.Call
            },
            onFriendsRequested = { currentScreen = AppScreen.Friends },
            onRechargeRequested = { currentScreen = AppScreen.Recharge },
            onChatRequested = { currentScreen = AppScreen.Chat },
            onHistoryRequested = { currentScreen = AppScreen.History },
            onGamesRequested = { currentScreen = AppScreen.Games },
            onProfileRequested = { currentScreen = AppScreen.Profile }
        )
        AppScreen.Profile -> ProfileScreen(
            onBack = { currentScreen = AppScreen.Home },
            onGiftVibeRequested = { currentScreen = AppScreen.GiftVibe },
            onWalletRequested = { currentScreen = AppScreen.Wallet },
            onRechargeRequested = { currentScreen = AppScreen.Recharge },
            onSettingsRequested = { currentScreen = AppScreen.Settings }
        )
        AppScreen.Settings -> SettingsScreen(
            onBack = { currentScreen = AppScreen.Profile },
            onLogout = {
                authSessionStore.setLoggedIn(false)
                authSessionStore.setAccessToken(null)
                currentScreen = AppScreen.Login
            }
        )
        AppScreen.GiftVibe -> GiftVibeScreen(
            onBack = { currentScreen = AppScreen.Profile }
        )
        AppScreen.Wallet -> WalletScreen(
            onBack = { currentScreen = AppScreen.Profile }
        )
        AppScreen.Chat -> ChatScreen(
            onBack = { currentScreen = AppScreen.Home },
            onChatSelected = { name, avatarRes ->
                activeChatName = name
                activeChatAvatar = avatarRes
                currentScreen = AppScreen.PersonalChat
            },
            onConnectSelected = { currentScreen = AppScreen.Home },
            onGamesSelected = { currentScreen = AppScreen.Games },
            onHistorySelected = { currentScreen = AppScreen.History }
        )
        AppScreen.History -> HistoryScreen(
            onBack = { currentScreen = AppScreen.Home },
            onConnectSelected = { currentScreen = AppScreen.Home },
            onGamesSelected = { currentScreen = AppScreen.Games },
            onChatSelected = { currentScreen = AppScreen.Chat }
        )
        AppScreen.Games -> GameScreen(
            onBack = { currentScreen = AppScreen.Home },
            onConnectSelected = { currentScreen = AppScreen.Home },
            onTruthDareSelected = { currentScreen = AppScreen.TruthDare },
            onChatSelected = { currentScreen = AppScreen.Chat },
            onHistorySelected = { currentScreen = AppScreen.History }
        )
        AppScreen.TruthDare -> TruthDareScreen(
            onBack = { currentScreen = AppScreen.Games }
        )
        AppScreen.PersonalChat -> PersonalChatScreen(
            personName = activeChatName,
            avatarRes = activeChatAvatar,
            onBack = { currentScreen = AppScreen.Chat }
        )
        AppScreen.Friends -> FriendsListScreen(
            onBack = { currentScreen = AppScreen.Home }
        )
        AppScreen.Recharge -> RechargeScreen(
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
    Profile,
    Settings,
    GiftVibe,
    Wallet,
    Chat,
    History,
    Games,
    TruthDare,
    PersonalChat,
    Friends,
    Recharge,
    Call
}
