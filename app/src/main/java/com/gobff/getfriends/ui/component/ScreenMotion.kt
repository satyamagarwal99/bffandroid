package com.gobff.getfriends.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun Modifier.screenEnterMotion(
    index: Int = 0,
    initialOffsetY: Dp = 18.dp,
    initialScale: Float = 0.985f,
    durationMillis: Int = 420
): Modifier {
    var visible by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val delayMillis = (index.coerceAtLeast(0) * 55).toLong()

    LaunchedEffect(Unit) {
        if (delayMillis > 0L) delay(delayMillis)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "screenEnterAlpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else initialOffsetY,
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "screenEnterOffset"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else initialScale,
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "screenEnterScale"
    )

    return graphicsLayer {
        this.alpha = alpha
        translationY = with(density) { offsetY.toPx() }
        scaleX = scale
        scaleY = scale
    }
}
