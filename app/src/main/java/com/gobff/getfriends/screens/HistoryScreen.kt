package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gobff.getfriends.R
import com.gobff.getfriends.data.model.CallHistoryItemResponse
import com.gobff.getfriends.ui.component.BffHeartChip
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.FreedokaFontFamily
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.viewmodel.CallHistoryViewModel
import com.gobff.getfriends.viewmodel.callTypeLabel
import com.gobff.getfriends.viewmodel.displayCallerName
import com.gobff.getfriends.viewmodel.displayDuration
import com.gobff.getfriends.viewmodel.displayTimestamp

private val HistoryOrange = Color(0xFFFD8663)

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    walletHearts: Int = 0,
    onBack: () -> Unit = {},
    onProfileRequested: () -> Unit = {},
    onRechargeRequested: () -> Unit = {},
    onConnectSelected: () -> Unit = {},
    onGamesSelected: () -> Unit = {},
    onHomeSelected: () -> Unit = {},
    callHistoryViewModel: CallHistoryViewModel = viewModel()
) {
    BackHandler(onBack = onBack)
    val callHistoryUiState = callHistoryViewModel.uiState

    LaunchedEffect(Unit) {
        callHistoryViewModel.loadCallHistory(size = 20)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            HistoryTopBar(
                walletHearts = walletHearts,
                onProfileRequested = onProfileRequested,
                onRechargeRequested = onRechargeRequested,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            HistoryTopArea()
            Spacer(modifier = Modifier.height(24.dp))
            HistoryTabs()
            when {
                callHistoryUiState.isLoading -> {
                    HistoryContentCard()
                }
                callHistoryUiState.errorMessage != null -> {
                    HistoryContentCard {
                        HistoryStatusText(
                            text = callHistoryUiState.errorMessage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 80.dp)
                        )
                    }
                }
                callHistoryUiState.calls.isEmpty() -> {
                    HistoryContentCard {
                        HistoryEmptyState(onCallNow = onConnectSelected)
                    }
                }
                else -> {
                    HistoryContentCard {
                        HistoryCallList(
                            calls = callHistoryUiState.calls.map { it.toHistoryCall() },
                            modifier = Modifier.padding(start = 24.dp, top = 30.dp, end = 24.dp, bottom = 48.dp)
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun HistoryContentCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {
        HistoryStatusText(
            text = "Loading calls...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 80.dp)
        )
    }
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 570.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(HistoryOrange)
    ) {
        Image(
            painter = painterResource(id = R.drawable.history_screen_bg_object),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 58.dp)
                .size(width = 122.dp, height = 4.dp)
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .background(Color(0xFFB44E35))
        )
        content()
    }
}

@Composable
private fun HistoryEmptyState(
    onCallNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .height(604.dp)
            .padding(horizontal = 36.dp)
    ) {
        Spacer(modifier = Modifier.height(74.dp))
        Image(
            painter = painterResource(id = R.drawable.history_empty_screen),
            contentDescription = null,
            modifier = Modifier.size(width = 276.dp, height = 228.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "No calls yet",
            color = Color.Black,
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "You haven't called anyone yet.\nStart a call and create some awesome memories!",
            color = Color.Black,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(36.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 156.dp, height = 48.dp)
                .offset(x = 3.dp, y = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = (-3).dp, y = (-4).dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCallNow
                    )
            ) {
                Text(
                    text = "Call now !",
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
private fun HistoryTopBar(
    walletHearts: Int,
    onProfileRequested: () -> Unit,
    onRechargeRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.man_avatar1),
            contentDescription = "Profile",
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onProfileRequested
                ),
            contentScale = ContentScale.Crop
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            BffHeartChip(
                hearts = walletHearts,
                onClick = onRechargeRequested
            )
            Spacer(modifier = Modifier.width(12.dp))
            Image(
                painter = painterResource(id = R.drawable.game_screen_question),
                contentDescription = "Help",
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun HistoryTopArea() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.gift_vibe_sparkle),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FRIENDSHIP LOG",
                color = HistoryOrange,
                fontSize = 32.sp,
                lineHeight = 32.sp,
                letterSpacing = 1.28.sp,
                fontFamily = FreedokaFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(1f, 1f),
                        blurRadius = 0f
                    )
                )
            )
            Spacer(modifier = Modifier.width(2.dp))
            Image(
                painter = painterResource(id = R.drawable.gift_vibe_sparkle),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "See who you've talked with",
            color = Color(0xFF3A393D),
            fontSize = 16.sp,
            lineHeight = 18.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistoryTabs() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 37.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            HistoryTabLabel(
                text = "Video",
                selected = true
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            HistoryTabLabel(
                text = "Audio",
                selected = false
            )
        }
    }
}

@Composable
private fun HistoryTabLabel(text: String, selected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = Color.Black.copy(alpha = if (selected) 1f else 0.78f),
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
@Composable
private fun HistoryCallList(
    calls: List<HistoryCall>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        calls.forEach { call ->
            HistoryCallRow(call = call)
        }
    }
}

@Composable
private fun HistoryStatusText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 15.sp,
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Bold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun HistoryCallRow(call: HistoryCall) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
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
                .padding(start = 18.dp, end = 18.dp)
        ) {
            Image(
                painter = painterResource(id = call.avatarRes),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.name,
                    color = Color(0xFF111111),
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = call.date,
                    color = Color(0xFF8A8A8A),
                    fontSize = 11.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            HistoryDurationPill(text = call.duration)
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 48.dp, height = 32.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF35A9E6))
            ) {
                Icon(
                    painter = painterResource(R.drawable.call_screen_camera),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryDurationPill(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 64.dp, height = 28.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp))
    ) {
        Text(
            text = text,
            color = Color(0xFF656565),
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class HistoryCall(
    val name: String,
    val date: String,
    val duration: String,
    val avatarRes: Int
)

private fun CallHistoryItemResponse.toHistoryCall(): HistoryCall =
    HistoryCall(
        name = displayCallerName,
        date = listOf(callTypeLabel, displayTimestamp)
            .filter { it.isNotBlank() }
            .joinToString(" - "),
        duration = displayDuration,
        avatarRes = avatarUrl.toHistoryAvatarRes()
    )

private fun String?.toHistoryAvatarRes(): Int =
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
private fun HistoryScreenPreview() {
    BffAndroidTheme {
        HistoryScreenPreviewContent()
    }
}

@Composable
private fun HistoryScreenPreviewContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            HistoryTopBar(
                walletHearts = 3230,
                onProfileRequested = {},
                onRechargeRequested = {},
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            HistoryTopArea()
            Spacer(modifier = Modifier.height(24.dp))
            HistoryTabs()
            HistoryContentCard {
                HistoryCallList(
                    calls = previewHistoryCalls(),
                    modifier = Modifier.padding(start = 24.dp, top = 30.dp, end = 24.dp, bottom = 48.dp)
                )
            }
        }
    }
}

private fun previewHistoryCalls(): List<HistoryCall> =
    listOf(
        HistoryCall("Anshu", "12 Jun, 10:32pm", "12 min", R.drawable.women_avatar1),
        HistoryCall("Riya", "13 Jun, 9:15am", "8 min", R.drawable.women_avatar1),
        HistoryCall("Karan", "13 Jun, 11:47am", "15 min", R.drawable.women_avatar1),
        HistoryCall("Meera", "14 Jun, 2:05pm", "10 min", R.drawable.women_avatar1),
        HistoryCall("Aditya", "14 Jun, 5:30pm", "7 min", R.drawable.women_avatar1)
    )
