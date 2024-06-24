package com.saggitt.omega.compose.components

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.saggitt.omega.util.addIf
import kotlinx.coroutines.awaitCancellation

@Composable
fun PreferenceLazyColumn(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    state: LazyListState = rememberLazyListState(),
    isChild: Boolean = false,
    content: LazyListScope.() -> Unit
) {
    if (!enabled) {
        LaunchedEffect(key1 = null) {
            state.scroll(scrollPriority = MutatePriority.PreventUserInput) {
                awaitCancellation()
            }
        }
    }

    NestedScrollStretch {
        LazyColumn(
            modifier = modifier
                .addIf(!isChild) {
                    fillMaxHeight()
                },
            state = state,
            content = content
        )
    }
}