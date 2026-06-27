package com.example.bffandroid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bffandroid.R
import com.example.bffandroid.data.MainRepository
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
import com.example.bffandroid.utils.AppSession
import com.example.bffandroid.utils.Constant
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 2_000L

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val mainRepository = remember { MainRepository() }
    var activeCallName by remember { mutableStateOf("Anshu") }
    var activeChatName by remember { mutableStateOf("Anshu") }
    var activeChatAvatar by remember { mutableStateOf(R.drawable.women_avatar3) }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route
    ) {
        composable(AppRoute.Splash.route) {
            LaunchedEffect(Unit) {
                runCatching {
                    mainRepository.getAppVersion(Constant.DEVICE_PLATFORM, Constant.APP_VERSION)
                }
                delay(SPLASH_DURATION_MS)
                navController.navigate(
                    if (AppSession.getBoolean(Constant.IS_USER_LOGGED_IN)) {
                        AppRoute.Home.route
                    } else {
                        AppRoute.Login.route
                    }
                ) {
                    popUpTo(AppRoute.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            SplashScreen()
        }

        composable(AppRoute.Login.route) {
            LoginScreen(
                onSkipLogin = { navController.navigateSingleTop(AppRoute.Gender) },
                onAuthenticated = { navController.navigateSingleTop(AppRoute.Gender) }
            )
        }

        composable(AppRoute.Gender.route) {
            GenderScreen(
                onAudioStepRequested = { navController.navigateSingleTop(AppRoute.Audio) }
            )
        }

        composable(AppRoute.Audio.route) {
            AudioScreen(
                onBack = { navController.navigateSingleTop(AppRoute.Gender) },
                onDone = { navController.navigateHome() }
            )
        }

        composable(AppRoute.Home.route) {
            HomeScreen(
                onLogout = {
                    AppSession.clear()
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                onCallRequested = { personName ->
                    activeCallName = personName
                    navController.navigateSingleTop(AppRoute.Call)
                },
                onFriendsRequested = { navController.navigateSingleTop(AppRoute.Friends) },
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onChatRequested = { navController.navigateSingleTop(AppRoute.Chat) },
                onHistoryRequested = { navController.navigateSingleTop(AppRoute.History) },
                onGamesRequested = { navController.navigateSingleTop(AppRoute.Games) },
                onProfileRequested = { navController.navigateSingleTop(AppRoute.Profile) }
            )
        }

        composable(AppRoute.Profile.route) {
            ProfileScreen(
                onBack = { navController.navigateHome() },
                onGiftVibeRequested = { navController.navigateSingleTop(AppRoute.GiftVibe) },
                onWalletRequested = { navController.navigateSingleTop(AppRoute.Wallet) },
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onSettingsRequested = { navController.navigateSingleTop(AppRoute.Settings) }
            )
        }

        composable(AppRoute.Settings.route) {
            SettingsScreen(
                onBack = { navController.navigateSingleTop(AppRoute.Profile) },
                onLogout = {
                    AppSession.clear()
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoute.GiftVibe.route) {
            GiftVibeScreen(
                onBack = { navController.navigateSingleTop(AppRoute.Profile) }
            )
        }

        composable(AppRoute.Wallet.route) {
            WalletScreen(
                onBack = { navController.navigateSingleTop(AppRoute.Profile) }
            )
        }

        composable(AppRoute.Chat.route) {
            ChatScreen(
                onBack = { navController.navigateHome() },
                onChatSelected = { name, avatarRes ->
                    activeChatName = name
                    activeChatAvatar = avatarRes
                    navController.navigateSingleTop(AppRoute.PersonalChat)
                },
                onConnectSelected = { navController.navigateHome() },
                onGamesSelected = { navController.navigateSingleTop(AppRoute.Games) },
                onHistorySelected = { navController.navigateSingleTop(AppRoute.History) }
            )
        }

        composable(AppRoute.History.route) {
            HistoryScreen(
                onBack = { navController.navigateHome() },
                onConnectSelected = { navController.navigateHome() },
                onGamesSelected = { navController.navigateSingleTop(AppRoute.Games) },
                onChatSelected = { navController.navigateSingleTop(AppRoute.Chat) }
            )
        }

        composable(AppRoute.Games.route) {
            GameScreen(
                onBack = { navController.navigateHome() },
                onConnectSelected = { navController.navigateHome() },
                onTruthDareSelected = { navController.navigateSingleTop(AppRoute.TruthDare) },
                onChatSelected = { navController.navigateSingleTop(AppRoute.Chat) },
                onHistorySelected = { navController.navigateSingleTop(AppRoute.History) }
            )
        }

        composable(AppRoute.TruthDare.route) {
            TruthDareScreen(
                onBack = { navController.navigateSingleTop(AppRoute.Games) }
            )
        }

        composable(AppRoute.PersonalChat.route) {
            PersonalChatScreen(
                personName = activeChatName,
                avatarRes = activeChatAvatar,
                onBack = { navController.navigateSingleTop(AppRoute.Chat) }
            )
        }

        composable(AppRoute.Friends.route) {
            FriendsListScreen(
                onBack = { navController.navigateHome() }
            )
        }

        composable(AppRoute.Recharge.route) {
            RechargeScreen(
                onBack = { navController.navigateHome() }
            )
        }

        composable(AppRoute.Call.route) {
            CallScreen(
                personName = activeCallName,
                onBack = { navController.navigateHome() }
            )
        }
    }
}

private fun NavHostController.navigateSingleTop(route: AppRoute) {
    navigate(route.route) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateHome() {
    navigate(AppRoute.Home.route) {
        launchSingleTop = true
    }
}
