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
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherPrefs
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.android.systemui.plugins.shared.LauncherOverlayManager.LauncherOverlayCallbacks
import com.android.systemui.plugins.shared.LauncherOverlayManager.LauncherOverlayTouchProxy
import com.google.android.libraries.launcherclient.IScrollCallback
import com.google.android.libraries.launcherclient.LauncherClient
import com.google.android.libraries.launcherclient.LauncherClientCallbacks
import com.google.android.libraries.launcherclient.StaticInteger
import com.neoapps.neolauncher.preferences.NeoPrefs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class OverlayCallbackImpl(val launcher: Launcher) : LauncherOverlayTouchProxy,
    LauncherClientCallbacks, LauncherOverlayManager,
    IScrollCallback {

    private var launcherClient: LauncherClient? = null
    private var launcherOverlayCallbacks: LauncherOverlayCallbacks? = null
    private var wasOverlayAttached = false
    private var flagsChanged = false
    private var flags = 0
    private var feedEnabled = false
    private val prefs = NeoPrefs.getInstance()
    private var job: Job? = null

    init {
        job = prefs.feedProvider.get()
            .onEach {
                setEnableFeed(prefs.feedProvider.getValue())
            }
            .launchIn(launcher.lifecycleScope)

        launcherClient = LauncherClient(
            launcher, this, StaticInteger(
                (if (feedEnabled) 1 else 0) or 2 or 4 or 8
            )
        )
    }

    fun reconnect() {
        launcherClient?.reconnect()
    }

    fun setEnableFeed(enable: Boolean) {
        feedEnabled = enable
        launcherClient?.setEnableFeed(enable)
        reconnect()
    }

    override fun onDeviceProvideChanged() {
        launcherClient?.redraw()
    }

    override fun onAttachedToWindow() {
        launcherClient?.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        launcherClient!!.onDetachedFromWindow()
    }

    override fun onActivityStarted() {
        launcherClient!!.onStart()
    }

    override fun onActivityResumed() {
        launcherClient!!.onResume()
    }

    override fun onActivityPaused() {
        launcherClient!!.onPause()
    }

    override fun onActivityStopped() {
        launcherClient!!.onStop()
    }

    override fun openOverlay() {
        launcherClient!!.showOverlay(true)
    }

    override fun hideOverlay(animate: Boolean) {
        launcherClient!!.hideOverlay(animate)
    }

    override fun hideOverlay(duration: Int) {
        launcherClient!!.hideOverlay(duration)
    }


    override fun onFlingVelocity(velocity: Float) {

    }

    override fun onOverlayMotionEvent(ev: MotionEvent?, scrollProgress: Float) {
        if (ev == null) return
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (launcherClient != null && !launcherClient!!.isConnected) {
                    launcherClient?.reconnect()
                }
                launcherClient?.startScroll()
            }
            MotionEvent.ACTION_MOVE -> launcherClient?.setScroll(scrollProgress)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> launcherClient?.endScroll()
        }
    }

    override fun onOverlayScrollChanged(progress: Float) {
        if (launcherOverlayCallbacks != null) {
            launcherOverlayCallbacks!!.onOverlayScrollChanged(progress)
        }
    }

    override fun onServiceStateChanged(overlayAttached: Boolean, hotwordActive: Boolean) {
        this.onServiceStateChanged(overlayAttached)
        if (overlayAttached != wasOverlayAttached) {
            wasOverlayAttached = overlayAttached
            launcher.setLauncherOverlay(if (overlayAttached) this else null)
        }
    }

    override fun onServiceStateChanged(overlayAttached: Boolean) {
        if (overlayAttached != wasOverlayAttached) {
            wasOverlayAttached = overlayAttached
            launcher.setLauncherOverlay(if (overlayAttached) this else null)
        }
    }

    override fun onActivityDestroyed() {
        job?.cancel()
        launcherClient?.onDestroy()
    }

    override fun setOverlayCallbacks(callbacks: LauncherOverlayCallbacks) {
        launcherOverlayCallbacks = callbacks
    }

    override fun setPersistentFlags(myFlags: Int) {
        val newFlags = myFlags and (8 or 16)
        if (newFlags != flags) {
            flagsChanged = true
            flags = newFlags
            LauncherPrefs.getDevicePrefs(launcher).edit { putInt(PREF_PERSIST_FLAGS, newFlags) }
        }
    }

    companion object {
        const val PREF_PERSIST_FLAGS = "pref_persistent_flags"
    }
}
