package com.gobff.getfriends.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gobff.getfriends.R

val GaretFontFamily = FontFamily(
    Font(R.font.garet_book, FontWeight.Normal),
    Font(R.font.garet_medium, FontWeight.Medium),
    Font(R.font.garet_bold, FontWeight.Bold),
    Font(R.font.garet_heavy, FontWeight.ExtraBold),
    Font(R.font.garet_heavy, FontWeight.Black),
)

val FreedokaFontFamily = FontFamily(
    Font(R.font.fredoka, FontWeight.Normal),
            Font(R.font.fredoka_bold, FontWeight.Bold),
            Font(R.font.fredoka_semibold, FontWeight.SemiBold)

)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GaretFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
)
