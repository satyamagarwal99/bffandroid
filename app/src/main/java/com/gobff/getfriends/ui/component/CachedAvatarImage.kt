package com.gobff.getfriends.ui.component

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.gobff.getfriends.utils.AvatarCache
import kotlinx.coroutines.delay

@Composable
fun CachedAvatarImage(
    avatarUrl: String?,
    gender: String? = null,
    @DrawableRes fallbackRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val cachedAvatar = rememberCachedAvatarBitmap(avatarUrl, gender)

    if (cachedAvatar != null) {
        Image(
            bitmap = cachedAvatar,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
private fun rememberCachedAvatarBitmap(avatarUrl: String?, gender: String?): ImageBitmap? {
    val context = LocalContext.current
    val key = remember(avatarUrl, gender) { AvatarCache.parseAvatarKey(avatarUrl, gender) }
    val avatarFile = remember(context, key) {
        key?.let { AvatarCache.avatarFile(context, it.gender, it.index) }
    }
    var refreshKey by remember(avatarFile?.absolutePath) { mutableStateOf(avatarFile?.lastModified() ?: 0L) }

    LaunchedEffect(avatarFile?.absolutePath) {
        val file = avatarFile ?: return@LaunchedEffect
        while (!file.exists() || file.length() <= 0L) {
            delay(1_000)
            refreshKey = file.lastModified()
        }
        refreshKey = file.lastModified()
    }

    return remember(avatarFile?.absolutePath, refreshKey) {
        val file = avatarFile
        if (file != null && file.exists() && file.length() > 0L) {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } else {
            null
        }
    }
}
