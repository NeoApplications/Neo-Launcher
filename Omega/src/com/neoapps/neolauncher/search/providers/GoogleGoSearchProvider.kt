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
class GoogleGoSearchProvider(context: Context) : AbstractSearchProvider(context) {

    override val name = context.getString(R.string.search_provider_google_go)
    override val supportsVoiceSearch = true
    override val supportsAssistant = false
    override val supportsFeed = true
    override val id = 1005L
    override val packageName: String
        get() = "com.google.android.apps.searchlite"
    override val isAvailable: Boolean
        get() = context.packageManager.isAppEnabled(packageName, 0)

    override fun startSearch(callback: (intent: Intent) -> Unit) =
        callback(
            Intent("$packageName.SEARCH").putExtra("showKeyboard", true)
                .putExtra("$packageName.SKIP_BYPASS_AND_ONBOARDING", true).setPackage(packageName)
        )

    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) =
        callback(
            Intent("$packageName.SEARCH").putExtra("openMic", true)
                .putExtra("$packageName.SKIP_BYPASS_AND_ONBOARDING", true).setPackage(packageName)
        )

    override fun startFeed(callback: (intent: Intent) -> Unit) =
        callback(
            Intent("$packageName.SEARCH").putExtra("$packageName.SKIP_BYPASS_AND_ONBOARDING", true)
                .setPackage(packageName)
        )

    override val iconRes: Int
        get() = R.drawable.ic_super_g_color
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, iconRes, null)!!

    override val voiceIcon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_mic_color, null)!!
}