package com.saggitt.omega.util

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: Modifier.() -> Modifier,
): Modifier =
    if (condition) factory() else this

inline fun <T> Modifier.addIfNotNull(
    value: T?,
    crossinline factory: Modifier.(T) -> Modifier,
): Modifier =
    if (value != null) factory(value) else this

fun Modifier.blockBorder() = composed {
    this
        .clip(MaterialTheme.shapes.extraLarge)
        .border(
            2.dp,
            MaterialTheme.colorScheme.outlineVariant,
            MaterialTheme.shapes.extraLarge,
        )
}
