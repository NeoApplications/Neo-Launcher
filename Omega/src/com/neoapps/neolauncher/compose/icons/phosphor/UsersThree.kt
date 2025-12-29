package com.neoapps.neolauncher.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.neoapps.neolauncher.compose.icons.Phosphor

val Phosphor.UsersThree: ImageVector
    get() {
        if (_users_three != null) {
            return _users_three!!
        }
        _users_three = Builder(
            name = "Users-three",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(248.8f, 146.4f)
                arcTo(7.7f, 7.7f, 0.0f, false, true, 244.0f, 148.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -6.4f, -3.2f)
                arcTo(51.6f, 51.6f, 0.0f, false, false, 196.0f, 124.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, false, -23.6f, -28.5f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -15.7f, -3.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, true, 66.3f, 37.0f)
                arcToRelative(67.8f, 67.8f, 0.0f, false, true, 27.4f, 21.7f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 248.8f, 146.4f)
                close()
                moveTo(192.8f, 212.5f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -3.7f, 10.7f)
                arcToRelative(9.3f, 9.3f, 0.0f, false, true, -3.5f, 0.8f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -7.2f, -4.5f)
                arcToRelative(56.1f, 56.1f, 0.0f, false, false, -100.8f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -10.7f, 3.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -3.7f, -10.7f)
                arcToRelative(72.1f, 72.1f, 0.0f, false, true, 35.6f, -34.4f)
                arcToRelative(48.0f, 48.0f, 0.0f, true, true, 58.4f, 0.0f)
                arcTo(72.1f, 72.1f, 0.0f, false, true, 192.8f, 212.5f)
                close()
                moveTo(128.0f, 172.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, false, -32.0f, -32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 128.0f, 172.0f)
                close()
                moveTo(68.0f, 116.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                arcTo(24.0f, 24.0f, 0.0f, true, true, 83.6f, 79.5f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 15.7f, -3.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, false, -66.3f, 37.0f)
                arcTo(67.8f, 67.8f, 0.0f, false, false, 5.6f, 135.2f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 1.6f, 11.2f)
                arcTo(7.7f, 7.7f, 0.0f, false, false, 12.0f, 148.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 6.4f, -3.2f)
                arcTo(51.6f, 51.6f, 0.0f, false, true, 60.0f, 124.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 68.0f, 116.0f)
                close()
            }
        }
            .build()
        return _users_three!!
    }

private var _users_three: ImageVector? = null
