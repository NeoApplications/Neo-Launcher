package com.neoapps.neolauncher.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.layout
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
            1.dp,
            MaterialTheme.colorScheme.outline,
            MaterialTheme.shapes.extraLarge,
        )
}

fun Modifier.blockShadow() =
    composed {
        this
            .shadow(elevation = 1.dp, shape = MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    }

fun Modifier.vertical() =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }
