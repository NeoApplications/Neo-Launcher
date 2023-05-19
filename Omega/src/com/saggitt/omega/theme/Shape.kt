package com.saggitt.omega.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

fun GroupItemShape(index: Int, lastIndex: Int) = RoundedCornerShape(
    topStart = if (index == 0) 24.dp else 8.dp,
    topEnd = if (index == 0) 24.dp else 8.dp,
    bottomStart = if (index == lastIndex) 24.dp else 8.dp,
    bottomEnd = if (index == lastIndex) 24.dp else 8.dp
)