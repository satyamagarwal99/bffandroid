package com.example.bffandroid.screens

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
import com.example.bffandroid.R
import com.example.bffandroid.ui.component.BffHeartChip
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.ui.theme.GaretFontFamily

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
    onHomeSelected: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

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
            HistoryCallList(
                calls = remember { historyCalls() },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
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
            .size(width = 52.dp, height = 28.dp)
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

private fun historyCalls(): List<HistoryCall> = listOf(
    HistoryCall("Anshu", "12 Jun, 10:32pm", "12 min", R.drawable.women_avatar3),
    HistoryCall("Riya", "13 Jun, 9:15am", "8 min", R.drawable.women_avatar3),
    HistoryCall("Karan", "13 Jun, 11:47am", "15 min", R.drawable.women_avatar3),
    HistoryCall("Meera", "14 Jun, 2:05pm", "10 min", R.drawable.women_avatar3),
    HistoryCall("Aditya", "14 Jun, 5:30pm", "7 min", R.drawable.women_avatar3),
    HistoryCall("Sneha", "15 Jun, 8:45am", "13 min", R.drawable.women_avatar3)
)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryScreenPreview() {
    BffAndroidTheme {
        HistoryScreen()
    }
}
