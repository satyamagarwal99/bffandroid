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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gobff.getfriends.R
import com.gobff.getfriends.data.model.CallHistoryItemResponse
import com.gobff.getfriends.ui.component.BffHeartChip
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.viewmodel.CallHistoryViewModel
import com.gobff.getfriends.viewmodel.callTypeLabel
import com.gobff.getfriends.viewmodel.displayCallerName
import com.gobff.getfriends.viewmodel.displayDuration
import com.gobff.getfriends.viewmodel.displayTimestamp

private val HistoryOrange = Color(0xFFD87400)

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
            .background(HistoryOrange)
    ) {
        Image(
            painter = painterResource(id = R.drawable.history_screen_bg_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            HistoryTopBar(
                walletHearts = walletHearts,
                onProfileRequested = onProfileRequested,
                onRechargeRequested = onRechargeRequested,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            HistoryTopArea()
            Spacer(modifier = Modifier.height(34.dp))
            HistoryTabs()
            Spacer(modifier = Modifier.height(18.dp))
            when {
                callHistoryUiState.isLoading -> {
                    HistoryStatusText(
                        text = "Loading calls...",
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                callHistoryUiState.errorMessage != null -> {
                    HistoryStatusText(
                        text = callHistoryUiState.errorMessage,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                callHistoryUiState.calls.isEmpty() -> {
                    HistoryStatusText(
                        text = "No calls yet",
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                else -> {
                    HistoryCallList(
                        calls = callHistoryUiState.calls.map { it.toHistoryCall() },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
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
        BffHeartChip(
            hearts = walletHearts,
            onClick = onRechargeRequested
        )
    }
}

@Composable
private fun HistoryTopArea() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(151.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.history_screen_header),
            contentDescription = null,
            modifier = Modifier
                .size(
                    width = 372.dp,
                    height = 99.dp
                )
                .align(Alignment.TopStart)
                .offset(
                    x = 11.dp,
                    y = 12.dp
                ),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
private fun HistoryTabs() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 37.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            HistoryTabLabel(
                text = "All calls",
                selected = true
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            HistoryTabLabel(
                text = "Missed",
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
            color = Color.White.copy(
                alpha = if (selected) 1f else 0.86f
            ),
            fontSize = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selected) {
            Box(
                modifier = Modifier
                    .size(
                        width = 122.dp,
                        height = 3.dp
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
            )
        } else {
            Spacer(
                modifier = Modifier.height(3.dp)
            )
        }
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
                    .background(Color(0xFFA032B3))
            ) {
                Icon(
                    painter = painterResource(R.drawable.chat_screen_phone),
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
        HistoryScreen()
    }
}
