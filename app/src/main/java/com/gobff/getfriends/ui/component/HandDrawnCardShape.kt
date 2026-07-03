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
object HandDrawnCardShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {

        val originalWidth = 130f
        val originalHeight = 140f

        val sx = size.width / originalWidth
        val sy = size.height / originalHeight

        val path = PathParser()
            .parsePathString(
                "M69.1997 139.856C57.9309 140.495 46.6222 138.795 35.3541 138.867C27.6373 138.982 19.9773 139.428 12.2564 138.867C9.23209 138.41 4.45773 139.856 2.12406 130.723C0.0372012 122.555 0.0692297 100.822 0.0255897 89.8296C0.00172514 80.154 -0.00576678 70.4781 0.00447202 60.802C0.00309893 47.5901 -0.0725845 34.3296 0.595552 21.3176C1.24594 8.66438 3.28852 3.68107 6.30939 2.22053C9.05075 0.895584 11.8153 1.07674 14.573 1.02013L27.2074 1.04141C38.7769 1.13336 50.347 0.796758 61.9151 0.0315664C66.5204 -0.0454729 71.1253 0.0183112 75.7301 0.223758L99.6399 0.916351L113.827 1.03287C117.538 1.06519 125.34 -0.944582 127.902 9.21342C130.625 20.0046 129.905 58.9955 129.936 73.8856C129.968 89.638 130.4 118.58 127.929 130.435C125.893 140.201 118.753 138.828 115.769 138.899L104.415 139.038C92.6766 139.045 80.9379 139.317 69.1997 139.856Z"
            )
            .toPath()

        val matrix = Matrix().apply {
            scale(sx, sy)
        }

        path.transform(matrix)

        return Outline.Generic(path)
    }
}