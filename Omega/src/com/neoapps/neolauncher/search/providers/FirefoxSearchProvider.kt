/*
 * This file is part of Neo Launcher
 * Copyright (c) 2024   Neo Launcher Team
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
 */

package com.neoapps.neolauncher.search.providers

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.neoapps.neolauncher.search.AbstractSearchProvider
import com.neoapps.neolauncher.util.isAppEnabled

@Keep
open class FirefoxSearchProvider(context: Context) : AbstractSearchProvider(context) {
    override val name = context.getString(R.string.search_provider_firefox)
    override val supportsVoiceSearch = false
    override val supportsAssistant = false
    override val supportsFeed = true
    override val id = 1005L
    override val packageName: String
        get() = getPackage(context)!!

    override val isAvailable: Boolean
        get() = getPackage(context) != null

    override fun startSearch(callback: (intent: Intent) -> Unit) =
        callback(
            Intent(Intent.ACTION_ASSIST).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setPackage(getPackage(context))
        )

    override fun startFeed(callback: (intent: Intent) -> Unit) = callback(
        Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setPackage(getPackage(context))
    )

    override val iconRes: Int
        get() = R.drawable.ic_firefox
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, iconRes, null)!!

    open fun getPackage(context: Context) = listOf(
        "org.mozilla.firefox",
        "org.mozilla.fennec_fdroid",
        "org.mozilla.firefox_beta",
        "org.mozilla.fennec_aurora",
        "org.mozilla.focus",
        "org.mozilla.fenix"
    ).firstOrNull { context.packageManager.isAppEnabled(it, 0) }
}
