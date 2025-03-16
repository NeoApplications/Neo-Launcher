/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.neoapps.launcher.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import com.android.launcher3.Launcher
import com.android.launcher3.Utilities
import com.neoapps.launcher.preferences.NeoPrefs
import com.neoapps.launcher.util.CoreUtils.Companion.minSDK
import java.util.Locale

fun Context.getLauncherOrNull(): Launcher? {
    return try {
        Launcher.getLauncher(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun Context.getIcon(): Drawable = packageManager.getApplicationIcon(applicationInfo)

fun <T> useApplicationContext(creator: (Context) -> T): (Context) -> T {
    return { it -> creator(it.applicationContext) }
}

val Context.locale: Locale
    get() = this.resources.configuration.locales[0]

val Context.prefs: NeoPrefs
    get() = NeoPrefs.getInstance()


@ColorInt
fun Context.getColorAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    @ColorInt val colorAccent = ta.getColor(0, 0)
    ta.recycle()
    return colorAccent
}

fun Context.getSystemAccent(darkTheme: Boolean): Int {
    val res = resources
    return if (minSDK(Build.VERSION_CODES.S)) {
        val colorName = if (darkTheme) "system_accent1_100" else "system_accent1_600"
        val colorId = res.getIdentifier(colorName, "color", "android")
        res.getColor(colorId)
    } else {
        var propertyValue = Utilities.getSystemProperty("persist.sys.theme.accentcolor", "")
        if (!TextUtils.isEmpty(propertyValue)) {
            if (!propertyValue.startsWith('#')) {
                propertyValue = "#$propertyValue"
            }
            try {
                return propertyValue.toColorInt()
            } catch (e: IllegalArgumentException) {
                // do nothing
            }
        }

        val typedValue = TypedValue()
        val theme =
            if (darkTheme) android.R.style.Theme_DeviceDefault else android.R.style.Theme_DeviceDefault_Light
        val contextWrapper = ContextThemeWrapper(this, theme)
        contextWrapper.theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
        typedValue.data
    }
}