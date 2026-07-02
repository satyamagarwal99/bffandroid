package com.gobff.getfriends.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gobff.getfriends.R
import com.gobff.getfriends.data.MainRepository
import com.gobff.getfriends.screens.AudioScreen
import com.gobff.getfriends.screens.CallScreen
import com.gobff.getfriends.screens.ChatScreen
import com.gobff.getfriends.screens.FriendsListScreen
import com.gobff.getfriends.screens.GameScreen
import com.gobff.getfriends.screens.GenderScreen
import com.gobff.getfriends.screens.GiftVibeScreen
import com.gobff.getfriends.screens.HistoryScreen
import com.gobff.getfriends.screens.HomeScreen
import com.gobff.getfriends.screens.HomeScreen2
import com.gobff.getfriends.screens.LoginScreen
import com.gobff.getfriends.screens.PersonalChatScreen
import com.gobff.getfriends.screens.ProfileScreen
import com.gobff.getfriends.screens.RechargeScreen
import com.gobff.getfriends.screens.SettingsScreen
import com.gobff.getfriends.screens.SplashScreen
import com.gobff.getfriends.screens.TruthDareScreen
import com.gobff.getfriends.screens.WalletScreen
import com.gobff.getfriends.service.PresenceForegroundService
import com.gobff.getfriends.ui.component.BffBottomBar
import com.gobff.getfriends.ui.component.MainBottomTab
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.Constant
import com.gobff.getfriends.utils.PresenceHeartbeat
import com.gobff.getfriends.utils.TokenUtils
import com.gobff.getfriends.viewmodel.MainViewModel
import com.gobff.getfriends.viewmodel.UserProfileViewModel
import com.gobff.getfriends.viewmodel.WalletViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPLASH_DURATION_MS = 2_000L
private const val TAG = "AppNavGraph"
private val MIN_BOTTOM_BAR_CONTENT_PADDING = 0.dp
private val MAX_BOTTOM_BAR_CONTENT_PADDING = 0.dp

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val mainRepository = remember { MainRepository() }
    val mainViewModel: MainViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val walletViewModel: WalletViewModel = viewModel()
    val userProfileViewModel: UserProfileViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val walletHearts = walletViewModel.uiState.hearts
    var activeCallName by remember { mutableStateOf("Anshu") }
    var activeChatName by remember { mutableStateOf("Anshu") }
    var activeChatAvatar by remember { mutableStateOf(R.drawable.women_avatar3) }
    var initialAppOpenDispatched by remember { mutableStateOf(false) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val selectedBottomTab = currentRoute.toMainBottomTab()
    val adaptiveBottomPadding = (configuration.screenHeightDp.dp * 0.035f)
        .coerceIn(MIN_BOTTOM_BAR_CONTENT_PADDING, MAX_BOTTOM_BAR_CONTENT_PADDING)
    val appBottomBarContentPadding = if (selectedBottomTab == null) 0.dp else adaptiveBottomPadding

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

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = appBottomBarContentPadding)
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
                    resolvePostAuthRoute(
                        hasProfile = hasProfile,
                        requiresVoiceVerification = userProfileViewModel.shouldCompleteVoiceVerification()
                    )
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
                        val hasProfile = userProfileViewModel.refreshProfile()
                        val nextRoute = resolvePostAuthRoute(
                            hasProfile = hasProfile,
                            requiresVoiceVerification = userProfileViewModel.shouldCompleteVoiceVerification()
                        )
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
                val hasProfile = userProfileViewModel.refreshProfile()
                val nextRoute = resolvePostAuthRoute(
                    hasProfile = hasProfile,
                    requiresVoiceVerification = userProfileViewModel.shouldCompleteVoiceVerification()
                )
                if (nextRoute != AppRoute.Home2.route) {
                    navController.navigate(nextRoute) {
                        popUpTo(AppRoute.Home2.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
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
                onAlwaysOnlineChanged = { enabled ->
                    if (enabled) {
                        mainViewModel.stopForegroundHeartbeat()
                        PresenceForegroundService.start(context.applicationContext)
                    } else {
                        PresenceForegroundService.stop(context.applicationContext)
                        mainViewModel.onAppOpen()
                    }
                },
                onLogout = {
                    PresenceHeartbeat.setAlwaysOnlineEnabled(false)
                    PresenceForegroundService.stop(context.applicationContext)
                    mainViewModel.stopForegroundHeartbeat(markOffline = true)
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsBottomHeight(androidx.compose.foundation.layout.WindowInsets.navigationBars)
                .background(Color.White)
        )

        selectedBottomTab?.let { selectedTab ->
            BffBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    when (tab) {
                        MainBottomTab.Home -> navController.navigateHome()
                        MainBottomTab.Connect -> navController.navigateSingleTop(AppRoute.Home)
                        MainBottomTab.Games -> navController.navigateSingleTop(AppRoute.Games)
                        MainBottomTab.History -> navController.navigateSingleTop(AppRoute.History)
                        MainBottomTab.Live -> Unit
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}

private fun String?.toMainBottomTab(): MainBottomTab? =
    when (this) {
        AppRoute.Home2.route -> MainBottomTab.Home
        AppRoute.Home.route -> MainBottomTab.Connect
        AppRoute.Games.route -> MainBottomTab.Games
        AppRoute.History.route -> MainBottomTab.History
        else -> null
    }

private fun resolvePostAuthRoute(
    hasProfile: Boolean,
    requiresVoiceVerification: Boolean
): String {
    return when {
        !hasProfile -> AppRoute.Gender.route
        requiresVoiceVerification -> AppRoute.Audio.route
        else -> AppRoute.Home2.route
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
