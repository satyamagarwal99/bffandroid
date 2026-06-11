package com.example.bffandroid.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bffandroid.R
import com.example.bffandroid.ui.theme.BffAndroidTheme

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Image(
            painter = painterResource(id = R.drawable.ss_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Image(
            painter = painterResource(id = R.drawable.ss_squiggle_red),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 10.dp)
                .size(width = 38.dp, height = 70.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.ss_star),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 48.dp)
                .size(width = 30.dp, height = 34.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.ss_bff_text),
            contentDescription = "BFF",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.22f)
                .width(screenWidth * 0.28f)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.43f)
                .width(screenWidth * 0.78f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ss_red_lines),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 26.dp, y = (-10).dp)
                    .size(width = 25.dp, height = 24.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Text(
                    text = "One hello away from a",
                    color = Color(0xFF6E35E5),
                    fontSize = 20.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "new friend...",
                    color = Color(0xFFFF3F62),
                    fontSize = 20.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            Image(
                painter = painterResource(id = R.drawable.ss_red_mark),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x=-10.dp, y = 65.dp)
                    .size(width = 110.dp, height = 15.dp)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ss_squiggle_white),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-1).dp, y = screenHeight * 0.53f)
                .size(width = 39.dp, height = 57.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.ss_people),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 18.dp)
                .width(screenWidth * 0.95f)
        )

        Image(
            painter = painterResource(id = R.drawable.ss_chat_bubble),
            contentDescription = "Hi!",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = (-74).dp, y = -(screenHeight * 0.355f))
                .size(width = 50.dp, height = 44.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SplashScreenPreview() {
    BffAndroidTheme {
        SplashScreen()
    }
}
