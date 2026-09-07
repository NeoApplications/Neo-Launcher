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

package com.neoapps.neolauncher.preferences

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.neoapps.neolauncher.compose.navigation.NAV_BASE
import com.neoapps.neolauncher.compose.navigation.PrefsComposeView
import com.neoapps.neolauncher.theme.OmegaAppTheme
import com.neoapps.neolauncher.theme.ThemeManager
import com.neoapps.neolauncher.theme.ThemeOverride
import com.neoapps.neolauncher.util.prefs
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PreferenceActivity : ComponentActivity(), ThemeManager.ThemeableActivity {
    private lateinit var navController: NavHostController
    override var currentTheme = 0
    override var currentAccent = 0
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentTheme = themeOverride.getTheme(this)
        currentAccent = prefs.profileAccentColor.getColor()
        setContent {
            val paneNavigator = rememberListDetailPaneScaffoldNavigator<Any>()

            LaunchedEffect(Unit) {
                (MainScope() + CoroutineName("PreferenceActivity")).launch {
                    prefs.profileTheme.get().distinctUntilChanged().collect {
                        enableEdgeToEdge(
                            statusBarStyle = SystemBarStyle.auto(
                                Color.TRANSPARENT,
                                Color.TRANSPARENT,
                            ) { ThemeManager.Companion.getInstance(this@PreferenceActivity).isDarkTheme },
                            navigationBarStyle = SystemBarStyle.auto(
                                Color.TRANSPARENT,
                                Color.TRANSPARENT,
                            ) { ThemeManager.Companion.getInstance(this@PreferenceActivity).isDarkTheme },
                        )
                    }
                }
            }

            OmegaAppTheme {
                navController = rememberNavController()
                PrefsComposeView(navController, paneNavigator)
            }
        }
    }

    override fun onThemeChanged(forceUpdate: Boolean) = recreate()

    companion object {
        fun navigateIntent(context: Context, destination: String): Intent {
            val uri = "${NAV_BASE}$destination".toUri()
            return Intent(Intent.ACTION_VIEW, uri, context, PreferenceActivity::class.java)
        }

        suspend fun startBlankActivityDialog(
            activity: Activity, targetIntent: Intent,
            dialogTitle: String, dialogMessage: String,
            positiveButton: String,
        ) {
            start(activity, targetIntent, Bundle().apply {
                putParcelable("intent", targetIntent)
                putString("dialogTitle", dialogTitle)
                putString("dialogMessage", dialogMessage)
                putString("positiveButton", positiveButton)
            })
        }

        suspend fun startBlankActivityForResult(
            activity: Activity,
            targetIntent: Intent,
        ): ActivityResult {
            return start(activity, targetIntent, Bundle.EMPTY)
        }

        private suspend fun start(
            activity: Activity,
            targetIntent: Intent,
            extras: Bundle,
        ): ActivityResult {
            return suspendCoroutine { continuation ->
                val intent = Intent(activity, PreferenceActivity::class.java)
                intent.putExtras(extras)
                intent.putExtra("intent", targetIntent)
                val resultReceiver = createResultReceiver {
                    continuation.resume(it)
                }
                activity.startActivity(intent.putExtra("callback", resultReceiver))
            }
        }

        private fun createResultReceiver(callback: (ActivityResult) -> Unit): ResultReceiver {
            return object : ResultReceiver(Handler(Looper.myLooper()!!)) {

                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    val data = Intent()
                    if (resultData != null) {
                        data.putExtras(resultData)
                    }
                    callback(ActivityResult(resultCode, data))
                }
            }
        }
    }
}