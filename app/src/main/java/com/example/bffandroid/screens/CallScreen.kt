package com.example.bffandroid.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.example.bffandroid.R
import com.example.bffandroid.data.model.GameCatalogItemDto
import com.example.bffandroid.data.model.GiftCatalogResponse
import com.example.bffandroid.data.model.GiftCategoryDto
import com.example.bffandroid.data.model.GiftItemDto
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.ui.theme.GaretFontFamily
import com.example.bffandroid.viewmodel.CallViewModel
import com.example.bffandroid.viewmodel.GameCatalogUiState
import com.example.bffandroid.viewmodel.GameCatalogViewModel
import com.example.bffandroid.viewmodel.GiftCatalogUiState
import com.example.bffandroid.viewmodel.GiftCatalogViewModel
import kotlinx.coroutines.delay

private val CallYellow = Color(0xFFF5B120)

@Composable
fun CallScreen(
    personName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
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
    var showFeedbackPopup by remember { mutableStateOf(false) }
    var selectedAddTimeOption by remember { mutableStateOf(AddTimeOptions.first()) }
    var sendingGift by remember { mutableStateOf<GiftItem?>(null) }
    var giftDeliveryPhase by remember { mutableStateOf<GiftDeliveryPhase?>(null) }
    val giftCatalogUiState = giftCatalogViewModel.uiState
    val gameCatalogUiState = gameCatalogViewModel.uiState
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    fun leaveAndClose() {
        callViewModel.leaveCall()
        onBack()
    }

    BackHandler {
        when {
            showGameSheet -> showGameSheet = false
            showGiftSheet -> showGiftSheet = false
            showAddTimeSheet -> showAddTimeSheet = false
            showFeedbackPopup -> showFeedbackPopup = false
            else -> leaveAndClose()
        }
    }

    LaunchedEffect(personName, hasAudioPermission) {
        if (hasAudioPermission) {
            callViewModel.createRandomOneToOneAudioCall(
                title = "$personName Audio Call"
            )
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            callViewModel.leaveCall()
        }
    }

    LaunchedEffect(personName, isConnected) {
        if (isConnected) return@LaunchedEffect
        for (value in 3 downTo 1) {
            countdown = value
            delay(1_000L)
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


        if (isConnected) {
            ActiveCallContent(
                personName = personName,
                callSecondsRemaining = callSecondsRemaining,
                onTimerClick = { showAddTimeSheet = true },
                onGiftClick = { showGiftSheet = true },
                onGameClick = { showGameSheet = true },
                isMuted = uiState.isMuted,
                isSpeakerEnabled = uiState.isSpeakerEnabled,
                onMuteToggle = { callViewModel.setMuted(!uiState.isMuted) },
                onSpeakerToggle = { callViewModel.setSpeakerEnabled(!uiState.isSpeakerEnabled) },
                onVideoClick = { callViewModel.requestVideoUpgrade() },
                onEndCall = {
                    callViewModel.leaveCall()
                    showFeedbackPopup = true
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ConnectingCallContent(
                personName = personName,
                countdown = countdown,
                statusText = when {
                    !hasAudioPermission -> "Microphone permission needed"
                    uiState.errorMessage != null -> uiState.errorMessage
                    uiState.isCreatingRoom -> "Creating room..."
                    uiState.isFetchingRtcToken -> "Fetching secure call token..."
                    uiState.isJoiningRtc -> "Joining audio..."
                    else -> "Connecting..."
                },
                onBack = ::leaveAndClose,
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

        if (showGiftSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable { showGiftSheet = false }
            )
            GiftBottomSheet(
                giftCatalogUiState = giftCatalogUiState,
                onDismiss = { showGiftSheet = false },
                onSendGift = { gift ->
                    showGiftSheet = false
                    sendingGift = gift
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
                onDismiss = onBack,
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
private fun ActiveCallContent(
    personName: String,
    callSecondsRemaining: Int,
    onTimerClick: () -> Unit,
    onGiftClick: () -> Unit,
    onGameClick: () -> Unit,
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
            CallSafetyButton()
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
                    avatarRes = R.drawable.women_avatar11,
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
                    name = "Nike",
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
            CallActionBubble(iconRes = R.drawable.call_screen_chats)
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
private fun CallTimerChip(
    secondsRemaining: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 38.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.5.dp, Color.Black, RoundedCornerShape(12.dp))
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
    onDismiss: () -> Unit,
    onSendGift: (GiftItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val catalogContent = giftCatalogUiState.catalog.toGiftCatalogUiContent()
    val giftQuantities = remember { mutableStateMapOf<String, Int>() }
    var sheetDragOffset by remember { mutableStateOf(0f) }
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
        modifier = modifier
            .graphicsLayer { translationY = sheetDragOffset }
            .fillMaxWidth()
            .fillMaxHeight(0.78f)
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
            GiftHeartsDivider(hearts = heartsRemaining)
            catalogContent.sections.forEachIndexed { index, section ->
                GiftSectionTitle(icon = section.icon, text = section.title)
                Spacer(modifier = Modifier.height(12.dp))
                if (section.layout == GiftSectionLayout.LoveRow) {
                    GiftLoveRow(
                        items = section.items,
                        quantities = giftQuantities,
                        onIncrement = ::incrementGift,
                        onDecrement = ::decrementGift
                    )
                } else {
                    GiftSmallGrid(
                        items = section.items,
                        quantities = giftQuantities,
                        onIncrement = ::incrementGift,
                        onDecrement = ::decrementGift
                    )
                }
                Spacer(modifier = Modifier.height(if (index == catalogContent.sections.lastIndex) 24.dp else 22.dp))
            }
            Spacer(modifier = Modifier.height(if (selectedGifts.isEmpty()) 30.dp else 116.dp))
        }

        if (selectedGifts.isNotEmpty()) {
            GiftSelectionSendBar(
                selectedGifts = selectedGifts,
                totalHearts = spentHearts,
                onSend = { onSendGift(selectedGifts.first().gift) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun GiftSelectionSendBar(
    selectedGifts: List<SelectedGiftItem>,
    totalHearts: Int,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .shadow(12.dp, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp), clip = false)
            .background(Color.White)
            .padding(horizontal = 30.dp)
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
                .background(Color(0xFFFFB51F))
                .border(1.4.dp, Color.Black, CircleShape)
                .clickable(onClick = onSend)
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
    dragModifier: Modifier = Modifier
) {
    val firstGift = featuredItems.getOrNull(0) ?: KissGiftItem
    val secondGift = featuredItems.getOrNull(1) ?: HugGiftItem
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(272.dp)
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
                .padding(top = 42.dp)
                .fillMaxWidth()
                .height(66.dp),
            contentScale = ContentScale.FillWidth
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 128.dp, start = 16.dp, end = 16.dp)
                .size(width = 361.dp, height = 117.dp)
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
                .padding(start = 10.dp, top = 12.dp, bottom = 12.dp)
                .width(82.dp)
        ) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 24.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = Color(0xFF7D4A59),
                fontSize = 8.sp,
                lineHeight = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(12.dp))
            GiftPricePill(price = item.price)
        }
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(width = 92.dp, height = 127.dp),
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
private fun GiftHeartsDivider(hearts: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .padding(top = 24.dp, bottom = 24.dp)
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
private fun GiftSectionTitle(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 18.dp)
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
    onDecrement: (GiftItem) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        items.chunked(3).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                            modifier = Modifier.size(width = 110.dp, height = 118.dp)
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
    onDecrement: (GiftItem) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items.forEach { item ->
            GiftLoveCard(
                item = item,
                quantity = quantities[item.code] ?: 0,
                onIncrement = { onIncrement(item) },
                onDecrement = { onDecrement(item) },
                modifier = Modifier
                    .weight(1f)
                    .height(92.dp)
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
                .size(width = 50.dp, height = 56.dp),
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
                fontSize = 11.sp,
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
            fontSize = 9.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 76.dp, height = 74.dp),
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
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRating by remember { mutableStateOf<FeedbackRating?>(null) }

    Box(
        modifier = modifier
            .width(356.dp)
            .height(428.dp)
            .padding(horizontal = 28.dp)

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 28.dp)
            ) {
                Text(
                    text = "Did you enjoy\ntalking with $personName?",
                    color = Color.Black,
                    fontSize = 22.sp,
                    lineHeight = 31.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(58.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeedbackRatings.forEach { rating ->
                        FeedbackEmojiButton(
                            rating = rating,
                            isSelected = selectedRating == rating,
                            onClick = { selectedRating = rating }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = selectedRating?.label ?: "Tap any emoji to rate",
                    color = selectedRating?.labelColor ?: Color(0xFFC7C7C7),
                    fontSize = 16.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 252.dp, height = 54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFC137))
                        .border(1.2.dp, Color.Black, RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                ) {
                    Text(
                        text = "Add to friend list",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Maybe later",
                    color = Color(0xFF7B7B7B),
                    fontSize = 16.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .border(0.7.dp, Color.Transparent)
                        .clickable(onClick = onDismiss)
                )
            }
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
            .size(50.dp)
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
        Text(
            text = rating.emoji,
            fontSize = if (isSelected) 39.sp else 34.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CallSafetyButton() {
    Box(modifier = Modifier.size(36.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 4.dp)
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
            Image(
                painter = painterResource(id = avatarRes),
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
    val emoji: String,
    val label: String,
    val labelColor: Color
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
    FeedbackRating("😡", "Very poor", Color(0xFFE23D3D)),
    FeedbackRating("☹️", "Poor", Color(0xFFE08A2F)),
    FeedbackRating("😐", "Normal", Color(0xFF8E8E8E)),
    FeedbackRating("😊", "Good", Color(0xFF1EA24A)),
    FeedbackRating("😍", "Excellent", Color(0xFFE83D73))
)

private fun formatCallTime(secondsRemaining: Int): String {
    val safeSeconds = secondsRemaining.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
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

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CallScreenPreview() {
    BffAndroidTheme {
        CallScreen(
            personName = "Anshu",
            onBack = {}
        )
    }
}
