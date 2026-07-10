package com.gobff.getfriends.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gobff.getfriends.R
import com.gobff.getfriends.data.model.UserProfileUiState
import com.gobff.getfriends.ui.component.BffHeartChip
import com.gobff.getfriends.ui.component.CachedAvatarImage
import com.gobff.getfriends.ui.component.HandDrawnCardShape
import com.gobff.getfriends.ui.component.HeartChipShape
import com.gobff.getfriends.ui.component.screenEnterMotion
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.utils.AvatarCache
import com.gobff.getfriends.utils.AvatarGender
import com.gobff.getfriends.utils.toAvatarGender
import com.gobff.getfriends.viewmodel.HomeOptionsUiState
import com.gobff.getfriends.viewmodel.HomeOptionsViewModel
import com.gobff.getfriends.viewmodel.UserProfileViewModel
import java.io.File

private val ProfileCoral = Color(0xFFFF7171)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    walletHearts: Int = 0,
    onBack: () -> Unit = {},
    onGiftVibeRequested: () -> Unit = {},
    onWalletRequested: () -> Unit = {},
    onRechargeRequested: () -> Unit = {},
    onSettingsRequested: () -> Unit = {},
    isAvailableForCalls: Boolean = true,
    onAvailabilityChanged: (Boolean) -> Unit = {},
    onNotificationAccessRequested: (onAccessReady: () -> Unit) -> Unit = { onAccessReady -> onAccessReady() },
    homeOptionsViewModel: HomeOptionsViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val homeOptionsState = homeOptionsViewModel.uiState
    val userProfileState = userProfileViewModel.uiState

    LaunchedEffect(Unit) {
        homeOptionsViewModel.loadHomeOptions()
        userProfileViewModel.loadProfile()
    }

    ProfileScreenContent(
        modifier = modifier,
        walletHearts = walletHearts,
        userProfileState = userProfileState,
        homeOptionsState = homeOptionsState,
        onBack = onBack,
        onGiftVibeRequested = onGiftVibeRequested,
        onWalletRequested = onWalletRequested,
        onRechargeRequested = onRechargeRequested,
        onSettingsRequested = onSettingsRequested,
        isAvailableForCalls = isAvailableForCalls,
        onAvailabilityChanged = onAvailabilityChanged,
        onNotificationAccessRequested = onNotificationAccessRequested,
        onSaveLanguages = { languages, onComplete ->
            userProfileViewModel.saveLanguages(languages, onComplete)
        },
        onSaveVibes = { vibes, onComplete ->
            userProfileViewModel.saveVibes(vibes, onComplete)
        },
        onSaveAvatar = { avatarUrl, onComplete ->
            userProfileViewModel.saveIdentity(avatarUrl = avatarUrl, onComplete = onComplete)
        },
        onSaveName = { displayName, onComplete ->
            userProfileViewModel.saveIdentity(displayName = displayName, onComplete = onComplete)
        }
    )
}

@Composable
private fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    walletHearts: Int = 0,
    userProfileState: UserProfileUiState = UserProfileUiState(),
    homeOptionsState: HomeOptionsUiState = HomeOptionsUiState(),
    onBack: () -> Unit = {},
    onGiftVibeRequested: () -> Unit = {},
    onWalletRequested: () -> Unit = {},
    onRechargeRequested: () -> Unit = {},
    onSettingsRequested: () -> Unit = {},
    isAvailableForCalls: Boolean = true,
    onAvailabilityChanged: (Boolean) -> Unit = {},
    onNotificationAccessRequested: (onAccessReady: () -> Unit) -> Unit = { onAccessReady -> onAccessReady() },
    onSaveLanguages: (Set<String>, () -> Unit) -> Unit = { _, onComplete -> onComplete() },
    onSaveVibes: (Set<String>, () -> Unit) -> Unit = { _, onComplete -> onComplete() },
    onSaveAvatar: (String, () -> Unit) -> Unit = { _, onComplete -> onComplete() },
    onSaveName: (String, () -> Unit) -> Unit = { _, onComplete -> onComplete() }
) {
    val context = LocalContext.current
    var openSheet by remember { mutableStateOf<ProfileSheet?>(null) }
    var selectedLanguages by remember { mutableStateOf(setOf<String>()) }
    var selectedVibes by remember { mutableStateOf(setOf<String>()) }
    var selectedInterests by remember { mutableStateOf(setOf("Gaming", "Memes", "Foodie", "Fashion", "Deep talks")) }
    var editingName by remember { mutableStateOf("") }
    var editingAvatarUrl by remember { mutableStateOf("") }
    var starHostScreen by remember { mutableStateOf(StarHostScreen.Profile) }
    var hasStarHostIntroVideo by remember { mutableStateOf(false) }
    var starHostIntroVideoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingStarHostVideoUri by remember { mutableStateOf<Uri?>(null) }
    var starHostVideoMessage by remember { mutableStateOf<String?>(null) }
    var isOnline by remember { mutableStateOf(isAvailableForCalls) }
    var notificationPermissionMessage by remember { mutableStateOf<String?>(null) }

    fun toggleAvailability() {
        if (isOnline) {
            notificationPermissionMessage = null
            isOnline = false
            onAvailabilityChanged(false)
            return
        }

        notificationPermissionMessage = null
        onNotificationAccessRequested {
            notificationPermissionMessage = null
            isOnline = true
            onAvailabilityChanged(true)
        }
    }

    fun createStarHostVideoUri(): Uri {
        val videoDir = File(context.cacheDir, "star_host_videos").apply { mkdirs() }
        val videoFile = File(videoDir, "star_host_intro_${System.currentTimeMillis()}.mp4")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            videoFile
        )
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { recorded ->
        if (recorded) {
            starHostIntroVideoUri = pendingStarHostVideoUri
            hasStarHostIntroVideo = true
            starHostVideoMessage = null
        } else {
            starHostVideoMessage = "Video recording cancelled. Please try again."
        }
        pendingStarHostVideoUri = null
    }

    fun launchStarHostVideoCapture() {
        val uri = createStarHostVideoUri()
        pendingStarHostVideoUri = uri
        captureVideoLauncher.launch(uri)
    }

    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (cameraGranted && audioGranted) {
            launchStarHostVideoCapture()
        } else {
            starHostVideoMessage = "Camera and microphone permissions are needed to record your intro video."
        }
    }

    fun startStarHostVideoCapture() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission && hasAudioPermission) {
            launchStarHostVideoCapture()
        } else {
            videoPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    fun playStarHostIntroVideo() {
        val uri = starHostIntroVideoUri ?: return
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "video/mp4")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            )
        }.onFailure {
            starHostVideoMessage = "Unable to play this video on your device."
        }
    }

    LaunchedEffect(userProfileState.languages) {
        selectedLanguages = userProfileState.languages
    }

    LaunchedEffect(userProfileState.vibes) {
        selectedVibes = userProfileState.vibes
    }

    LaunchedEffect(isAvailableForCalls) {
        isOnline = isAvailableForCalls
        if (isAvailableForCalls) {
            notificationPermissionMessage = null
        }
    }

    BackHandler {
        if (starHostScreen == StarHostScreen.Queue) {
            starHostScreen = StarHostScreen.Profile
        } else if (starHostScreen == StarHostScreen.Apply) {
            starHostScreen = StarHostScreen.Progress
        } else if (starHostScreen == StarHostScreen.Progress) {
            starHostScreen = StarHostScreen.Profile
        } else if (openSheet != null) {
            openSheet = null
        } else {
            onBack()
        }
    }

    when (starHostScreen) {
        StarHostScreen.Progress -> {
            StarHostProgressScreen(
                modifier = modifier,
                onBack = { starHostScreen = StarHostScreen.Profile },
                onApply = { starHostScreen = StarHostScreen.Apply }
            )
            return
        }

        StarHostScreen.Apply -> {
            StarHostApplyScreen(
                modifier = modifier,
                hasIntroVideo = hasStarHostIntroVideo,
                message = starHostVideoMessage,
                onBack = { starHostScreen = StarHostScreen.Progress },
                onRecordVideo = { startStarHostVideoCapture() },
                onPlayVideo = { playStarHostIntroVideo() },
                onDeleteVideo = {
                    hasStarHostIntroVideo = false
                    starHostIntroVideoUri = null
                    starHostVideoMessage = null
                },
                onApply = { starHostScreen = StarHostScreen.Queue }
            )
            return
        }

        StarHostScreen.Queue -> {
            StarHostQueueScreen(
                modifier = modifier,
                onBackToProfile = { starHostScreen = StarHostScreen.Profile }
            )
            return
        }

        StarHostScreen.Profile -> Unit
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ProfileTopBar(
                onBack = onBack,
                walletBalance = 0,
                walletHearts = walletHearts,
                onWalletRequested = onWalletRequested,
                onRechargeRequested = onRechargeRequested,
                onSettingsRequested = onSettingsRequested,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp)
                    .screenEnterMotion(index = 0)
            )
            Spacer(modifier = Modifier.height(46.dp))
            ProfileIdentity(
                displayName = userProfileState.displayName,
                gender = userProfileState.gender,
                avatarUrl = userProfileState.avatarUrl,
                isOnline = isOnline,
                notificationPermissionMessage = notificationPermissionMessage,
                onToggleAvailability = ::toggleAvailability,
                onAvatarEditClick = {
                    editingAvatarUrl = AvatarCache.normalizeAvatarValue(userProfileState.avatarUrl)
                        ?: AvatarCache.avatarValue(1)
                    openSheet = ProfileSheet.Avatar
                },
                onNameEditClick = {
                    editingName = userProfileState.displayName.orEmpty()
                    openSheet = ProfileSheet.Name
                },
                modifier = Modifier.screenEnterMotion(index = 1, initialOffsetY = 20.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            ProfileContentCard(
                onStarHostClick = { starHostScreen = StarHostScreen.Progress },
                onLanguageClick = { openSheet = ProfileSheet.Language },
                onInterestClick = { openSheet = ProfileSheet.Interests },
                onVibeClick = { openSheet = ProfileSheet.Vibe },
                onGameStatsClick = { openSheet = ProfileSheet.GameStats },
                onGiftVibeRequested = onGiftVibeRequested,
                modifier = Modifier.screenEnterMotion(index = 2, initialOffsetY = 24.dp)
            )
        }

        if (openSheet != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.48f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        openSheet = null
                    }
            )
        }

        when (openSheet) {
            ProfileSheet.Language -> LanguageFilterSheet(
                selectedLanguages = selectedLanguages,
                languageOptions = homeOptionsState.languageOptions,
                onLanguageSelected = { selectedLanguages = selectedLanguages.toggleValue(it) },
                onClear = { selectedLanguages = emptySet() },
                onApply = {
                    onSaveLanguages(selectedLanguages) {
                        openSheet = null
                    }
                },
                actionText = "Save",
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            ProfileSheet.Vibe -> VibeFilterSheet(
                selectedVibes = selectedVibes,
                vibeOptions = homeOptionsState.vibeOptions,
                onVibeSelected = { selectedVibes = selectedVibes.toggleValue(it) },
                onClear = { selectedVibes = emptySet() },
                onApply = {
                    onSaveVibes(selectedVibes) {
                        openSheet = null
                    }
                },
                actionText = "Save",
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            ProfileSheet.Interests -> InterestsFilterSheet(
                selectedInterests = selectedInterests,
                onInterestSelected = { selectedInterests = selectedInterests.toggleValue(it) },
                onClear = { selectedInterests = emptySet() },
                onSave = { openSheet = null },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            ProfileSheet.GameStats -> GameStatsSheet(
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            ProfileSheet.Avatar -> AvatarEditSheet(
                selectedAvatarUrl = editingAvatarUrl,
                gender = userProfileState.gender,
                onAvatarSelected = { editingAvatarUrl = it },
                onSave = {
                    onSaveAvatar(editingAvatarUrl) {
                        openSheet = null
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            ProfileSheet.Name -> NameEditSheet(
                name = editingName,
                onNameChange = { editingName = it },
                onSave = {
                    onSaveName(editingName) {
                        openSheet = null
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
            )

            null -> Unit
        }
    }
}

@Composable
private fun ProfileTopBar(
    onBack: () -> Unit,
    walletBalance: Int,
    walletHearts: Int,
    onWalletRequested: () -> Unit,
    onRechargeRequested: () -> Unit,
    onSettingsRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
        )
        Spacer(modifier = Modifier.weight(1f))
        ProfileTopChip(
            width = 61.dp,
            iconRes = R.drawable.profile_screen_wallet,
            text = "₹$walletBalance",
            onClick = onWalletRequested
        )
        Spacer(modifier = Modifier.width(12.dp))
        BffHeartChip(hearts = walletHearts, onClick = onRechargeRequested)
        Spacer(modifier = Modifier.width(12.dp))
        ProfileSettingsChip(onClick = onSettingsRequested)
    }
}

@Composable
private fun ProfileTopChip(
    width: androidx.compose.ui.unit.Dp,
    iconRes: Int,
    text: String,
    onClick: () -> Unit
) {
    val shape = HeartChipShape
    Box(
        modifier = Modifier
            .size(width = width, height = 32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 1.5.dp, y = 1.5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.2.dp, Color.Black, shape)
                .padding(horizontal = 6.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                color = Color.Black,
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileSettingsChip(onClick: () -> Unit) {
    val shape = HeartChipShape
    Box(
        modifier = Modifier
            .size(32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 1.5.dp, y = 1.5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.2.dp, Color.Black, shape)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ProfileIdentity(
    displayName: String?,
    gender: String?,
    avatarUrl: String?,
    isOnline: Boolean,
    notificationPermissionMessage: String?,
    onToggleAvailability: () -> Unit,
    onAvatarEditClick: () -> Unit,
    onNameEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(modifier = Modifier.size(112.dp)) {
            CachedAvatarImage(
                avatarUrl = avatarUrl,
                gender = gender,
                fallbackRes = gender.toProfileFallbackAvatarRes(),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(104.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD33F))
                    .border(2.dp, Color.Black, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAvatarEditClick
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayName?.takeIf { it.isNotBlank() } ?: "Profile",
                color = Color.Black,
                fontSize = 24.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD33F))
                    .border(1.5.dp, Color.Black, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNameEditClick
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit name",
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ProfileAvailabilityToggle(
            isOnline = isOnline,
            onToggle = onToggleAvailability
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (isOnline) {
                "Available to take calls right now"
            } else {
                "You're currently unavailable for calls"
            },
            color = Color(0xFF7D7D7D),
            fontSize = 14.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        notificationPermissionMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color(0xFF7D7D7D),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

private fun String?.toProfileAvatarRes(): Int {
    val normalized = this?.trim().orEmpty()
    return when {
        normalized == "women_avatar1" -> R.drawable.women_avatar1
        normalized == "women_avatar2" -> R.drawable.women_avatar1
        normalized == "women_avatar3" -> R.drawable.women_avatar1
        normalized == "women_avatar4" -> R.drawable.women_avatar1
        normalized == "women_avatar5" -> R.drawable.women_avatar1
        normalized == "women_avatar6" -> R.drawable.women_avatar1
        normalized == "women_avatar7" -> R.drawable.women_avatar1
        normalized == "women_avatar8" -> R.drawable.women_avatar1
        normalized == "women_avatar9" -> R.drawable.women_avatar1
        normalized == "women_avatar10" -> R.drawable.women_avatar1
        normalized == "women_avatar11" -> R.drawable.women_avatar1
        normalized == "women_avatar12" -> R.drawable.women_avatar1
        normalized == "man_avatar1" -> R.drawable.man_avatar1
        normalized == "man_avatar2" -> R.drawable.man_avatar1
        normalized == "man_avatar3" -> R.drawable.man_avatar1
        normalized == "man_avatar4" -> R.drawable.man_avatar1
        normalized == "man_avatar5" -> R.drawable.man_avatar1
        normalized == "man_avatar6" -> R.drawable.man_avatar1
        normalized == "man_avatar7" -> R.drawable.man_avatar1
        normalized == "man_avatar8" -> R.drawable.man_avatar1
        normalized == "man_avatar9" -> R.drawable.man_avatar1
        normalized == "man_avatar10" -> R.drawable.man_avatar1
        normalized == "man_avatar11" -> R.drawable.man_avatar1
        normalized == "man_avatar12" -> R.drawable.man_avatar1
        else -> R.drawable.women_avatar1
    }
}

private fun String?.toProfileFallbackAvatarRes(): Int {
    return when (this.toAvatarGender()) {
        AvatarGender.Female -> R.drawable.women_avatar1
        AvatarGender.Male -> R.drawable.man_avatar1
        null -> R.drawable.women_avatar1
    }
}

@Composable
private fun ProfileAvailabilityToggle(
    isOnline: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val background = if (isOnline) Color(0xFF08C879) else Color(0xFFE6E6E6)
    val textColor = if (isOnline) Color.Black else Color(0xFF171717)
    val iconRes = if (isOnline) R.drawable.toggle_online else R.drawable.toggle_offline

    Box(
        modifier = Modifier
            .size(width = 134.dp, height = 42.dp)
            .clip(shape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 7.dp)
    ) {
        Text(
            text = if (isOnline) "Online" else "Offline",
            color = textColor,
            fontSize = 15.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(if (isOnline) Alignment.CenterEnd else Alignment.CenterStart)
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isOnline) Color.White else Color.White.copy(alpha = 0.72f))
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}


@Composable
private fun ProfileContentCard(
    onStarHostClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onInterestClick: () -> Unit,
    onVibeClick: () -> Unit,
    onGameStatsClick: () -> Unit,
    onGiftVibeRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(ProfileCoral)
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile_screen_bg_objects),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 26.dp, bottom = 0.dp)
        ) {
            BecomeStarHostCard(onClick = onStarHostClick)
            Spacer(modifier = Modifier.height(24.dp))
            ProfileInfoGrid(
                onLanguageClick = onLanguageClick,
                onInterestClick = onInterestClick,
                onVibeClick = onVibeClick,
                onGameStatsClick = onGameStatsClick
            )
            Spacer(modifier = Modifier.height(24.dp))
            GiftReceivedCard(onViewAll = onGiftVibeRequested)
            Spacer(modifier = Modifier.height(24.dp))

        }
    }
}

@Composable
private fun ProfileInfoGrid(
    onLanguageClick: () -> Unit,
    onInterestClick: () -> Unit,
    onVibeClick: () -> Unit,
    onGameStatsClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileFeatureCard(
                title = "My languages",
                imageRes = R.drawable.profile_screen_language,
                icon = Icons.Default.Language,
                background = Color(0xFFFFF2F5),
                onClick = onLanguageClick,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
            )

            ProfileFeatureCard(
                title = "My Interests",
                imageRes = R.drawable.profile_screen_interest,
                icon = Icons.Default.Favorite,
                background = Color(0xFFFFF2F5),
                onClick = onInterestClick,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
            )
        }


        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileFeatureCard(
                title = "Current vibe",
                imageRes = R.drawable.profile_screen_vibe,
                icon = Icons.Default.Star,
                background = Color(0xFFE6D4F9),
                onClick = onVibeClick,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
            )

            ProfileFeatureCard(
                title = "Game stats",
                imageRes = R.drawable.profile_screen_game_stats,
                icon = Icons.Default.SportsEsports,
                background = Color(0xFFFFF0C9),
                onClick = onGameStatsClick,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
            )
        }
    }
}

@Composable
private fun ProfileFeatureCard(
    title: String,
    imageRes: Int,
    icon: ImageVector,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = HandDrawnCardShape
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 1.5.dp, y = 1.5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(background)
                .border(1.2.dp, Color.Black, shape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 14.dp, top = 14.dp)
            ) {
                IconBubble(icon = icon)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .size(width = 118.dp, height = 66.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun IconBubble(icon: ImageVector) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.72f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFA032B3),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun BecomeStarHostCard(onClick: () -> Unit) {
    val shape = HandDrawnCardShape
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 1.5.dp, y = 1.5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color(0xFFFFF0F7))
                .border(1.3.dp, Color.Black, shape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp, end = 150.dp)
            ) {
                Text(
                    text = "Become a",
                    color = Color(0xFF202020),
                    fontSize = 18.sp,
                    lineHeight = 19.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Star Host",
                    color = Color(0xFF8F3EEB),
                    fontSize = 23.sp,
                    lineHeight = 25.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unlock premium\nperks and earn more!",
                    color = Color(0xFF2E2E2E),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            Image(
                painter = painterResource(id = R.drawable.profile_become_host),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp)
                    .size(width = 154.dp, height = 116.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun StarHostProgressScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onApply: () -> Unit
) {
    val completedCount = 3
    val totalCount = 3
    val canApply = completedCount == totalCount

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 58.dp, bottom = 34.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack
                        )
                )
            }
            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = "BECOME A STAR HOST",
                color = Color(0xFFFF4F93),
                fontSize = 25.sp,
                lineHeight = 30.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Earn more. Be seen by thousands.",
                color = Color(0xFF5F5F5F),
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(26.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StarHostStatCard("35 ❤", "per audio call", Color(0xFFFFF0E8), Modifier.weight(1f))
                StarHostStatCard("60 ❤", "per video call", Color(0xFFFFE8F1), Modifier.weight(1f))
                StarHostStatCard("20x", "More reach", Color(0xFFFFF8E3), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(38.dp))
            StarHostProgressPanel(completedCount = completedCount, totalCount = totalCount)
            Spacer(modifier = Modifier.height(34.dp))
            StarHostApplyButton(
                text = "Apply for Star Host",
                enabled = canApply,
                onClick = onApply,
                backgroundColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StarHostStatCard(
    value: String,
    label: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier.height(80.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(background)
                .border(1.2.dp, Color.Black, shape)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = value,
                color = Color(0xFFFF5B29),
                fontSize = 17.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = label,
                color = Color.Black,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StarHostProgressPanel(
    completedCount: Int,
    totalCount: Int
) {
    val panelShape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(404.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(panelShape)
                .background(Color(0xFFFF72A9))
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = Stroke(width = 26f)
            drawPath(
                path = Path().apply {
                    moveTo(-40f, size.height * 0.92f)
                    cubicTo(size.width * 0.2f, size.height * 0.68f, size.width * 0.62f, size.height * 0.98f, size.width + 50f, size.height * 0.75f)
                },
                color = Color.White.copy(alpha = 0.10f),
                style = stroke
            )
            drawPath(
                path = Path().apply {
                    moveTo(-20f, size.height * 0.35f)
                    cubicTo(size.width * 0.35f, size.height * 0.22f, size.width * 0.56f, size.height * 0.54f, size.width + 30f, size.height * 0.30f)
                },
                color = Color.White.copy(alpha = 0.08f),
                style = stroke
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp, vertical = 40.dp)
                .fillMaxWidth()
                .height(278.dp)
        ) {
            val cardShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 3.dp, y = 4.dp)
                    .clip(cardShape)
                    .background(Color.Black)
            )
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
                    .background(Color.White)
                    .border(1.2.dp, Color.Black, cardShape)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "◎",
                        color = Color(0xFFB76DFF),
                        fontSize = 21.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your Progress",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(25.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFE2EE))
                            .padding(horizontal = 13.dp)
                    ) {
                        Text(
                            text = "$completedCount of $totalCount Completed",
                            color = Color(0xFF79364D),
                            fontSize = 10.sp,
                            fontFamily = GaretFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
                StarHostProgressItem(
                    title = "Connect with users for 50 hours",
                    progressText = "50/50",
                    progress = 1f,
                    completed = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                StarHostProgressItem(
                    title = "Maintain a 4.8 Star rating",
                    progressText = "100%",
                    progress = 1f,
                    completed = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                StarHostProgressItem(
                    title = "Earn 1,000 hearts from users",
                    progressText = "1000/1000",
                    progress = 1f,
                    completed = true
                )
            }
        }
    }
}

@Composable
private fun StarHostProgressItem(
    title: String,
    progressText: String,
    progress: Float,
    completed: Boolean
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(shape)
            .background(if (completed) Color(0xFFF0FAEC) else Color.White)
            .border(1.dp, Color(0xFFE0E0E0), shape)
            .padding(horizontal = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (completed) Color(0xFF65C83F) else Color.White)
                .border(1.2.dp, if (completed) Color(0xFF65C83F) else Color(0xFFC4C4C4), CircleShape)
        ) {
            if (completed) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFF2A2A2A),
                fontSize = 11.5.sp,
                lineHeight = 13.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(9.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE7E7E7))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF66C843))
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = progressText,
            color = Color(0xFF4F8B3F),
            fontSize = 10.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StarHostApplyScreen(
    modifier: Modifier = Modifier,
    hasIntroVideo: Boolean,
    message: String?,
    onBack: () -> Unit,
    onRecordVideo: () -> Unit,
    onPlayVideo: () -> Unit,
    onDeleteVideo: () -> Unit,
    onApply: () -> Unit
) {
    var selectedTopic by remember { mutableStateOf("Psychology") }
    val topics = listOf(
        listOf("Psychology", "Dating Advice"),
        listOf("Gaming pro", "Fitness", "Pro Listener"),
        listOf("Career Guidance", "Music & Fun", "Life Advice"),
        listOf("Movies & Entertainment", "Late Night Talks"),
        listOf("Spirituality")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 58.dp, bottom = 120.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Apply for Star Host",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(46.dp))
            Text(
                text = "What are you great at ?",
                color = Color.Black,
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = "Choose one topic that represents you the best.",
                color = Color(0xFF888888),
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(18.dp))
            topics.forEach { rowTopics ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowTopics.forEach { topic ->
                        StarHostTopicChip(
                            text = topic,
                            selected = selectedTopic == topic,
                            onClick = { selectedTopic = topic },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowTopics.size == 1) {
                        Spacer(modifier = Modifier.weight(2f))
                    }
                }
                Spacer(modifier = Modifier.height(11.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFFE6E6E6))
                )
                Image(
                    painter = painterResource(id = R.drawable.single_heart),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(20.dp),
                    contentScale = ContentScale.Fit
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFFE6E6E6))
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = "Introduce yourself to our team",
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color(0xFFEAF3FF))
                    .padding(horizontal = 2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This private video helps us review your application",
                color = Color(0xFF888888),
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (hasIntroVideo) {
                StarHostRecordedVideoCard(
                    onPlay = onPlayVideo,
                    onDelete = onDeleteVideo
                )
            } else {
                StarHostRecordVideoCard(onRecord = onRecordVideo)
            }
            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    color = Color(0xFFFF4F93),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        StarHostApplyButton(
            text = "Apply for Star Host",
            enabled = hasIntroVideo,
            onClick = onApply,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 38.dp, vertical = 32.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun StarHostTopicChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) Color(0xFFFFEDF4) else Color.White)
            .border(if (selected) 2.dp else 1.2.dp, Color.Black, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF333333),
            fontSize = 12.sp,
            lineHeight = 12.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StarHostRecordVideoCard(onRecord: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(shape)
            .background(Color(0xFFFFF0F6))
            .border(1.dp, Color(0xFFFFB8D2), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onRecord
            )
            .padding(horizontal = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDDEB))
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = Color(0xFFFF4F93),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Record 1 minute video",
            color = Color(0xFF333333),
            fontSize = 12.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 82.dp, height = 34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(1.2.dp, Color.Black, RoundedCornerShape(6.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRecord
                )
        ) {
            Text(
                text = "Record",
                color = Color(0xFF333333),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StarHostRecordedVideoCard(
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(shape)
            .background(Color(0xFFF5FFF1))
            .border(1.dp, Color(0xFF98EE89), shape)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Intro Video",
            color = Color.Black,
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        StarHostSmallActionButton(
            text = "Play",
            icon = Icons.Default.PlayArrow,
            onClick = onPlay
        )
        Spacer(modifier = Modifier.width(8.dp))
        StarHostSmallActionButton(
            text = "Delete",
            icon = Icons.Default.Delete,
            onClick = onDelete
        )
    }
}

@Composable
private fun StarHostSmallActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(width = 72.dp, height = 32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.1.dp, Color.Black, RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF333333),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text,
            color = Color(0xFF333333),
            fontSize = 10.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun StarHostVideoTipsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onStartRecording: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
    ) {
        StarHostCameraPreview(dimmed = true)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 38.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFFF2F7))
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Text(
                text = "♡  Tips for great video",
                color = Color(0xFFFF4F93),
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            listOf(
                "Introduce yourself",
                "Share why you love this topic",
                "Tell us what makes you a great host",
                "Speak in English or your preferred language."
            ).forEach { tip ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.single_heart),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        color = Color(0xFF777777),
                        fontSize = 11.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        StarHostCameraTopBar(tint = Color.White)
        StarHostCameraAction(
            icon = Icons.Default.Videocam,
            label = "",
            onClick = onStartRecording,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 70.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 24.dp)
                .size(44.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
        )
    }
}

@Composable
private fun StarHostVideoRecordingScreen(
    modifier: Modifier = Modifier,
    onStopRecording: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        StarHostCameraPreview(dimmed = false)
        StarHostCameraTopBar(tint = Color.White)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 92.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF2F2F))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "0 : 35",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 78.dp)
        ) {
            StarHostCameraAction(
                icon = Icons.Default.Stop,
                label = "",
                background = Color(0xFFFF2F2F),
                onClick = onStopRecording
            )
            StarHostCameraAction(
                icon = Icons.Default.Pause,
                label = "",
                background = Color.White.copy(alpha = 0.2f),
                onClick = {}
            )
        }
    }
}

@Composable
private fun StarHostVideoReviewScreen(
    modifier: Modifier = Modifier,
    onRetake: () -> Unit,
    onAccept: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        StarHostCameraPreview(dimmed = false)
        StarHostCameraTopBar(tint = Color.White)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 92.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = "1 : 04",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .size(54.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 38.dp, vertical = 58.dp)
        ) {
            StarHostCameraAction(
                icon = Icons.Default.Close,
                label = "",
                background = Color.Transparent,
                onClick = onRetake
            )
            StarHostCameraAction(
                icon = Icons.Default.Check,
                label = "",
                background = Color.Transparent,
                onClick = onAccept
            )
        }
    }
}

@Composable
private fun StarHostCameraPreview(dimmed: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB7ADA2))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color(0xFFE1D9D1), radius = size.width * 0.72f, center = Offset(size.width * 0.5f, size.height * 0.36f))
            drawCircle(Color(0xFF7E8B75), radius = size.width * 0.18f, center = Offset(size.width * 0.18f, size.height * 0.72f))
            drawCircle(Color(0xFF3E5136), radius = size.width * 0.12f, center = Offset(size.width * 0.12f, size.height * 0.78f))
        }
        Image(
            painter = painterResource(id = R.drawable.women_avatar1),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(330.dp),
            contentScale = ContentScale.Fit
        )
        if (dimmed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
            )
        }
    }
}

@Composable
private fun StarHostCameraTopBar(tint: Color) {
    Text(
        text = "9:41",
        color = tint,
        fontSize = 13.sp,
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 48.dp, start = 30.dp)
    )
}

@Composable
private fun StarHostCameraAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    background: Color = Color.White.copy(alpha = 0.2f),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(background)
                .border(2.dp, Color.White, CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        if (label.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StarHostQueueScreen(
    modifier: Modifier = Modifier,
    onBackToProfile: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFF6FA9))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = Stroke(width = 30f)
            repeat(6) { index ->
                drawPath(
                    path = Path().apply {
                        val y = size.height * (0.12f + index * 0.13f)
                        moveTo(-50f, y)
                        cubicTo(size.width * 0.28f, y - 42f, size.width * 0.66f, y + 44f, size.width + 60f, y - 20f)
                    },
                    color = Color.White.copy(alpha = 0.08f),
                    style = stroke
                )
            }
        }
        StarHostCameraTopBar(tint = Color.Black)
        Image(
            painter = painterResource(id = R.drawable.become_host_queue),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 213.dp)
                .size(width = 317.dp, height = 283.dp),
            contentScale = ContentScale.Fit
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 170.dp)
                .padding(horizontal = 34.dp)
        ) {
            Text(
                text = "You’re in the queue !",
                color = Color.White,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Our team is reviewing your profile to ensure\ntop quality. Check back in 24-48 hours.",
                color = Color.Black,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        StarHostApplyButton(
            text = "Back to profile",
            enabled = true,
            onClick = onBackToProfile,
            backgroundColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 38.dp, vertical = 34.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun StarHostApplyButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0xFFFF6FA9),
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier.height(54.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 4.dp)
                .clip(shape)
                .background(Color.Black)
                .alpha(if (enabled) 1f else 0.35f)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(if (enabled) backgroundColor else Color(0xFFFFC6DC))
                .border(1.2.dp, Color.Black.copy(alpha = if (enabled) 1f else 0.2f), shape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
        ) {
            Text(
                text = text,
                color = if (enabled) Color.Black else Color(0xFF8C5F72),
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GiftReceivedCard(onViewAll: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Column(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.2.dp, Color.Black, shape)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎁", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gift received",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "View all 〉",
                    color = Color(0xFF8C8C8C),
                    fontSize = 13.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onViewAll
                    )
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileGiftItem(
                    title = "Garam chai",
                    resId = R.drawable.gift_chai,
                    count = "x40",
                    modifier = Modifier.weight(1f)
                )

                ProfileGiftItem(
                    title = "Ice cream",
                    resId = R.drawable.gift_icecream,
                    count = "x12",
                    modifier = Modifier.weight(1f)
                )

                ProfileGiftItem(
                    title = "Maggie",
                    resId = R.drawable.gift_maggie,
                    count = "x23",
                    modifier = Modifier.weight(1f)
                )

                ProfileGiftItem(
                    title = "Yellow rose",
                    resId = R.drawable.gift_yellow_rose,
                    count = "x32",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileGiftItem(
    title: String,
    resId: Int,
    count: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(9.dp)

    Box(
        modifier = modifier
            .aspectRatio(70f / 72f)
    ) {

        // shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )

        // card
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color.Black, shape)
        ) {

            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .size(width = 44.dp, height = 34.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = title,
                color = Color.Black,
                fontSize = 7.5.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 4.dp, vertical = 7.dp)
            )
        }


        // count badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 5.dp, y = (-5).dp)
                .size(width = 21.dp, height = 14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFF5A9C))
        ) {
            Text(
                text = count,
                color = Color.White,
                fontSize = 8.sp,
                lineHeight = 8.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-0.5f).dp)
            )
        }
    }
}

@Composable
private fun InterestsFilterSheet(
    selectedInterests: Set<String>,
    onInterestSelected: (String) -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileBottomSheetContainer(
        title = "What’s your interests?",
        onClear = onClear,
        onSave = onSave,
        modifier = modifier.height(650.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            InterestSection(
                title = "Fun & Entertainment",
                rows = listOf(
                    listOf("Gaming", "Anime", "Movies"),
                    listOf("K-pop", "Music", "Memes")
                ),
                selectedInterests = selectedInterests,
                onInterestSelected = onInterestSelected
            )
            InterestSection(
                title = "Food & Lifestyle",
                rows = listOf(
                    listOf("Foodie", "Travel", "Fitness"),
                    listOf("Coffee", "Fashion", "Art")
                ),
                selectedInterests = selectedInterests,
                onInterestSelected = onInterestSelected
            )
            InterestSection(
                title = "Talk & Mood",
                rows = listOf(
                    listOf("Late night talks", "Deep talks"),
                    listOf("Coffee", "Fashion", "Art")
                ),
                selectedInterests = selectedInterests,
                onInterestSelected = onInterestSelected
            )
            InterestSection(
                title = "Skill & Growth",
                rows = listOf(
                    listOf("Learning", "Tech", "Books")
                ),
                selectedInterests = selectedInterests,
                onInterestSelected = onInterestSelected
            )
        }
    }
}

@Composable
private fun InterestSection(
    title: String,
    rows: List<List<String>>,
    selectedInterests: Set<String>,
    onInterestSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = Color(0xFF858585),
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        rows.forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { interest ->
                    InterestChip(
                        text = interest,
                        selected = selectedInterests.contains(interest),
                        onClick = { onInterestSelected(interest) },
                        modifier = Modifier.weight(if (rowItems.size == 2) 1.5f else 1f)
                    )
                }
                if (rowItems.size == 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InterestChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = when (text) {
        "Gaming", "Memes" -> Color(0xFFFF5A8D)
        "Foodie", "Fashion" -> Color(0xFF39B9B0)
        "Deep talks", "Tech" -> Color(0xFFB953DF)
        else -> Color(0xFFFF5A8D)
    }
    val shape = HandDrawnCardShape

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) selectedColor else Color.White)
            .border(1.2.dp, Color.Black, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = "# $text",
            color = Color.Black,
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GameStatsSheet(modifier: Modifier = Modifier) {
    ProfileBottomSheetContainer(
        title = "Your player record",
        onClear = {},
        onSave = {},
        showActions = false,
        modifier = modifier.height(633.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            val shape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(334.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 3.dp, y = 4.dp)
                        .clip(shape)
                        .background(Color.Black)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(shape)
                        .background(Color(0xFF8B4BD6))
                        .border(1.5.dp, Color.Black, shape)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "TRUTH / DARE",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = "You earned",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 13.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "120 Hearts",
                        color = Color(0xFF9CFF67),
                        fontSize = 24.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GameStatMiniCard(
                            value = "32",
                            label = "Dares Completed",
                            modifier = Modifier.weight(1f)
                        )
                        GameStatMiniCard(
                            value = "18",
                            label = "Truths Told",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFFD8B9EC))
                )
                Text(
                    text = "Coming soon 🚀",
                    color = Color(0xFF606060),
                    fontSize = 13.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFFD8B9EC))
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ComingSoonGameCard(R.drawable.game_uno, "Uno", Modifier.weight(1f))
                ComingSoonGameCard(R.drawable.game_ludo, "Ludo", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AvatarEditSheet(
    selectedAvatarUrl: String,
    gender: String?,
    onAvatarSelected: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarOptions = remember(gender) { profileAvatarOptions(gender) }
    ProfileEditSheetContainer(
        title = "Choose your avatar",
        onSave = onSave,
        modifier = modifier.height(620.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            avatarOptions.chunked(4).forEach { rowOptions ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowOptions.forEach { option ->
                        ProfileAvatarOptionCard(
                            avatarUrl = option,
                            gender = gender,
                            selected = selectedAvatarUrl == option,
                            onClick = { onAvatarSelected(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatarOptionCard(
    avatarUrl: String,
    gender: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(if (selected) Color(0xFFFFD33F) else Color(0xFFFFF3F5))
            .border(if (selected) 2.dp else 1.dp, Color.Black, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(7.dp)
    ) {
        CachedAvatarImage(
            avatarUrl = avatarUrl,
            gender = gender,
            fallbackRes = gender.toProfileFallbackAvatarRes(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.White),
            contentScale = ContentScale.Crop
        )
        if (selected) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NameEditSheet(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileEditSheetContainer(
        title = "Edit your name",
        onSave = onSave,
        modifier = modifier.height(285.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            val shape = RoundedCornerShape(14.dp)
            BasicTextField(
                value = name,
                onValueChange = { onNameChange(it.take(30)) },
                singleLine = true,
                cursorBrush = SolidColor(ProfileCoral),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(shape)
                    .background(Color(0xFFFFF3F5))
                    .border(1.4.dp, Color.Black, shape)
                    .padding(horizontal = 16.dp, vertical = 15.dp)
            )
        }
    }
}

@Composable
private fun ProfileEditSheetContainer(
    title: String,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(width = 62.dp, height = 5.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFD7D7D7))
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = title,
            color = Color.Black,
            fontSize = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .background(Color.White)
                .border(0.5.dp, Color(0xFFE5E5E5), RoundedCornerShape(0.dp))
                .padding(horizontal = 20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(width = 132.dp, height = 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF6464))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSave
                    )
            ) {
                Text(
                    text = "Save",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GameStatMiniCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val shape = HandDrawnCardShape
    Box(modifier = modifier.height(78.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color(0xFFFFE16A))
                .border(1.2.dp, Color.Black, shape)
        ) {
            Text(
                text = value,
                color = Color.Black,
                fontSize = 28.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.Black,
                fontSize = 9.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ComingSoonGameCard(resId: Int, label: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally,     modifier = modifier.alpha(0.6f)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(89.dp)
                .clip(shape)
                .background(Color.White)
                .border(1.2.dp, Color(0xFF777777), shape)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = label,
                    color = Color(0xFF777777),
                    fontSize = 13.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileBottomSheetContainer(
    title: String,
    onClear: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    showActions: Boolean = true,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(width = 62.dp, height = 5.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFD7D7D7))
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = title,
            color = Color.Black,
            fontSize = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(34.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
        }
        if (showActions) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFE5E5E5), RoundedCornerShape(0.dp))
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Clear All",
                    color = Color(0xFF1F1F1F),
                    fontSize = 15.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClear
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 132.dp, height = 40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF6464))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSave
                        )
                ) {
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private enum class ProfileSheet {
    Avatar,
    Name,
    Language,
    Interests,
    Vibe,
    GameStats
}

private enum class StarHostScreen {
    Profile,
    Progress,
    Apply,
    Queue
}

private fun profileAvatarOptions(gender: String?): List<String> {
    val avatarGender = gender.toAvatarGender() ?: AvatarGender.Female
    return (1..avatarGender.count).map { AvatarCache.avatarValue(it) }
}

private fun Set<String>.toggleValue(value: String): Set<String> {
    return if (contains(value)) this - value else this + value
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ProfileScreenPreview() {
    BffAndroidTheme {
        ProfileScreenContent(
            walletHearts = 30,
            userProfileState = UserProfileUiState(
                displayName = "Badal",
                avatarUrl = "man_avatar1",
                languages = setOf("HINDI", "ENGLISH"),
                vibes = setOf("GAMING", "DEEP_TALK")
            )
        )
    }
}
