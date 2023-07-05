/*
 * This file is part of Omega Launcher
 * Copyright (c) 2021   Saul Henriquez
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

package com.saggitt.omega

import android.app.Activity
import com.android.launcher3.Launcher
import com.android.launcher3.Utilities
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.android.systemui.plugins.shared.LauncherOverlayManager.LauncherOverlayCallbacks
import com.saggitt.omega.launcherclient.IScrollCallback
import com.saggitt.omega.launcherclient.LauncherClient
import com.saggitt.omega.launcherclient.LauncherClientCallbacks
import com.saggitt.omega.launcherclient.StaticInteger
import com.saggitt.omega.preferences.NeoPrefs

class OverlayCallbackImpl(launcher: Launcher) : LauncherOverlayManager.LauncherOverlay,
    LauncherClientCallbacks, LauncherOverlayManager,
    IScrollCallback {

    private var mClient: LauncherClient? = null
    private val mLauncher = launcher
    private val prefs = NeoPrefs.getInstance(mLauncher)
    private var enableFeed: Boolean = prefs.feedProvider.getValue() != ""
    private var mLauncherOverlayCallbacks: LauncherOverlayCallbacks? = null
    private var mWasOverlayAttached = false
    var mFlagsChanged = false
    private var mFlags = 0

    init {
        mClient = LauncherClient(
            mLauncher, this, StaticInteger(
                (if (enableFeed) 1 else 0) or 2 or 4 or 8
            )
        )
    }

    override fun onAttachedToWindow() {
        mClient?.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        mClient?.onDetachedFromWindow()
    }

    override fun onActivityStarted(activity: Activity) {
        mClient!!.onStart()
    }

    override fun onActivityResumed(activity: Activity) {
        mClient!!.onResume()
    }

    override fun onActivityPaused(activity: Activity) {
        mClient!!.onPause()
    }

    override fun onActivityStopped(activity: Activity) {
        mClient!!.onStop()
    }

    override fun onActivityDestroyed(activity: Activity) {
        mClient!!.onDestroy()
        mClient!!.mDestroyed = true
    }

    override fun openOverlay() {
        mClient!!.showOverlay(true)
    }

    override fun hideOverlay(animate: Boolean) {
        mClient!!.hideOverlay(animate)
    }

    override fun hideOverlay(duration: Int) {
        mClient!!.hideOverlay(duration)
    }

    override fun onScrollInteractionBegin() {
        mClient!!.startScroll()
    }

    override fun onScrollInteractionEnd() {
        mClient!!.endScroll()
    }

    override fun onScrollChange(progress: Float, rtl: Boolean) {
        mClient!!.setScroll(progress)
    }

    override fun setOverlayCallbacks(callbacks: LauncherOverlayCallbacks) {
        mLauncherOverlayCallbacks = callbacks
    }

    override fun onOverlayScrollChanged(progress: Float) {
        if (mLauncherOverlayCallbacks != null) {
            mLauncherOverlayCallbacks!!.onOverlayScrollChanged(progress)
        }
    }

    override fun onServiceStateChanged(overlayAttached: Boolean, hotwordActive: Boolean) {
        this.onServiceStateChanged(overlayAttached)
    }

    override fun onServiceStateChanged(overlayAttached: Boolean) {
        if (overlayAttached != mWasOverlayAttached) {
            mWasOverlayAttached = overlayAttached
            mLauncher.setLauncherOverlay(if (overlayAttached) this else null)
        }
    }

    override fun setPersistentFlags(flags: Int) {
        var flags = flags
        flags = flags and (8 or 16)
        if (flags != mFlags) {
            mFlagsChanged = true
            mFlags = flags
            Utilities.getDevicePrefs(mLauncher).edit().putInt(PREF_PERSIST_FLAGS, flags).apply()
        }
    }

    companion object {
        const val PREF_PERSIST_FLAGS = "pref_persistent_flags"
    }
}