package com.gobff.getfriends.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WifiTethering
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.theme.GaretFontFamily

@Composable
fun BffBottomBar(
    selectedTab: MainBottomTab,
    onTabSelected: (MainBottomTab) -> Unit,
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
            MainBottomTab.entries.forEach { tab ->
                BffBottomBarItem(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun BffBottomBarItem(
    tab: MainBottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bubbleColor by animateColorAsState(
        targetValue = if (isSelected) tab.tint.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = spring(stiffness = 380f),
        label = "mainBottomBubbleColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
        label = "mainBottomIconScale"
    )
    val circleSize by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 40.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "mainBottomCircleSize"
    )
    val itemTopOffset by animateDpAsState(
        targetValue = if (isSelected) (-11).dp else 12.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "mainBottomItemTopOffset"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color(0xFF7A7A7A),
        label = "mainBottomLabelColor"
    )

    Box(
        modifier = Modifier
            .size(width = 66.dp, height = 99.dp)
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
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class MainBottomTab(
    val label: String,
    val icon: ImageVector,
    val tint: Color
) {
    Home("Home", Icons.Default.Home, Color(0xFF196DFF)),
    Games("Games", Icons.Default.SportsEsports, Color(0xFF8D32F7)),
    Connect("Connect", Icons.Default.Phone, Color(0xFFF5BE2E)),

    History("History", Icons.Default.History, Color(0xFFFF9518)),
    Live("Live", Icons.Default.WifiTethering, Color(0xFF06AFC9))
}

@Composable
fun BffHeartChip(
    hearts: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(width = 88.dp, height = 32.dp)
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
                .padding(horizontal = 9.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.single_heart),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%,d", hearts),
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
