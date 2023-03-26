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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.search.webproviders

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.saggitt.omega.search.WebSearchProvider

class BaiduWebSearchProvider(context: Context) : WebSearchProvider(context) {
    override val iconRes: Int
        get() = R.drawable.ic_baidu
    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, iconRes, null)!!

    override val packageName: String
        get() = "https://www.baidu.com/s?wd=%s"

    override val suggestionsUrl: String
        get() = "https://m.baidu.com/su?action=opensearch&ie=UTF-8&wd=%s"

    override val displayName: String
        get() = context.resources.getString(R.string.web_search_baidu)
}