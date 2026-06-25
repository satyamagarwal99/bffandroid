package com.example.bffandroid.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bffandroid.R
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.ui.theme.GaretFontFamily
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onCallRequested: (String) -> Unit = {},
    onFriendsRequested: () -> Unit = {},
    onRechargeRequested: () -> Unit = {},
    onChatRequested: () -> Unit = {},
    onHistoryRequested: () -> Unit = {},
    onGamesRequested: () -> Unit = {},
    onProfileRequested: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(HomeTab.Connect) }
    var openFilterSheet by remember { mutableStateOf<HomeFilterSheet?>(null) }
    var selectedLanguages by remember { mutableStateOf(setOf<String>()) }
    var selectedVibes by remember { mutableStateOf(setOf<String>()) }
    var callDragProgress by remember { mutableStateOf(0f) }

    BackHandler(enabled = openFilterSheet != null) {
        openFilterSheet = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomePurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_background_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top section with header, title, and filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp)
                    .graphicsLayer {
                        alpha = (1f - (callDragProgress * 1.35f)).coerceIn(0f, 1f)
                    }
            ) {
                HomeHeader(
                    onFriendsClick = onFriendsRequested,
                    onRechargeClick = onRechargeRequested,
                    onProfileClick = onProfileRequested
                )
                Spacer(modifier = Modifier.height(30.dp))
                HomeTitle()
                Spacer(modifier = Modifier.height(24.dp))
                HomeFilters(
                    languageCount = selectedLanguages.size,
                    vibeCount = selectedVibes.size,
                    onLanguageClick = { openFilterSheet = HomeFilterSheet.Language },
                    onVibeClick = { openFilterSheet = HomeFilterSheet.Vibe }
                )
                Spacer(modifier = Modifier.height(0.dp))
            }

            // Card stack that fills remaining space
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                HomeCardStack(
                    onCallRequested = { profile -> onCallRequested(profile.name) },
                    onCallDragProgress = { callDragProgress = it }
                )
            }

            // Bottom bar space - no extra space needed
            Spacer(modifier = Modifier.height(88.dp))
        }

        // Bottom bar overlay
        HomeBottomBar(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                when (tab) {
                    HomeTab.Games -> onGamesRequested()
                    HomeTab.Chat -> onChatRequested()
                    HomeTab.History -> onHistoryRequested()
                    else -> {
                        selectedTab = tab
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (openFilterSheet != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.36f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        openFilterSheet = null
                    }
            )
        }

        when (openFilterSheet) {
            HomeFilterSheet.Language -> LanguageFilterSheet(
                selectedLanguages = selectedLanguages,
                onLanguageSelected = { language ->
                    selectedLanguages = selectedLanguages.toggleValue(language)
                },
                onClear = { selectedLanguages = emptySet() },
                onApply = { openFilterSheet = null },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            HomeFilterSheet.Vibe -> VibeFilterSheet(
                selectedVibes = selectedVibes,
                onVibeSelected = { vibe ->
                    selectedVibes = selectedVibes.toggleValue(vibe)
                },
                onClear = { selectedVibes = emptySet() },
                onApply = { openFilterSheet = null },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            null -> Unit
        }
    }
}

@Composable
private fun HomeHeader(
    onFriendsClick: () -> Unit,
    onRechargeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.man_avatar1),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onProfileClick
                ),
            contentScale = ContentScale.Crop

        )
        Spacer(modifier = Modifier.weight(1f))
        HeaderIconChip(
            icon = Icons.Default.Groups,
            containerColor = Color(0xFF5879FF),
            modifier = Modifier.size(46.dp),
            onClick = onFriendsClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        HeartCountChip(onClick = onRechargeClick)
    }
}

@Composable
private fun HeaderIconChip(
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier.then(
            Modifier.clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
        )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun HeartCountChip(onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.dp)
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
        ) {
            Image(
                painter = painterResource(id = R.drawable.single_heart),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "30",
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HomeTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Meet someone",
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "new",
                color = Color(0xFFF7FF35),
                fontSize = 22.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.rotate(-6f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "today",
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

    }
}

@Composable
private fun HomeFilters(
    languageCount: Int,
    vibeCount: Int,
    onLanguageClick: () -> Unit,
    onVibeClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeFilterChip(
            text = "Language",
            selectedCount = languageCount,
            width = 132.dp,
            onClick = onLanguageClick
        )
        HomeFilterChip(
            text = "Vibe",
            selectedCount = vibeCount,
            width = 112.dp,
            onClick = onVibeClick
        )
    }
}

@Composable
private fun HomeFilterChip(
    text: String,
    selectedCount: Int,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val hasSelection = selectedCount > 0
    val chipColor = if (hasSelection) Color(0xFFFFC22D) else Color.White

    Box(
        modifier = modifier
            .size(width = width, height = 40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .clip(shape)
                .background(Color(0x66331245))
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(chipColor)
                .border(1.dp, Color(0xFF2C252B), shape)
                .padding(horizontal = 10.dp)
        ) {
            if (hasSelection) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Text(
                        text = selectedCount.toString(),
                        color = Color(0xFF2C252B),
                        fontSize = 12.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = Color(0xFF2C252B),
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(6.dp))
            Image(
                painter = painterResource(id = R.drawable.down_arrow),
                contentDescription = "Down arrow",
                modifier = Modifier
                    .size(16.dp)
            )
        }
    }
}

@Composable
fun LanguageFilterSheet(
    selectedLanguages: Set<String>,
    onLanguageSelected: (String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    actionText: String = "Apply",
    modifier: Modifier = Modifier
) {
    FilterSheetContainer(
        title = "Choose your languages",
        onClear = onClear,
        onApply = onApply,
        actionText = actionText,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            LanguageOptions.chunked(3).forEach { rowOptions ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowOptions.forEach { option ->
                        LanguageOptionCard(
                            option = option,
                            isSelected = selectedLanguages.contains(option.id),
                            onClick = { onLanguageSelected(option.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VibeFilterSheet(
    selectedVibes: Set<String>,
    onVibeSelected: (String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    actionText: String = "Apply",
    modifier: Modifier = Modifier
) {
    FilterSheetContainer(
        title = "What’s your vibe today?",
        onClear = onClear,
        onApply = onApply,
        actionText = actionText,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            VibeOptions.chunked(2).forEach { rowOptions ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowOptions.forEach { option ->
                        VibeOptionCard(
                            option = option,
                            isSelected = selectedVibes.contains(option.id),
                            onClick = { onVibeSelected(option.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSheetContainer(
    title: String,
    onClear: () -> Unit,
    onApply: () -> Unit,
    actionText: String = "Apply",
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(526.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
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
            color = Color(0xFF1F1F1F),
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
        FilterSheetActions(
            onClear = onClear,
            onApply = onApply,
            actionText = actionText
        )
    }
}

@Composable
private fun LanguageOptionCard(
    option: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = when (option.id) {
        "English" -> Color(0xFF473CB4)
        "Tamil" -> Color(0xFF5DB8AE)
        "Hindi" -> Color(0xFFB23BA1)
        else -> Color(0xFF8B31A1)
    }
    val backgroundColor = if (isSelected) selectedColor else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF3A3A3A)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.4.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = option.title,
            color = textColor,
            fontSize = 12.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = option.subtitle,
            color = textColor.copy(alpha = 0.86f),
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun VibeOptionCard(
    option: VibeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFEFA0CF) else Color.White)
            .border(1.4.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp)
    ) {
        Image(
            painter = painterResource(id = option.iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = option.label,
            color = Color(0xFF343434),
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun FilterSheetActions(
    onClear: () -> Unit,
    onApply: () -> Unit,
    actionText: String = "Apply"
) {
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
                .background(Color(0xFF8D2EA0))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onApply
                )
        ) {
            Text(
                text = actionText,
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeCardStack(
    onCallRequested: (HomeProfile) -> Unit,
    onCallDragProgress: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeIndex by remember { mutableStateOf(0) }
    var activeDragMode by remember { mutableStateOf(HomeDragMode.Undecided) }
    val wheelProgress = remember { Animatable(0f) }
    val pullDownOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 96.dp.toPx() }
    val callThresholdPx = with(density) { 108.dp.toPx() }
    val progress = wheelProgress.value
    val callProgress = (pullDownOffset.value / callThresholdPx).coerceIn(0f, 1f)
    onCallDragProgress(callProgress)
    val previousProfile = HomeProfiles[(activeIndex - 1 + HomeProfiles.size) % HomeProfiles.size]
    val currentProfile = HomeProfiles[activeIndex]
    val nextProfile = HomeProfiles[(activeIndex + 1) % HomeProfiles.size]
    val farPreviousProfile = HomeProfiles[(activeIndex - 2 + HomeProfiles.size) % HomeProfiles.size]
    val farNextProfile = HomeProfiles[(activeIndex + 2) % HomeProfiles.size]

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CallRevealSemiCircle(
            progress = callProgress,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        CallDragPhoneIcon(
            progress = callProgress,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 28.dp)
        )

        if (progress < 0f) {
            CarouselCard(
                profile = currentProfile,
                transform = interpolateTransform(CarouselCenterSlot, CarouselLeftSlot, -progress)
            )
            CarouselCard(
                profile = nextProfile,
                transform = interpolateTransform(CarouselRightSlot, CarouselCenterSlot, -progress)
            )
            CarouselCard(
                profile = farNextProfile,
                transform = interpolateTransform(CarouselFarRightSlot, CarouselRightSlot, -progress)
            )
        } else if (progress > 0f) {
            CarouselCard(
                profile = farPreviousProfile,
                transform = interpolateTransform(CarouselFarLeftSlot, CarouselLeftSlot, progress)
            )
            CarouselCard(
                profile = previousProfile,
                transform = interpolateTransform(CarouselLeftSlot, CarouselCenterSlot, progress)
            )
            CarouselCard(
                profile = currentProfile,
                transform = interpolateTransform(CarouselCenterSlot, CarouselRightSlot, progress)
            )
        } else {
            CarouselCard(profile = previousProfile, transform = CarouselLeftSlot)
            CarouselCard(profile = nextProfile, transform = CarouselRightSlot)
            CarouselCard(
                profile = currentProfile,
                transform = CarouselCenterSlot.copy(
                    y = CarouselCenterSlot.y + with(density) { pullDownOffset.value.toDp().value }
                )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 236.dp, height = 332.dp)
                .pointerInput(activeIndex) {
                    detectDragGestures(
                        onDragStart = {
                            activeDragMode = HomeDragMode.Undecided
                            scope.launch {
                                wheelProgress.stop()
                                pullDownOffset.stop()
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                if (activeDragMode == HomeDragMode.Undecided) {
                                    activeDragMode = if (abs(dragAmount.x) > abs(dragAmount.y) * 1.12f) {
                                        HomeDragMode.Horizontal
                                    } else if (abs(dragAmount.y) > abs(dragAmount.x) * 0.82f) {
                                        HomeDragMode.Vertical
                                    } else {
                                        HomeDragMode.Undecided
                                    }
                                }

                                if (activeDragMode == HomeDragMode.Horizontal) {
                                    val nextValue = (wheelProgress.value + ((dragAmount.x / swipeThresholdPx) * 0.86f)).coerceIn(-1f, 1f)
                                    wheelProgress.snapTo(nextValue)
                                    pullDownOffset.snapTo((pullDownOffset.value * 0.92f).coerceAtLeast(0f))
                                } else if (activeDragMode == HomeDragMode.Vertical) {
                                    val nextPull = (pullDownOffset.value + (dragAmount.y * 0.9f)).coerceAtLeast(0f)
                                    pullDownOffset.snapTo(nextPull)
                                    wheelProgress.snapTo(wheelProgress.value * 0.9f)
                                }
                            }
                        },
                        onDragEnd = {
                            val currentProgress = wheelProgress.value
                            val currentPull = pullDownOffset.value
                            scope.launch {
                                when {
                                    currentPull > callThresholdPx && abs(currentProgress) < 0.28f -> {
                                        activeDragMode = HomeDragMode.Undecided
                                        onCallDragProgress(0f)
                                        onCallRequested(currentProfile)
                                        pullDownOffset.animateTo(
                                            0f,
                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f)
                                        )
                                    }

                                    currentProgress <= -0.42f -> {
                                        activeDragMode = HomeDragMode.Undecided
                                        wheelProgress.animateTo(
                                            -1f,
                                            animationSpec = spring(dampingRatio = 0.86f, stiffness = 240f)
                                        )
                                        activeIndex = (activeIndex + 1) % HomeProfiles.size
                                        wheelProgress.snapTo(0f)
                                        pullDownOffset.snapTo(0f)
                                        onCallDragProgress(0f)
                                    }

                                    currentProgress >= 0.42f -> {
                                        activeDragMode = HomeDragMode.Undecided
                                        wheelProgress.animateTo(
                                            1f,
                                            animationSpec = spring(dampingRatio = 0.86f, stiffness = 240f)
                                        )
                                        activeIndex = (activeIndex - 1 + HomeProfiles.size) % HomeProfiles.size
                                        wheelProgress.snapTo(0f)
                                        pullDownOffset.snapTo(0f)
                                        onCallDragProgress(0f)
                                    }

                                    else -> {
                                        activeDragMode = HomeDragMode.Undecided
                                        wheelProgress.animateTo(
                                            0f,
                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 320f)
                                        )
                                        pullDownOffset.animateTo(
                                            0f,
                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 320f)
                                        )
                                        onCallDragProgress(0f)
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            activeDragMode = HomeDragMode.Undecided
                            scope.launch {
                                wheelProgress.animateTo(
                                    0f,
                                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 320f)
                                )
                                pullDownOffset.animateTo(
                                    0f,
                                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 320f)
                                )
                                onCallDragProgress(0f)
                            }
                        }
                    )
                }
        )

        SwipeHint(
            progress = callProgress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
                .padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun CarouselCard(
    profile: HomeProfile,
    transform: CarouselSlotTransform
) {
    val density = LocalDensity.current
    MainProfileCard(
        profile = profile,
        modifier = Modifier.graphicsLayer {
            translationX = with(density) { transform.x.dp.toPx() }
            translationY = with(density) { transform.y.dp.toPx() }
            scaleX = transform.scale
            scaleY = transform.scale
            rotationZ = transform.rotation
            alpha = transform.alpha
        }
    )
}

@Composable
private fun CallDragPhoneIcon(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val revealProgress = ((progress - 0.04f) / 0.7f).coerceIn(0f, 1f)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                alpha = revealProgress
                scaleX = 0.82f + (revealProgress * 0.18f)
                scaleY = 0.82f + (revealProgress * 0.18f)
                translationY = (1f - revealProgress) * 20f
            }
            .size(82.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 6.dp)
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 18.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .rotate(-42f)
                        .background(Color.White.copy(alpha = 0.42f))
                )
            }
            Spacer(modifier = Modifier.width(42.dp))
            repeat(2) {
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 18.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .rotate(42f)
                        .background(Color.White.copy(alpha = 0.42f))
                )
            }
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .offset(x = 2.dp, y = 3.dp)
                .clip(CircleShape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFFCC02E))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawArc(
                    color = Color.White,
                    startAngle = 24f,
                    sweepAngle = 78f,
                    useCenter = true
                )
            }
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(27.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun CallRevealSemiCircle(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val revealProgress = ((progress - 0.08f) / 0.92f).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(124.dp)
            .graphicsLayer {
                alpha = revealProgress
                translationY = (1f - revealProgress) * 60f
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .size(width = 388.dp, height = 124.dp)
                .clip(RoundedCornerShape(topStart = 220.dp, topEnd = 220.dp))
                .background(Color(0xFFFCC02E))
        )
    }
}

private data class CarouselSlotTransform(
    val x: Float,
    val y: Float,
    val scale: Float,
    val rotation: Float,
    val alpha: Float = 1f
)

private val CarouselFarLeftSlot = CarouselSlotTransform(
    x = -344f,
    y = 12f,
    scale = 0.78f,
    rotation = 16f,
    alpha = 0.9f
)

private val CarouselLeftSlot = CarouselSlotTransform(
    x = -256f,
    y = -56f,
    scale = 0.9f,
    rotation = 12f
)

private val CarouselCenterSlot = CarouselSlotTransform(
    x = 0f,
    y = -20f,
    scale = 1f,
    rotation = 0f
)

private val CarouselRightSlot = CarouselSlotTransform(
    x = 256f,
    y = -56f,
    scale = 0.9f,
    rotation = -12f
)

private val CarouselFarRightSlot = CarouselSlotTransform(
    x = 344f,
    y = 12f,
    scale = 0.78f,
    rotation = -16f,
    alpha = 0.9f
)

private fun interpolateTransform(
    start: CarouselSlotTransform,
    end: CarouselSlotTransform,
    progress: Float
): CarouselSlotTransform {
    val clamped = progress.coerceIn(0f, 1f)
    return CarouselSlotTransform(
        x = lerpFloat(start.x, end.x, clamped),
        y = lerpFloat(start.y, end.y, clamped),
        scale = lerpFloat(start.scale, end.scale, clamped),
        rotation = lerpFloat(start.rotation, end.rotation, clamped),
        alpha = lerpFloat(start.alpha, end.alpha, clamped)
    )
}

private fun lerpFloat(
    start: Float,
    stop: Float,
    progress: Float
): Float = start + ((stop - start) * progress)


@Composable
private fun MainProfileCard(
    profile: HomeProfile,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(width = 216.dp, height = 280.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        )
        Column(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp)
                    .background(profile.headerColor)
            ) {
                Image(
                    painter = painterResource(id = profile.avatarRes),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(104.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF21C251))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = profile.name,
                color = Color(0xFF202020),
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                profile.languages.forEach { language ->
                    LanguageChip(
                        text = language.text,
                        background = language.background,
                        textColor = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                profile.tags.forEach { tag ->
                    TagChip(text = tag.text, accent = tag.accent, modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFFDF6E6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.prompt,
                    color = Color(0xFF212121),
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun LanguageChip(
    text: String,
    background: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(66.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TagChip(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, accent, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = accent,
            fontSize = 10.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SwipeHint(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = 1f - (progress * 0.9f)
                translationY = progress * 18f
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.arrow_down_double),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Swipe down to call",
            color = Color(0xFFB489C6),
            fontSize = 15.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun HomeBottomBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(99.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(88.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color.White)
        )
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(88.dp)
        ) {
            HomeTab.entries.forEach { tab ->
                HomeBottomBarItem(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun HomeBottomBarItem(
    tab: HomeTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bubbleColor by animateColorAsState(
        targetValue = if (isSelected) tab.tint.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = spring(stiffness = 380f),
        label = "bubbleColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
        label = "iconScale"
    )
    val circleSize by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 40.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "circleSize"
    )
    val itemTopOffset by animateDpAsState(
        targetValue = if (isSelected) (-11).dp else 12.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "itemTopOffset"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color(0xFF7A7A7A),
        label = "labelColor"
    )

    Box(
        modifier = Modifier
            .size(width = 74.dp, height = 99.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = itemTopOffset)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(if (isSelected) bubbleColor else Color.Transparent)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isSelected) 46.dp else 40.dp)
                        .clip(CircleShape)
                        .background(tab.tint)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = Color.White,
                        modifier = Modifier
                            .size(if (isSelected) 22.dp else 21.dp)
                            .scale(iconScale)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tab.label,
                color = labelColor,
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

private enum class HomeTab(
    val label: String,
    val icon: ImageVector,
    val tint: Color
) {
    Connect("Connect", Icons.Default.Phone, Color(0xFFF5BE2E)),
    Games("Games", Icons.Default.SportsEsports, Color(0xFF8D32F7)),
    Chat("Chat", Icons.Default.ChatBubbleOutline, Color(0xFF196DFF)),
    History("History", Icons.Default.History, Color(0xFFFF9518))
}

private data class HomeProfile(
    val name: String,
    val avatarRes: Int,
    val headerColor: Color,
    val languages: List<HomeProfileLanguage>,
    val tags: List<HomeProfileTag>,
    val prompt: String
)

private data class HomeProfileLanguage(
    val text: String,
    val background: Color
)

private data class HomeProfileTag(
    val text: String,
    val accent: Color
)

private enum class HomeFilterSheet {
    Language,
    Vibe
}

private enum class HomeDragMode {
    Undecided,
    Horizontal,
    Vertical
}

private data class LanguageOption(
    val id: String,
    val title: String,
    val subtitle: String
)

private data class VibeOption(
    val id: String,
    val iconRes: Int,
    val label: String
)

private fun Set<String>.toggleValue(value: String): Set<String> {
    return if (contains(value)) {
        this - value
    } else {
        this + value
    }
}

private val LanguageOptions = listOf(
    LanguageOption("English", "English", "English"),
    LanguageOption("Malayalam", "Malayalam", "മലയാളം"),
    LanguageOption("Tamil", "Tamil", "தமிழ்"),
    LanguageOption("Hindi", "Hindi", "हिन्दी"),
    LanguageOption("Marathi", "Marathi", "मराठी"),
    LanguageOption("Punjabi", "Punjabi", "ਪੰਜਾਬੀ"),
    LanguageOption("Bengali", "Bengali", "বাংলা"),
    LanguageOption("Kannada", "Kannada", "ಕನ್ನಡ"),
    LanguageOption("Gujarati", "Gujarati", "ગુજરાતી"),
    LanguageOption("Telugu", "Telugu", "తెలుగు"),
    LanguageOption("Urdu", "Urdu", "اردو"),
    LanguageOption("Odia", "Odia", "ଓଡ଼ିଆ")
)

private val VibeOptions = listOf(
    VibeOption("Friends", R.drawable.profile_screen_friend_sqaud, "Friends"),
    VibeOption("Dating", R.drawable.vibe_dating, "Dating"),
    VibeOption("Advice", R.drawable.vibe_advice, "Advice"),
    VibeOption("Late Night", R.drawable.vibe_late_night, "Late Night"),
    VibeOption("Breakup", R.drawable.vibe_breakup, "Breakup"),
    VibeOption("Deep Talks", R.drawable.vibe_deep_talk, "Deep Talks"),
    VibeOption("Movies", R.drawable.vibe_movie, "Movies"),
    VibeOption("Timepass", R.drawable.vibe_timepass, "Timepass"),
    VibeOption("Gaming", R.drawable.vibe_gaming, "Gaming"),
    VibeOption("Astrology", R.drawable.vibe_astrology, "Astrology"),
    VibeOption("Gossip", R.drawable.vibe_gossip, "Gossip"),
    VibeOption("Antakshari", R.drawable.vibe_antakshari, "Antakshari")
)

private val HomeProfiles = listOf(
    HomeProfile(
        name = "Anshu",
        avatarRes = R.drawable.home_screen_avatar,
        headerColor = Color(0xFFFCC02E),
        languages = listOf(
            HomeProfileLanguage("அ Tamil", Color(0xFF38AFA4)),
            HomeProfileLanguage("अ Hindi", Color(0xFFB53CA6))
        ),
        tags = listOf(
            HomeProfileTag("# Frndship", Color(0xFFFF625A)),
            HomeProfileTag("# Game", Color(0xFF7E45FF)),
            HomeProfileTag("# Foodie", Color(0xFFCC63FF))
        ),
        prompt = "\"Tell me the best thing that\nhappened today.\""
    ),
    HomeProfile(
        name = "Mira",
        avatarRes = R.drawable.women_avatar3,
        headerColor = Color(0xFF79B6FF),
        languages = listOf(
            HomeProfileLanguage("English", Color(0xFF38AFA4)),
            HomeProfileLanguage("ह Hindi", Color(0xFFB53CA6))
        ),
        tags = listOf(
            HomeProfileTag("# Music", Color(0xFFFF625A)),
            HomeProfileTag("# Chat", Color(0xFF7E45FF)),
            HomeProfileTag("# Movies", Color(0xFFCC63FF))
        ),
        prompt = "\"What song has been stuck\nin your head lately?\""
    ),
    HomeProfile(
        name = "Kabir",
        avatarRes = R.drawable.man_avatar4,
        headerColor = Color(0xFFCEFB42),
        languages = listOf(
            HomeProfileLanguage("English", Color(0xFF38AFA4)),
            HomeProfileLanguage("മ Malayalam", Color(0xFFB53CA6))
        ),
        tags = listOf(
            HomeProfileTag("# Advice", Color(0xFFFF625A)),
            HomeProfileTag("# Gaming", Color(0xFF7E45FF)),
            HomeProfileTag("# Travel", Color(0xFFCC63FF))
        ),
        prompt = "\"What is one tiny win from\nyour week?\""
    ),
    HomeProfile(
        name = "Tara",
        avatarRes = R.drawable.women_avatar7,
        headerColor = Color(0xFFFF9BD1),
        languages = listOf(
            HomeProfileLanguage("ಕ Kannada", Color(0xFF38AFA4)),
            HomeProfileLanguage("English", Color(0xFFB53CA6))
        ),
        tags = listOf(
            HomeProfileTag("# Deep", Color(0xFFFF625A)),
            HomeProfileTag("# Books", Color(0xFF7E45FF)),
            HomeProfileTag("# Foodie", Color(0xFFCC63FF))
        ),
        prompt = "\"If today had a title,\nwhat would it be?\""
    )
)

private val HomePurple = Color(0xFF9933A9)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeScreenPreview() {
    BffAndroidTheme {
        HomeScreen()
    }
}
