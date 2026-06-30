package com.example.bffandroid.screens

import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bffandroid.R
import com.example.bffandroid.ui.component.BffBottomBar
import com.example.bffandroid.ui.component.MainBottomTab
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.ui.theme.FreedokaFontFamily
import com.example.bffandroid.ui.theme.GaretFontFamily

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    walletHearts: Int = 0,
    onBack: () -> Unit = {},
    onConnectSelected: () -> Unit = {},
    onTruthDareSelected: () -> Unit = {},
    onHomeSelected: () -> Unit = {},
    onHistorySelected: () -> Unit = {},
    onProfileRequested: () -> Unit = {},
    onRechargeRequested: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFECE1FB), Color(0xFFE9E0FA))
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.game_screen_bg_object),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 104.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(236.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.man_avatar1),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 20.dp, top = 48.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onProfileRequested
                        )
                )

                HeartChip(
                    hearts = walletHearts,
                    onClick = onRechargeRequested,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 20.dp)
                )

                GameHeader(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 122.dp)
                )
                CurvedChatTagline(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 170.dp)
                )
            }

            GameCard(
                title = "TRUTH\nOR DARE",
                cta = "Just",
                price = "35",
                imageRes = R.drawable.game_screen_truth_dare,
                imageWidth = 146.dp,
                imageHeight = 156.dp,
                imageOffsetX = 187.dp,
                imageOffsetY = (-24).dp,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFFC9071), Color(0xFFFD8461))
                ),
                onClick = onTruthDareSelected
            )
            Spacer(modifier = Modifier.height(12.dp))

            GameCard(
                title = "TIC TAC\nTOE",
                cta = "Just",
                price = "25",
                imageRes = R.drawable.game_screen_tictactoe,
                imageWidth = 168.dp,
                imageHeight = 142.dp,
                imageOffsetX = 178.dp,
                imageOffsetY = (-4).dp,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFF97AB9), Color(0xFFF15AA3))
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            GameCard(
                title = "UNO",
                cta = "Coming soon",
                imageRes = R.drawable.game_screen_uno,
                imageWidth = 198.dp,
                imageHeight = 148.dp,
                imageOffsetX = 150.dp,
                imageOffsetY = (-14).dp,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFB892EE), Color(0xFF9678F4))
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            GameCard(
                title = "LUDO",
                cta = "Coming soon",
                imageRes = R.drawable.game_screen_ludo,
                imageWidth = 197.dp,
                imageHeight = 158.dp,
                imageOffsetX = 152.dp,
                imageOffsetY = (-4).dp,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF82B5F2), Color(0xFF6398EF))
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        BffBottomBar(
            selectedTab = MainBottomTab.Games,
            onTabSelected = { tab ->
                when (tab) {
                    MainBottomTab.Connect -> onConnectSelected()
                    MainBottomTab.Home -> onHomeSelected()
                    MainBottomTab.History -> onHistorySelected()
                    else -> Unit
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun GameHeader(modifier: Modifier = Modifier) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
            Image(
                painter = painterResource(R.drawable.game_screen_header),
                contentDescription = null,
                modifier = Modifier.size(width = 330.dp, height = 76.dp),
                contentScale = ContentScale.Fit
            )


    }
}

@Composable
private fun GameCard(
    title: String,
    cta: String,
    imageRes: Int,
    imageWidth: Dp,
    imageHeight: Dp,
    imageOffsetX: androidx.compose.ui.unit.Dp,
    imageOffsetY: androidx.compose.ui.unit.Dp,
    gradient: Brush,
    modifier: Modifier = Modifier,
    price: String? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(32.dp)
    Box(
        modifier = modifier
            .size(width = 329.dp, height = 166.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(width = 329.dp, height = 150.dp)
                .clip(shape)
                .background(gradient)
                .border(2.dp, Color.White, shape)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 25.sp,
                lineHeight = 34.sp,
                fontFamily = FreedokaFontFamily,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        blurRadius = 0f,
                        offset = androidx.compose.ui.geometry.Offset(2f, 3f)
                    )
                ),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp, bottom = 34.dp)
            )
            GamePill(
                label = cta,
                price = price,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
            )
        }
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = imageOffsetX, y = imageOffsetY)
                .size(
                    width = imageWidth,
                    height = imageHeight
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun GamePill(
    label: String,
    price: String?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFFFD861).copy(alpha = if (price == null) 0.8f else 1f))
            .padding(horizontal = 14.dp)
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 11.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
        if (price != null) {
            Spacer(modifier = Modifier.size(4.dp))
            Image(
                painter = painterResource(id = R.drawable.single_heart),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = price,
                color = Color.Black,
                fontSize = 13.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        } else {
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HeartChip(
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
                .offset(x = 1.5.dp, y = 2.dp)
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
        ) {
            Image(
                painter = painterResource(id = R.drawable.single_heart),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = String.format("%,d", hearts),
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OptionalDrawableImage(
    drawableName: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    val context = LocalContext.current
    val resId = remember(drawableName) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
private fun CurvedChatTagline(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(
            width = 230.dp,
            height = 38.dp
        )
    ) {

        val text = "Play together, Talk together"

        val path = AndroidPath().apply {
            moveTo(
                10f,
                size.height * 0.65f
            )

            quadTo(
                size.width / 2f,
                -12f,   // curve depth (increase for more curve)
                size.width - 10f,
                size.height * 0.65f
            )
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#7670AE")
            textAlign = Paint.Align.CENTER
            textSize = 13.sp.toPx()
            letterSpacing = 0.015f
        }

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawTextOnPath(
                text,
                path,
                0f,
                0f,
                paint
            )
        }
    }
}
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun GameScreenPreview() {
    BffAndroidTheme {
        GameScreen()
    }
}
