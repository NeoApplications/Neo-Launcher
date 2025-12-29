package com.neoapps.neolauncher.compose.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun ElevatedIcon(originalIcon: Drawable) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.medium
            )
            .requiredSize(60.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = originalIcon.toBitmap(128, 128).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.requiredSize(48.dp)
        )
    }
}