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
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.neoapps.neolauncher.search.AbstractSearchProvider
import com.neoapps.neolauncher.util.isAppEnabled

@Keep
class BaiduSearchProvider(context: Context) : AbstractSearchProvider(context) {
    override val name = context.getString(R.string.search_provider_baidu)
    override val supportsVoiceSearch = true
    override val supportsAssistant = false
    override val supportsFeed = true
    override val id = 1001L
    override val packageName: String
        get() = "com.baidu.searchbox"

    override val isAvailable: Boolean
        get() = context.packageManager.isAppEnabled(packageName, 0)

    override fun startSearch(callback: (intent: Intent) -> Unit) =
        callback(
            Intent(Intent.ACTION_ASSIST)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setPackage(packageName)
        )

    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) =
        callback(
            Intent(Intent.ACTION_SEARCH_LONG_PRESS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setPackage(packageName)
        )

    override fun startFeed(callback: (intent: Intent) -> Unit) =
        callback(
            Intent("$packageName.action.HOME")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setPackage(packageName)
        )

    override val iconRes: Int
        get() = R.drawable.ic_baidu
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, iconRes, null)!!

    override val voiceIcon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_qsb_mic, null)!!
            .mutate()
            .apply {
                setTint(Color.rgb(0x2d, 0x03, 0xe4))
            }
}
