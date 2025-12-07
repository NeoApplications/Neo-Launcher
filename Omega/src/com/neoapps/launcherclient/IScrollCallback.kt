package com.neoapps.launcherclient

interface IScrollCallback {
    fun onOverlayScrollChanged(progress: Float)

    fun onServiceStateChanged(overlayAttached: Boolean)
}