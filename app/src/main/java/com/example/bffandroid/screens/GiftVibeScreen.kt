package com.example.bffandroid.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bffandroid.R
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.ui.theme.GaretFontFamily

private val GiftVibeCoral = Color(0xFFFF7171)

@Composable
fun GiftVibeScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

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
            GiftVibeHeader(onBack = onBack)
            Spacer(modifier = Modifier.height(34.dp))
            GiftVibeSearchBar(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(34.dp))
            GiftVibeGrid(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp)
            )
        }
    }
}

@Composable
private fun GiftVibeHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(GiftVibeCoral)
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile_screen_bg_objects),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 48.dp)
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
        )
        Image(
            painter = painterResource(id = R.drawable.gift_vibe_header),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
                .size(width = 270.dp, height = 60.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = "Little surprise from friends",
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 155.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 38.dp)
        ) {
            GiftVibeStatCard("Total Gifts", "123", Color(0xFFFF5F21), Modifier.weight(1f))
            GiftVibeStatCard("Hearts Earned", "6,720", Color(0xFFFF4D92), Modifier.weight(1f))
            GiftVibeStatCard("Unique Gifts", "42", Color(0xFFFFA90F), Modifier.weight(1f))
        }
    }
}

@Composable
private fun GiftVibeStatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Box(modifier = modifier.height(74.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color(0xFFFFF5EA))
                .border(1.2.dp, Color.Black, shape)
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GiftVibeSearchBar(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(80.dp)
    Box(modifier = modifier.height(56.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.4.dp, Color.Black, shape)
                .padding(horizontal = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF777777),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(14.dp))
            Text(
                text = "Search here...",
                color = Color(0xFFAAAAAA),
                fontSize = 17.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
        QuestionSparkle(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 45.dp, y = (-12).dp)
        )
    }
}

@Composable
private fun GiftVibeGrid(modifier: Modifier = Modifier) {
    val gifts = listOf(
        GiftVibeItem("Garam chai", R.drawable.gift_chai, "20", "x40"),
        GiftVibeItem("Ice cream", R.drawable.gift_icecream, "25", "x12"),
        GiftVibeItem("Maggie", R.drawable.gift_maggie, "30", "x24"),
        GiftVibeItem("Momo", R.drawable.gift_momo, "40", "x32"),
        GiftVibeItem("Coffee", R.drawable.gift_coffee, "50", "x08"),
        GiftVibeItem("Pizza", R.drawable.gift_pizza, "100", "x14"),
        GiftVibeItem("Biriyani", R.drawable.gift_biryani, "100", "x32"),
        GiftVibeItem("Yellow Rose", R.drawable.gift_yellow_rose, "5", "x122"),
        GiftVibeItem("Red Rose", R.drawable.gift_red_rose, "10", "x123")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        gifts.chunked(3).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { gift ->
                    GiftVibeCard(
                        item = gift,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GiftVibeCard(item: GiftVibeItem, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier.height(118.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 4.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(Color.White)
                .border(1.2.dp, Color.Black, shape)
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp)
                    .size(width = 64.dp, height = 48.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = item.title,
                color = Color.Black,
                fontSize = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp)
                    .offset(y = 18.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.single_heart),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = item.price,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .size(width = 34.dp, height = 18.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFF5A9C))
                    .border(1.dp, Color.White, RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = item.count,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = GaretFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = (-3f).dp)
                )
            }
        }
    }
}

@Composable
private fun QuestionSparkle(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val path = Path().apply {
            moveTo(center.x, 0f)
            quadraticTo(center.x + 3f, center.y - 3f, size.width, center.y)
            quadraticTo(center.x + 3f, center.y + 3f, center.x, size.height)
            quadraticTo(center.x - 3f, center.y + 3f, 0f, center.y)
            quadraticTo(center.x - 3f, center.y - 3f, center.x, 0f)
            close()
        }
        drawPath(path, color = Color(0xFFFFD33F))
        drawPath(path, color = Color(0xFF4B6EFF), style = Stroke(width = 1.5f))
    }
}

private data class GiftVibeItem(
    val title: String,
    val imageRes: Int,
    val price: String,
    val count: String
)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun GiftVibeScreenPreview() {
    BffAndroidTheme {
        GiftVibeScreen()
    }
}
