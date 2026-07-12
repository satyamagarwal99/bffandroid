package com.gobff.getfriends.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import android.view.View
import androidx.compose.foundation.Image
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.gobff.getfriends.CallEndedPush
import com.gobff.getfriends.R
import com.gobff.getfriends.data.model.GameCatalogItemDto
import com.gobff.getfriends.data.model.GiftCatalogResponse
import com.gobff.getfriends.data.model.GiftCategoryDto
import com.gobff.getfriends.data.model.GiftItemDto
import com.gobff.getfriends.data.model.RoomMessageResponse
import com.gobff.getfriends.data.model.RoomType
import com.gobff.getfriends.ui.component.CachedAvatarImage
import com.gobff.getfriends.ui.component.ChatBubbleShape
import com.gobff.getfriends.ui.component.HeartChipShape
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.utils.AvatarGender
import com.gobff.getfriends.utils.toAvatarGender
import com.gobff.getfriends.utils.TokenUtils
import com.gobff.getfriends.viewmodel.CallViewModel
import com.gobff.getfriends.viewmodel.GameCatalogUiState
import com.gobff.getfriends.viewmodel.GameCatalogViewModel
import com.gobff.getfriends.viewmodel.GiftCatalogUiState
import com.gobff.getfriends.viewmodel.GiftCatalogViewModel
import io.agora.base.internal.SurfaceViewRenderer
import kotlinx.coroutines.delay

private enum class VideoPendingAction {
    SHOW_REQUEST_SHEET,
    REQUEST_VIDEO,
    ACCEPT_VIDEO
}

private val CallYellow = Color(0xFFF5B120)

@Composable
fun CallScreen(
    personName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    currentUserDisplayName: String? = null,
    currentUserAvatarUrl: String? = null,
    currentUserGender: String? = null,
    outgoingInvitedUserId: String? = null,
    incomingRoomId: String? = null,
    incomingRequestedRole: String = "SPEAKER",
    walletHearts: Int = 145,
    callEndedPush: CallEndedPush? = null,
    onCallEndedPushHandled: () -> Unit = {},
    callViewModel: CallViewModel = viewModel(),
    giftCatalogViewModel: GiftCatalogViewModel = viewModel(),
    gameCatalogViewModel: GameCatalogViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState = callViewModel.uiState
    var countdown by remember { mutableIntStateOf(3) }
    var callSecondsRemaining by remember { mutableIntStateOf(4 * 60 + 59) }
    val isConnected = uiState.isRtcJoined
    var showAddTimeSheet by remember { mutableStateOf(false) }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showGameSheet by remember { mutableStateOf(false) }
    var showChatSheet by remember { mutableStateOf(false) }
    var showSafetySheet by remember { mutableStateOf(false) }
    var showFeedbackPopup by remember { mutableStateOf(false) }
    var showVideoUpgradeRequestSheet by remember { mutableStateOf(false) }
    var remoteCallEndMessage by remember { mutableStateOf<String?>(null) }
    var remoteCallDeclined by remember { mutableStateOf(false) }
    var hasRetriedWithRandom by remember { mutableStateOf(false) }
    var selectedAddTimeOption by remember { mutableStateOf(AddTimeOptions.first()) }
    var sendingGift by remember { mutableStateOf<GiftItem?>(null) }
    var giftDeliveryPhase by remember { mutableStateOf<GiftDeliveryPhase?>(null) }
    var isSendingGift by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var pendingVideoAction by remember { mutableStateOf<VideoPendingAction?>(null) }
    var shouldEndBackendOnDispose by remember { mutableStateOf(true) }
    val giftCatalogUiState = giftCatalogViewModel.uiState
    val gameCatalogUiState = gameCatalogViewModel.uiState
    val isOutgoingCall = incomingRoomId.isNullOrBlank()
    val hasRemoteUserJoined = uiState.remoteAudioUserIds.isNotEmpty()
    val shouldShowDialingScreen = isOutgoingCall && !hasRemoteUserJoined
    val currentUserId = TokenUtils.getCurrentUserId()
    val videoUpgradeStatus = uiState.videoUpgradeStatus
    val shouldShowVideoCall = uiState.isVideoEnabled ||
        (videoUpgradeStatus?.status == "COMPLETED" &&
            videoUpgradeStatus.roomType == RoomType.OneToOneVideoCall)
    val showVideoUpgradePrompt = videoUpgradeStatus?.status == "PENDING" &&
        videoUpgradeStatus.requestedByUserId?.isNotBlank() == true &&
        videoUpgradeStatus.requestedByUserId != currentUserId
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        val nextAction = pendingVideoAction
        pendingVideoAction = null
        if (!isGranted || nextAction == null) return@rememberLauncherForActivityResult

        when (nextAction) {
            VideoPendingAction.SHOW_REQUEST_SHEET -> showVideoUpgradeRequestSheet = true
            VideoPendingAction.REQUEST_VIDEO -> callViewModel.requestVideoUpgrade()
            VideoPendingAction.ACCEPT_VIDEO -> callViewModel.acceptVideoUpgrade()
        }
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    fun leaveAndClose() {
        shouldEndBackendOnDispose = true
        callViewModel.leaveCall(roomId = incomingRoomId ?: callViewModel.uiState.room?.id)
        onBack()
    }

    fun closeEndedCallScreen() {
        shouldEndBackendOnDispose = false
        onBack()
    }

    fun startRandomFallback(message: String) {
        if (hasRetriedWithRandom) return
        hasRetriedWithRandom = true
        remoteCallEndMessage = message
        remoteCallDeclined = false
        showFeedbackPopup = false
        callViewModel.leaveCall(roomId = incomingRoomId ?: callViewModel.uiState.room?.id)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        callViewModel.createRandomOneToOneAudioCall(
            title = "$personName Audio Call",
            onSuccess = {
                remoteCallEndMessage = null
                remoteCallDeclined = false
            },
            onFailure = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                closeEndedCallScreen()
            }
        )
    }

    val requestCameraPermission: (VideoPendingAction) -> Unit = { nextAction ->
        if (hasCameraPermission) {
            when (nextAction) {
                VideoPendingAction.SHOW_REQUEST_SHEET -> showVideoUpgradeRequestSheet = true
                VideoPendingAction.REQUEST_VIDEO -> callViewModel.requestVideoUpgrade()
                VideoPendingAction.ACCEPT_VIDEO -> callViewModel.acceptVideoUpgrade()
            }
        } else {
            pendingVideoAction = nextAction
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    BackHandler {
        when {
            showVideoUpgradeRequestSheet -> showVideoUpgradeRequestSheet = false
            showVideoUpgradePrompt -> callViewModel.declineVideoUpgrade()
            showChatSheet -> showChatSheet = false
            showGameSheet -> showGameSheet = false
            showGiftSheet -> showGiftSheet = false
            showSafetySheet -> showSafetySheet = false
            showAddTimeSheet -> showAddTimeSheet = false
            showFeedbackPopup -> closeEndedCallScreen()
            remoteCallEndMessage != null -> closeEndedCallScreen()
            else -> leaveAndClose()
        }
    }

    LaunchedEffect(personName, incomingRoomId, incomingRequestedRole, outgoingInvitedUserId, hasAudioPermission) {
        if (hasAudioPermission) {
            if (incomingRoomId.isNullOrBlank()) {
                if (!outgoingInvitedUserId.isNullOrBlank()) {
                    callViewModel.createOneToOneAudioCall(
                        title = "$personName Audio Call",
                        invitedUserId = outgoingInvitedUserId,
                        onFailure = { error ->
                            if (error.contains("token", ignoreCase = true) || error.contains("permission", ignoreCase = true)) {
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                closeEndedCallScreen()
                            } else {
                                startRandomFallback("User didn't pick the call. Calling a random person...")
                            }
                        }
                    )
                } else {
                    callViewModel.createRandomOneToOneAudioCall(
                        title = "$personName Audio Call",
                        onFailure = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            closeEndedCallScreen()
                        }
                    )
                }
            } else {
                callViewModel.joinAudioRoom(
                    roomId = incomingRoomId,
                    requestedRole = incomingRequestedRole
                )
            }
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (shouldEndBackendOnDispose) {
                callViewModel.leaveCall(roomId = incomingRoomId ?: callViewModel.uiState.room?.id)
            }
        }
    }

    LaunchedEffect(callEndedPush, incomingRoomId, uiState.room?.id) {
        val push = callEndedPush ?: return@LaunchedEffect
        val activeRoomId = incomingRoomId ?: uiState.room?.id
        if (!push.matchesRoom(activeRoomId)) return@LaunchedEffect

        val currentUser = TokenUtils.getCurrentUserId()
        val message = push.displayMessage(currentUser)
        val wasDeclined = push.wasDeclinedByCallee(currentUser)
        shouldEndBackendOnDispose = false
        callViewModel.handleRemoteCallEnded(activeRoomId)
        remoteCallEndMessage = message
        remoteCallDeclined = wasDeclined
        showAddTimeSheet = false
        showGiftSheet = false
        showGameSheet = false
        showSafetySheet = false
        showVideoUpgradeRequestSheet = false
        showFeedbackPopup = !wasDeclined
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        onCallEndedPushHandled()

        if (wasDeclined && incomingRoomId.isNullOrBlank() && !outgoingInvitedUserId.isNullOrBlank()) {
            delay(1_200L)
            startRandomFallback("User didn't pick the call. Calling a random person...")
        }
    }

    LaunchedEffect(personName, isConnected) {
        if (isConnected) return@LaunchedEffect
        for (value in 3 downTo 1) {
            countdown = value
            delay(1_000L)
        }
    }

    LaunchedEffect(outgoingInvitedUserId, uiState.room?.id, isConnected) {
        if (!incomingRoomId.isNullOrBlank()) return@LaunchedEffect
        if (outgoingInvitedUserId.isNullOrBlank()) return@LaunchedEffect
        if (isConnected || hasRetriedWithRandom) return@LaunchedEffect
        if (uiState.room?.id.isNullOrBlank()) return@LaunchedEffect

        delay(25_000L)
        if (!isConnected && !hasRetriedWithRandom) {
            startRandomFallback("User didn't pick the call. Calling a random person...")
        }
    }

    LaunchedEffect(isConnected, personName) {
        if (isConnected) {
            callSecondsRemaining = 4 * 60 + 59
            while (callSecondsRemaining > 0) {
                delay(1_000L)
                callSecondsRemaining -= 1
            }
        }
    }

    LaunchedEffect(sendingGift) {
        val gift = sendingGift ?: return@LaunchedEffect
        giftDeliveryPhase = GiftDeliveryPhase.Delivering
        delay(1_100L)
        giftDeliveryPhase = GiftDeliveryPhase.Arrived
        delay(1_400L)
        sendingGift = null
        giftDeliveryPhase = null
    }

    LaunchedEffect(showGiftSheet) {
        if (showGiftSheet) {
            giftCatalogViewModel.loadGiftCatalog(forceRefresh = giftCatalogUiState.catalog == null)
        }
    }

    LaunchedEffect(showGameSheet) {
        if (showGameSheet) {
            gameCatalogViewModel.loadGameCatalog(forceRefresh = gameCatalogUiState.games.isEmpty())
        }
    }

    LaunchedEffect(showChatSheet, incomingRoomId, uiState.room?.id) {
        val roomId = incomingRoomId ?: uiState.room?.id ?: return@LaunchedEffect
        if (!showChatSheet) return@LaunchedEffect

        callViewModel.loadRoomMessages(roomId = roomId, forceRefresh = true)
        while (showChatSheet) {
            delay(10_000L)
            if (!showChatSheet) break
            callViewModel.loadRoomMessages(roomId = roomId, forceRefresh = true)
        }
    }

    LaunchedEffect(isConnected, uiState.room?.id) {
        if (!isConnected || uiState.room?.id.isNullOrBlank()) return@LaunchedEffect
        while (true) {
            callViewModel.refreshVideoUpgradeStatus()
            delay(10_000L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CallYellow)
    ) {
        Image(
            painter = painterResource(id = R.drawable.call_screen_bg_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )


        if (shouldShowDialingScreen) {
            DialingCallScreen(
                callerName = personName,
                callerAvatarUrl = null,
                statusText = when {
                    remoteCallEndMessage != null -> remoteCallEndMessage.orEmpty()
                    !hasAudioPermission -> "Microphone permission needed"
                    uiState.errorMessage != null -> uiState.errorMessage
                    uiState.isCreatingRoom -> "Starting call..."
                    uiState.isFetchingRtcToken -> "Securing connection..."
                    uiState.isJoiningRtc -> "Connecting..."
                    uiState.isRtcJoined -> "Ringing..."
                    else -> "Connecting..."
                },
                onCancel = if (remoteCallDeclined) ::closeEndedCallScreen else ::leaveAndClose,
                modifier = Modifier.fillMaxSize()
            )
        } else if (isConnected) {
            if (shouldShowVideoCall) {
                VideoCallContent(
                    personName = personName,
                    currentUserDisplayName = currentUserDisplayName,
                    currentUserAvatarUrl = currentUserAvatarUrl,
                    currentUserGender = currentUserGender,
                    callSecondsRemaining = callSecondsRemaining,
                    callViewModel = callViewModel,
                    remoteUserId = uiState.remoteAudioUserIds.firstOrNull(),
                    isMuted = uiState.isMuted,
                    isSpeakerEnabled = uiState.isSpeakerEnabled,
                    onChatClick = { showChatSheet = true },
                    onGiftClick = { showGiftSheet = true },
                    onMuteToggle = { callViewModel.setMuted(!uiState.isMuted) },
                    onSpeakerToggle = { callViewModel.setSpeakerEnabled(!uiState.isSpeakerEnabled) },
                    onSafetyClick = { showSafetySheet = true },
                    onEndCall = {
                        callViewModel.leaveCall()
                        showFeedbackPopup = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ActiveCallContent(
                    personName = personName,
                    currentUserDisplayName = currentUserDisplayName,
                    currentUserAvatarUrl = currentUserAvatarUrl,
                    currentUserGender = currentUserGender,
                    callSecondsRemaining = callSecondsRemaining,
                    onTimerClick = { showAddTimeSheet = true },
                    onChatClick = { showChatSheet = true },
                    onGiftClick = { showGiftSheet = true },
                    onGameClick = { showGameSheet = true },
                    onSafetyClick = { showSafetySheet = true },
                    isMuted = uiState.isMuted,
                    isSpeakerEnabled = uiState.isSpeakerEnabled,
                    onMuteToggle = { callViewModel.setMuted(!uiState.isMuted) },
                    onSpeakerToggle = { callViewModel.setSpeakerEnabled(!uiState.isSpeakerEnabled) },
                    onVideoClick = { requestCameraPermission(VideoPendingAction.SHOW_REQUEST_SHEET) },
                    onEndCall = {
                        callViewModel.leaveCall()
                        showFeedbackPopup = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            ConnectingCallContent(
                personName = personName,
                countdown = countdown,
                statusText = when {
                    remoteCallEndMessage != null -> remoteCallEndMessage.orEmpty()
                    !hasAudioPermission -> "Microphone permission needed"
                    uiState.errorMessage != null -> uiState.errorMessage
                    uiState.isCreatingRoom -> "Creating room..."
                    uiState.isFetchingRtcToken -> "Fetching secure call token..."
                    uiState.isJoiningRtc -> "Joining audio..."
                    else -> "Connecting..."
                },
                onBack = if (remoteCallEndMessage != null) ::closeEndedCallScreen else ::leaveAndClose,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showAddTimeSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable { showAddTimeSheet = false }
            )
            AddTimeBottomSheet(
                timeLeftText = formatCallTime(callSecondsRemaining),
                selectedOption = selectedAddTimeOption,
                onOptionSelected = { selectedAddTimeOption = it },
                onAddTime = {
                    callSecondsRemaining += selectedAddTimeOption.minutes * 60
                    showAddTimeSheet = false
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showVideoUpgradeRequestSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable { showVideoUpgradeRequestSheet = false }
            )
            VideoUpgradeSwitchBottomSheet(
                heartsAvailable = walletHearts,
                pricePerMinute = 30,
                isLoading = uiState.isVideoUpgradeActionLoading,
                onSwitchToVideo = {
                    requestCameraPermission(VideoPendingAction.REQUEST_VIDEO)
                    showVideoUpgradeRequestSheet = false
                },
                onDismiss = { showVideoUpgradeRequestSheet = false },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showVideoUpgradePrompt) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )
            VideoUpgradeIncomingBottomSheet(
                partnerName = personName,
                heartsAvailable = walletHearts,
                pricePerMinute = 30,
                isLoading = uiState.isVideoUpgradeActionLoading,
                onAccept = { requestCameraPermission(VideoPendingAction.ACCEPT_VIDEO) },
                onDecline = callViewModel::declineVideoUpgrade,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showGiftSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(enabled = !isSendingGift) { showGiftSheet = false }
            )
            GiftBottomSheet(
                giftCatalogUiState = giftCatalogUiState,
                isSendingGift = isSendingGift,
                onDismiss = { showGiftSheet = false },
                onSendGift = { gift ->
                    val roomId = incomingRoomId ?: uiState.room?.id
                    val recipientUserId = resolveGiftRecipientUserId(
                        room = uiState.room,
                        currentUserId = currentUserId
                    )
                    if (roomId.isNullOrBlank() || recipientUserId.isNullOrBlank()) {
                        Toast.makeText(context, "Unable to send gift right now", Toast.LENGTH_SHORT).show()
                    } else {
                        isSendingGift = true
                        callViewModel.sendGift(
                            roomId = roomId,
                            giftCode = gift.code,
                            recipientUserId = recipientUserId,
                            onSuccess = {
                                isSendingGift = false
                                showGiftSheet = false
                                sendingGift = gift
                            },
                            onFailure = { message ->
                                isSendingGift = false
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showChatSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable { showChatSheet = false }
            )
            CallChatBottomSheet(
                messages = uiState.roomMessages,
                isLoading = uiState.isLoadingRoomMessages,
                isSending = uiState.isSendingRoomMessage,
                errorMessage = uiState.roomMessageErrorMessage,
                currentUserId = TokenUtils.getCurrentUserId(),
                onLoadMessages = {
                    callViewModel.loadRoomMessages(
                        roomId = incomingRoomId ?: uiState.room?.id,
                        forceRefresh = true
                    )
                },
                onSendMessage = { message ->
                    callViewModel.sendRoomMessage(
                        message = message,
                        roomId = incomingRoomId ?: uiState.room?.id
                    )
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showGameSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable { showGameSheet = false }
            )
            GameBottomSheet(
                gameCatalogUiState = gameCatalogUiState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showSafetySheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable { showSafetySheet = false }
            )
            CallSafetyBottomSheet(
                onReportClick = { showSafetySheet = false },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        sendingGift?.let { gift ->
            GiftDeliveryOverlay(
                gift = gift,
                phase = giftDeliveryPhase,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showFeedbackPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.48f))
            )
            CallFeedbackPopup(
                personName = personName,
                callEndedMessage = remoteCallEndMessage,
                isSubmitting = uiState.isSubmittingFeedback,
                errorMessage = uiState.feedbackErrorMessage,
                onSubmitFeedback = { rating, tags, comment, addFriend, onSubmitted ->
                    callViewModel.submitFeedback(
                        rating = rating,
                        tags = tags,
                        comment = comment,
                        addFriend = addFriend,
                        onSubmitted = onSubmitted
                    )
                },
                onDismiss = ::closeEndedCallScreen,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
private fun ConnectingCallContent(
    personName: String,
    countdown: Int,
    statusText: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(top = 84.dp, bottom = 32.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            CallAvatar(
                avatarRes = R.drawable.home_screen_avatar,
                size = 76,
                borderWidth = 3
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = personName,
                color = Color.Black,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = statusText ?: "Connecting...",
                color = Color(0xFF8B6814),
                fontSize = 17.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = countdown.toString(),
                color = Color.Black,
                fontSize = 86.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = 3.dp, y = 5.dp)
            )
            Text(
                text = countdown.toString(),
                color = Color.White,
                fontSize = 86.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = 166.dp, height = 48.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(14.dp))
                .clickable(onClick = onBack)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "x",
                    color = Color.Black,
                    fontSize = 25.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Cancel call",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VideoUpgradeSwitchBottomSheet(
    heartsAvailable: Int,
    pricePerMinute: Int,
    isLoading: Boolean,
    onSwitchToVideo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 30.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 94.dp, height = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFD5D5D5))
        )
        Spacer(modifier = Modifier.height(34.dp))
        Text(
            text = "Switch to Video Call",
            color = Color(0xFF4058C6),
            fontSize = 30.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(Color(0xFFB168E1))
            )
            Text(
                text = "$heartsAvailable Hearts Available",
                color = Color(0xFF242424),
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(Color(0xFFB168E1))
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFAEE))
                .padding(vertical = 28.dp, horizontal = 20.dp)
        ) {
            Text(
                text = "$pricePerMinute hearts / min",
                color = Color.Black,
                fontSize = 27.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Charges will start only after you switch to video.",
                color = Color(0xFF858585),
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(34.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(5.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFFCD47))
                .border(1.5.dp, Color.Black, RoundedCornerShape(14.dp))
                .clickable(enabled = !isLoading, onClick = onSwitchToVideo)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isLoading) "Please wait..." else "Switch to video call",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Not now",
            color = Color.Black,
            fontSize = 17.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(enabled = !isLoading, onClick = onDismiss)
        )
    }
}

@Composable
private fun VideoUpgradeIncomingBottomSheet(
    partnerName: String,
    heartsAvailable: Int,
    pricePerMinute: Int,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 30.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 94.dp, height = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFD5D5D5))
        )
        Spacer(modifier = Modifier.height(34.dp))
        Text(
            text = "Switch to Video Call",
            color = Color(0xFF4058C6),
            fontSize = 30.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(Color(0xFFB168E1))
            )
            Text(
                text = "$heartsAvailable Hearts Available",
                color = Color(0xFF242424),
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(Color(0xFFB168E1))
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFAEE))
                .padding(vertical = 28.dp, horizontal = 20.dp)
        ) {
            Text(
                text = "$pricePerMinute hearts / min",
                color = Color.Black,
                fontSize = 27.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "$partnerName wants to switch this audio call to video.",
                color = Color(0xFF858585),
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(34.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(5.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFFCD47))
                .border(1.5.dp, Color.Black, RoundedCornerShape(14.dp))
                .clickable(enabled = !isLoading, onClick = onAccept)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isLoading) "Please wait..." else "Accept video call",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Decline",
            color = Color.Black,
            fontSize = 17.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(enabled = !isLoading, onClick = onDecline)
        )
    }
}

@Composable
private fun ActiveCallContent(
    personName: String,
    currentUserDisplayName: String?,
    currentUserAvatarUrl: String?,
    currentUserGender: String?,
    callSecondsRemaining: Int,
    onTimerClick: () -> Unit,
    onChatClick: () -> Unit,
    onGiftClick: () -> Unit,
    onGameClick: () -> Unit,
    onSafetyClick: () -> Unit,
    isMuted: Boolean,
    isSpeakerEnabled: Boolean,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoClick: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(top = 68.dp, bottom = 28.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
        ) {
            CallTimerChip(
                secondsRemaining = callSecondsRemaining,
                onClick = onTimerClick
            )
            Spacer(modifier = Modifier.weight(1f))
            CallSafetyButton(onClick = onSafetyClick)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-64).dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

            Box(
                modifier = Modifier.weight(0.9f),
                contentAlignment = Alignment.Center
            ) {
                CallParticipantCard(
                    name = personName,
                    avatarRes = R.drawable.women_avatar1,
                    background = Color(0xFFFFF17A)
                )
            }

            Box(
                modifier = Modifier
                    .weight(0.35f),
                contentAlignment = Alignment.Center
            ) {
                AudioWaveform()
            }

            Box(
                modifier = Modifier.weight(0.9f),
                contentAlignment = Alignment.Center
            ) {
                CallParticipantCard(
                    name = currentUserDisplayName?.takeIf { it.isNotBlank() } ?: "You",
                    avatarUrl = currentUserAvatarUrl,
                    gender = currentUserGender,
                    avatarRes = R.drawable.man_avatar1,
                    background = Color(0xFF6CCBEE)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-126).dp)
        ) {
            CallActionBubble(
                iconRes = R.drawable.call_screen_chats,
                onClick = onChatClick
            )
            CallActionBubble(
                iconRes = R.drawable.call_screen_gift,
                onClick = onGiftClick
            )
            CallActionBubble(
                iconRes = R.drawable.call_screen_games,
                onClick = onGameClick
            )
        }

        CallControls(
            isMuted = isMuted,
            isSpeakerEnabled = isSpeakerEnabled,
            onMuteToggle = onMuteToggle,
            onSpeakerToggle = onSpeakerToggle,
            onVideoClick = onVideoClick,
            onEndCall = onEndCall,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

}

@Composable
private fun VideoCallContent(
    personName: String,
    currentUserDisplayName: String?,
    currentUserAvatarUrl: String?,
    currentUserGender: String?,
    callSecondsRemaining: Int,
    callViewModel: CallViewModel,
    remoteUserId: Int?,
    isMuted: Boolean,
    isSpeakerEnabled: Boolean,
    onChatClick: () -> Unit,
    onGiftClick: () -> Unit,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onSafetyClick: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localVideoView by remember { mutableStateOf<View?>(null) }
    var remoteVideoView by remember { mutableStateOf<View?>(null) }

    LaunchedEffect(localVideoView, remoteVideoView, remoteUserId) {
        callViewModel.bindVideoViews(
            localView = localVideoView,
            remoteView = remoteVideoView,
            remoteUid = remoteUserId
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).also { view ->
                        remoteVideoView = view
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 28.dp)
        ) {
            CallTimerChip(secondsRemaining = callSecondsRemaining, onClick = {})
            Spacer(modifier = Modifier.weight(1f))
            CallSafetyButton(onClick = onSafetyClick)
        }

        if (remoteUserId == null) {
            Text(
                text = "Waiting for $personName to join video...",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 168.dp)
                .size(width = 104.dp, height = 150.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.18f))
                .border(1.5.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
        ) {
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).also { view ->
                        localVideoView = view
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
            )
            Text(
                text = "You",
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }

        VideoQuickActions(
            onChatClick = onChatClick,
            onGiftClick = onGiftClick,
            onGameClick = { },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )

        VideoCallControls(
            isMuted = isMuted,
            isSpeakerEnabled = isSpeakerEnabled,
            onMuteToggle = onMuteToggle,
            onSpeakerToggle = onSpeakerToggle,
            onSwitchCamera = callViewModel::switchCamera,
            onEndCall = onEndCall,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CallTimerChip(
    secondsRemaining: Int,
    onClick: () -> Unit
) {
    val shape = HeartChipShape

    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 38.dp)
            .clickable(onClick = onClick)
    ) {

        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )

        // Main Chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(
                    width = 1.5.dp,
                    color = Color.Black,
                    shape = shape
                )
        ) {

            Text(
                text = formatCallTime(secondsRemaining),
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFFFF5A5A),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddTimeBottomSheet(
    timeLeftText: String,
    selectedOption: AddTimeOption,
    onOptionSelected: (AddTimeOption) -> Unit,
    onAddTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(horizontal = 30.dp)
            .padding(top = 10.dp, bottom = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 74.dp, height = 5.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFD6D6D6))
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFE7EEFF))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$timeLeftText left",
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Add more time to continue",
            color = Color.Black,
            fontSize = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AddTimeOptions.forEach { option ->
                AddTimeOptionRow(
                    option = option,
                    isSelected = selectedOption == option,
                    onClick = { onOptionSelected(option) }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 306.dp, height = 54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFFCA44))
                .border(1.5.dp, Color.Black, RoundedCornerShape(14.dp))
                .clickable(onClick = onAddTime)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Add Rs99 • 50 coins",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.width(34.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF8B8B8B),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Secure payments  •  10 coins / min",
                color = Color(0xFF8B8B8B),
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CallChatBottomSheet(
    messages: List<RoomMessageResponse>,
    isLoading: Boolean,
    isSending: Boolean,
    errorMessage: String?,
    currentUserId: String,
    onLoadMessages: () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draftMessage by remember { mutableStateOf("") }
    val messageScrollState = rememberScrollState()
    val consumeClicks = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        onLoadMessages()
    }

    LaunchedEffect(messages.size) {
        messageScrollState.animateScrollTo(messageScrollState.maxValue)
    }

    Column(
        modifier = modifier
            .imePadding()
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .clickable(
                interactionSource = consumeClicks,
                indication = null,
                onClick = {}
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .size(width = 60.dp, height = 5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFD6D6D6))
            )
            Text(
                text = "Chat",
                color = Color.Black,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp, top = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFFFF7E5))
                .verticalScroll(messageScrollState)
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            when {
                isLoading -> {
                    CallChatStatusText(text = "Loading messages...")
                }
                messages.isEmpty() -> {
                    CallChatStatusText(text = errorMessage ?: "No messages yet")
                }
                else -> {
                    messages.forEach { message ->
                        CallChatBubble(
                            message = message,
                            isMine = message.senderUserId == currentUserId
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    if (!errorMessage.isNullOrBlank()) {
                        CallChatStatusText(text = errorMessage)
                    }
                }
            }
        }

        CallChatInputBar(
            value = draftMessage,
            onValueChange = { draftMessage = it },
            isSending = isSending,
            onSend = {
                val text = draftMessage.trim()
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    draftMessage = ""
                }
            }
        )
    }
}

@Composable
private fun CallChatStatusText(text: String) {
    Text(
        text = text,
        color = Color(0xFF8B8B8B),
        fontSize = 13.sp,
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    )
}

@Composable
private fun CallChatBubble(
    message: RoomMessageResponse,
    isMine: Boolean
) {
    Column(
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        val bubbleShape = ChatBubbleShape
        Box(
            modifier = Modifier
                .width(if (isMine) 250.dp else 164.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 2.dp, y = 3.dp)
                    .clip(bubbleShape)
                    .background(Color.Black)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bubbleShape)
                    .background(if (isMine) Color.White else Color(0xFFFFC137))
                    .border(1.2.dp, Color.Black, bubbleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.body.orEmpty(),
                    color = Color.Black,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message.createdAt.toCallChatTime(),
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(
                end = if (isMine) 6.dp else 0.dp,
                start = if (isMine) 0.dp else 6.dp
            )
        )
    }
}

@Composable
private fun CallChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 3.dp, y = 3.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.2.dp, Color.Black, RoundedCornerShape(24.dp))
                    .padding(start = 12.dp, end = 10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chat_bottom_sheet_emoji), // Replace with your drawable
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        cursorBrush = SolidColor(Color.Black),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (value.isBlank()) {
                        Text(
                            text = "Type your message...",
                            color = Color(0xFFB6B6B6),
                            fontSize = 13.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = Color(0xFF7E7E7E),
                    modifier = Modifier.size(18.dp)
                )
            }
            QuestionSparkle(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 38.dp, y = (-8).dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFC137))
                .border(1.2.dp, Color.Black, CircleShape)
                .clickable(enabled = !isSending, onClick = onSend)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.chat_bottom_sheet_sent), // Replace with your drawable ,
                contentDescription = "Send",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AddTimeOptionRow(
    option: AddTimeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFCF5))
            .border(0.7.dp, Color(0xFFF1EEE6), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF2FBF64) else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color(0xFF2FBF64) else Color(0xFFBFC4CA),
                    shape = CircleShape
                )
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "+${option.minutes.toString().padStart(2, '0')} min",
            color = Color.Black,
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "❤",
            color = Color(0xFFFF477A),
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${option.hearts} Hearts",
            color = Color.Black,
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun GameBottomSheet(
    gameCatalogUiState: GameCatalogUiState,
    modifier: Modifier = Modifier
) {
    val games = gameCatalogUiState.games.toGameCatalogItems()
    val sheetHeight = if (games.size > 3) 430.dp else 330.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(horizontal = 30.dp)
            .padding(top = 12.dp, bottom = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 74.dp, height = 5.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFD6D6D6))
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Choose a game",
            color = Color.Black,
            fontSize = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Add some fun to your call",
            color = Color(0xFF7B7B7B),
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(34.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(26.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            games.take(3).forEach { game ->
                GameOptionCard(
                    iconRes = game.iconRes,
                    title = game.title,
                    comingSoon = !game.isAvailable,
                    modifier = Modifier.weight(1f)
                )
            }
            repeat(3 - games.take(3).size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        if (games.size > 3) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(26.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                games.drop(3).take(3).forEach { game ->
                    GameOptionCard(
                        iconRes = game.iconRes,
                        title = game.title,
                        comingSoon = !game.isAvailable,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - games.drop(3).take(3).size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GameOptionCard(
    iconRes: Int,
    title: String,
    comingSoon: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .alpha(if (comingSoon) 0.5f else 1f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 92.dp, height = 108.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 3.dp, y = 3.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFF7B7B7B))
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White)
                    .border(1.2.dp, Color.Black, RoundedCornerShape(9.dp))
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 14.dp)
                        .size(58.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = title,
                    color = if (comingSoon) Color(0xFF777777) else Color.Black,
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 9.dp)
                )
            }
        }
        if (comingSoon) {
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = "Coming soon",
                color = Color(0xFFFF2A1E),
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GiftBottomSheet(
    giftCatalogUiState: GiftCatalogUiState,
    isSendingGift: Boolean,
    onDismiss: () -> Unit,
    onSendGift: (GiftItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var sheetDragOffset by remember { mutableStateOf(0f) }
    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer { translationY = sheetDragOffset }
            .fillMaxWidth()
    ) {
        val sheetWidth = maxWidth
        val isTablet = sheetWidth >= 600.dp
        val catalogContent = giftCatalogUiState.catalog.toGiftCatalogUiContent()
        val giftQuantities = remember { mutableStateMapOf<String, Int>() }
        val allGiftItems = catalogContent.allItems
        val spentHearts = allGiftItems.sumOf { item ->
            item.price * (giftQuantities[item.code] ?: 0)
        }
        val heartsRemaining = (catalogContent.heartBalance - spentHearts).coerceAtLeast(0)
        val selectedGifts = allGiftItems
            .mapNotNull { item ->
                val quantity = giftQuantities[item.code] ?: 0
                if (quantity > 0) SelectedGiftItem(item, quantity) else null
            }

        fun incrementGift(item: GiftItem) {
            if (heartsRemaining >= item.price) {
                giftQuantities[item.code] = (giftQuantities[item.code] ?: 0) + 1
            }
        }

        fun decrementGift(item: GiftItem) {
            val currentQuantity = giftQuantities[item.code] ?: 0
            when {
                currentQuantity > 1 -> giftQuantities[item.code] = currentQuantity - 1
                currentQuantity == 1 -> giftQuantities.remove(item.code)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (isTablet) 0.88f else 0.80f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
            GiftTopPanel(
                featuredItems = catalogContent.featuredItems,
                quantities = giftQuantities,
                onIncrement = ::incrementGift,
                onDecrement = ::decrementGift,
                isTablet = isTablet,
                dragModifier = Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            sheetDragOffset = (sheetDragOffset + dragAmount.y).coerceAtLeast(0f)
                        },
                        onDragEnd = {
                            if (sheetDragOffset > 120f) {
                                onDismiss()
                            } else {
                                sheetDragOffset = 0f
                            }
                        },
                        onDragCancel = {
                            sheetDragOffset = 0f
                        }
                    )
                }
            )
            GiftHeartsDivider(
                hearts = heartsRemaining,
                horizontalPadding = if (isTablet) 36.dp else 28.dp,
                topPadding = if (isTablet) 28.dp else 24.dp,
                bottomPadding = if (isTablet) 28.dp else 24.dp
            )
            catalogContent.sections.forEachIndexed { index, section ->
                GiftSectionTitle(
                    icon = section.icon,
                    text = section.title,
                    horizontalPadding = if (isTablet) 24.dp else 18.dp
                )
                Spacer(modifier = Modifier.height(if (isTablet) 14.dp else 12.dp))
                    if (section.layout == GiftSectionLayout.LoveRow) {
                        GiftLoveRow(
                            items = section.items,
                            quantities = giftQuantities,
                            onIncrement = ::incrementGift,
                            onDecrement = ::decrementGift,
                            containerWidth = sheetWidth,
                            isTablet = isTablet
                        )
                    } else {
                        GiftSmallGrid(
                            items = section.items,
                            quantities = giftQuantities,
                            onIncrement = ::incrementGift,
                            onDecrement = ::decrementGift,
                            containerWidth = sheetWidth,
                            isTablet = isTablet
                        )
                    }
                Spacer(modifier = Modifier.height(if (index == catalogContent.sections.lastIndex) 24.dp else if (isTablet) 24.dp else 22.dp))
            }
            Spacer(modifier = Modifier.height(if (selectedGifts.isEmpty()) 30.dp else if (isTablet) 126.dp else 116.dp))
            }

            if (selectedGifts.isNotEmpty()) {
                GiftSelectionSendBar(
                    selectedGifts = selectedGifts,
                    totalHearts = spentHearts,
                    enabled = !isSendingGift,
                    isTablet = isTablet,
                    onSend = { onSendGift(selectedGifts.first().gift) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun GiftSelectionSendBar(
    selectedGifts: List<SelectedGiftItem>,
    totalHearts: Int,
    enabled: Boolean,
    isTablet: Boolean,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(if (isTablet) 86.dp else 78.dp)
            .shadow(12.dp, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp), clip = false)
            .background(Color.White)
            .padding(horizontal = if (isTablet) 24.dp else 30.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            selectedGifts.take(3).forEach { selectedGift ->
                SelectedGiftPreview(selectedGift = selectedGift)
            }
            val hiddenCount = selectedGifts.size - 3
            if (hiddenCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF4F4F4))
                        .border(1.dp, Color(0xFFD0D0D0), CircleShape)
                ) {
                    Text(
                        text = "+$hiddenCount",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .height(44.dp)
                .width(1.dp)
                .background(Color(0xFFE3E3E3))
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = "❤", color = Color(0xFFFF477A), fontSize = 27.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = totalHearts.toString(),
            color = Color.Black,
            fontSize = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(20.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (enabled) Color(0xFFFFB51F) else Color(0xFFE8CFA0))
                .border(1.4.dp, Color.Black, CircleShape)
                .clickable(enabled = enabled, onClick = onSend)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

private fun resolveGiftRecipientUserId(
    room: com.gobff.getfriends.data.model.RoomResponse?,
    currentUserId: String
): String? {
    val normalizedCurrentUserId = currentUserId.trim()
    val roomRecipient = room?.participants.orEmpty()
        .mapNotNull { it.userId?.trim() }
        .firstOrNull { it.isNotBlank() && it != normalizedCurrentUserId }

    return roomRecipient
        ?: room?.invitedUserId?.trim()?.takeIf { it.isNotBlank() && it != normalizedCurrentUserId }
        ?: room?.createdByUserId?.trim()?.takeIf { it.isNotBlank() && it != normalizedCurrentUserId }
}

@Composable
private fun SelectedGiftPreview(selectedGift: SelectedGiftItem) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFFBEC))
            .border(1.3.dp, Color(0xFFFFB51F), CircleShape)
    ) {
        Image(
            painter = painterResource(id = selectedGift.gift.imageRes),
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            contentScale = ContentScale.Fit
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(19.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFB51F))
                .border(1.dp, Color.Black, CircleShape)
        ) {
            Text(
                text = selectedGift.quantity.toString(),
                color = Color.Black,
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GiftDeliveryOverlay(
    gift: GiftItem,
    phase: GiftDeliveryPhase?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (phase == GiftDeliveryPhase.Delivering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x8C000000))
            )
            Image(
                painter = painterResource(id = gift.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-150).dp)
                    .size(width = 112.dp, height = 112.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "Delivering your ${gift.deliveryLabel}...",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-124).dp)
            )
        } else if (phase == GiftDeliveryPhase.Arrived) {
//            Box(
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .offset(x = (-109).dp, y = (-63).dp)
//                    .size(width = 138.dp, height = 138.dp)
//                    .clip(RoundedCornerShape(14.dp))
//                    .border(3.dp, Color.White, RoundedCornerShape(14.dp))
//            )
            Image(
                painter = painterResource(id = gift.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-42).dp, y = (-18).dp)
                    .size(width = 72.dp, height = 72.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun GiftTopPanel(
    featuredItems: List<GiftItem>,
    quantities: Map<String, Int>,
    onIncrement: (GiftItem) -> Unit,
    onDecrement: (GiftItem) -> Unit,
    isTablet: Boolean,
    dragModifier: Modifier = Modifier
) {
    val firstGift = featuredItems.getOrNull(0) ?: KissGiftItem
    val secondGift = featuredItems.getOrNull(1) ?: HugGiftItem
    val heroRowHorizontalPadding = if (isTablet) 24.dp else 16.dp
    val heroRowSpacing = if (isTablet) 12.dp else 8.dp
    val heroCardWidth = (((if (isTablet) 640.dp else 361.dp) - (heroRowHorizontalPadding * 2) - heroRowSpacing) / 2)
        .coerceAtMost(if (isTablet) 240.dp else 172.dp)
    val heroCardHeight = if (isTablet) 148.dp else 117.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isTablet) 306.dp else 272.dp)
            .then(dragModifier)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF67259E), Color(0xFF57188B))
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.gift_bs_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .size(width = 58.dp, height = 5.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.42f))
        )
        Image(
            painter = painterResource(id = R.drawable.gift_bs_heading),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (isTablet) 48.dp else 42.dp)
                .fillMaxWidth()
                .height(if (isTablet) 76.dp else 66.dp),
            contentScale = ContentScale.FillWidth
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(heroRowSpacing),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (isTablet) 142.dp else 128.dp, start = heroRowHorizontalPadding, end = heroRowHorizontalPadding)
                .width(heroCardWidth * 2 + heroRowSpacing)
                .height(heroCardHeight)
        ) {
            GiftHeroCard(
                item = firstGift,
                title = firstGift.name,
                subtitle = firstGift.subtitle.orEmpty(),
                background = Brush.linearGradient(
                    colors = listOf(Color(0xFFF5B0D1), Color(0xFFF395C3))
                ),
                titleColor = Color(0xFFE82977),
                quantity = quantities[firstGift.code] ?: 0,
                onIncrement = { onIncrement(firstGift) },
                onDecrement = { onDecrement(firstGift) },
                isTablet = isTablet,
                modifier = Modifier.weight(1f)
            )
            GiftHeroCard(
                item = secondGift,
                title = secondGift.name,
                subtitle = secondGift.subtitle.orEmpty(),
                background = Brush.linearGradient(
                    colors = listOf(Color(0xFFFDD293), Color(0xFFF6B872))
                ),
                titleColor = Color(0xFFFF5B00),
                quantity = quantities[secondGift.code] ?: 0,
                onIncrement = { onIncrement(secondGift) },
                onDecrement = { onDecrement(secondGift) },
                isTablet = isTablet,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GiftHeroCard(
    item: GiftItem,
    title: String,
    subtitle: String,
    background: Brush,
    titleColor: Color,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(onClick = onIncrement)
    ) {
        Column(
            modifier = Modifier
                .padding(start = if (isTablet) 12.dp else 10.dp, top = if (isTablet) 14.dp else 12.dp, bottom = if (isTablet) 14.dp else 12.dp)
                .width(if (isTablet) 96.dp else 82.dp)
        ) {
            Text(
                text = title,
                color = titleColor,
                fontSize = if (isTablet) 26.sp else 24.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = Color(0xFF7D4A59),
                fontSize = if (isTablet) 9.sp else 8.sp,
                lineHeight = if (isTablet) 12.sp else 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(if (isTablet) 14.dp else 12.dp))
            GiftPricePill(price = item.price)
        }
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(width = if (isTablet) 102.dp else 92.dp, height = if (isTablet) 138.dp else 127.dp),
            contentScale = ContentScale.Fit
        )
        if (quantity > 0) {
            GiftQuantityStepper(
                quantity = quantity,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            )
        }
    }
}

@Composable
private fun GiftHeartsDivider(
    hearts: Int,
    horizontalPadding: Dp = 28.dp,
    topPadding: Dp = 24.dp,
    bottomPadding: Dp = 24.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .padding(top = topPadding, bottom = bottomPadding)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xFF8D35C3))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = "❤", color = Color(0xFFFF477A), fontSize = 18.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$hearts Hearts Available",
            color = Color.Black,
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xFF8D35C3))
        )
    }
}

@Composable
private fun GiftSectionTitle(icon: String, text: String, horizontalPadding: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = horizontalPadding)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.Black,
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GiftSmallGrid(
    items: List<GiftItem>,
    quantities: Map<String, Int>,
    onIncrement: (GiftItem) -> Unit,
    onDecrement: (GiftItem) -> Unit,
    containerWidth: Dp,
    isTablet: Boolean
) {
    val horizontalPadding = if (isTablet) 20.dp else 16.dp
    val columnSpacing = if (isTablet) 12.dp else 10.dp
    val rowSpacing = if (isTablet) 12.dp else 10.dp
    val cardWidth = (((containerWidth - (horizontalPadding * 2) - (columnSpacing * 2)) / 3))
        .coerceAtMost(if (isTablet) 144.dp else 110.dp)
    val cardHeight = if (isTablet) 126.dp else 118.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(rowSpacing),
        modifier = Modifier.padding(horizontal = horizontalPadding)
    ) {
        items.chunked(3).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(columnSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { item ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        GiftProductCard(
                            item = item,
                            quantity = quantities[item.code] ?: 0,
                            onIncrement = { onIncrement(item) },
                            onDecrement = { onDecrement(item) },
                            isTablet = isTablet,
                            modifier = Modifier.size(width = cardWidth, height = cardHeight)
                        )
                    }
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GiftLoveRow(
    items: List<GiftItem>,
    quantities: Map<String, Int>,
    onIncrement: (GiftItem) -> Unit,
    onDecrement: (GiftItem) -> Unit,
    containerWidth: Dp,
    isTablet: Boolean
) {
    val horizontalPadding = if (isTablet) 20.dp else 16.dp
    val spacing = if (isTablet) 12.dp else 10.dp
    val cardWidth = (((containerWidth - (horizontalPadding * 2) - spacing) / 2))
        .coerceAtMost(if (isTablet) 270.dp else 180.dp)
    val cardHeight = if (isTablet) 108.dp else 92.dp
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    ) {
        items.forEach { item ->
            GiftLoveCard(
                item = item,
                quantity = quantities[item.code] ?: 0,
                onIncrement = { onIncrement(item) },
                onDecrement = { onDecrement(item) },
                isTablet = isTablet,
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight)
            )
        }
    }
}

@Composable
private fun GiftProductCard(
    item: GiftItem,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (quantity > 0) Color(0xFFF5B120) else Color.Black
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(if (quantity > 0) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onIncrement)
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = if (isTablet) 54.dp else 50.dp, height = if (isTablet) 60.dp else 56.dp),
            contentScale = ContentScale.Fit
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Text(
                text = item.name,
                color = Color.Black,
                fontSize = if (isTablet) 12.sp else 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(5.dp))
            GiftPriceLine(item = item)
        }
        if (quantity > 0) {
            GiftQuantityStepper(
                quantity = quantity,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun GiftLoveCard(
    item: GiftItem,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (quantity > 0) Color(0xFFF5B120) else Color.Black
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (item.name.contains("Red")) Color(0xFFFFE8E8) else Color(0xFFFFF9DD))
            .border(if (quantity > 0) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onIncrement)
            .padding(10.dp)
    ) {
        Text(
            text = item.name,
            color = Color.Black,
            fontSize = if (isTablet) 10.sp else 9.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = if (isTablet) 84.dp else 76.dp, height = if (isTablet) 82.dp else 74.dp),
            contentScale = ContentScale.Fit
        )
        if (quantity > 0) {
            GiftQuantityStepper(
                quantity = quantity,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
        GiftPriceLine(
            item = item,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun GiftQuantityStepper(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .size(width = 56.dp, height = 24.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFC42E))
            .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
    ) {
        Text(
            text = "−",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .clickable(onClick = onDecrement)
        )
        Text(
            text = quantity.toString(),
            color = Color.Black,
            fontSize = 12.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "+",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .clickable(onClick = onIncrement)
        )
    }
}

@Composable
private fun GiftPricePill(price: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(54.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        Text(text = "❤", color = Color(0xFFFF477A), fontSize = 12.sp)
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = price.toString(),
            color = Color.Black,
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GiftPriceLine(
    item: GiftItem,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(text = "❤", color = Color(0xFFFF477A), fontSize = 12.sp)
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = item.price.toString(),
            color = Color.Black,
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        item.oldPrice?.let { oldPrice ->
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = oldPrice.toString(),
                color = Color(0xFF8E8E8E),
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.LineThrough
            )
        }
    }
}

@Composable
private fun CallFeedbackPopup(
    personName: String,
    callEndedMessage: String?,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmitFeedback: (
        rating: Int,
        tags: List<String>,
        comment: String?,
        addFriend: Boolean,
        onSubmitted: () -> Unit
    ) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRating by remember { mutableStateOf<FeedbackRating?>(null) }
    var selectedOptions by remember { mutableStateOf(setOf<String>()) }
    var comment by remember { mutableStateOf("") }
    var addToFriends by remember { mutableStateOf(false) }
    var submittedRating by remember { mutableStateOf<FeedbackRating?>(null) }
    val callEndedMessageHeight = if (callEndedMessage.isNullOrBlank()) 0.dp else 30.dp
    val cardHeight = when {
        submittedRating != null -> 214.dp
        selectedRating == null -> 300.dp
        selectedRating?.isPositive == true -> 506.dp
        else -> 454.dp
    } + callEndedMessageHeight

    Box(
        modifier = modifier
            .width(342.dp)
            .height(cardHeight)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 6.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.feedback_popup_objects),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            submittedRating?.let { rating ->
                FeedbackThankYouContent(
                    rating = rating,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onDismiss)
                )
                return@Box
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                if (!callEndedMessage.isNullOrBlank()) {
                    Text(
                        text = callEndedMessage,
                        color = Color(0xFFE25252),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = "Did you enjoy\ntalking with $personName?",
                    color = Color.Black,
                    fontSize = 22.sp,
                    lineHeight = 31.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(if (selectedRating == null) 38.dp else 24.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeedbackRatings.forEach { rating ->
                        FeedbackEmojiButton(
                            rating = rating,
                            isSelected = selectedRating == rating,
                            onClick = {
                                selectedRating = rating
                                selectedOptions = emptySet()
                                comment = ""
                                addToFriends = false
                            }
                        )
                    }
                }

                if (selectedRating == null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Tap any emoji to rate",
                        color = Color(0xFFC7C7C7),
                        fontSize = 16.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    FeedbackSubmitButton(
                        text = "Maybe later",
                        onClick = onDismiss
                    )
                } else {
                    val rating = selectedRating ?: return@Column
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        text = rating.prompt,
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeedbackOptionGrid(
                        options = rating.options,
                        selectedOptions = selectedOptions,
                        onOptionClick = { option ->
                            selectedOptions = if (option in selectedOptions) {
                                selectedOptions - option
                            } else {
                                selectedOptions + option
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FeedbackCommentBox(
                        value = comment,
                        onValueChange = { comment = it }
                    )
                    if (rating.isPositive) {
                        Spacer(modifier = Modifier.height(14.dp))
                        FeedbackAddFriendCard(
                            checked = addToFriends,
                            onToggle = { addToFriends = !addToFriends }
                        )
                    }
                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFE53935),
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    FeedbackSubmitButton(
                        text = if (isSubmitting) "Submitting..." else rating.submitText,
                        enabled = !isSubmitting,
                        onClick = {
                            onSubmitFeedback(
                                rating.rating,
                                selectedOptions.toList(),
                                comment.trim().takeIf { it.isNotBlank() },
                                addToFriends
                            ) {
                                submittedRating = rating
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackCommentBox(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color(0xFF1F2430),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            ),
            singleLine = false,
            cursorBrush = SolidColor(Color(0xFF7D2DE2)),
            modifier = Modifier.fillMaxSize()
        )
        if (value.isBlank()) {
            Text(
                text = "Add a comment",
                color = Color(0xFFB4B4B4),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FeedbackEmojiButton(
    rating: FeedbackRating,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(if (isSelected) 56.dp else 44.dp)
            .shadow(if (isSelected) 6.dp else 0.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(if (isSelected) Color.White else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = rating.iconRes),
            contentDescription = rating.id,
            modifier = Modifier.size(if (isSelected) 48.dp else 44.dp),
            contentScale = ContentScale.Fit
        )
        if (isSelected) {
            Image(
                painter = painterResource(id = R.drawable.call_feedback_sign),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-4).dp)
                    .size(18.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun FeedbackOptionGrid(
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        options.chunked(2).forEach { rowOptions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowOptions.forEach { option ->
                    FeedbackOptionChip(
                        text = option,
                        isSelected = option in selectedOptions,
                        onClick = { onOptionClick(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackOptionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) Color(0xFFE9D7FF) else Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = if (isSelected) "✓" else "+",
            color = if (isSelected) Color(0xFF7D2DE2) else Color(0xFF1F2430),
            fontSize = 15.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = if (isSelected) Color(0xFF7D2DE2) else Color(0xFF1F2430),
            fontSize = 12.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FeedbackAddFriendCard(
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEE5F6))
            .clickable(onClick = onToggle)
            .padding(horizontal = 10.dp)
    ) {
        Text(text = "💗", fontSize = 17.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Add to Friends list",
            color = Color(0xFF5A5361),
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        FeedbackToggle(checked = checked)
    }
}

@Composable
private fun FeedbackToggle(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (checked) Color(0xFF7D2DE2) else Color(0xFFD7D7D7))
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun FeedbackSubmitButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .width(252.dp)
            .height(48.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(if (enabled) Color(0xFFFFC137) else Color(0xFFD7D7D7))
                .clickable(enabled = enabled, onClick = onClick)
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FeedbackThankYouContent(
    rating: FeedbackRating,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 30.dp)
    ) {
        Image(
            painter = painterResource(
                id = if (rating.isPositive) {
                    R.drawable.call_feedback_positive
                } else {
                    R.drawable.call_feedback_negative
                }
            ),
            contentDescription = null,
            modifier = Modifier.size(66.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = if (rating.isPositive) "Thank you!" else "Thank you for\nyour feedback",
            color = Color.Black,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (rating.isPositive) {
                "Your feedback helps us\nimprove and connect better"
            } else {
                "We'll review your feedback to\nhelp improve future conversations."
            },
            color = Color(0xFF8E8E8E),
            fontSize = 15.sp,
            lineHeight = 21.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

private data class CallSafetyItem(
    val title: String,
    val body: String,
    val iconRes: Int,
    val iconBackground: Color
)

private val CallSafetyItems = listOf(
    CallSafetyItem(
        title = "Keep your info private",
        body = "Never share personal details like your address, phone number, email, or financial information.",
        iconRes = R.drawable.call_screen_lock,
        iconBackground = Color(0xFFF1EDFC)
    ),
    CallSafetyItem(
        title = "Be respectful",
        body = "Treat others with kindness and respect. Harassment or abusive behavior is not allowed.",
        iconRes = R.drawable.call_screen_account,
        iconBackground = Color(0xFFD0EEFE)
    ),
    CallSafetyItem(
        title = "Report & block",
        body = "If someone makes you uncomfortable, you can report or block them anytime.",
        iconRes = R.drawable.call_screen_report,
        iconBackground = Color(0xFFFEC5C0)
    ),
    CallSafetyItem(
        title = "Stay within the app",
        body = "For your safety, keep conversations and payments within the app.",
        iconRes = R.drawable.call_screen_money,
        iconBackground = Color(0xFFFDECC2)
    ),
    CallSafetyItem(
        title = "We're here to help",
        body = "Our team is available 24/7. Reach out if you need any assistance.",
        iconRes = R.drawable.call_screen_support,
        iconBackground = Color(0xFFFCE2EC)
    )
)

@Composable
private fun CallSafetyBottomSheet(
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(625.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(103.dp)
                .background(Color(0x1A60E3AB))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .size(width = 82.dp, height = 6.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFD6D6D6))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(345.dp).padding(top = 18.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.call_screen_shield),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
                Column {
                    Text(
                        text = "Your Safety is Our Priority",
                        color = Color(0xFF242436),
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "We're here to help you feel safe and secure.",
                        color = Color(0xFF242436),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            CallSafetyItems.forEach { item ->
                CallSafetyInfoRow(item = item)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        CallSafetyReportButton(
            onClick = onReportClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(38.dp))
    }
}

@Composable
private fun CallSafetyInfoRow(
    item: CallSafetyItem,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .width(345.dp)
            .height(65.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.iconBackground)
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                color = Color(0xFF242436),
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.body,
                color = Color(0xFF242436),
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CallSafetyReportButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .width(345.dp)
            .height(58.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color(0xFFFF6252))
                .clickable(onClick = onClick)
        ) {
            Text(
                text = "Report",
                color = Color.Black,
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CallSafetyButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF55E6A9))
                .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.call_screen_verified_user),
                contentDescription = null,
                modifier = Modifier.size(width = 24.dp, height = 24.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun CallParticipantCard(
    name: String,
    avatarUrl: String? = null,
    gender: String? = null,
    avatarRes: Int,
    background: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(132.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(background)
                .border(
                    1.4.dp,
                    Color.Black,
                    RoundedCornerShape(12.dp)
                )
        ) {
            CachedAvatarImage(
                avatarUrl = avatarUrl,
                gender = gender,
                fallbackRes = gender.toCallFallbackAvatarRes(avatarRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = name,
            color = Color.Black,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun String?.toCallFallbackAvatarRes(defaultRes: Int): Int {
    return when (this.toAvatarGender()) {
        AvatarGender.Female -> R.drawable.women_avatar1
        AvatarGender.Male -> R.drawable.man_avatar1
        else -> defaultRes
    }
}

@Composable
private fun AudioWaveform(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.width(48.dp)
    ) {
        listOf(18, 28, 42, 32, 22, 36, 26, 18).forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun CallActionBubble(
    iconRes: Int,
    onClick: (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .shadow(7.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.24f))
            .border(0.32.dp, Color.White, CircleShape)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(7.dp)
                .offset(x = (-3).dp, y = (-3).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.24f))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(8.dp)
                .offset(x = 4.dp, y = 4.dp)
                .clip(CircleShape)
                .background(Color(0x332A1700))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(9.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun VideoQuickActions(
    onChatClick: () -> Unit,
    onGiftClick: () -> Unit,
    onGameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        CallActionBubble(iconRes = R.drawable.call_screen_chats, onClick = onChatClick)
        CallActionBubble(iconRes = R.drawable.call_screen_gift, onClick = onGiftClick)
        CallActionBubble(iconRes = R.drawable.call_screen_games, onClick = onGameClick)
    }
}

@Composable
private fun CallControls(
    isMuted: Boolean,
    isSpeakerEnabled: Boolean,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoClick: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(2.dp, Color.Black, RoundedCornerShape(18.dp))
    ) {
        CallControlIcon(
            iconRes = R.drawable.call_screen_mic,
            isSelected = isMuted,
            onClick = onMuteToggle
        )
        CallControlIcon(
            iconRes = R.drawable.call_screen_speaker,
            isSelected = isSpeakerEnabled,
            onClick = onSpeakerToggle
        )
        CallControlIcon(
            iconRes = R.drawable.call_screen_camera,
            onClick = onVideoClick
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 88.dp, height = 44.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFFF64A4A))
                .clickable(onClick = onEndCall)
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun VideoCallControls(
    isMuted: Boolean,
    isSpeakerEnabled: Boolean,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(2.dp, Color.Black, RoundedCornerShape(18.dp))
    ) {
        CallControlIcon(
            iconRes = R.drawable.call_screen_mic,
            isSelected = isMuted,
            onClick = onMuteToggle
        )
        CallControlIcon(
            iconRes = R.drawable.call_screen_speaker,
            isSelected = isSpeakerEnabled,
            onClick = onSpeakerToggle
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE4E4))
                .clickable(onClick = onSwitchCamera)
        ) {
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 88.dp, height = 44.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFFF64A4A))
                .clickable(onClick = onEndCall)
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun CallControlIcon(
    iconRes: Int,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFFFFE4E4) else Color.Transparent)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .alpha(if (isSelected) 0.58f else 1f),
            contentScale = ContentScale.Fit
        )
    }
}

private data class AddTimeOption(
    val minutes: Int,
    val hearts: Int
)

private data class GiftItem(
    val code: String,
    val name: String,
    val imageRes: Int,
    val price: Int,
    val oldPrice: Int?,
    val subtitle: String? = null
)

private data class SelectedGiftItem(
    val gift: GiftItem,
    val quantity: Int
)

private data class GameCatalogItem(
    val code: String,
    val title: String,
    val iconRes: Int,
    val description: String?,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val isAvailable: Boolean
)

private val GiftItem.deliveryLabel: String
    get() = name.lowercase()

private enum class GiftDeliveryPhase {
    Delivering,
    Arrived
}

private data class GiftCatalogUiContent(
    val heartBalance: Int,
    val featuredItems: List<GiftItem>,
    val sections: List<GiftSection>
) {
    val allItems: List<GiftItem>
        get() = featuredItems + sections.flatMap { it.items }
}

private data class GiftSection(
    val code: String,
    val title: String,
    val icon: String,
    val layout: GiftSectionLayout,
    val items: List<GiftItem>
)

private enum class GiftSectionLayout {
    Grid,
    LoveRow
}

private data class FeedbackRating(
    val id: String,
    val rating: Int,
    val iconRes: Int,
    val prompt: String,
    val submitText: String,
    val options: List<String>,
    val isPositive: Boolean
)

private val AddTimeOptions = listOf(
    AddTimeOption(minutes = 5, hearts = 50),
    AddTimeOption(minutes = 15, hearts = 150),
    AddTimeOption(minutes = 30, hearts = 300)
)

private val DefaultGameCatalogItems = listOf(
    GameCatalogItem(
        code = "TRUTH_OR_DARE",
        title = "Truth/Dare",
        iconRes = R.drawable.game_truth_dare,
        description = null,
        minPlayers = 2,
        maxPlayers = 2,
        isAvailable = true
    ),
    GameCatalogItem(
        code = "TIC_TAC_TOE",
        title = "Tic Tac Toe",
        iconRes = R.drawable.game_screen_tictactoe,
        description = null,
        minPlayers = 2,
        maxPlayers = 2,
        isAvailable = true
    ),
    GameCatalogItem(
        code = "UNO",
        title = "Uno",
        iconRes = R.drawable.game_uno,
        description = null,
        minPlayers = null,
        maxPlayers = null,
        isAvailable = false
    ),
    GameCatalogItem(
        code = "LUDO",
        title = "Ludo",
        iconRes = R.drawable.game_ludo,
        description = null,
        minPlayers = null,
        maxPlayers = null,
        isAvailable = false
    )
)

private fun List<GameCatalogItemDto>.toGameCatalogItems(): List<GameCatalogItem> {
    if (isEmpty()) return DefaultGameCatalogItems

    return mapNotNull { it.toGameCatalogItem() }
        .ifEmpty { DefaultGameCatalogItems }
}

private fun GameCatalogItemDto.toGameCatalogItem(): GameCatalogItem? {
    val gameCode = code ?: return null
    val gameTitle = title ?: return null

    return GameCatalogItem(
        code = gameCode,
        title = gameTitle,
        iconRes = iconKey.toGameDrawableRes(gameCode),
        description = description,
        minPlayers = minPlayers,
        maxPlayers = maxPlayers,
        isAvailable = true
    )
}

private fun String?.toGameDrawableRes(gameCode: String): Int =
    when (this ?: gameCode) {
        "game_truth_dare", "TRUTH_OR_DARE" -> R.drawable.game_truth_dare
        "game_tic_tac_toe", "TIC_TAC_TOE" -> R.drawable.game_screen_tictactoe
        "game_uno", "UNO" -> R.drawable.game_uno
        "game_ludo", "LUDO" -> R.drawable.game_ludo
        else -> R.drawable.call_screen_games
    }

private val KissGiftItem = GiftItem(
    code = "KISS",
    name = "A kiss",
    imageRes = R.drawable.gift_kiss_person,
    price = 200,
    oldPrice = null,
    subtitle = "One little kiss won't hurt"
)
private val HugGiftItem = GiftItem(
    code = "HUG",
    name = "A Hug",
    imageRes = R.drawable.gift_hug_person,
    price = 100,
    oldPrice = null,
    subtitle = "I could really use one"
)

private val TreatGiftItems = listOf(
    GiftItem("GARAM_CHAI", "Garam chai", R.drawable.gift_chai, price = 20, oldPrice = 45),
    GiftItem("ICE_CREAM", "Ice cream", R.drawable.gift_icecream, price = 25, oldPrice = 64),
    GiftItem("MAGGIE", "Maggie", R.drawable.gift_maggie, price = 30, oldPrice = 55),
    GiftItem("MOMO", "Momo", R.drawable.gift_momo, price = 40, oldPrice = 75),
    GiftItem("COFFEE", "Coffee", R.drawable.gift_coffee, price = 50, oldPrice = 65),
    GiftItem("PIZZA", "Pizza", R.drawable.gift_pizza, price = 100, oldPrice = 180),
    GiftItem("BIRYANI", "Biriyani", R.drawable.gift_biryani, price = 100, oldPrice = 200)
)

private val LoveGiftItems = listOf(
    GiftItem("YELLOW_ROSE", "Yellow Rose", R.drawable.gift_yellow_rose, price = 5, oldPrice = 10),
    GiftItem("RED_ROSE", "Red Rose", R.drawable.gift_red_rose, price = 10, oldPrice = 20)
)

private val BuyGiftItems = listOf(
    GiftItem("TEDDY_BEAR", "Teddy bear", R.drawable.gift_teddy, price = 100, oldPrice = 250),
    GiftItem("PERFUME", "Perfume", R.drawable.gift_perfume, price = 200, oldPrice = 400),
    GiftItem("LIPSTICK", "Lipstick", R.drawable.gift_lipstick, price = 200, oldPrice = 350)
)

private val DefaultGiftCatalogContent = GiftCatalogUiContent(
    heartBalance = 150,
    featuredItems = listOf(KissGiftItem, HugGiftItem),
    sections = listOf(
        GiftSection(
            code = "TREAT_HER",
            title = "Treat Her to...",
            icon = "🛍️",
            layout = GiftSectionLayout.Grid,
            items = TreatGiftItems
        ),
        GiftSection(
            code = "SEND_LOVE",
            title = "Send some love",
            icon = "🌹",
            layout = GiftSectionLayout.LoveRow,
            items = LoveGiftItems
        ),
        GiftSection(
            code = "BUY_YOU",
            title = "Let me Buy you a...",
            icon = "🎁",
            layout = GiftSectionLayout.Grid,
            items = BuyGiftItems
        )
    )
)

private fun GiftCatalogResponse?.toGiftCatalogUiContent(): GiftCatalogUiContent {
    val categories = this?.categories.orEmpty()
    if (categories.isEmpty()) return DefaultGiftCatalogContent

    val featuredItems = categories
        .firstOrNull { it.code == "FEATURED" }
        ?.items
        .orEmpty()
        .mapNotNull { it.toGiftItem() }
        .ifEmpty { DefaultGiftCatalogContent.featuredItems }

    val sections = categories
        .filterNot { it.code == "FEATURED" }
        .mapNotNull { it.toGiftSection() }
        .ifEmpty { DefaultGiftCatalogContent.sections }

    return GiftCatalogUiContent(
        heartBalance = this?.heartBalance ?: DefaultGiftCatalogContent.heartBalance,
        featuredItems = featuredItems,
        sections = sections
    )
}

private fun GiftCategoryDto.toGiftSection(): GiftSection? {
    val categoryCode = code ?: return null
    val giftItems = items.orEmpty().mapNotNull { it.toGiftItem() }
    if (giftItems.isEmpty()) return null

    return GiftSection(
        code = categoryCode,
        title = title.orEmpty(),
        icon = iconKey.toGiftCategoryIcon(categoryCode),
        layout = if (categoryCode == "SEND_LOVE") GiftSectionLayout.LoveRow else GiftSectionLayout.Grid,
        items = giftItems
    )
}

private fun GiftItemDto.toGiftItem(): GiftItem? {
    val giftCode = code ?: return null
    val giftTitle = title ?: return null
    val price = heartPrice ?: return null

    return GiftItem(
        code = giftCode,
        name = giftTitle,
        imageRes = imageKey.toGiftDrawableRes(giftCode),
        price = price,
        oldPrice = originalHeartPrice,
        subtitle = subtitle
    )
}

private fun String?.toGiftCategoryIcon(categoryCode: String): String =
    when (this ?: categoryCode) {
        "gift_category_treat", "TREAT_HER" -> "🛍️"
        "gift_category_rose", "SEND_LOVE" -> "🌹"
        "gift_category_premium", "BUY_YOU" -> "🎁"
        else -> "🎁"
    }

private fun String?.toGiftDrawableRes(giftCode: String): Int =
    when (this ?: giftCode) {
        "gift_kiss", "KISS" -> R.drawable.gift_kiss_person
        "gift_hug", "HUG" -> R.drawable.gift_hug_person
        "gift_chai", "GARAM_CHAI" -> R.drawable.gift_chai
        "gift_ice_cream", "ICE_CREAM" -> R.drawable.gift_icecream
        "gift_maggie", "MAGGIE" -> R.drawable.gift_maggie
        "gift_momo", "MOMO" -> R.drawable.gift_momo
        "gift_coffee", "COFFEE" -> R.drawable.gift_coffee
        "gift_pizza", "PIZZA" -> R.drawable.gift_pizza
        "gift_biryani", "BIRYANI" -> R.drawable.gift_biryani
        "gift_yellow_rose", "YELLOW_ROSE" -> R.drawable.gift_yellow_rose
        "gift_red_rose", "RED_ROSE" -> R.drawable.gift_red_rose
        "gift_teddy", "TEDDY_BEAR" -> R.drawable.gift_teddy
        "gift_perfume", "PERFUME" -> R.drawable.gift_perfume
        "gift_lipstick", "LIPSTICK" -> R.drawable.gift_lipstick
        else -> R.drawable.call_screen_gift
    }

private val FeedbackRatings = listOf(
    FeedbackRating(
        id = "angry",
        rating = 1,
        iconRes = R.drawable.call_feedback_angry,
        prompt = "Oh no! What went wrong?",
        submitText = "Report & Submit",
        options = listOf(
            "Sexual talks",
            "Poor connection",
            "Rude behaviour",
            "Abusive language",
            "Fake profile",
            "Others"
        ),
        isPositive = false
    ),
    FeedbackRating(
        id = "sad",
        rating = 2,
        iconRes = R.drawable.call_feedback_sad,
        prompt = "Oh no! What went wrong?",
        submitText = "Report & Submit",
        options = listOf(
            "Not engaging",
            "Long wait",
            "Poor connection",
            "Didn't match",
            "Ended soon",
            "Others"
        ),
        isPositive = false
    ),
    FeedbackRating(
        id = "just_okay",
        rating = 3,
        iconRes = R.drawable.call_feedback_just_okay,
        prompt = "Tell us more !",
        submitText = "Done",
        options = listOf(
            "Average convo",
            "Not very engaging",
            "Poor connection",
            "Could be better",
            "Ended soon",
            "Others"
        ),
        isPositive = false
    ),
    FeedbackRating(
        id = "good",
        rating = 4,
        iconRes = R.drawable.call_feedback_good,
        prompt = "What did you like ?",
        submitText = "Done",
        options = listOf(
            "Cute voice",
            "Friendly",
            "Funny",
            "Good listener",
            "Others"
        ),
        isPositive = true
    ),
    FeedbackRating(
        id = "loved_it",
        rating = 5,
        iconRes = R.drawable.call_feedback_loved_it,
        prompt = "What stood out the most?",
        submitText = "Done",
        options = listOf(
            "Cute voice",
            "Great personality",
            "Friendly",
            "Would talk again",
            "Funny",
            "Others"
        ),
        isPositive = true
    )
)

private fun formatCallTime(secondsRemaining: Int): String {
    val safeSeconds = secondsRemaining.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private fun String?.toCallChatTime(): String {
    val value = this?.takeIf { it.isNotBlank() } ?: return ""
    val timePart = value.substringAfter("T", missingDelimiterValue = value)
        .substringBefore(".")
        .substringBefore("Z")
    val parts = timePart.split(":")
    if (parts.size < 2) return ""

    val hour24 = parts[0].toIntOrNull() ?: return ""
    val minute = parts[1].padStart(2, '0')
    val amPm = if (hour24 >= 12) "PM" else "AM"
    val hour12 = when (val hour = hour24 % 12) {
        0 -> 12
        else -> hour
    }
    return "$hour12:$minute $amPm"
}

@Composable
private fun QuestionSparkle(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val path = Path().apply {
            moveTo(center.x, 0f)
            quadraticTo(center.x + 3f, center.y - 3f, size.width, center.y)
            quadraticTo(center.x + 3f, center.y + 3f, center.x, size.height)
            quadraticTo(center.x - 3f, center.y + 3f, 0f, center.y)
            quadraticTo(center.x - 3f, center.y - 3f, center.x, 0f)
            close()
        }
        drawPath(path, color = Color(0xFFFFD33F))
        drawPath(path, color = Color(0xFF4B6EFF), style = Stroke(width = 1.5f))
    }
}

@Composable
private fun CallAvatar(
    avatarRes: Int,
    size: Int,
    borderWidth: Int
) {
    Image(
        painter = painterResource(id = avatarRes),
        contentDescription = null,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(borderWidth.dp, Color.White, CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 625)
//@Composable
//private fun CallScreenPreview() {
//    BffAndroidTheme {
//        CallScreen(
//            personName = "Anshu",
//            onBack = {}
//        )
//    }
//}
@Composable
private fun CallChatBottomSheetPreview() {
    CallChatBottomSheet(
        messages = listOf(
            RoomMessageResponse(
                messageId = "preview-1",
                roomId = "room",
                senderUserId = "other-user",
                senderDisplayName = "Anshu",
                senderAvatarUrl = "women_avatar1",
                body = "How that is spelled, can you type here ?",
                createdAt = "2026-07-09T06:27:40Z"
            ),
            RoomMessageResponse(
                messageId = "preview-2",
                roomId = "room",
                senderUserId = "me",
                senderDisplayName = "Badal",
                senderAvatarUrl = "man_avatar1",
                body = "It spelled \"Isabella\"",
                createdAt = "2026-07-09T06:28:40Z"
            )
        ),
        isLoading = false,
        isSending = false,
        errorMessage = null,
        currentUserId = "me",
        onLoadMessages = {},
        onSendMessage = {}
    )
}
