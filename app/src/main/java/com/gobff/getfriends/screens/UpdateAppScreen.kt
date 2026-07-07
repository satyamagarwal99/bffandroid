package com.gobff.getfriends.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily

@Composable
fun UpdateAppScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    BackHandler(enabled = true) {
        // Keep users on the required update screen.
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth

        Image(
            painter = painterResource(id = R.drawable.ss_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Image(
            painter = painterResource(id = R.drawable.ss_bff_text),
            contentDescription = "BFF",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.13f)
                .width(screenWidth * 0.3f)
        )

        Image(
            painter = painterResource(id = R.drawable.double_heart),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.27f)
                .size(140.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.47f)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            Text(
                text = "Update BFF",
                color = Color(0xFF24123F),
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "A newer version is required to continue.",
                color = Color(0xFF5B4A70),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 42.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFFFF3F62))
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
                    context.startActivity(intent)
                }
        ) {
            Text(
                text = "Update App",
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun UpdateAppScreenPreview() {
    BffAndroidTheme {
        UpdateAppScreen()
    }
}

private const val PLAY_STORE_URL = "http://play.google.com/store/apps/details?id=com.gobff.getfriends"
