package com.gobff.getfriends.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.component.BffHeartChip
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.FreedokaFontFamily
import com.gobff.getfriends.ui.theme.GaretFontFamily

@Composable
fun LiveScreen(
    modifier: Modifier = Modifier,
    walletHearts: Int = 0,
    hasNotificationAccess: Boolean = false,
    onNotificationAccessRequested: (onAccessReady: () -> Unit) -> Unit = {},
    onRechargeRequested: () -> Unit = {}
) {
    var notifyEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(hasNotificationAccess) {
        if (!hasNotificationAccess) {
            notifyEnabled = false
        }
    }

    fun toggleNotification() {
        if (notifyEnabled) {
            notifyEnabled = false
            return
        }

        if (hasNotificationAccess) {
            notifyEnabled = true
        } else {
            onNotificationAccessRequested {
                notifyEnabled = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        LiveTopBar(
            walletHearts = walletHearts,
            onRechargeRequested = onRechargeRequested
        )
        Spacer(modifier = Modifier.height(40.dp))
        LiveTitleArea()
        Spacer(modifier = Modifier.height(18.dp))
        LiveEmptyCard(
            notifyEnabled = notifyEnabled,
            onNotifyToggle = ::toggleNotification,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LiveTopBar(
    walletHearts: Int,
    onRechargeRequested: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        BffHeartChip(
            hearts = walletHearts,
            onClick = onRechargeRequested,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 48.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.game_screen_question),
            contentDescription = "Help",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
                .size(36.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        )
    }
}

@Composable
private fun LiveTitleArea() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gift_vibe_sparkle),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIVE HANGOUT",
                    color = Color(0xFF5369C8),
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    fontFamily = FreedokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.gift_vibe_sparkle),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Find your perfect vibe",
                color = Color(0xFF3D3D3D),
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LiveEmptyCard(
    notifyEnabled: Boolean,
    onNotifyToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 610.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFFFD8663))
    ) {
        Image(
            painter = painterResource(id = R.drawable.live_screen_bg_object),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .padding(top = 48.dp, bottom = 42.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.no_host_connect_screen),
                contentDescription = null,
                modifier = Modifier.size(width = 258.dp, height = 220.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "No friends are live right now",
                color = Color.Black,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "There are no live or upcoming sessions\nat the moment.",
                color = Color.Black,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(50.dp))
            LiveNotifyCard(
                enabled = notifyEnabled,
                onToggle = onNotifyToggle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LiveNotifyCard(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(72.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.48f), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF5369C8))
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = "We'll notify you know\nwhen a friend is available",
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        LiveNotifyToggle(
            enabled = enabled,
            onToggle = onToggle
        )
    }
}

@Composable
private fun LiveNotifyToggle(
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val trackColor by animateColorAsState(
        targetValue = if (enabled) Color(0xFF02C96B) else Color(0x00000000),
        label = "liveNotifyTrackColor"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (enabled) 28.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 360f),
        label = "liveNotifyThumbOffset"
    )

    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 32.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(trackColor)
            .border(1.4.dp, Color.White, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbOffset)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LiveScreenPreview() {
    BffAndroidTheme {
        LiveScreen(
            walletHearts = 3230,
            hasNotificationAccess = true
        )
    }
}
