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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.saggitt.omega.compose.navigation.PrefsComposeView
import com.saggitt.omega.theme.OmegaAppTheme
import com.saggitt.omega.theme.ThemeManager
import com.saggitt.omega.theme.ThemeOverride

class PreferenceActivity : AppCompatActivity(), ThemeManager.ThemeableActivity {
    private lateinit var navController: NavHostController
    override var currentTheme = 0
    override var currentAccent = 0
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentTheme = themeOverride.getTheme(this)
        currentAccent = Color.Green.hashCode() //omegaPrefs.themeAccentColor.onGetValue()
        setContent {
            OmegaAppTheme {
                navController = rememberAnimatedNavController()
                PrefsComposeView(navController)
            }
        }
    }

    override fun onThemeChanged(forceUpdate: Boolean) = recreate()

    companion object {
        fun createIntent(context: Context, destination: String): Intent {
            val uri = "android-app://androidx.navigation//$destination".toUri()
            Log.d("PrefsActivityX", "Creating intent for $uri")
            return Intent(Intent.ACTION_VIEW, uri, context, PreferenceActivity::class.java)
        }

        fun getFragmentManager(context: Context): FragmentManager {
            return (context as PreferenceActivity).supportFragmentManager
        }
    }
}