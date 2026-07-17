package com.gobff.getfriends.navigation

import android.Manifest
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gobff.getfriends.BuildConfig
import com.gobff.getfriends.CallEndedPush
import com.gobff.getfriends.IncomingCallPush
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
import com.gobff.getfriends.screens.IncomingCallScreen
import com.gobff.getfriends.screens.LiveScreen
import com.gobff.getfriends.screens.LoginScreen
import com.gobff.getfriends.screens.PersonalChatScreen
import com.gobff.getfriends.screens.ProfileScreen
import com.gobff.getfriends.screens.RechargeScreen
import com.gobff.getfriends.screens.SettingsScreen
import com.gobff.getfriends.screens.SplashScreen
import com.gobff.getfriends.screens.TruthDareScreen
import com.gobff.getfriends.screens.UpdateAppScreen
import com.gobff.getfriends.screens.WalletScreen
import com.gobff.getfriends.service.PresenceForegroundService
import com.gobff.getfriends.ui.component.BffBottomBar
import com.gobff.getfriends.ui.component.MainBottomTab
import com.gobff.getfriends.utils.AppSession
import com.gobff.getfriends.utils.Constant
import com.gobff.getfriends.utils.NotificationPermissionAction
import com.gobff.getfriends.utils.NotificationPermissionState
import com.gobff.getfriends.utils.NotificationPermissionState.findActivity
import com.gobff.getfriends.utils.NotificationPermissionUiState
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
    navController: NavHostController = rememberNavController(),
    incomingCallPush: IncomingCallPush? = null,
    onIncomingCallPushHandled: () -> Unit = {},
    callEndedPush: CallEndedPush? = null,
    onCallEndedPushHandled: () -> Unit = {}
) {
    val mainRepository = remember { MainRepository() }
    val mainViewModel: MainViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val activity = context.findActivity()
    val walletViewModel: WalletViewModel = viewModel()
    val userProfileViewModel: UserProfileViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val walletHearts = walletViewModel.uiState.hearts
    val currentUserProfile = userProfileViewModel.uiState
    var activeCallName by remember { mutableStateOf("Anshu") }
    var activeCallInvitedUserId by remember { mutableStateOf<String?>(null) }
    var incomingCallRoomId by remember { mutableStateOf<String?>(null) }
    var incomingCallRequestedRole by remember { mutableStateOf("SPEAKER") }
    var incomingCallAvatarUrl by remember { mutableStateOf<String?>(null) }
    var activeChatName by remember { mutableStateOf("Anshu") }
    var activeChatAvatar by remember { mutableStateOf(R.drawable.women_avatar1) }
    var initialAppOpenDispatched by remember { mutableStateOf(false) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val selectedBottomTab = currentRoute.toMainBottomTab()
    val adaptiveBottomPadding = (configuration.screenHeightDp.dp * 0.035f)
        .coerceIn(MIN_BOTTOM_BAR_CONTENT_PADDING, MAX_BOTTOM_BAR_CONTENT_PADDING)
    val appBottomBarContentPadding = if (selectedBottomTab == null) 0.dp else adaptiveBottomPadding
    var lastHomeBackPressAt by remember { mutableStateOf(0L) }
    var notificationPermissionUiState by remember {
        mutableStateOf(NotificationPermissionState.currentUiState(context, activity))
    }
    var pendingNotificationAccessAction by remember {
        mutableStateOf<(() -> Unit)?>(null)
    }
    var pendingNotificationSettingsReturn by remember { mutableStateOf(false) }
    val notificationBannerTopPadding by animateDpAsState(
        targetValue = if (notificationPermissionUiState.showBanner) {
            NOTIFICATION_BANNER_CONTENT_HEIGHT
        } else {
            0.dp
        },
        animationSpec = tween(NOTIFICATION_BANNER_ANIMATION_DURATION_MS),
        label = "notificationBannerTopPadding"
    )

    fun refreshNotificationPermissionState() {
        notificationPermissionUiState = NotificationPermissionState.currentUiState(context, activity)
    }

    fun resolvePendingNotificationAction(clearWhenAccessMissing: Boolean) {
        val pendingAction = pendingNotificationAccessAction ?: return
        if (NotificationPermissionState.hasNotificationAccess(context)) {
            pendingNotificationAccessAction = null
            pendingNotificationSettingsReturn = false
            pendingAction()
        } else if (clearWhenAccessMissing) {
            pendingNotificationAccessAction = null
            pendingNotificationSettingsReturn = false
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        NotificationPermissionState.markRuntimePermissionRequested()
        if (granted) {
            NotificationPermissionState.clearRuntimePermissionDenied()
        } else {
            NotificationPermissionState.markRuntimePermissionDenied()
        }
        refreshNotificationPermissionState()
        resolvePendingNotificationAction(clearWhenAccessMissing = true)
    }

    fun requestNotificationAccess(onAccessReady: () -> Unit = {}) {
        if (NotificationPermissionState.hasNotificationAccess(context)) {
            onAccessReady()
            refreshNotificationPermissionState()
            return
        }

        pendingNotificationAccessAction = onAccessReady
        val latestState = NotificationPermissionState.currentUiState(context, activity)
        notificationPermissionUiState = latestState
        when (latestState.action) {
            NotificationPermissionAction.RequestPermission -> {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            NotificationPermissionAction.OpenSettings -> {
                pendingNotificationSettingsReturn = true
                NotificationPermissionState.openAppNotificationSettings(context)
            }
            NotificationPermissionAction.None -> Unit
        }
    }

    fun clearUserStateAndNavigateToLogin() {
        PresenceHeartbeat.setAlwaysOnlineEnabled(false)
        PresenceForegroundService.stop(context.applicationContext)
        mainViewModel.stopForegroundHeartbeat(markOffline = true)
        AppSession.clear()
        navController.navigate(AppRoute.Login.route) {
            popUpTo(0)
            launchSingleTop = true
        }
    }

    BackHandler(enabled = currentRoute == AppRoute.Home2.route) {
        val now = System.currentTimeMillis()
        if (now - lastHomeBackPressAt <= HOME_EXIT_BACK_PRESS_WINDOW_MS) {
            (context as? Activity)?.finish()
        } else {
            lastHomeBackPressAt = now
            Toast.makeText(context, "Press back again to close the app", Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler(enabled = currentRoute.isSecondaryBottomTabRoute()) {
        navController.navigateHome()
    }

    LaunchedEffect(incomingCallPush) {
        val push = incomingCallPush ?: return@LaunchedEffect
        activeCallName = push.callerName
        activeCallInvitedUserId = null
        incomingCallRoomId = push.roomId
        incomingCallRequestedRole = push.requestedRole
        incomingCallAvatarUrl = push.callerAvatarUrl
        onIncomingCallPushHandled()
        navController.navigateSingleTop(AppRoute.IncomingCall)
    }

    LaunchedEffect(callEndedPush, currentRoute, incomingCallRoomId) {
        val push = callEndedPush ?: return@LaunchedEffect
        if (currentRoute == AppRoute.Call.route) return@LaunchedEffect

        if (currentRoute == AppRoute.IncomingCall.route && push.matchesRoom(incomingCallRoomId)) {
            incomingCallRoomId = null
            incomingCallAvatarUrl = null
            Toast.makeText(
                context,
                push.displayMessage(TokenUtils.getCurrentUserId()),
                Toast.LENGTH_SHORT
            ).show()
            onCallEndedPushHandled()
            navController.navigateHome()
            return@LaunchedEffect
        }

        Toast.makeText(
            context,
            push.displayMessage(TokenUtils.getCurrentUserId()),
            Toast.LENGTH_SHORT
        ).show()
        onCallEndedPushHandled()
    }

    LaunchedEffect(Unit) {
        refreshNotificationPermissionState()
        if (NotificationPermissionState.shouldRequestOnAppStart(context)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    refreshNotificationPermissionState()
                    if (pendingNotificationSettingsReturn) {
                        resolvePendingNotificationAction(clearWhenAccessMissing = true)
                    }
                    if (initialAppOpenDispatched) {
                        mainViewModel.onAppOpen()
                    } else {
                        initialAppOpenDispatched = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    refreshNotificationPermissionState()
                    if (pendingNotificationSettingsReturn) {
                        resolvePendingNotificationAction(clearWhenAccessMissing = true)
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
            enterTransition = {
                appEnterTransition(initialState, targetState)
            },
            exitTransition = {
                appExitTransition(initialState, targetState)
            },
            popEnterTransition = {
                appPopEnterTransition(initialState, targetState)
            },
            popExitTransition = {
                appPopExitTransition(initialState, targetState)
            },
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(
                    top = notificationBannerTopPadding,
                    bottom = appBottomBarContentPadding
                )
        ) {
        composable(AppRoute.Splash.route) {
            LaunchedEffect(Unit) {
                AppSession.logSnapshot("Splash.start")
                val appVersionResponse = runCatching {
                    mainRepository.getAppVersion(Constant.DEVICE_PLATFORM, BuildConfig.VERSION_NAME)
                }.onFailure { error ->
                    Log.w(TAG, "App version check failed during splash", error)
                }.getOrNull()
                val hasStoredSession = TokenUtils.hasStoredSession()
                if (hasStoredSession) {
                    walletViewModel.loadWalletBalance()
                }
                delay(SPLASH_DURATION_MS)
                val appVersionStatus = appVersionResponse?.body()?.status
                if (!appVersionStatus.isNullOrBlank() && !appVersionStatus.equals("OK", ignoreCase = true)) {
                    Log.d(TAG, "Splash navigating to update app status=$appVersionStatus")
                    navController.navigate(AppRoute.UpdateApp.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    return@LaunchedEffect
                }
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

        composable(AppRoute.UpdateApp.route) {
            UpdateAppScreen()
        }

        composable(AppRoute.Login.route) {
            LoginScreen(
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
                onCallRequested = { profile ->
                    activeCallName = profile.name
                    activeCallInvitedUserId = profile.userId.takeIf { it.isNotBlank() }
                    incomingCallRoomId = null
                    incomingCallRequestedRole = "SPEAKER"
                    incomingCallAvatarUrl = null
                    navController.navigateSingleTop(AppRoute.Call)
                },
                onFriendsRequested = { navController.navigateSingleTop(AppRoute.Friends) },
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onHomeRequested = { navController.navigateHome() },
                onHistoryRequested = { navController.navigateSingleTop(AppRoute.History) },
                onGamesRequested = { navController.navigateSingleTop(AppRoute.Games) },
                onProfileRequested = { navController.navigateSingleTop(AppRoute.Profile) },
                onNotificationAccessRequested = { onAccessReady ->
                    requestNotificationAccess(onAccessReady)
                },
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
                avatarUrl = userProfileViewModel.uiState.avatarUrl,
                gender = userProfileViewModel.uiState.gender,
                onBack =  { navController.navigateHome() },
                onLogout = {
                    AppSession.clear()
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                onHomeSelected = { navController.navigateHome() },
                onConnectSelected = { navController.navigateBottomTab(AppRoute.Home) },
                onGamesSelected = { navController.navigateBottomTab(AppRoute.Games) },
                onChatSelected = { navController.navigateSingleTop(AppRoute.Chat) },
                onHistorySelected = { navController.navigateBottomTab(AppRoute.History) },
                onProfileRequested = { navController.navigateSingleTop(AppRoute.Profile) },
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onLiveSelected = { navController.navigateBottomTab(AppRoute.Live) },
                onFriendsListSelected = { navController.navigateSingleTop(AppRoute.Friends) },
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
                isAvailableForCalls = mainViewModel.userAvailableForCalls,
                onAvailabilityChanged = mainViewModel::updateUserAvailableForCalls,
                onNotificationAccessRequested = { onAccessReady ->
                    requestNotificationAccess(onAccessReady)
                },
                userProfileViewModel = userProfileViewModel
            )
        }

        composable(AppRoute.Settings.route) {
            SettingsScreen(
                walletHearts = walletHearts,
                onBack = navController::navigateUp,
                hasNotificationAccess = NotificationPermissionState.hasNotificationAccess(context),
                onAlwaysOnlineChanged = { enabled ->
                    if (enabled) {
                        mainViewModel.stopForegroundHeartbeat()
                        PresenceForegroundService.start(context.applicationContext)
                    } else {
                        PresenceForegroundService.stop(context.applicationContext)
                        mainViewModel.onAppOpen()
                    }
                },
                onNotificationAccessRequested = {
                    requestNotificationAccess(it)
                },
                onLogout = { clearUserStateAndNavigateToLogin() },
                onDeleteAccount = { clearUserStateAndNavigateToLogin() }
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
                currentUserAvatarUrl = currentUserProfile.avatarUrl,
                currentUserGender = currentUserProfile.gender,
                onBack = navController::navigateUp,  // ← Natural back navigation
                onProfileRequested = { navController.navigateSingleTop(AppRoute.Profile) },  // ← Added
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) },
                onConnectSelected = { navController.navigateBottomTab(AppRoute.Home) },
                onGamesSelected = { navController.navigateBottomTab(AppRoute.Games) },
                onHomeSelected = { navController.navigateHome() }
            )
        }

        composable(AppRoute.Games.route) {
            GameScreen(
                walletHearts = walletHearts,
                currentUserAvatarUrl = currentUserProfile.avatarUrl,
                currentUserGender = currentUserProfile.gender,
                onBack = navController::navigateUp,
                onConnectSelected = { navController.navigateBottomTab(AppRoute.Home) },
                onTruthDareSelected = { navController.navigateSingleTop(AppRoute.TruthDare) },
                onHomeSelected = { navController.navigateHome() },
                onHistorySelected = { navController.navigateBottomTab(AppRoute.History) },
                onProfileRequested = { navController.navigateSingleTop(AppRoute.Profile) },
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) }
            )
        }

        composable(AppRoute.Live.route) {
            LiveScreen(
                walletHearts = walletHearts,
                hasNotificationAccess = NotificationPermissionState.hasNotificationAccess(context),
                onNotificationAccessRequested = { onAccessReady ->
                    requestNotificationAccess(onAccessReady)
                },
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
                walletHearts = walletHearts,
                onBack = navController::navigateUp,
                onRechargeRequested = { navController.navigateSingleTop(AppRoute.Recharge) }
            )
        }

        composable(AppRoute.Recharge.route) {
            RechargeScreen(
                walletHearts = walletHearts,
                onBack = navController::navigateUp
            )
        }

        composable(AppRoute.IncomingCall.route) {
            val roomId = incomingCallRoomId
            IncomingCallScreen(
                callerName = activeCallName,
                callerAvatarUrl = incomingCallAvatarUrl,
                onAccept = {
                    if (!roomId.isNullOrBlank()) {
                        navController.navigateSingleTop(AppRoute.Call)
                    }
                },
                onDecline = {
                    val declinedRoomId = roomId
                    incomingCallRoomId = null
                    incomingCallAvatarUrl = null
                    navController.navigateHome()
                    if (!declinedRoomId.isNullOrBlank()) {
                        coroutineScope.launch {
                            runCatching {
                                val token = TokenUtils.getToken()
                                if (token.isNotBlank()) {
                                    mainRepository.endRoom(token, declinedRoomId)
                                }
                            }.onFailure { error ->
                                Log.w(TAG, "Incoming call decline failed roomId=$declinedRoomId", error)
                            }
                        }
                    }
                }
            )
        }

        composable(AppRoute.Call.route) {
            CallScreen(
                personName = activeCallName,
                currentUserDisplayName = currentUserProfile.displayName,
                currentUserAvatarUrl = currentUserProfile.avatarUrl,
                currentUserGender = currentUserProfile.gender,
                outgoingInvitedUserId = activeCallInvitedUserId,
                incomingRoomId = incomingCallRoomId,
                incomingRequestedRole = incomingCallRequestedRole,
                walletHearts = walletHearts,
                callEndedPush = callEndedPush,
                onCallEndedPushHandled = onCallEndedPushHandled,
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
                    if (tab != selectedTab) {
                        when (tab) {
                            MainBottomTab.Home -> navController.navigateHome()
                            MainBottomTab.Connect -> navController.navigateBottomTab(AppRoute.Home)
                            MainBottomTab.Games -> navController.navigateBottomTab(AppRoute.Games)
                            MainBottomTab.History -> navController.navigateBottomTab(AppRoute.History)
                            MainBottomTab.Live -> navController.navigateBottomTab(AppRoute.Live)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }

        GlobalNotificationPermissionBanner(
            uiState = notificationPermissionUiState,
            onEnableClick = {
                requestNotificationAccess()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
        )
    }
}

@Composable
private fun GlobalNotificationPermissionBanner(
    uiState: NotificationPermissionUiState,
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = uiState.showBanner,
        enter = slideInVertically(
            animationSpec = tween(NOTIFICATION_BANNER_ANIMATION_DURATION_MS),
            initialOffsetY = { fullHeight -> -fullHeight }
        ) + fadeIn(animationSpec = tween(NOTIFICATION_BANNER_FADE_DURATION_MS)),
        exit = slideOutVertically(
            animationSpec = tween(NOTIFICATION_BANNER_ANIMATION_DURATION_MS),
            targetOffsetY = { fullHeight -> -fullHeight }
        ) + fadeOut(animationSpec = tween(NOTIFICATION_BANNER_FADE_DURATION_MS)),
        modifier = modifier
    ) {
        Surface(
            color = Color(0xFFFFF6D6),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = NOTIFICATION_BANNER_CONTENT_HEIGHT)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFFF7A1A)
                )
                Text(
                    text = "Enable notifications for call alerts.",
                    color = Color(0xFF241A10),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Enable",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF111111))
                        .clickable(onClick = onEnableClick)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

private fun appEnterTransition(
    initialState: NavBackStackEntry,
    targetState: NavBackStackEntry
): EnterTransition {
    val direction = navigationDirection(
        fromRoute = initialState.destination.route,
        toRoute = targetState.destination.route
    )
    return if (direction != 0) {
        slideInHorizontally(
            animationSpec = tween(NAVIGATION_TRANSITION_DURATION_MS),
            initialOffsetX = { fullWidth -> direction * fullWidth / 5 }
        ) + fadeIn(animationSpec = tween(NAVIGATION_FADE_DURATION_MS))
    } else {
        scaleIn(
            animationSpec = tween(NAVIGATION_TRANSITION_DURATION_MS),
            initialScale = NAVIGATION_SCALE_IN
        ) + fadeIn(animationSpec = tween(NAVIGATION_FADE_DURATION_MS))
    }
}

private fun appExitTransition(
    initialState: NavBackStackEntry,
    targetState: NavBackStackEntry
): ExitTransition {
    val direction = navigationDirection(
        fromRoute = initialState.destination.route,
        toRoute = targetState.destination.route
    )
    return if (direction != 0) {
        slideOutHorizontally(
            animationSpec = tween(NAVIGATION_TRANSITION_DURATION_MS),
            targetOffsetX = { fullWidth -> -direction * fullWidth / 7 }
        ) + fadeOut(animationSpec = tween(NAVIGATION_FADE_DURATION_MS))
    } else {
        scaleOut(
            animationSpec = tween(NAVIGATION_TRANSITION_DURATION_MS),
            targetScale = NAVIGATION_SCALE_OUT
        ) + fadeOut(animationSpec = tween(NAVIGATION_FADE_DURATION_MS))
    }
}

private fun appPopEnterTransition(
    initialState: NavBackStackEntry,
    targetState: NavBackStackEntry
): EnterTransition {
    val direction = navigationDirection(
        fromRoute = targetState.destination.route,
        toRoute = initialState.destination.route
    )
    return if (direction != 0) {
        slideInHorizontally(
            animationSpec = tween(NAVIGATION_TRANSITION_DURATION_MS),
            initialOffsetX = { fullWidth -> -direction * fullWidth / 5 }
        ) + fadeIn(animationSpec = tween(NAVIGATION_FADE_DURATION_MS))
    } else {
        scaleIn(
            animationSpec = tween(NAVIGATION_TRANSITION_DURATION_MS),
            initialScale = NAVIGATION_SCALE_IN
        ) + fadeIn(animationSpec = tween(NAVIGATION_FADE_DURATION_MS))
    }
}

private fun appPopExitTransition(
    initialState: NavBackStackEntry,
    targetState: NavBackStackEntry
): ExitTransition {
    val direction = navigationDirection(
        fromRoute = targetState.destination.route,
        toRoute = initialState.destination.route
    )
    return if (direction != 0) {
        slideOutHorizontally(
            animationSpec = tween(NAVIGATION_TRANSITION_DURATION_MS),
            targetOffsetX = { fullWidth -> direction * fullWidth / 7 }
        ) + fadeOut(animationSpec = tween(NAVIGATION_FADE_DURATION_MS))
    } else {
        scaleOut(
            animationSpec = tween(NAVIGATION_TRANSITION_DURATION_MS),
            targetScale = NAVIGATION_SCALE_OUT
        ) + fadeOut(animationSpec = tween(NAVIGATION_FADE_DURATION_MS))
    }
}

private fun navigationDirection(fromRoute: String?, toRoute: String?): Int {
    val fromIndex = fromRoute.navigationOrderIndex()
    val toIndex = toRoute.navigationOrderIndex()
    if (fromIndex == null || toIndex == null || fromIndex == toIndex) return 0
    return if (toIndex > fromIndex) 1 else -1
}

private fun String?.navigationOrderIndex(): Int? =
    when (this) {
        AppRoute.Home2.route -> 0
        AppRoute.Home.route -> 1
        AppRoute.Games.route -> 2
        AppRoute.History.route -> 3
        AppRoute.Live.route -> 4
        AppRoute.Chat.route -> 5
        AppRoute.Profile.route -> 6
        AppRoute.Settings.route -> 7
        AppRoute.GiftVibe.route -> 8
        AppRoute.Wallet.route -> 9
        AppRoute.Recharge.route -> 10
        AppRoute.Friends.route -> 11
        AppRoute.PersonalChat.route -> 12
        AppRoute.TruthDare.route -> 13
        AppRoute.IncomingCall.route -> 14
        AppRoute.Call.route -> 15
        AppRoute.Gender.route -> 16
        AppRoute.Audio.route -> 17
        AppRoute.Login.route -> 18
        AppRoute.UpdateApp.route -> 19
        AppRoute.Splash.route -> 20
        else -> null
    }

private fun String?.toMainBottomTab(): MainBottomTab? =
    when (this) {
        AppRoute.Home2.route -> MainBottomTab.Home
        AppRoute.Home.route -> MainBottomTab.Connect
        AppRoute.Games.route -> MainBottomTab.Games
        AppRoute.History.route -> MainBottomTab.History
        AppRoute.Live.route -> MainBottomTab.Live
        else -> null
    }

private fun String?.isSecondaryBottomTabRoute(): Boolean =
    this == AppRoute.Home.route ||
        this == AppRoute.Games.route ||
        this == AppRoute.History.route ||
        this == AppRoute.Live.route

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

private fun NavHostController.navigateBottomTab(route: AppRoute) {
    navigate(route.route) {
        popUpTo(AppRoute.Home2.route) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

private fun NavHostController.navigateHome() {
    navigate(AppRoute.Home2.route) {
        popUpTo(0)
        launchSingleTop = true
    }
}

private const val HOME_EXIT_BACK_PRESS_WINDOW_MS = 2_000L
private const val NAVIGATION_TRANSITION_DURATION_MS = 280
private const val NAVIGATION_FADE_DURATION_MS = 180
private const val NAVIGATION_SCALE_IN = 0.98f
private const val NAVIGATION_SCALE_OUT = 0.99f
private const val NOTIFICATION_BANNER_ANIMATION_DURATION_MS = 240
private const val NOTIFICATION_BANNER_FADE_DURATION_MS = 160
private val NOTIFICATION_BANNER_CONTENT_HEIGHT = 44.dp
