package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily

private val IncomingCallYellow = Color(0xFFFFB91D)

@Composable
fun IncomingCallScreen(
    callerName: String,
    callerAvatarUrl: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDecline)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IncomingCallYellow)
    ) {
        Image(
            painter = painterResource(id = R.drawable.incoming_call_bg_vector),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        IncomingCallHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 128.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 34.dp)
        ) {
            PulsingCallerAvatar(avatarRes = callerAvatarUrl.toIncomingAvatarRes())
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = callerName,
                color = Color.Black,
                fontSize = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Calling....",
                color = Color(0xFF8F6A16),
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(44.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            IncomingCallActionButton(
                label = "Decline",
                onClick = onDecline,
                isAccept = false
            )
            IncomingCallActionButton(
                label = "Accept",
                onClick = onAccept,
                isAccept = true
            )
        }
    }
}

@Composable
fun DialingCallScreen(
    callerName: String,
    callerAvatarUrl: String?,
    statusText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onCancel)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IncomingCallYellow)
    ) {
        Image(
            painter = painterResource(id = R.drawable.incoming_call_bg_vector),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        IncomingCallHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 128.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 34.dp)
        ) {
            PulsingCallerAvatar(avatarRes = callerAvatarUrl.toIncomingAvatarRes())
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = callerName,
                color = Color.Black,
                fontSize = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = statusText,
                color = Color.Black.copy(alpha = 0.42f),
                fontSize = 15.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        IncomingCallActionButton(
            label = "Cancel",
            onClick = onCancel,
            isAccept = false,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun IncomingCallHeader(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "Audio call",
            color = Color.Black,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.72f))
                .padding(horizontal = 22.dp, vertical = 9.dp)
        ) {
            Text(
                text = "You will earn",
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(6.dp))
            Image(
                painter = painterResource(id = R.drawable.call_reward_heart),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "1 / min",
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PulsingCallerAvatar(
    avatarRes: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "incoming-call-pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_050),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring-scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.58f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_050),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring-alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(148.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = ringScale
                    scaleY = ringScale
                    alpha = ringAlpha
                }
                .clip(CircleShape)
                .border(3.dp, Color.White, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .border(3.dp, Color.White, CircleShape)
                .background(Color.White.copy(alpha = 0.22f))
                .padding(5.dp)
        ) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF66B7FF))
            )
        }
    }
}

@Composable
private fun IncomingCallActionButton(
    label: String,
    onClick: () -> Unit,
    isAccept: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(bottom = 52.dp)
    ) {
        Image(
            painter = painterResource(
                id = if (isAccept) R.drawable.call_pickup else R.drawable.call_reject
            ),
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(width = 100.dp, height = 56.dp)
                .clickable(onClick = onClick)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            color = Color.Black,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun String?.toIncomingAvatarRes(): Int =
    when (this) {
        "man_avatar1" -> R.drawable.man_avatar1
        "man_avatar2" -> R.drawable.man_avatar1
        "man_avatar3" -> R.drawable.man_avatar1
        "man_avatar4" -> R.drawable.man_avatar1
        "man_avatar5" -> R.drawable.man_avatar1
        "man_avatar6" -> R.drawable.man_avatar1
        "man_avatar7" -> R.drawable.man_avatar1
        "man_avatar8" -> R.drawable.man_avatar1
        "man_avatar9" -> R.drawable.man_avatar1
        "man_avatar10" -> R.drawable.man_avatar1
        "man_avatar11" -> R.drawable.man_avatar1
        "man_avatar12" -> R.drawable.man_avatar1
        "women_avatar1" -> R.drawable.women_avatar1
        "women_avatar2" -> R.drawable.women_avatar1
        "women_avatar3" -> R.drawable.women_avatar1
        "women_avatar4" -> R.drawable.women_avatar1
        "women_avatar5" -> R.drawable.women_avatar1
        "women_avatar6" -> R.drawable.women_avatar1
        "women_avatar7" -> R.drawable.women_avatar1
        "women_avatar8" -> R.drawable.women_avatar1
        "women_avatar9" -> R.drawable.women_avatar1
        "women_avatar10" -> R.drawable.women_avatar1
        "women_avatar11" -> R.drawable.women_avatar1
        "women_avatar12" -> R.drawable.women_avatar1
        else -> R.drawable.man_avatar1
    }

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun IncomingCallScreenPreview() {
    BffAndroidTheme {
        IncomingCallScreen(
            callerName = "Sujal",
            callerAvatarUrl = "man_avatar1",
            onAccept = {},
            onDecline = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun DialingCallScreenPreview() {
    BffAndroidTheme {
        DialingCallScreen(
            callerName = "Laser",
            callerAvatarUrl = "man_avatar1",
            statusText = "Connecting...",
            onCancel = {}
        )
    }
}
