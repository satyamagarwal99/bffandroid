package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.component.HandDrawnCardShape
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.FreedokaFontFamily
import com.gobff.getfriends.ui.theme.GaretFontFamily

@Composable
fun EarnCoinsScreen(
    modifier: Modifier = Modifier,
    onGotIt: () -> Unit = {}
) {
    BackHandler(onBack = {})

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EarnCoinsTeal)
    ) {
        EarnCoinsBackgroundPattern(modifier = Modifier.fillMaxSize())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 62.dp, bottom = 32.dp)
        ) {
            EarnCoinsHeader()
            Spacer(modifier = Modifier.height(32.dp))
            EarnCoinsCard(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(40.dp))
            EarnCoinsButton(
                text = "Got it",
                onClick = onGotIt,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EarnCoinsBackgroundPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeColor = Color.White.copy(alpha = 0.08f)
        val fillColor = Color.White.copy(alpha = 0.06f)

        drawCircle(
            color = fillColor,
            radius = size.minDimension * 0.42f,
            center = Offset(size.width * 0.92f, size.height * 0.13f)
        )
        drawCircle(
            color = fillColor.copy(alpha = 0.04f),
            radius = size.minDimension * 0.58f,
            center = Offset(size.width * 0.18f, size.height * 0.82f)
        )
        drawArc(
            color = strokeColor,
            startAngle = 192f,
            sweepAngle = 116f,
            useCenter = false,
            topLeft = Offset(-size.width * 0.34f, size.height * 0.14f),
            size = androidx.compose.ui.geometry.Size(size.width * 1.42f, size.height * 0.74f),
            style = Stroke(width = 34.dp.toPx())
        )
        drawArc(
            color = strokeColor.copy(alpha = 0.12f),
            startAngle = 16f,
            sweepAngle = 146f,
            useCenter = false,
            topLeft = Offset(size.width * 0.16f, -size.height * 0.1f),
            size = androidx.compose.ui.geometry.Size(size.width * 1.05f, size.height * 0.62f),
            style = Stroke(width = 28.dp.toPx())
        )
    }
}

@Composable
private fun EarnCoinsHeader(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SparkleGlyph(rotation = -12f)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "HOW TO EARN COINS ?",
                color = Color.White,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center,
                fontFamily = FreedokaFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            SparkleGlyph(rotation = 12f)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Simple ways to earn and grow your coins",
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            fontFamily = FreedokaFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun SparkleGlyph(rotation: Float) {
    Icon(
        imageVector = Icons.Default.Celebration,
        contentDescription = null,
        tint = EarnCoinsYellow,
        modifier = Modifier
            .size(18.dp)
            .graphicsLayer { rotationZ = rotation }
    )
}

@Composable
private fun EarnCoinsCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(565.dp)
            .clip(HandDrawnCardShape)
            .background(Color.White)
            .border(2.dp, Color.Black, HandDrawnCardShape)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EarnCoinsRow(
                title = "Stay Online",
                body = "Keep the app active and stay connected.",
                reward = {
                    CoinRewardBox(
                        amount = "30",
                        caption = "Coins/ Hour",
                        background = Color(0xFFF1FCF7),
                        amountColor = Color(0xFF24A66A)
                    )
                }
            )
            EarnDivider()
            EarnCoinsRow(
                title = "Answer Calls",
                body = "Answer incoming calls and talk more",
                reward = {
                    CoinRewardBox(
                        amount = "1",
                        caption = "Coins/ Minute",
                        background = Color(0xFFF9F4FE),
                        amountColor = Color(0xFF754BE7)
                    )
                }
            )
            EarnDivider()
            EarnCoinsRow(
                title = "Receive Gifts on Call",
                body = "Eligible gifts are automatically converted into coins",
                reward = { GiftToCoinBox() }
            )
            EarnDivider()
            EarnCoinsRow(
                title = "Withdraw Your Earnings",
                body = "Once you have 100 Coins, you can request a withdrawal.",
                reward = { WithdrawRewardBox() }
            )
            Spacer(modifier = Modifier.height(22.dp))
            ImportantNotice(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun EarnCoinsRow(
    title: String,
    body: String,
    reward: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                color = Color.Black,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        reward()
    }
}

@Composable
private fun EarnDivider() {
    Spacer(modifier = Modifier.height(22.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE8E8E8))
    )
    Spacer(modifier = Modifier.height(22.dp))
}

@Composable
private fun CoinRewardBox(
    amount: String,
    caption: String,
    background: Color,
    amountColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .size(width = 94.dp, height = 62.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.coin_icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = amount,
                color = amountColor,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = caption,
            color = Color.Black,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GiftToCoinBox(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .size(width = 94.dp, height = 62.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEF4F7))
    ) {
        Icon(
            imageVector = Icons.Default.CardGiftcard,
            contentDescription = null,
            tint = Color(0xFFFF466F),
            modifier = Modifier.size(28.dp)
        )
        Canvas(modifier = Modifier.size(width = 22.dp, height = 18.dp)) {
            val path = Path().apply {
                moveTo(2f, size.height / 2f)
                lineTo(size.width - 5f, size.height / 2f)
                moveTo(size.width - 8f, 3f)
                lineTo(size.width - 2f, size.height / 2f)
                lineTo(size.width - 8f, size.height - 3f)
            }
            drawPath(path, Color(0xFFFF6A86), style = Stroke(width = 2.dp.toPx()))
        }
        Image(
            painter = painterResource(id = R.drawable.coin_icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun WithdrawRewardBox(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .size(width = 94.dp, height = 62.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEF6E9))
            .padding(horizontal = 6.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.coin_icon),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "100 = ",
            color = Color(0xFF955903),
            fontSize = 14.sp,
            lineHeight = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "₹90",
            color = Color(0xFF19A863),
            fontSize = 14.sp,
            lineHeight = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ImportantNotice(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFEAEB))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Important",
            color = Color(0xFFE9251F),
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "If you've been online for at least 20 minutes but do not answer an incoming call, the coins earned during the previous 20 minutes may be reversed.",
            color = Color.Black,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun EarnCoinsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = HandDrawnCardShape
    Box(
        modifier = modifier
            .height(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 3.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 3.dp)
                .clip(shape)
                .background(Color.White)
        ) {
            Text(
                text = text,
                color = Color(0xFF212121),
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private val EarnCoinsTeal = Color(0xFF19B1A5)
private val EarnCoinsYellow = Color(0xFFFFD22E)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun EarnCoinsScreenPreview() {
    BffAndroidTheme {
        EarnCoinsScreen()
    }
}
