package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.component.HandDrawnCardShape
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import kotlinx.coroutines.delay

private val OnlineFlowYellow = Color(0xFFF6B51C)
private val OnlineFlowCardBackground = Color(0x1AFFFFFF)

@Composable
fun OnlineFlowScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onCompleted: () -> Unit = {}
) {
    var step by remember { mutableIntStateOf(1) }
    var showWaitingScreen by remember { mutableStateOf(false) }

    BackHandler {
        if (showWaitingScreen || step == 1) {
            onBack()
        } else {
            step = 1
        }
    }

    if (showWaitingScreen) {
        OnlineWaitingScreen(
            onBack = onBack,
            modifier = modifier
        )
        return
    }

    OnlineFlowStep(
        step = step,
        onBack = {
            if (step == 1) {
                onBack()
            } else {
                step = 1
            }
        },
        onPrimaryClick = {
            if (step == 1) {
                step = 2
            } else {
                onCompleted()
                showWaitingScreen = true
            }
        },
        modifier = modifier
    )
}

@Composable
private fun OnlineFlowStep(
    step: Int,
    onBack: () -> Unit,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFirstStep = step == 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFC23A), OnlineFlowYellow)
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.call_screen_bg_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(start = 20.dp, top = 48.dp, end = 20.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
            )
            Text(
                text = "Step $step of 2",
                color = Color.Black,
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(28.dp))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 122.dp)
                .padding(horizontal = 28.dp)
        ) {
            Text(
                text = if (isFirstStep) "You're Online" else "Safety is always one tap away.",
                color = Color.Black,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isFirstStep) {
                    "Stay online to get\ncall requests from friends."
                } else {
                    "Tap the shield to report or block a user."
                },
                color = Color.Black,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        Image(
            painter = painterResource(
                id = if (isFirstStep) R.drawable.online_flow_icon1 else R.drawable.online_flow_icon2
            ),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = if (isFirstStep) 16.dp else 24.dp)
                .size(
                    width = if (isFirstStep) 270.dp else 238.dp,
                    height = if (isFirstStep) 250.dp else 424.dp
                ),
            contentScale = ContentScale.Fit
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .padding(bottom = 34.dp)
        ) {
            OnlineFlowButton(
                text = if (isFirstStep) "Let's Go" else "Got it",
                onClick = onPrimaryClick
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "100% Safe and Private",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OnlineFlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = HandDrawnCardShape
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 4.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.6.dp, Color.Black, shape)
                .padding(start = 24.dp, end = 18.dp)
        ) {
            Text(
                text = text,
                color = Color(0xFF242424),
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun OnlineWaitingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSafetySheet by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(true) }
    var speakerOn by remember { mutableStateOf(false) }
    var cardIndex by remember { mutableIntStateOf(0) }
    val cards = remember { OnlineWaitingCards }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2_200)
            cardIndex = (cardIndex + 1) % cards.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFC23A), OnlineFlowYellow)
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.call_screen_bg_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 48.dp)
                .size(28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
        )

        OnlineSafetyButton(
            onClick = { showSafetySheet = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 118.dp)
                .padding(horizontal = 28.dp)
        ) {
            Text(
                text = "Looking for your next BFF...",
                color = Color.Black,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Someone will join in just a moment.",
                color = Color(0xFF7B5215),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        OnlineRadar(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-46).dp)
                .size(348.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .padding(bottom = 38.dp)
        ) {
            OnlineInfoCard(
                item = cards[cardIndex],
                selectedIndex = cardIndex,
                total = cards.size
            )
            Spacer(modifier = Modifier.height(30.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(58.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OnlineCircleControl(
                    active = muted,
                    iconRes = if (muted) R.drawable.mic_off else R.drawable.mic_on,
                    label = "Mute",
                    onClick = { muted = !muted }
                )
                OnlineCircleControl(
                    active = !speakerOn,
                    iconRes = if (speakerOn) R.drawable.speaker_on else R.drawable.speaker_off,
                    label = if (speakerOn) "Speaker on" else "Speaker off",
                    onClick = { speakerOn = !speakerOn }
                )
            }
        }

        if (showSafetySheet) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showSafetySheet = false }
                    )
            )
            OnlineSafetyBottomSheet(
                onReportClick = { showSafetySheet = false },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun OnlineRadar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "onlineRadar")
    val sweepRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "onlineRadarSweep"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = size.minDimension / 2f
            val center = center
            listOf(0.29f, 0.64f, 0.98f).forEach { scale ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.62f),
                    radius = radius * scale,
                    center = center,
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }
            rotate(degrees = sweepRotation, pivot = center) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, Color.White.copy(alpha = 0.04f))
                    ),
                    start = center,
                    end = androidx.compose.ui.geometry.Offset(size.width, center.y),
                    strokeWidth = 4.dp.toPx()
                )
            }
        }

        RadarAvatar(
            avatarRes = R.drawable.women_avatar1,
            label = "You",
            modifier = Modifier.align(Alignment.Center)
        )
        SmallRadarAvatar(
            avatarRes = R.drawable.women_avatar_2,
            alpha = 0.7f,
            modifier = Modifier.offset(x = (-96).dp, y = (-114).dp)
        )
        SmallRadarAvatar(
            avatarRes = R.drawable.man_avatar_2,
            alpha = 0.45f,
            modifier = Modifier.offset(x = 96.dp, y = (-92).dp)
        )
        SmallRadarAvatar(
            avatarRes = R.drawable.man_avatar_2,
            alpha = 0.7f,
            modifier = Modifier.offset(x = (-132).dp, y = 8.dp)
        )
        SmallRadarAvatar(
            avatarRes = R.drawable.women_avatar_2,
            alpha = 0.85f,
            modifier = Modifier.offset(x = 96.dp, y = 96.dp)
        )
        SmallRadarAvatar(
            avatarRes = R.drawable.man_avatar1,
            alpha = 0.5f,
            modifier = Modifier.offset(x = (-74).dp, y = 116.dp)
        )
    }
}

@Composable
private fun RadarAvatar(
    avatarRes: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Image(
            painter = painterResource(id = avatarRes),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .offset(y = (-8).dp)
                .clip(HandDrawnCardShape)
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 3.dp)
        ) {
            Text(
                text = label,
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SmallRadarAvatar(
    avatarRes: Int,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = avatarRes),
        contentDescription = null,
        modifier = modifier
            .alpha(alpha)
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun OnlineInfoCard(
    item: OnlineWaitingCard,
    selectedIndex: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OnlineFlowCardBackground)
            .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = item.title,
                color = Color.Black,
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.body,
                color = Color.Black,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(total) { index ->
                    val selected = index == selectedIndex
                    val dotWidth by animateDpAsState(
                        targetValue = if (selected) 24.dp else 7.dp,
                        animationSpec = tween(durationMillis = 320),
                        label = "onlineWaitingDotWidth"
                    )
                    val dotAlpha by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (selected) 1f else 0.62f,
                        animationSpec = tween(durationMillis = 320),
                        label = "onlineWaitingDotAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(width = dotWidth, height = 7.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = dotAlpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun OnlineCircleControl(
    active: Boolean,
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        val background = if (active) Color(0xFF7B2BE8) else Color.White
        val iconTint = if (active) Color.White else Color.Black
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(background)
                .border(1.4.dp, Color.Black, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            color = Color.Black,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OnlineSafetyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
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
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private data class OnlineWaitingCard(
    val title: String,
    val body: String
)

private val OnlineWaitingCards = listOf(
    OnlineWaitingCard("How to earn ?", "Earn Coins for every minute you stay\nconnected."),
    OnlineWaitingCard("Earn Gifts", "Hosts may receive virtual gifts from people\nthey enjoy talking to."),
    OnlineWaitingCard("Stay online to get more calls", "Keeping your status online helps you\nreceive more call requests."),
    OnlineWaitingCard("Your privacy comes first", "Your phone number and personal details\nare never shared."),
    OnlineWaitingCard("Don't feel like waiting?", "Play games, join Live, or hop into a Chat\nRoom anytime.")
)

private data class OnlineSafetyItem(
    val title: String,
    val body: String,
    val iconRes: Int,
    val iconBackground: Color
)

private val OnlineSafetyItems = listOf(
    OnlineSafetyItem(
        title = "Keep your info private",
        body = "Never share personal details like your address, phone number, email, or financial information.",
        iconRes = R.drawable.call_screen_lock,
        iconBackground = Color(0xFFF1EDFC)
    ),
    OnlineSafetyItem(
        title = "Be respectful",
        body = "Treat others with kindness and respect. Harassment or abusive behavior is not allowed.",
        iconRes = R.drawable.call_screen_account,
        iconBackground = Color(0xFFD0EEFE)
    ),
    OnlineSafetyItem(
        title = "Report & block",
        body = "If someone makes you uncomfortable, you can report or block them anytime.",
        iconRes = R.drawable.call_screen_report,
        iconBackground = Color(0xFFFEC5C0)
    ),
    OnlineSafetyItem(
        title = "Stay within the app",
        body = "For your safety, keep conversations and payments within the app.",
        iconRes = R.drawable.call_screen_money,
        iconBackground = Color(0xFFFDECC2)
    ),
    OnlineSafetyItem(
        title = "We're here to help",
        body = "Our team is available 24/7. Reach out if you need any assistance.",
        iconRes = R.drawable.call_screen_support,
        iconBackground = Color(0xFFFCE2EC)
    )
)

@Composable
private fun OnlineSafetyBottomSheet(
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
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 18.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            OnlineSafetyItems.forEach { item ->
                OnlineSafetyInfoRow(item = item)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OnlineSafetyReportButton(
            onClick = onReportClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(38.dp))
    }
}

@Composable
private fun OnlineSafetyInfoRow(
    item: OnlineSafetyItem,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
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
private fun OnlineSafetyReportButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
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

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OnlineFlowScreenPreview() {
    BffAndroidTheme {
        OnlineFlowScreen()
//        OnlineWaitingScreen(
//            onBack = {}
//        )
    }
}
