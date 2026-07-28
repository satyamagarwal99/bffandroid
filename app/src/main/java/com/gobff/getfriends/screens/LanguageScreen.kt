package com.gobff.getfriends.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily

@Composable
fun LanguageScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    isSubmitting: Boolean = false,
    submitError: String? = null,
    onStartTalking: (Set<String>) -> Unit = {}
) {
    val selectedLanguages = remember { mutableStateListOf<String>() }

    BackHandler(onBack = onBack)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LanguageBackground)
    ) {
        val screenHeight = maxHeight
        val gridTopPadding = screenHeight * 0.3f
        val gridRequiredHeight = 380.dp
        val gridMaxHeight = (screenHeight - gridTopPadding - 112.dp).coerceAtLeast(180.dp)
        val shouldScrollGrid = gridMaxHeight < gridRequiredHeight
        val gridScrollState = rememberScrollState()

        LanguagePatternBackground(modifier = Modifier.fillMaxSize())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.075f)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Choose your language",
                color = Color.White,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Select the languages you can\ncomfortably speak.",
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = gridTopPadding)
                .width(345.dp)
                .then(
                    if (shouldScrollGrid) {
                        Modifier
                            .heightIn(max = gridMaxHeight)
                            .verticalScroll(gridScrollState)
                    } else {
                        Modifier
                    }
                )
        ) {
            LanguageOptions.chunked(3).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { language ->
                        LanguageCard(
                            language = language,
                            selected = selectedLanguages.contains(language.value),
                            onClick = {
                                if (selectedLanguages.contains(language.value)) {
                                    selectedLanguages.remove(language.value)
                                } else {
                                    selectedLanguages.add(language.value)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        LanguagePrimaryButton(
            text = if (isSubmitting) "Saving..." else "Start talking",
            enabled = selectedLanguages.isNotEmpty() && !isSubmitting,
            onClick = { onStartTalking(selectedLanguages.toSet()) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 22.dp)
        )

        if (!submitError.isNullOrBlank()) {
            Text(
                text = submitError,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 84.dp)
            )
        }
    }
}

@Composable
private fun LanguagePatternBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeColor = Color.White.copy(alpha = 0.09f)
        val stroke = Stroke(width = 34.dp.toPx())
        val shapes = listOf(
            Offset(size.width * 0.74f, size.height * -0.08f) to Size(size.width * 0.78f, size.height * 0.34f),
            Offset(size.width * 0.32f, size.height * 0.02f) to Size(size.width * 0.86f, size.height * 0.34f),
            Offset(size.width * -0.18f, size.height * 0.44f) to Size(size.width * 1.08f, size.height * 0.44f),
            Offset(size.width * 0.26f, size.height * 0.77f) to Size(size.width * 0.34f, size.height * 0.18f)
        )

        shapes.forEachIndexed { index, (topLeft, arcSize) ->
            drawArc(
                color = strokeColor,
                startAngle = if (index % 2 == 0) 18f else 205f,
                sweepAngle = 295f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
            drawArc(
                color = strokeColor,
                startAngle = if (index % 2 == 0) 28f else 215f,
                sweepAngle = 230f,
                useCenter = false,
                topLeft = topLeft + Offset(34.dp.toPx(), 28.dp.toPx()),
                size = Size(arcSize.width * 0.64f, arcSize.height * 0.64f),
                style = stroke
            )
        }
    }
}

@Composable
private fun LanguageCard(
    language: SpokenLanguage,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(7.dp)
    val background = if (selected) language.selectedColor else Color.White
    val textColor = if (selected) Color.White else Color(0xFF2A2A2A)

    Box(
        modifier = modifier
            .height(77.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 3.dp, start = 2.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(background)
                .border(1.5.dp, Color.Black, shape)
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = language.name,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = language.nativeName,
                color = textColor,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun LanguagePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(16.dp)
    val buttonColor = if (enabled) Color.White else Color.White.copy(alpha = 0.62f)
    val textColor = if (enabled) Color.Black else Color.Black.copy(alpha = 0.55f)

    Box(modifier = modifier.clickable(enabled = enabled, onClick = onClick)) {
        Box(
            modifier = Modifier
                .size(width = 168.dp, height = 48.dp)
                .padding(top = 4.dp, start = 4.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 168.dp, height = 48.dp)
                .clip(shape)
                .background(buttonColor)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class SpokenLanguage(
    val name: String,
    val nativeName: String,
    val value: String,
    val selectedColor: Color = Color(0xFFB339AE)
)

private val LanguageOptions = listOf(
    SpokenLanguage("English", "English", "ENGLISH", Color(0xFF3C37B8)),
    SpokenLanguage("Malayalam", "മലയാളം", "MALAYALAM"),
    SpokenLanguage("Tamil", "தமிழ்", "TAMIL", Color(0xFF9941D1)),
    SpokenLanguage("Hindi", "हिन्दी", "HINDI"),
    SpokenLanguage("Marathi", "मराठी", "MARATHI"),
    SpokenLanguage("Punjabi", "ਪੰਜਾਬੀ", "PUNJABI"),
    SpokenLanguage("Bengali", "বাংলা", "BENGALI"),
    SpokenLanguage("Kannada", "ಕನ್ನಡ", "KANNADA"),
    SpokenLanguage("Gujarati", "ગુજરાતી", "GUJARATI"),
    SpokenLanguage("Telugu", "తెలుగు", "TELUGU"),
    SpokenLanguage("Urdu", "اردو", "URDU"),
    SpokenLanguage("Odia", "ଓଡ଼ିଆ", "ODIA")
)

private val LanguageBackground = Color(0xFFFF666A)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LanguageScreenPreview() {
    BffAndroidTheme {
        LanguageScreen()
    }
}
