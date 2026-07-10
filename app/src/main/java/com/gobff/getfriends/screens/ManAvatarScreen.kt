package com.gobff.getfriends.screens

import android.graphics.BitmapFactory
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
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
import com.gobff.getfriends.R
import com.gobff.getfriends.ui.theme.BffAndroidTheme
import com.gobff.getfriends.ui.theme.GaretFontFamily
import com.gobff.getfriends.utils.AvatarCache
import com.gobff.getfriends.utils.AvatarGender
import kotlinx.coroutines.delay

@Composable
fun ManAvatarScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onComplete: (selectedAvatar: Int, nickname: String) -> Unit = { _, _ -> },
    isSubmitting: Boolean = false,
    submitError: String? = null
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
            .background(ManAvatarBackground)
    ) {
        Image(
            painter = painterResource(id = R.drawable.man_avatar_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        if (showNickname) {
            ManNicknameContent(
                selectedAvatar = selectedAvatar,
                nickname = nickname,
                onNicknameChange = { nickname = it },
                onComplete = { onComplete(selectedAvatar, nickname) },
                isSubmitting = isSubmitting,
                submitError = submitError,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ManAvatarPickerContent(
                selectedAvatar = selectedAvatar,
                onAvatarSelected = { selectedAvatar = it },
                onContinue = { showNickname = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ManAvatarPickerContent(
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
        ManAvatarImage(
            index = selectedAvatar,
            size = 116.dp,
            borderWidth = 2,
            contentDescription = "Selected avatar"
        )
        Spacer(modifier = Modifier.height(44.dp))
        ManAvatarGrid(
            selectedAvatar = selectedAvatar,
            onAvatarSelected = onAvatarSelected
        )
        Spacer(modifier = Modifier.height(22.dp))
        ManOrDivider()
        Spacer(modifier = Modifier.height(24.dp))
        ManPhotoAvatarCard()
        Spacer(modifier = Modifier.height(36.dp))
        ManPrimaryButton(
            onClick = onContinue,
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ManNicknameContent(
    selectedAvatar: Int,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onComplete: () -> Unit,
    isSubmitting: Boolean,
    submitError: String?,
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
        ManAvatarImage(
            index = selectedAvatar,
            size = 216.dp,
            borderWidth = 3,
            contentDescription = "Selected avatar"
        )
        Spacer(modifier = Modifier.height(44.dp))
        ManNicknameField(
            value = nickname,
            onValueChange = onNicknameChange,
            onSubmit = onComplete,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        if (isSubmitting || submitError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isSubmitting) "Saving profile..." else submitError.orEmpty(),
                color = if (isSubmitting) Color.White else Color(0xFF4B0000),
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontFamily = GaretFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(170.dp))
        Text(
            text = "I'll choose later",
            color = Color.Black,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontFamily = GaretFontFamily,
            fontWeight = FontWeight.Medium,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(enabled = !isSubmitting, onClick = onComplete)
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun ManAvatarGrid(
    selectedAvatar: Int,
    onAvatarSelected: (Int) -> Unit
) {
    val rows = (1..AvatarGender.Male.count).chunked(4)

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
                                ManAvatarGridItem(
                                    index = index,
                                    isSelected = selectedAvatar == index,
                                    onClick = { onAvatarSelected(index) },
                                    itemSize = itemSize,
                                    avatarSize = avatarSize,
                                    avatarScale = manAvatarGridScale(index)
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
private fun ManAvatarGridItem(
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    itemSize: Dp,
    avatarSize: Dp,
    avatarScale: Float
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

        ManAvatarImage(
            index = index,
            size = avatarSize,
            borderWidth = 1,
            contentDescription = "Avatar $index",
            imageScale = avatarScale
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
                    tint = Color(0xFF2BA49D),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ManAvatarImage(
    index: Int,
    size: Dp,
    borderWidth: Int,
    contentDescription: String?,
    imageScale: Float = 1f
) {
    val cachedAvatar = rememberCachedAvatarBitmap(AvatarGender.Male, index)
    val avatarResId = manAvatarResourceId(index)

    val imageModifier = Modifier
            .size(size)
            .scale(imageScale)
            .clip(CircleShape)
            .border(borderWidth.dp, Color.White, CircleShape)

    if (cachedAvatar != null) {
        Image(
            bitmap = cachedAvatar,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = avatarResId),
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = ContentScale.Crop
        )
    }
}

private fun manAvatarGridScale(index: Int): Float {
    return when (index) {
        4, 8, 12 -> 1.1f
        else -> 1f
    }
}

private fun manAvatarResourceId(index: Int): Int {
    return when (index) {
        1 -> R.drawable.man_avatar1
        2 -> R.drawable.man_avatar1
        3 -> R.drawable.man_avatar1
        4 -> R.drawable.man_avatar1
        5 -> R.drawable.man_avatar1
        6 -> R.drawable.man_avatar1
        7 -> R.drawable.man_avatar1
        8 -> R.drawable.man_avatar1
        9 -> R.drawable.man_avatar1
        10 -> R.drawable.man_avatar1
        11 -> R.drawable.man_avatar1
        12 -> R.drawable.man_avatar1
        else -> R.drawable.gender_man
    }
}

@Composable
private fun rememberCachedAvatarBitmap(gender: AvatarGender, index: Int): ImageBitmap? {
    val context = LocalContext.current
    val avatarFile = remember(context, gender, index) {
        AvatarCache.avatarFile(context, gender, index)
    }
    var refreshKey by remember(avatarFile.absolutePath) { mutableStateOf(avatarFile.lastModified()) }

    LaunchedEffect(avatarFile.absolutePath) {
        while (!avatarFile.exists() || avatarFile.length() <= 0L) {
            delay(1_000)
            refreshKey = avatarFile.lastModified()
        }
        refreshKey = avatarFile.lastModified()
    }

    return remember(avatarFile.absolutePath, refreshKey) {
        if (avatarFile.exists() && avatarFile.length() > 0L) {
            BitmapFactory.decodeFile(avatarFile.absolutePath)?.asImageBitmap()
        } else {
            null
        }
    }
}

@Composable
private fun ManOrDivider() {
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
private fun ManPhotoAvatarCard() {
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(shape)
            .background(Color(0x26214F4B))
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
private fun ManPrimaryButton(
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
private fun ManNicknameField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
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
                color = Color(0xFFFF8AA1),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(6.dp.toPx(), 6.dp.toPx())
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
                    enabled = enabled,
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
                        .clickable(enabled = enabled, onClick = onSubmit)
                )
            }
        }
    }
}

private val ManAvatarBackground = Color(0xFF288A82)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ManAvatarScreenPreview() {
    BffAndroidTheme {
        ManAvatarScreen()
    }
}
