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

package com.neoapps.neolauncher

import android.view.MotionEvent
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.android.launcher3.Launcher
import com.android.launcher3.Utilities
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.android.systemui.plugins.shared.LauncherOverlayManager.LauncherOverlayCallbacks
import com.android.systemui.plugins.shared.LauncherOverlayManager.LauncherOverlayTouchProxy
import com.saggitt.omega.preferences.NeoPrefs
import com.saulhdev.launcherclient.IScrollCallback
import com.saulhdev.launcherclient.LauncherClient
import com.saulhdev.launcherclient.LauncherClientCallbacks
import com.saulhdev.launcherclient.StaticInteger
import kotlinx.coroutines.launch

class OverlayCallbackImpl(val launcher: Launcher) : LauncherOverlayTouchProxy,
    LauncherClientCallbacks, LauncherOverlayManager,
    IScrollCallback {

    private var mClient: LauncherClient? = null
    private var mLauncherOverlayCallbacks: LauncherOverlayCallbacks? = null
    private var mWasOverlayAttached = false
    private var mFlagsChanged = false
    private var mFlags = 0
    private var feedEnabled = false
    private val prefs = NeoPrefs.getInstance()

    init {
        mClient = LauncherClient(
            launcher, this, StaticInteger(
                (if (feedEnabled) 1 else 0) or 2 or 4 or 8
            )
        )
    }

    override fun onAttachedToWindow() {
        launcher.nLauncher.lifecycleScope.launch {
            prefs.feedProvider.get().collect {
                feedEnabled = it != ""
                mClient = LauncherClient(
                    launcher, this@OverlayCallbackImpl, StaticInteger(
                        (if (feedEnabled) 1 else 0) or 2 or 4 or 8
                    )
                )
            }
        }
        mClient?.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        prefs.feedProvider.get().asLiveData().removeObserver { }
        mClient?.onDetachedFromWindow()
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


    override fun onFlingVelocity(velocity: Float) {
        TODO("Not yet implemented")
    }

    override fun onOverlayMotionEvent(ev: MotionEvent?, scrollProgress: Float) {
        TODO("Not yet implemented")
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
            launcher.setLauncherOverlay(if (overlayAttached) this else null)
        }
    }

    override fun setPersistentFlags(myFlags: Int) {
        var flags = myFlags
        flags = flags and (8 or 16)
        if (flags != mFlags) {
            mFlagsChanged = true
            mFlags = flags
            Utilities.getDevicePrefs(launcher).edit().putInt(PREF_PERSIST_FLAGS, flags).apply()
        }
    }

    companion object {
        const val PREF_PERSIST_FLAGS = "pref_persistent_flags"
    }
}