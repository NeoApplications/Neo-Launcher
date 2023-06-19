/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
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
package com.saggitt.omega.allapps

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.ComposeView
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.workprofile.PersonalWorkSlidingTabStrip
import com.saggitt.omega.compose.components.ComposeBottomSheet
import com.saggitt.omega.groups.ui.EditGroupBottomSheet
import com.saggitt.omega.nLauncher
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.util.prefs
import java.util.function.Consumer

class AllAppsTabsLayout(context: Context, attrs: AttributeSet) :
    PersonalWorkSlidingTabStrip(context, attrs) {
    val prefs: NeoPrefs = Utilities.getOmegaPrefs(context)
    val launcher: Launcher = Launcher.getLauncher(context)
    private val selectedPage: MutableState<Int> = mutableIntStateOf(0)

    override fun setActiveMarker(activePage: Int) {
        if (mOnActivePageChangedListener != null && mLastActivePage != activePage) {
            mOnActivePageChangedListener.onActivePageChanged(activePage)
        }
        selectedPage.value = activePage
        mLastActivePage = activePage
    }

    override fun setMarkersCount(numMarkers: Int) {
    }

    fun inflateButtons(tabs: AllAppsTabs, switcher: Consumer<Int>) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.all_apps_tab_bar, this)
        findViewById<ComposeView>(R.id.compose_bar).setContent {
            TabsBar(
                list = tabs.tabs,
                selectedPage = selectedPage,
                onClick = { i -> switcher.accept(i) },
                onLongClick = { item -> // TODO fix
                    ComposeBottomSheet.show(context, true) {
                        EditGroupBottomSheet(
                            category = context.prefs.drawerAppGroupsManager.getEnabledType()!!,
                            group = item.drawerTab,
                            onClose = { AbstractFloatingView.closeAllOpenViews(context.nLauncher) }
                        )
                    }
                }
            )
        }
    }
}