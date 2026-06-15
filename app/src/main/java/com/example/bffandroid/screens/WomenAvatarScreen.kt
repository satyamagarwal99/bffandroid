package com.example.bffandroid.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bffandroid.R
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.ui.theme.GaretFontFamily

@Composable
fun WomenAvatarScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    var selectedAvatar by remember { mutableIntStateOf(1) }
    var nickname by remember { mutableStateOf("") }
    var showNickname by remember { mutableStateOf(false) }

    BackHandler {
        if (showNickname) {
            showNickname = false
        } else {
            onBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WomenAvatarBackground)
    ) {
        Image(
            painter = painterResource(id = R.drawable.women_avatar_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        if (showNickname) {
            WomenNicknameContent(
                selectedAvatar = selectedAvatar,
                nickname = nickname,
                onNicknameChange = { nickname = it },
                onComplete = onComplete,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            WomenAvatarPickerContent(
                selectedAvatar = selectedAvatar,
                onAvatarSelected = { selectedAvatar = it },
                onContinue = { showNickname = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun WomenAvatarPickerContent(
    selectedAvatar: Int,
    onAvatarSelected: (Int) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 34.dp)
    ) {
        Spacer(modifier = Modifier.height(76.dp))
        Text(
            text = "Choose your avatar",
            color = Color.White,
            fontSize = 28.sp,
            lineHeight = 28.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Pick a look that feels like you.",
            color = Color.White,
            fontSize = 17.sp,
            lineHeight = 17.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(50.dp))
        AvatarImage(
            index = selectedAvatar,
            size = 116.dp,
            borderWidth = 2,
            contentDescription = "Selected avatar"
        )
        Spacer(modifier = Modifier.height(44.dp))
        WomenAvatarGrid(
            selectedAvatar = selectedAvatar,
            onAvatarSelected = onAvatarSelected
        )
        Spacer(modifier = Modifier.height(22.dp))
        OrDivider()
        Spacer(modifier = Modifier.height(24.dp))
        PhotoAvatarCard()
        Spacer(modifier = Modifier.height(36.dp))
        ThisIsMeButton(
            onClick = onContinue,
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun WomenNicknameContent(
    selectedAvatar: Int,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 48.dp)
    ) {
        Spacer(modifier = Modifier.height(76.dp))
        Text(
            text = "Pick a nickname",
            color = Color.White,
            fontSize = 28.sp,
            lineHeight = 28.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(108.dp))
        AvatarImage(
            index = selectedAvatar,
            size = 216.dp,
            borderWidth = 3,
            contentDescription = "Selected avatar"
        )
        Spacer(modifier = Modifier.height(44.dp))
        NicknameField(
            value = nickname,
            onValueChange = onNicknameChange,
            onSubmit = onComplete,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(170.dp))
        Text(
            text = "I'll choose later",
            color = Color.Black,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(onClick = onComplete)
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun WomenAvatarGrid(
    selectedAvatar: Int,
    onAvatarSelected: (Int) -> Unit
) {
    val rows = (1..12).chunked(4)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val itemSpacing = 12.dp
        val itemSize = ((maxWidth - (itemSpacing * 3)) / 4).coerceIn(60.dp, 78.dp)
        val avatarSize = (itemSize - 6.dp).coerceIn(54.dp, 72.dp)

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(4) { columnIndex ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            val index = row.getOrNull(columnIndex)
                            if (index != null) {
                                AvatarGridItem(
                                    index = index,
                                    isSelected = selectedAvatar == index,
                                    onClick = { onAvatarSelected(index) },
                                    itemSize = itemSize,
                                    avatarSize = avatarSize
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarGridItem(
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    itemSize: Dp,
    avatarSize: Dp
) {
    Box(
        modifier = Modifier
            .size(itemSize)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(itemSize)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
            )
        }

        AvatarImage(
            index = index,
            size = avatarSize,
            borderWidth = 1,
            contentDescription = "Avatar $index"
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF8846DD),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (index == 12) {
            Text(
                text = "+39",
                color = Color.White,
                fontSize = 19.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AvatarImage(
    index: Int,
    size: Dp,
    borderWidth: Int,
    contentDescription: String?
) {
    val avatarResId = womenAvatarResourceId(index)

    Image(
        painter = painterResource(id = avatarResId),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth.dp, Color.White, CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun womenAvatarResourceId(index: Int): Int {
    val context = LocalContext.current
    val resourceId = remember(index) {
        context.resources.getIdentifier(
            "women_avatar$index",
            "drawable",
            context.packageName
        )
    }

    return if (resourceId != 0) resourceId else R.drawable.gender_women
}

@Composable
private fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.7f))
        )
        Text(
            text = "or",
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.7f))
        )
    }
}

@Composable
private fun PhotoAvatarCard() {
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(shape)
            .background(Color(0x42A83232))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.55f),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(7.dp.toPx(), 7.dp.toPx())
                    )
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Create an avatar from your photo",
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "We don't save or store your photo.",
                color = Color.White,
                fontSize = 11.sp,
                lineHeight = 11.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ThisIsMeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Box(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .size(width = 132.dp, height = 56.dp)
                .offset(x = 5.dp, y = 5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Box(
            modifier = Modifier
                .size(width = 132.dp, height = 56.dp)
                .clip(shape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "This is Me!",
                color = Color.Black,
                fontSize = 18.sp,
                lineHeight = 18.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NicknameField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(shape)
            .background(Color.White)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = WomenAvatarBackground,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(4.dp.toPx(), 4.dp.toPx())
                    )
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = "Enter a nickname",
                        color = Color(0xFFD0D0D0),
                        fontSize = 15.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = modifier.padding(horizontal = 12.dp)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    cursorBrush = SolidColor(Color.Black),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontFamily = GaretFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }

            if (value.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2FA66A),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF4A5AEF),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onSubmit)
                )
            }
        }
    }
}

private val WomenAvatarBackground = Color(0xFFFF6464)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun WomenAvatarScreenPreview() {
    BffAndroidTheme {
        WomenAvatarScreen()
    }
}
