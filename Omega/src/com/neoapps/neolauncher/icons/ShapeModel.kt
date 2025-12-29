/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neoapps.neolauncher.icons

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class ShapeModel(val shapeName: String) {

    fun getIcon(): Shape {
        return when (shapeName) {
            "circle" -> CircleShape
            "square" -> RoundedCornerShape(corner = CornerSize(4.dp))
            "roundedSquare" -> RoundedCornerShape(corner = CornerSize(16.dp))
            "squircle" -> SquircleShape()
            "sammy" -> SammyShape()
            "teardrop" -> TearDropShape
            "cylinder" -> CylinderShape()
            "cupertino" -> RoundedCornerShape(corner = CornerSize(12.dp))
            "hexagon" -> HexagonShape()
            "octagon" -> CutCornerShape(25)
            "egg" -> Egg
            else -> CircleShape
        }
    }
}

class SquircleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(Path().apply {
            reset()
            moveTo(0.0f, size.height * 3 / 5)
            lineTo(0.0f, size.height * 2 / 5)
            cubicTo(
                0.0f, size.height / 6,
                size.width / 6, 0.0f,
                size.width * 2 / 5, 0.0f
            )
            lineTo(size.width * 3 / 5, 0.0f)
            cubicTo(
                size.width * 5 / 6, 0.0f,
                size.width, size.height / 6,
                size.width, size.height * 2 / 5
            )
            lineTo(size.width, size.height * 3 / 5)
            cubicTo(
                size.width, size.height * 5 / 6,
                size.width * 5 / 6, size.height,
                size.width * 3 / 5, size.height
            )
            lineTo(size.width * 2 / 5, size.height)
            cubicTo(
                size.width / 6, size.height,
                0.0f, size.height * 5 / 6,
                0.0f, size.height * 3 / 5
            )
            close()
        }
        )
    }
}

class SammyShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(Path().apply {
            reset()
            moveTo(0.0f, size.height / 2)
            cubicTo(
                0.0f, size.height / 6,
                size.width / 6, 0.0f,
                size.width / 2, 0.0f
            )
            cubicTo(
                size.width * 5 / 6, 0.0f,
                size.width, size.height / 6,
                size.width, size.height / 2
            )
            cubicTo(
                size.width, size.height * 5 / 6,
                size.width * 5 / 6, size.height,
                size.width / 2, size.height
            )
            cubicTo(
                size.width / 6, size.height,
                0.0f, size.height * 5 / 6,
                0.0f, size.height / 2
            )
            close()
        }
        )
    }
}

class CylinderShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(Path().apply {
            reset()
            moveTo(0.0f, size.height * 2 / 7)
            lineTo(0.0f, size.height * 5 / 7)
            quadraticBezierTo(
                size.width / 13, size.height,
                size.width / 2, size.height
            )
            quadraticBezierTo(
                size.width * 12 / 13, size.height,
                size.width, size.height * 5 / 7
            )
            lineTo(size.width, size.height * 2 / 7)
            quadraticBezierTo(
                size.width * 12 / 13, 0.0f,
                size.width / 2, 0.0f
            )
            quadraticBezierTo(
                size.width / 13, 0.0f,
                0.0f, size.height * 2 / 7
            )
            close()
        })
    }
}

class HexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        val sideLength = Math.min(width, height) / 2
        val apothem = (sideLength * Math.sqrt(3.0) / 2).toFloat()

        return Outline.Generic(Path().apply {
            reset()
            moveTo(centerX, centerY - sideLength)
            lineTo(centerX + apothem, centerY - sideLength / 2)
            lineTo(centerX + apothem, centerY + sideLength / 2)
            lineTo(centerX, centerY + sideLength)
            lineTo(centerX - apothem, centerY + sideLength / 2)
            lineTo(centerX - apothem, centerY - sideLength / 2)
            close()
        })
    }
}

val TearDropShape = RoundedCornerShape(
    topStartPercent = 50,
    topEndPercent = 50,
    bottomStartPercent = 50,
    bottomEndPercent = 15
)

val Egg = RoundedCornerShape(
    topStartPercent = 100,
    topEndPercent = 100,
    bottomStartPercent = 75,
    bottomEndPercent = 75
)