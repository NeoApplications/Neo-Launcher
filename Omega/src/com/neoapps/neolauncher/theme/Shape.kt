package com.neoapps.neolauncher.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun GroupItemShape(index: Int, lastIndex: Int) = RoundedCornerShape(
    topStart = if (index == 0) MaterialTheme.shapes.large.topStart
    else MaterialTheme.shapes.extraSmall.topStart,
    topEnd = if (index == 0) MaterialTheme.shapes.large.topEnd
    else MaterialTheme.shapes.extraSmall.topEnd,
    bottomStart = if (index == lastIndex) MaterialTheme.shapes.large.bottomStart
    else MaterialTheme.shapes.extraSmall.bottomStart,
    bottomEnd = if (index == lastIndex) MaterialTheme.shapes.large.bottomEnd
    else MaterialTheme.shapes.extraSmall.bottomEnd
)