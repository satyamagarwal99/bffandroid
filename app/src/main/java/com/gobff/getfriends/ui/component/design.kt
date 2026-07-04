package com.gobff.getfriends.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.theme.GaretFontFamily

/*
@Composable
fun BffBottomBar(
    selectedTab: MainBottomTab,
    onTabSelected: (MainBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(116.dp)
    ) {
        val selectedIndex = MainBottomTab.entries.indexOf(selectedTab)
        val bottomBarShape = remember(selectedIndex) {
            SelectedBottomBarShape(
                selectedIndex = selectedIndex,
                itemCount = MainBottomTab.entries.size
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(98.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = bottomBarShape,
                    ambientColor = Color.Black.copy(alpha = 0.10f),
                    spotColor = Color.Black.copy(alpha = 0.16f)
                )
                .clip(bottomBarShape)
                .background(Color.White)
        )
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(106.dp)
        ) {
            MainBottomTab.entries.forEach { tab ->
                BffBottomBarItem(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun BffBottomBarItem(
    tab: MainBottomTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleColor by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(alpha = 0.82f) else Color.Transparent,
        animationSpec = spring(stiffness = 380f),
        label = "mainBottomBubbleColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
        label = "mainBottomIconScale"
    )
    val circleSize by animateDpAsState(
        targetValue = if (isSelected) 76.dp else 42.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "mainBottomCircleSize"
    )
    val itemTopOffset by animateDpAsState(
        targetValue = if (isSelected) (-12).dp else 22.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "mainBottomItemTopOffset"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color(0xFF7A7A7A),
        label = "mainBottomLabelColor"
    )

    Box(
        modifier = modifier
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
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.10f),
                                spotColor = Color.Black.copy(alpha = 0.18f)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clip(CircleShape)
                    .background(if (isSelected) bubbleColor else Color.Transparent)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.62f))
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isSelected) 58.dp else 42.dp)
                        .clip(CircleShape)
                        .background(tab.tint)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = Color.White,
                        modifier = Modifier
                            .size(if (isSelected) 27.dp else 22.dp)
                            .scale(iconScale)
                    )
                }
            }
            Spacer(modifier = Modifier.height(if (isSelected) 5.dp else 12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (tab == MainBottomTab.Live && !isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF6464))
                    )
                }
                Text(
                    text = tab.label,
                    color = if (tab == MainBottomTab.Live && !isSelected) {
                        Color(0xFFFF6464)
                    } else {
                        labelColor
                    },
                    fontSize = 16.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private class SelectedBottomBarShape(
    private val selectedIndex: Int,
    private val itemCount: Int
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val topCorner = with(density) { 28.dp.toPx() }
        val topY = with(density) { 28.dp.toPx() }
        val notchDepth = with(density) { 18.dp.toPx() }
        val shoulder = with(density) { 70.dp.toPx() }
        val centerX = ((selectedIndex + 0.5f) / itemCount) * size.width
        val startX = (centerX - shoulder).coerceAtLeast(topCorner)
        val endX = (centerX + shoulder).coerceAtMost(size.width - topCorner)

        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, topY + topCorner)
            arcTo(
                rect = Rect(0f, topY, topCorner * 2f, topY + topCorner * 2f),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(startX, topY)
            cubicTo(
                startX + shoulder * 0.24f,
                topY,
                centerX - shoulder * 0.38f,
                topY + notchDepth,
                centerX,
                topY + notchDepth
            )
            cubicTo(
                centerX + shoulder * 0.38f,
                topY + notchDepth,
                endX - shoulder * 0.24f,
                topY,
                endX,
                topY
            )
            lineTo(size.width - topCorner, topY)
            arcTo(
                rect = Rect(
                    size.width - topCorner * 2f,
                    topY,
                    size.width,
                    topY + topCorner * 2f
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height)
            close()
        }
        return Outline.Generic(path)
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
*/
@Composable
fun BffBottomBar(
    selectedTab: MainBottomTab,
    onTabSelected: (MainBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val selectedIndex = MainBottomTab.entries.indexOf(selectedTab)
        val animatedSelectedIndex by animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = tween(durationMillis = 260),
            label = "mainBottomSelectedIndex"
        )
        val bottomBarShape = remember(animatedSelectedIndex) {
            SelectedBottomBarShape(
                selectedIndex = animatedSelectedIndex,
                itemCount = MainBottomTab.entries.size
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(86.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = bottomBarShape,
                    ambientColor = Color.Black.copy(alpha = 0.10f),
                    spotColor = Color.Black.copy(alpha = 0.16f)
                )
                .clip(bottomBarShape)
                .background(Color.White)
        )
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(94.dp)
        ) {
            MainBottomTab.entries.forEach { tab ->
                BffBottomBarItem(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun BffBottomBarItem(
    tab: MainBottomTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Fake-glass alphas for the bubble fill / border. For a *real* frosted
    // blur of whatever sits behind the bar (e.g. the video/photo in your
    // screenshot), pair this with a backdrop-blur library such as
    // dev.chrisbanes.haze (Modifier.hazeChild(hazeState)) applied to this
    // Box instead of / in addition to the alpha gradient below.
    val bubbleTopAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.45f else 0f,
        animationSpec = spring(stiffness = 380f),
        label = "mainBottomBubbleTopAlpha"
    )
    val bubbleBottomAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.18f else 0f,
        animationSpec = spring(stiffness = 380f),
        label = "mainBottomBubbleBottomAlpha"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
        label = "mainBottomIconScale"
    )
    val circleSize by animateDpAsState(
        targetValue = if (isSelected) 58.dp else 38.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "mainBottomCircleSize"
    )
    // Selected icon now only peeks slightly above the bar instead of
    // floating high above it.
    val itemTopOffset by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 24.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "mainBottomItemTopOffset"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color(0xFF7A7A7A),
        label = "mainBottomLabelColor"
    )

    Box(
        modifier = modifier
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
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.10f),
                                spotColor = Color.Black.copy(alpha = 0.18f)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = bubbleTopAlpha),
                                Color.White.copy(alpha = bubbleBottomAlpha)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (isSelected) 0.55f else 0f),
                        shape = CircleShape
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isSelected) 44.dp else 38.dp)
                        .clip(CircleShape)
                        .background(tab.tint)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = Color.White,
                        modifier = Modifier
                            .size(if (isSelected) 20.dp else 18.dp)
                            .scale(iconScale)
                    )
                }
            }
            Spacer(modifier = Modifier.height(if (isSelected) 5.dp else 10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (tab == MainBottomTab.Live && !isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF6464))
                    )
                }
                Text(
                    text = tab.label,
                    color = if (tab == MainBottomTab.Live && !isSelected) {
                        Color(0xFFFF6464)
                    } else {
                        labelColor
                    },
                    fontSize = 12.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private class SelectedBottomBarShape(
    private val selectedIndex: Float,
    private val itemCount: Int
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        // No corner rounding — the bar sits flush/rectangular against the
        // screen edges, joined straight into the sides of the screen.
        val topY = with(density) { 28.dp.toPx() }
        // Very shallow dip — just a small notch around the selected icon,
        // not a deep scoop.
        val notchDepth = with(density) { 10.dp.toPx() }
        val shoulder = with(density) { 50.dp.toPx() }
        val centerX = ((selectedIndex + 0.5f) / itemCount) * size.width
        val startX = (centerX - shoulder).coerceAtLeast(0f)
        val endX = (centerX + shoulder).coerceAtMost(size.width)

        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, topY)
            lineTo(startX, topY)
            cubicTo(
                startX + shoulder * 0.24f,
                topY,
                centerX - shoulder * 0.38f,
                topY + notchDepth,
                centerX,
                topY + notchDepth
            )
            cubicTo(
                centerX + shoulder * 0.38f,
                topY + notchDepth,
                endX - shoulder * 0.24f,
                topY,
                endX,
                topY
            )
            lineTo(size.width, topY)
            lineTo(size.width, size.height)
            close()
        }
        return Outline.Generic(path)
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
    val shape = HeartChipShape
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
                .offset(x = 1.5.dp, y = 1.5.dp)
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
