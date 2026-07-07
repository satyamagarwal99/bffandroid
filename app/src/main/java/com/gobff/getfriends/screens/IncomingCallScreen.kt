package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import kotlin.math.roundToInt

private val IncomingCallYellow = Color(0xFFFFB91D)
private const val ACCEPT_SWIPE_THRESHOLD = -96f

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
            .padding(horizontal = 28.dp, vertical = 46.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-78).dp)
        ) {
            PulsingCallerAvatar(avatarRes = callerAvatarUrl.toIncomingAvatarRes())
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = callerName,
                color = Color.Black,
                fontSize = 25.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Incoming BFF call",
                color = Color.Black.copy(alpha = 0.42f),
                fontSize = 15.sp,
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
                color = Color(0xFFFF4C58),
                onClick = onDecline,
                isAccept = false
            )
            IncomingCallActionButton(
                label = "Accept",
                color = Color(0xFF25C76A),
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
            .padding(horizontal = 28.dp, vertical = 46.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-68).dp)
        ) {
            PulsingCallerAvatar(avatarRes = callerAvatarUrl.toIncomingAvatarRes())
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = callerName,
                color = Color.Black,
                fontSize = 25.sp,
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
            color = Color(0xFFFF4C58),
            onClick = onCancel,
            isAccept = false,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
    color: Color,
    onClick: () -> Unit,
    isAccept: Boolean,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val settledOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = tween(180),
        label = "accept-offset"
    )
    val arrowTransition = rememberInfiniteTransition(label = "accept-arrow")
    val arrowOffset by arrowTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 780),
            repeatMode = RepeatMode.Restart
        ),
        label = "arrow-offset"
    )
    val arrowAlpha by arrowTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 780),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow-alpha"
    )
    val pulseScale by arrowTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.34f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 980),
            repeatMode = RepeatMode.Restart
        ),
        label = "button-pulse-scale"
    )
    val pulseAlpha by arrowTransition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 980),
            repeatMode = RepeatMode.Restart
        ),
        label = "button-pulse-alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset = (dragOffset + dragAmount).coerceIn(-128f, 0f)
                    },
                    onDragEnd = {
                        if (dragOffset <= ACCEPT_SWIPE_THRESHOLD) {
                            onClick()
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f }
                )
            }
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = Color.White.copy(alpha = arrowAlpha),
            modifier = Modifier.offset(y = arrowOffset.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset { IntOffset(0, settledOffset.roundToInt()) }
                .size(68.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
                    .clip(CircleShape)
                    .background(color)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, Color.Black.copy(alpha = 0.42f), CircleShape)
            )
            Icon(
                imageVector = if (isAccept) Icons.Default.Call else Icons.Default.CallEnd,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            color = Color.Black,
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun String?.toIncomingAvatarRes(): Int =
    when (this) {
        "man_avatar1" -> R.drawable.man_avatar1
        "man_avatar2" -> R.drawable.man_avatar2
        "man_avatar3" -> R.drawable.man_avatar3
        "man_avatar4" -> R.drawable.man_avatar4
        "man_avatar5" -> R.drawable.man_avatar5
        "man_avatar6" -> R.drawable.man_avatar6
        "man_avatar7" -> R.drawable.man_avatar7
        "man_avatar8" -> R.drawable.man_avatar8
        "man_avatar9" -> R.drawable.man_avatar9
        "man_avatar10" -> R.drawable.man_avatar10
        "man_avatar11" -> R.drawable.man_avatar11
        "man_avatar12" -> R.drawable.man_avatar12
        "women_avatar1" -> R.drawable.women_avatar1
        "women_avatar2" -> R.drawable.women_avatar2
        "women_avatar3" -> R.drawable.women_avatar3
        "women_avatar4" -> R.drawable.women_avatar4
        "women_avatar5" -> R.drawable.women_avatar5
        "women_avatar6" -> R.drawable.women_avatar6
        "women_avatar7" -> R.drawable.women_avatar7
        "women_avatar8" -> R.drawable.women_avatar8
        "women_avatar9" -> R.drawable.women_avatar9
        "women_avatar10" -> R.drawable.women_avatar10
        "women_avatar11" -> R.drawable.women_avatar11
        "women_avatar12" -> R.drawable.women_avatar12
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
