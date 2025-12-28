/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.compose.pages.preferences

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.android.launcher3.R
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.icons.Phosphor
import com.saggitt.omega.compose.icons.phosphor.BracketsSquare
import com.saggitt.omega.compose.icons.phosphor.GithubLogo
import com.saggitt.omega.compose.icons.phosphor.Megaphone
import com.saggitt.omega.compose.icons.phosphor.TelegramLogo
import com.saggitt.omega.util.Config
import java.io.InputStream

@Composable
fun AboutPrefPage() {
    ViewWithActionBar(
        title = stringResource(R.string.title__general_about),
    ) { paddingValues ->
        /*LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            MaterialTheme.shapes.extraLarge
                        )
                        .clip(MaterialTheme.shapes.extraLarge),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ListItem(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        leadingContent = {
                            ResourcesCompat.getDrawable(
                                LocalContext.current.resources,
                                R.drawable.ic_launcher,
                                LocalContext.current.theme
                            )?.let { drawable ->
                                val bitmap = Bitmap.createBitmap(
                                    drawable.intrinsicWidth,
                                    drawable.intrinsicHeight,
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bitmap)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .requiredSize(84.dp)
                                        .clip(MaterialTheme.shapes.large)
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = stringResource(id = R.string.derived_app_name),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = kaushanScript,
                            )
                        },
                        supportingContent = {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.app_version) + ": "
                                            + BuildConfig.VERSION_NAME + " ( Build " + BuildConfig.VERSION_CODE + " )",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = BuildConfig.APPLICATION_ID,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(8.dp),
                    ) {
                        items(links) { link ->
                            ItemLink(
                                icon = link.icon,
                                label = stringResource(id = link.labelResId),
                                url = link.url
                            )
                        }
                    }
                }
            }
            item {
                PreferenceGroupHeading(stringResource(id = R.string.about_team))
            }
            itemsIndexed(contributors) { i, it ->
                ContributorRow(
                    nameId = it.name,
                    roleId = it.descriptionRes,
                    photoUrl = it.photoUrl,
                    url = it.webpage,
                    index = i,
                    groupSize = contributors.size
                )
            }
            item {
                PreferenceGroupHeading(stringResource(id = R.string.about_translators_group))
            }
            item {
                ContributorRow(
                    nameId = R.string.contributor2,
                    roleId = R.string.contributor_role,
                    photoUrl = "https://avatars.githubusercontent.com/u/69337602",
                    url = "https://github.com/nonaybay",
                    index = 0,
                    groupSize = 2
                )
            }
            item {
                val page = PageItem.AboutTranslators
                PagePreference(
                    titleId = page.titleId,
                    icon = page.icon,
                    route = page.route,
                    index = 1,
                    groupSize = 2
                )
            }
            item {
                PreferenceGroupHeading(stringResource(id = R.string.about_build_information))
            }
            itemsIndexed(listOf(PageItem.AboutLicense, PageItem.AboutChangelog)) { i, page ->
                PagePreference(
                    titleId = page.titleId,
                    icon = page.icon,
                    route = page.route,
                    index = i,
                    groupSize = 2
                )
            }
        }*/

    }
}

private data class Link(
    val icon: ImageVector,
    @StringRes val labelResId: Int,
    val url: String,
)

private data class TeamMember(
    @StringRes val name: Int,
    @StringRes val descriptionRes: Int,
    val photoUrl: String,
    val webpage: String,
)

private val links = listOf(
    Link(
        icon = Phosphor.GithubLogo,
        labelResId = R.string.about_source,
        url = "https://github.com/NeoApplications/Neo-Launcher"
    ),
    Link(
        icon = Phosphor.Megaphone,
        labelResId = R.string.about_channel,
        url = "https://t.me/neo_applications"
    ),
    Link(
        icon = Phosphor.TelegramLogo,
        labelResId = R.string.about_community_telegram,
        url = "https://t.me/neo_launcher"
    ),
    Link(
        icon = Phosphor.BracketsSquare,
        labelResId = R.string.about_community_matrix,
        url = "https://matrix.to/#/#neo-launcher:matrix.org"
    )
)

private val contributors = listOf(
    TeamMember(
        name = R.string.author,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/6044050",
        webpage = "https://github.com/saulhdev"
    ),
    TeamMember(
        name = R.string.contributor1,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/40302595",
        webpage = "https://github.com/machiav3lli"
    )
)

@Composable
fun LicenseScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.about_open_source),
    ) {
        Column(
            modifier = Modifier.padding(top = it.calculateTopPadding() + 8.dp)
        ) {
            ComposableWebView(url = "file:///android_asset/license.htm")
        }
    }
}

@Composable
fun ChangelogScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.title__about_changelog),
    ) {
        Column(
            modifier = Modifier.padding(top = it.calculateTopPadding() + 8.dp)
        ) {
            ComposableWebView(url = "file:///android_asset/changelog.htm")
        }
    }
}

@Composable
fun TranslatorsScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.about_translators),
    ) {
        Column(
            modifier = Modifier.padding(top = it.calculateTopPadding() + 8.dp)
        ) {
            ComposableWebView(url = "file:///android_asset/translators.htm")
        }
    }
}

@Composable
fun ComposableWebView(url: String) {

    val cssFile = when (Config.getCurrentTheme(LocalContext.current)) {
        Config.THEME_BLACK -> "black.css"
        Config.THEME_DARK  -> "dark.css"
        else               -> "light.css"
    }
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        if (url.startsWith("file:///android_asset")) {
                            try {
                                settings.javaScriptEnabled = true
                                val inputStream: InputStream = context.assets.open(cssFile)
                                val buffer = ByteArray(inputStream.available())
                                inputStream.read(buffer)
                                inputStream.close()
                                val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
                                loadUrl(
                                    "javascript:(function() { " +
                                            "var head  = document.getElementsByTagName('head')[0];" +
                                            "var style = document.createElement('style');" +
                                            "style.type = 'text/css';" +
                                            "style.innerHTML =  window.atob('" + encoded + "');" +
                                            "head.appendChild(style);" +
                                            "})()"
                                )
                                settings.javaScriptEnabled = false
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        super.onPageFinished(view, url)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        if (url.contains("file://")) {
                            view.loadUrl(url)
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            try {
                                ContextCompat.startActivity(context, intent, null)
                            } catch (e: ActivityNotFoundException) {
                                view.loadUrl(url)
                            }
                        }
                        return true
                    }
                }
            }
        },
        update = { webView -> webView.loadUrl(url) }
    )
}
