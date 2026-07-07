package com.gobff.getfriends.ui.component

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Stable
object ChatBubbleShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {

        val originalWidth = 257f
        val originalHeight = 68f

        val path = PathParser()
            .parsePathString(
                "M129.981 63.941C129.797 63.9435 129.615 63.9456 129.431 63.9471C106.635 64.0354 83.8373 64.0131 61.0425 63.8802C48.8622 63.7635 36.4939 64.0457 24.6387 62.6262C5.62701 60.3499 2.4332 53.1993 0.919679 45.356C0.384065 42.7085 0.0816363 40.052 0.0123921 37.3931C-0.0144633 36.3687 0.00815028 35.3445 0.0251181 34.315C0.142398 27.1567 0.816501 19.9082 2.47841 12.7928C3.12848 10.0098 4.73954 6.88054 8.67102 4.69135C16.5467 0.304884 30.796 0.550971 41.9686 0.524723C46.7933 0.510941 51.6179 0.484697 56.4412 0.444666L127.764 0.163145L174.3 0.00696842C181.034 -0.0133747 187.768 0.0109066 194.501 0.0804683C214.718 0.282588 242.1 1.42313 251.625 11.3143C255.098 14.9213 255.848 20.6402 256.346 24.5866C256.606 26.3556 256.794 28.1267 256.91 29.8989C257.211 34.9467 256.756 39.9981 255.55 45.0167C254.718 48.296 253.458 51.4201 251.8 54.6215C249.417 59.2236 242.303 61.3712 232.661 62.2324C224.933 62.9226 216.847 63.0498 209.038 63.1333L193.876 63.3131L129.981 63.941Z"
            )
            .toPath()

        val matrix = Matrix().apply {
            scale(
                size.width / originalWidth,
                size.height / originalHeight
            )
        }

        path.transform(matrix)

        return Outline.Generic(path)
    }
}