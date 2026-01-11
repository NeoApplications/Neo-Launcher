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
package com.neoapps.neolauncher.compose.pages.preferences

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Canvas
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.components.ActionItemPreference
import com.neoapps.neolauncher.compose.components.ContributorRow
import com.neoapps.neolauncher.compose.components.ViewWithActionBar
import com.neoapps.neolauncher.compose.components.preferences.PagePreference
import com.neoapps.neolauncher.compose.components.preferences.PreferenceGroupHeading
import com.neoapps.neolauncher.compose.icons.Phosphor
import com.neoapps.neolauncher.compose.icons.phosphor.BracketsSquare
import com.neoapps.neolauncher.compose.icons.phosphor.GithubLogo
import com.neoapps.neolauncher.compose.icons.phosphor.Megaphone
import com.neoapps.neolauncher.compose.icons.phosphor.TelegramLogo
import com.neoapps.neolauncher.compose.objects.PageItem
import com.neoapps.neolauncher.theme.kaushanScript
import com.neoapps.neolauncher.util.Config
import java.io.InputStream

@Composable
fun AboutPrefPage() {
    ViewWithActionBar(
        title = stringResource(R.string.title__general_about),
    ) { paddingValues ->
        LazyColumn(
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
                        .padding(4.dp),
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
                                LocalResources.current,
                                R.drawable.ic_launcher,
                                LocalContext.current.theme
                            )?.let { drawable ->
                                val bitmap =
                                    createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
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
                            }
                        }
                    )
                }
            }
            item {
                PreferenceGroupHeading(stringResource(id = R.string.about_team))
            }
            itemsIndexed(developers) { i, it ->
                ContributorRow(
                    nameId = it.name,
                    roleId = it.descriptionRes,
                    photoUrl = it.photoUrl,
                    url = it.webpage,
                    index = i,
                    groupSize = developers.size
                )
            }

            item {
                PreferenceGroupHeading(stringResource(id = R.string.about_community))
            }

            val groupSize = community.size
            itemsIndexed(community) { index, item ->
                ActionItemPreference(
                    titleId = item.labelResId,
                    icon = item.icon,
                    url = item.url,
                    index = index,
                    groupSize = groupSize
                )
            }

            item {
                PreferenceGroupHeading(stringResource(id = R.string.about_build_information))
            }

            item {
                ActionItemPreference(
                    titleId = R.string.about_source,
                    icon = Phosphor.GithubLogo,
                    url = "https://github.com/NeoApplications/Neo-Launcher",
                    index = 0,
                    groupSize = 4
                )
            }

            val pages =
                listOf(PageItem.AboutTranslators, PageItem.AboutLicense, PageItem.Acknowledgement)
            itemsIndexed(pages) { i, page ->
                PagePreference(
                    titleId = page.titleId,
                    icon = page.icon,
                    route = page.route,
                    index = i + 1,
                    groupSize = 4,
                    modifier = Modifier
                )
            }
        }
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

private val community = listOf(
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

private val developers = listOf(
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
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding()
            ),
        ) {
            ComposableWebView(url = "file:///android_asset/license.htm")
        }
    }
}

@Composable
fun AcknowledgementScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.title__about_acknowledgement)
    ) {
        Column(
            modifier = Modifier.padding(
                top = it.calculateTopPadding() + 8.dp,
                bottom = it.calculateBottomPadding()
            )
        ) {
            ComposableWebView(url = "file:///android_asset/acknowledgement.htm")
        }
    }
}


@Composable
fun ChangelogScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.title__about_changelog),
    ) {
        Column(
            modifier = Modifier.padding(
                top = it.calculateTopPadding() + 8.dp,
                bottom = it.calculateBottomPadding()
            )
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
            modifier = Modifier.padding(
                top = it.calculateTopPadding() + 8.dp,
                bottom = it.calculateBottomPadding()
            )
        ) {
            ComposableWebView(url = "file:///android_asset/translators.htm")
        }
    }
}

@Composable
fun ComposableWebView(url: String) {

    val cssFile = when (Config.getCurrentTheme(LocalContext.current)) {
        Config.THEME_BLACK -> "black.css"
        Config.THEME_DARK -> "dark.css"
        else -> "light.css"
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

@Preview
@Composable
fun AboutPrefPagePreview() {
    AboutPrefPage()
}