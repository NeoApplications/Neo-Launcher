/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023 Neo Launcher Team
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

package com.saggitt.omega.allapps.search

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityOptionsCompat
import com.android.launcher3.ExtendedEditText
import com.android.launcher3.Insettable
import com.android.launcher3.R
import com.android.launcher3.allapps.ActivityAllAppsContainerView
import com.android.launcher3.allapps.SearchUiManager
import com.saggitt.omega.compose.navigation.Routes
import com.saggitt.omega.nLauncher
import com.saggitt.omega.preferences.PreferenceActivity
import com.saggitt.omega.search.AppsSearchProvider
import com.saggitt.omega.search.SearchProvider
import com.saggitt.omega.search.SearchProviderController
import com.saggitt.omega.search.WebSearchProvider

class AllAppsSearchLayout(mContext: Context, attrs: AttributeSet? = null) :
    AbstractSearchLayout(mContext, attrs), SearchUiManager, Insettable {

    var mDoNotRemoveFallback = false
    private val mVerticalOffset =
        resources.getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset)

    private var mSearchBlock: AllAppsSearchBlock? = null
    private lateinit var mAppsView: ActivityAllAppsContainerView<*>
    private var mCancelButton: ImageButton? = null

    init {
        visibility = (if (prefs.searchDrawerEnabled.getValue()) View.VISIBLE else View.GONE)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        findViewById<ImageButton?>(R.id.search_settings_button).apply {
            setOnClickListener {
                context.startActivity(
                    PreferenceActivity.createIntent(
                        context,
                        "${Routes.PREFS_SEARCH}/"
                    )
                )
            }
        }

        findViewById<AppCompatImageView?>(R.id.mic_icon).apply {
            if (!prefs.searchGlobal.getValue()) {
                visibility = View.GONE
            }
        }

        setOnClickListener {
            val provider = controller.searchProvider
            if (prefs.searchGlobal.getValue()) {
                provider.startSearch(mContext) { intent: Intent? ->
                    context.startActivity(
                        intent,
                        ActivityOptionsCompat.makeClipRevealAnimation(this, 0, 0, width, height)
                            .toBundle()
                    )
                }
            } else {
                searchFallback("")
            }
        }

        mCancelButton = findViewById(R.id.search_cancel_button)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // Shift the widget horizontally so that its centered in the parent (b/63428078)
        val parent = parent as View
        val availableWidth = parent.width - parent.paddingLeft - parent.paddingRight
        val myWidth = right - left
        val expectedLeft = parent.paddingLeft + (availableWidth - myWidth) / 2
        val shift = expectedLeft - left
        translationX = shift.toFloat()

        var containerTopMargin = 0
        if (!prefs.searchDrawerEnabled.getValue()) {
            val mlp = layoutParams as MarginLayoutParams
            containerTopMargin = -(mlp.topMargin + mlp.height)
        }
        offsetTopAndBottom(mVerticalOffset - containerTopMargin)
    }

    /*override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        if (key == PREFS_SEARCH_GLOBAL) {
            reloadPreferences(sharedPreferences)
        }
    }*/

    override fun getMicIcon(): Drawable? {
        return if (prefs.searchGlobal.getValue()) {
            if (searchProvider.supportsAssistant && mShowAssistant) {
                searchProvider.assistantIcon
            } else if (searchProvider.supportsVoiceSearch) {
                searchProvider.voiceIcon
            } else {
                micIconView?.visibility = View.GONE
                ColorDrawable(Color.TRANSPARENT)
            }
        } else {
            micIconView?.visibility = View.GONE
            ColorDrawable(Color.TRANSPARENT)
        }
    }

    override fun getIcon(): Drawable {
        return if (prefs.searchGlobal.getValue()) {
            super.getIcon()
        } else {
            AppsSearchProvider(context).icon
        }
    }

    override fun setInsets(insets: Rect?) {
        val mlp = layoutParams as MarginLayoutParams
        mlp.topMargin = insets!!.top
        requestLayout()
    }

    override fun initializeSearch(allAppsContainerView: ActivityAllAppsContainerView<*>) {
        mAppsView = allAppsContainerView
        /*mAppsView.addElevationController(object : RecyclerView.OnScrollListener() {
            var initialElevation = 1f
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (mFallback != null) {
                    initialElevation = mFallback!!.elevation
                }
                val currentScrollY = (recyclerView as FastScrollRecyclerView).scrollToPositionAtProgress
                val elevationScale = Utilities.boundToRange(currentScrollY / 255f, 0f, 1f)
                //if (prefs.drawerLayout.onGetValue() != Config.DRAWER_PAGED)
                mFallback?.elevation = initialElevation + elevationScale * initialElevation
            }
        })*/
    }

    override fun resetSearch() {
        mSearchBlock?.clearSearchResult()
        mSearchBlock?.resetSearch()
    }

    override fun startSearch() {
        post { startSearch("") }
    }

    override fun getEditText(): ExtendedEditText? {
        ensureFallbackView()
        return mSearchBlock
    }

    override fun startSearch(str: String?) {
        val provider = SearchProviderController.getInstance(mContext).searchProvider
        if (shouldUseFallbackSearch(provider)) {
            searchFallback(str)
        } else {
            provider.startSearch(mContext) { intent: Intent? ->
                mContext.nLauncher.startActivity(intent)
            }
        }
    }

    private fun searchFallback(query: String?) {
        ensureFallbackView()
        mSearchBlock?.setText(query)
        mSearchBlock?.showKeyboard()
    }

    private fun ensureFallbackView() {
        if (mSearchBlock == null) {
            mSearchBlock =
                this.findViewById(R.id.search_container_all_apps_bar) as AllAppsSearchBlock
            val allAppsContainerView: ActivityAllAppsContainerView<*> = mAppsView
            mSearchBlock!!.allAppsQsbLayout = this
            mSearchBlock!!.setCancelButton(mCancelButton!!)
            mSearchBlock!!.initializeSearch(allAppsContainerView)
        }
    }

    private fun shouldUseFallbackSearch(provider: SearchProvider) =
        !prefs.searchGlobal.getValue()
                || provider is AppsSearchProvider
                || provider is WebSearchProvider
}