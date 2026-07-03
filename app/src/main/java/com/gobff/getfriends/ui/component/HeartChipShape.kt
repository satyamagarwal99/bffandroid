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
object HeartChipShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {

        val originalWidth = 88f
        val originalHeight = 32f

        val path = PathParser()
            .parsePathString(
                "M41.516 0.0295115C41.576 0.0282384 41.6356 0.0272147 41.6957 0.0264404C49.1466 -0.0177111 56.5979 -0.00656827 64.0484 0.0598722C68.0295 0.118247 72.072 -0.0228461 75.9469 0.686871C82.1608 1.82504 83.2047 5.40034 83.6994 9.32199C83.8745 10.6457 83.9733 11.974 83.9959 13.3034C84.0047 13.8157 83.9973 14.3278 83.9918 14.8425C83.9535 18.4217 83.7331 22.0459 83.1899 25.6036C82.9775 26.9951 82.4509 28.5597 81.1659 29.6543C78.5917 31.8476 73.9344 31.7245 70.2826 31.7376C68.7057 31.7445 67.1288 31.7577 65.5523 31.7777L42.2407 31.9184L27.0305 31.9965C24.8294 32.0067 22.6282 31.9946 20.4277 31.9598C13.8198 31.8587 4.87016 31.2885 1.75691 26.3428C0.621609 24.5394 0.376572 21.6799 0.213891 19.7067C0.128809 18.8222 0.0672844 17.9367 0.0292701 17.0506C-0.0688373 14.5267 0.0797556 12.0009 0.47394 9.49163C0.745722 7.852 1.15783 6.28996 1.69959 4.68928C2.4784 2.38822 4.8036 1.31439 7.95528 0.883774C10.4809 0.538693 13.124 0.475114 15.6764 0.433358L20.6321 0.343434L41.516 0.0295115Z"
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