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
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.search.AbstractSearchProvider
import com.neoapps.neolauncher.util.isAppEnabled

@Keep
class SFinderSearchProvider(context: Context) : AbstractSearchProvider(context) {

    override val name = context.getString(R.string.search_provider_s_finder)
    override val supportsVoiceSearch: Boolean
        get() = true

    override val supportsAssistant: Boolean
        get() = false
    override val supportsFeed = false
    override val id = 1006L
    override val packageName: String
        get() = PACKAGE

    override val isAvailable: Boolean
        get() = context.packageManager.isAppEnabled(PACKAGE, 0)

    override fun startSearch(callback: (intent: Intent) -> Unit) {
        callback(
            Intent(Intent.ACTION_MAIN)
                .setClassName(PACKAGE, CLASS)
        )
    }

    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) {
        callback(
            Intent(Intent.ACTION_MAIN)
                .setClassName(PACKAGE, CLASS)
                .putExtra("launch_mode", "voice_input")
        )
    }

    override val iconRes: Int
        get() = R.drawable.ic_search
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, iconRes, null)!!
            .mutate().apply { setTint(NeoPrefs.getInstance().profileAccentColor.getColor()) }

    override val voiceIcon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_mic, null)!!
            .mutate()
            .apply {
                setTint(NeoPrefs.getInstance().profileAccentColor.getColor())
            }

    companion object {
        const val PACKAGE = "com.samsung.android.app.galaxyfinder"
        const val CLASS = "$PACKAGE.GalaxyFinderActivity"
    }
}
