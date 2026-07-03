package com.gobff.getfriends.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.component.BffHeartChip
import com.gobff.getfriends.ui.component.HandDrawnCardShape
import com.gobff.getfriends.ui.component.HeartChipShape
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.FreedokaFontFamily
import com.gobff.getfriends.ui.theme.GaretFontFamily

private val HomeScreen2Pink = Color(0xFFFF639C)
private val HomeScreen2Yellow = Color(0xFFF9BF25)
private val HomeScreen2Blue = Color(0xFF1676F3)
private val HomeScreen2Purple = Color(0xFF8C2FF1)
private val HomeScreen2Orange = Color(0xFFFF8C0F)
private val HomeScreen2Teal = Color(0xFF06AFC9)
private val HomeScreen2Ink = Color(0xFF141414)

@Composable
fun HomeScreen2(
    modifier: Modifier = Modifier,
    walletHearts: Int = 0,
    displayName: String? = null,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onConnectSelected: () -> Unit = {},
    onGamesSelected: () -> Unit = {},
    onChatSelected: () -> Unit = {},
    onHistorySelected: () -> Unit = {},
    onLiveSelected: () -> Unit = {},
    onRechargeRequested: () -> Unit = {},
    onHomeSelected: () -> Unit = {},
    onProfileRequested: () -> Unit = {},
    onTruthDareSelected: () -> Unit = {}

) {
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
            HomeScreen2TopSection(
                walletHearts = walletHearts,
                displayName = displayName,
                onChatClick = onChatSelected,
                onProfileClick = onProfileRequested,
                onRechargeRequested = onRechargeRequested
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(HomeScreen2Pink)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_screen2_bg_object),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    modifier = Modifier
                        .padding(top = 26.dp, bottom = 116.dp)
                ) {
                    HomeScreen2SectionHeader(
                        title = "Star Friends",
                        showStarIcon = true
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    HomeScreen2StarFriendRow()

                    Spacer(modifier = Modifier.height(30.dp))
                    HomeScreen2SectionHeader(title = "Friends Online")
                    Spacer(modifier = Modifier.height(18.dp))
                    HomeScreen2FriendRow()

                    Spacer(modifier = Modifier.height(30.dp))
                    HomeScreen2SectionHeader(title = "Streaming live now")
                    Spacer(modifier = Modifier.height(18.dp))
                    HomeScreen2LiveRow()

                    Spacer(modifier = Modifier.height(30.dp))
                    HomeScreen2SectionHeader(
                        title = "Games Center",
                        onViewAllClick = onGamesSelected
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    HomeScreen2GameRow(onTruthDareSelected = onTruthDareSelected)
                }
                }
        }

    }
}

@Composable
private fun HomeScreen2TopSection(
    walletHearts: Int,
    displayName: String?,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRechargeRequested: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(226.dp)
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.man_avatar1),
            contentDescription = null,
            modifier = Modifier
                .offset(
                    x = 20.dp,
                    y = 48.dp
                )
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
        ) {
            BffHeartChip(hearts = walletHearts, onClick = onRechargeRequested)
            HomeScreen2IconButton(
                icon = Icons.Filled.ChatBubbleOutline,
                iconSize = 20.dp,
                modifier = Modifier.size(32.dp),
                onClick = onChatClick
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp)
                .fillMaxWidth()
        ) {
            val greetingName = displayName?.trim()?.takeIf { it.isNotBlank() } ?: "there"
            Text(
                text = "Hi, $greetingName 👋",
                color = HomeScreen2Pink,
                fontSize = 32.sp,
                fontFamily = FreedokaFontFamily,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "What do you want to do today?",
                color = Color(0xFF404040),
                fontSize = 17.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
private fun HomeScreen2IconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    background: Color = Color.White,
    iconColor: Color = Color(0xFF444444),
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.size(size)
    ) {

        val shape = HeartChipShape
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 1.5.dp, y = 1.5.dp)
                .clip(shape)
                .background(Color.Black)
        )

        // Main button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(background)
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = shape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun HomeScreen2SectionHeader(
    title: String,
    showStarIcon: Boolean = false,
    onViewAllClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showStarIcon) {
                Icon(
                    painter = painterResource(id = R.drawable.star_icon), // 👈 apna drawable name daalo
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = title,
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
                lineHeight = 18.sp,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onViewAllClick
            )
        ) {
            Text(
                text = "View all",
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HomeScreen2StarFriendRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        HomeScreen2StarFriendCard(name = "Anshu", avatarRes = R.drawable.women_avatar1)
        HomeScreen2StarFriendCard(name = "Dev", avatarRes = R.drawable.women_avatar3)
        HomeScreen2StarFriendCard(name = "Priya", avatarRes = R.drawable.women_avatar1)
    }
}

@Composable
private fun HomeScreen2StarFriendCard(
    name: String,
    avatarRes: Int
) {
    val shape = HandDrawnCardShape

    Box(
        modifier = Modifier
            .size(width = 130.dp, height = 140.dp)
    ) {

        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )

        // Card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color(0xFFFFE7A0))
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = shape
                )
                .padding(
                    top = 13.dp,
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 10.dp
                )
        ) {

            Box {
                Image(
                    painter = painterResource(avatarRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = -2.dp, y = 0.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF24B64F))
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Text(
                    text = name,
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    painter = painterResource(id = R.drawable.star_badge),
                    contentDescription = null,
                    tint = Color.Unspecified, // keeps original icon colors
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HomeScreen2PillButton(
                text = "Call expert",
                icon = Icons.Default.Call,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

//val HandDrawnCardShape = GenericShape { size, _ ->
//
//    val w = size.width
//    val h = size.height
//
//    moveTo(w * 0.10f, h * 0.03f)
//
//    // ---------- TOP ----------
//    cubicTo(
//        w * 0.24f, h * 0.005f,
//        w * 0.47f, h * 0.018f,
//        w * 0.64f, h * 0.015f
//    )
//
//    cubicTo(
//        w * 0.81f, h * 0.010f,
//        w * 0.94f, h * 0.020f,
//        w * 0.980f, h * 0.055f
//    )
//
//    // ---------- RIGHT ----------
//    cubicTo(
//        w * 1.010f, h * 0.20f,
//        w * 1.008f, h * 0.43f,
//        w * 1.002f, h * 0.66f
//    )
//
//    cubicTo(
//        w * 0.997f, h * 0.85f,
//        w * 0.988f, h * 0.95f,
//        w * 0.955f, h * 0.980f
//    )
//
//    // ---------- BOTTOM ----------
//    cubicTo(
//        w * 0.80f, h * 1.000f,
//        w * 0.64f, h * 0.990f,
//        w * 0.50f, h * 0.996f
//    )
//
//    cubicTo(
//        w * 0.36f, h * 1.002f,
//        w * 0.20f, h * 0.998f,
//        w * 0.075f, h * 0.975f
//    )
//
//    // ---------- LEFT ----------
//    cubicTo(
//        w * 0.005f, h * 0.82f,
//        w * 0.008f, h * 0.45f,
//        w * 0.018f, h * 0.25f
//    )
//
//    cubicTo(
//        w * 0.030f, h * 0.12f,
//        w * 0.045f, h * 0.055f,
//        w * 0.10f, h * 0.03f
//    )
//
//    close()
//}

@Composable
private fun HomeScreen2FriendRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        HomeScreen2FriendCard(name = "Anshu", avatarRes = R.drawable.women_avatar1)
        HomeScreen2FriendCard(name = "Dev", avatarRes = R.drawable.women_avatar3)
        HomeScreen2FriendCard(name = "Priya", avatarRes = R.drawable.women_avatar1)
    }
}

@Composable
private fun HomeScreen2FriendCard(name: String, avatarRes: Int) {
    val shape = HandDrawnCardShape
    Box(
        modifier = Modifier.size(width = 130.dp, height = 148.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color.White, shape)
                .padding(top = 13.dp, start = 14.dp, end = 14.dp, bottom = 10.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = -2.dp, y = 0.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF24B64F))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = name,
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(9.dp))
            HomeScreen2PillButton(
                text = "Call",
                icon = Icons.Filled.Call,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HomeScreen2PillButton(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    background: Color = HomeScreen2Yellow,
    textColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(80.dp))
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp,
            )
        }
    }
}

@Composable
private fun HomeScreen2LiveRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        HomeScreen2LiveCard(
            name = "Alia",
            subtitle = "Let's chill & talk",
            viewers = "35",
            imageRes = R.drawable.home_screen_demo1
        )
        HomeScreen2LiveCard(
            name = "Benji",
            subtitle = "Coding session",
            viewers = "42",
            imageRes = R.drawable.home_screen_demo2
        )
        HomeScreen2LiveCard(
            name = "Cara",
            subtitle = "Let's play game",
            viewers = "38",
            imageRes = R.drawable.home_screen_demo1
        )
    }
}

@Composable
private fun HomeScreen2LiveCard(
    name: String,
    subtitle: String,
    viewers: String,
    imageRes: Int
) {
    val shape = HandDrawnCardShape

    Box(
        modifier = Modifier.size(width = 130.dp, height = 138.dp)
    ) {

        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )

        // Card
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color.White, shape)
        ) {

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.18f))
            )

            // LIVE badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {

                Text(
                    text = "Live",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color(0xFFFF3333))
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            // Viewers
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )

                Spacer(modifier = Modifier.width(3.dp))

                Text(
                    text = viewers,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bottom text
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        bottom = 12.dp
                    )
            ) {

                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun HomeScreen2GameRow(
    onTruthDareSelected: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        HomeScreen2GameCard(
            imageRes = R.drawable.game_screen_truth_dare,
            background = Color(0xFFEBDDFF),
            onPlayClick = onTruthDareSelected
        )
        HomeScreen2GameCard(
            imageRes = R.drawable.game_screen_tictactoe,
            background = Color(0xFFFFEEF4)
        )
    }
}

@Composable
private fun HomeScreen2GameCard(
    imageRes: Int,
    background: Color,
    onPlayClick: () -> Unit = {}
) {
    val shape = HandDrawnCardShape
    Box(
        modifier = Modifier.size(width = 130.dp, height = 138.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 1.5.dp, y = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(background)
                .border(1.dp, Color.White, shape)
                .padding(top = 12.dp, start = 18.dp, end = 18.dp, bottom = 10.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(74.dp),
                contentScale = ContentScale.Fit
            )
            HomeScreen2PillButton(
                text = "Play",
                modifier = Modifier.fillMaxWidth(),
                onClick = onPlayClick
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeScreen2Preview() {
    BffAndroidTheme {
        HomeScreen2()
    }
}
