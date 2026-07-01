package com.example.bffandroid.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.bffandroid.screens.HomeScreen2
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
import com.example.bffandroid.utils.TokenUtils
import com.example.bffandroid.viewmodel.MainViewModel
import com.example.bffandroid.viewmodel.UserProfileViewModel
import com.example.bffandroid.viewmodel.WalletViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPLASH_DURATION_MS = 2_000L
private const val TAG = "AppNavGraph"

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val mainRepository = remember { MainRepository() }
    val mainViewModel: MainViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val walletViewModel: WalletViewModel = viewModel()
    val userProfileViewModel: UserProfileViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val walletHearts = walletViewModel.uiState.hearts
    var activeCallName by remember { mutableStateOf("Anshu") }
    var activeChatName by remember { mutableStateOf("Anshu") }
    var activeChatAvatar by remember { mutableStateOf(R.drawable.women_avatar3) }
    var initialAppOpenDispatched by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (initialAppOpenDispatched) {
                        mainViewModel.onAppOpen()
                    } else {
                        initialAppOpenDispatched = true
                    }
                }
                Lifecycle.Event.ON_STOP -> mainViewModel.onAppClose()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (!initialAppOpenDispatched) {
            initialAppOpenDispatched = true
            mainViewModel.onAppOpen()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route
    ) {
        composable(AppRoute.Splash.route) {
            LaunchedEffect(Unit) {
                AppSession.logSnapshot("Splash.start")
                runCatching {
                    mainRepository.getAppVersion(Constant.DEVICE_PLATFORM, Constant.APP_VERSION)
                }.onFailure { error ->
                    Log.w(TAG, "App version check failed during splash", error)
                }
                val hasStoredSession = TokenUtils.hasStoredSession()
                if (hasStoredSession) {
                    walletViewModel.loadWalletBalance()
                }
                delay(SPLASH_DURATION_MS)
                val hasSessionAfterSplash = TokenUtils.hasStoredSession()
                val nextRoute = if (hasSessionAfterSplash) {
                    val hasProfile = userProfileViewModel.refreshProfile()
                    Log.d(TAG, "Splash profile refresh result=$hasProfile")
                    if (hasProfile) AppRoute.Home2.route else AppRoute.Gender.route
                } else {
                    AppRoute.Login.route
                }
                AppSession.logSnapshot("Splash.navigate.$nextRoute")
                Log.d(TAG, "Splash navigating to $nextRoute")
                navController.navigate(nextRoute) {
                    popUpTo(AppRoute.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            SplashScreen()
        }

        composable(AppRoute.Login.route) {
            LoginScreen(
                onSkipLogin = { navController.navigateSingleTop(AppRoute.Gender) },
                onAuthenticated = {
                    coroutineScope.launch {
                        val nextRoute = if (userProfileViewModel.refreshProfile()) {
                            AppRoute.Home2.route
                        } else {
                            AppRoute.Gender.route
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(AppRoute.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(AppRoute.Gender.route) {
            GenderScreen(
                onAudioStepRequested = { navController.navigateSingleTop(AppRoute.Audio) },
                onHomeRequested = {
                    navController.navigate(AppRoute.Home2.route) {
                        popUpTo(AppRoute.Gender.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
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
                walletHearts = walletHearts,
                onCallRequested = { personName ->
                    activeCallName = personName
                    navController.navigateSingleTop(AppRoute.Call)
                },
                onFriendsRequested = { navController.navigateSingleTop(AppRoute.Friends) },
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onHomeRequested = { navController.navigateHome() },
                onHistoryRequested = { navController.navigateSingleTop(AppRoute.History) },
                onGamesRequested = { navController.navigateSingleTop(AppRoute.Games) },
                onProfileRequested = { navController.navigateSingleTop(AppRoute.Profile) },
                userProfileViewModel = userProfileViewModel
            )
        }

        composable(AppRoute.Home2.route) {
            LaunchedEffect(Unit) {
                walletViewModel.loadWalletBalance()
                userProfileViewModel.loadProfile()
            }
            HomeScreen2(
                walletHearts = walletHearts,
                displayName = userProfileViewModel.uiState.displayName,
                onBack =  { navController.navigateHome() },
                onLogout = {
                    AppSession.clear()
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                onHomeSelected = { navController.navigateHome() },
                onConnectSelected = { navController.navigateSingleTop(AppRoute.Home) },
                onGamesSelected = { navController.navigateSingleTop(AppRoute.Games) },
                onChatSelected = { navController.navigateSingleTop(AppRoute.Chat) },
                onHistorySelected = { navController.navigateSingleTop(AppRoute.History) },
                onProfileRequested = { navController.navigateSingleTop(AppRoute.Profile) },
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onLiveSelected = { },
                onTruthDareSelected = { navController.navigateSingleTop(AppRoute.TruthDare) }
            )
        }

        composable(AppRoute.Profile.route) {
            ProfileScreen(
                walletHearts = walletHearts,
                onBack = navController::navigateUp,
                onGiftVibeRequested = { navController.navigateSingleTop(AppRoute.GiftVibe) },
                onWalletRequested = { navController.navigateSingleTop(AppRoute.Wallet) },
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onSettingsRequested = { navController.navigateSingleTop(AppRoute.Settings) },
                userProfileViewModel = userProfileViewModel
            )
        }

        composable(AppRoute.Settings.route) {
            SettingsScreen(
                onBack = navController::navigateUp,
                onLogout = {
                    mainViewModel.onAppClose()
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
                onBack = navController::navigateUp
            )
        }

        composable(AppRoute.Wallet.route) {
            WalletScreen(
                onBack = navController::navigateUp
            )
        }

        composable(AppRoute.Chat.route) {
            ChatScreen(
                walletHearts = walletHearts,
                onBack = navController::navigateUp,
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onChatSelected = { name, avatarRes ->
                    activeChatName = name
                    activeChatAvatar = avatarRes
                    navController.navigateSingleTop(AppRoute.PersonalChat)
                },
                /*onConnectSelected = { navController.navigateHome() },
                onGamesSelected = { navController.navigateSingleTop(AppRoute.Games) },
                onHistorySelected = { navController.navigateSingleTop(AppRoute.History) }*/
            )
        }

        composable(AppRoute.History.route) {
            HistoryScreen(
                walletHearts = walletHearts,  // ← Added hearts
                onBack = navController::navigateUp,  // ← Natural back navigation
                onProfileRequested = { navController.navigateSingleTop(AppRoute.Profile) },  // ← Added
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onConnectSelected = { navController.navigateSingleTop(AppRoute.Home) },
                onGamesSelected = { navController.navigateSingleTop(AppRoute.Games) },
                onHomeSelected = { navController.navigateHome() }
            )
        }

        composable(AppRoute.Games.route) {
            GameScreen(
                walletHearts = walletHearts,
                onBack = navController::navigateUp,
                onConnectSelected = { navController.navigateSingleTop(AppRoute.Home) },
                onTruthDareSelected = { navController.navigateSingleTop(AppRoute.TruthDare) },
                onHomeSelected = { navController.navigateHome() },
                onHistorySelected = { navController.navigateSingleTop(AppRoute.History) },
                onProfileRequested = { navController.navigateSingleTop(AppRoute.Profile) },
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) }
            )
        }

        composable(AppRoute.TruthDare.route) {
            TruthDareScreen(
                walletHearts = walletHearts,
                onBack = navController::navigateUp,
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) }
            )
        }

        composable(AppRoute.PersonalChat.route) {
            PersonalChatScreen(
                personName = activeChatName,
                avatarRes = activeChatAvatar,
                onBack = navController::navigateUp
            )
        }

        composable(AppRoute.Friends.route) {
            FriendsListScreen(
                onBack = navController::navigateUp
            )
        }

        composable(AppRoute.Recharge.route) {
            RechargeScreen(
                walletHearts = walletHearts,
                onBack = navController::navigateUp
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
    navigate(AppRoute.Home2.route) {
        launchSingleTop = true
    }
}
