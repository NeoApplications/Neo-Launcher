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

package com.neoapps.neolauncher.search

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

abstract class AbstractSearchProvider(val context: Context) {
    abstract val name: String
    abstract val packageName: String
    abstract val icon: Drawable
    abstract val iconRes: Int
    abstract val supportsVoiceSearch: Boolean
    abstract val supportsAssistant: Boolean
    abstract val supportsFeed: Boolean
    abstract val id: Long

    open val isAvailable: Boolean = false
    abstract fun startSearch(callback: (intent: Intent) -> Unit = {})
    open fun startVoiceSearch(callback: (intent: Intent) -> Unit = {}) {
        if (supportsVoiceSearch) throw RuntimeException("Voice search supported but not implemented")
    }

    open fun startAssistant(callback: (intent: Intent) -> Unit = {}) {
        if (supportsAssistant) throw RuntimeException("Assistant supported but not implemented")
    }

    open fun startFeed(callback: (intent: Intent) -> Unit = {}) {
        if (supportsFeed) throw RuntimeException("Feed supported but not implemented")
    }

    open val voiceIcon: Drawable?
        get() = if (supportsVoiceSearch)
            throw RuntimeException("Voice search supported but not implemented")
        else null
    open val assistantIcon: Drawable?
        get() = if (supportsVoiceSearch)
            throw RuntimeException("Assistant supported but not implemented")
        else null
}