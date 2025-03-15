/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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

package com.neoapps.launcher.wallpaper

import android.app.WallpaperManager
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.SafeCloseable
import com.neoapps.launcher.util.CoreUtils
import com.neoapps.launcher.wallpaper.WallpaperColorsCompat.Companion.HINT_SUPPORTS_DARK_THEME

sealed class WallpaperManagerCompat(val context: Context) : SafeCloseable {
    private val listeners = mutableListOf<OnColorsChangedListenerCompat>()
    private val colorHints: Int get() = wallpaperColors?.getColorHints() ?: 0
    protected val wallpaperManager: WallpaperManager =
        checkNotNull(getSystemService(context, WallpaperManager::class.java))

    abstract val wallpaperColors: WallpaperColorsCompat?

    val supportsDarkTheme: Boolean get() = (colorHints and HINT_SUPPORTS_DARK_THEME) != 0

    open fun addOnChangeListener(listener: OnColorsChangedListenerCompat) {
        listeners.add(listener)
    }

    fun removeOnChangeListener(listener: OnColorsChangedListenerCompat) {
        listeners.remove(listener)
    }

    interface OnColorsChangedListenerCompat {
        fun onColorsChanged(colors: WallpaperColorsCompat?, which: Int)
    }

    override fun close() {

    }

    companion object {

        @JvmField
        val INSTANCE = MainThreadInitializedObject { context ->
            when {
                CoreUtils.AT_LEAST_S -> WallpaperManagerCompatVS(context)
                CoreUtils.AT_LEAST_OREO_MR1 -> WallpaperManagerCompatVOMR1(context)
                else -> WallpaperManagerCompatVO(context)
            }
        }
    }
}