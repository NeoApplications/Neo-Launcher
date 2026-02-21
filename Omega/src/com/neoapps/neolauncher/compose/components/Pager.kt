package com.neoapps.neolauncher.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun HorizontalPagerPage(
    pagerState: PagerState,
    tabs: List<TabItem>,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
    ) { page ->
        tabs[page].screen()
    }
}

@Composable
fun HorizontalPagerNavBar(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    NavigationBar(
        modifier = Modifier.padding(horizontal = 8.dp),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        tabs.forEachIndexed { index, tab ->
            NavBarItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                icon = painterResource(id = tab.icon),
                labelId = tab.title,
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

@Composable
fun NavBarItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    labelId: Int,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = icon,
                contentDescription = stringResource(id = labelId),
                modifier = Modifier
                    .background(
                        if (selected) MaterialTheme.colorScheme.surfaceContainerHighest
                        else Color.Transparent,
                        CircleShape
                    )
                    .padding(8.dp)
                    .size(if (selected) 36.dp else 26.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            if (!selected)
                Text(
                    text = stringResource(id = labelId),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
        }
    }
}