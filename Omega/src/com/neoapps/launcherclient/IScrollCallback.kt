package com.saulhdev.launcherclient

interface IScrollCallback {
    fun onOverlayScrollChanged(progress: Float)

    fun onServiceStateChanged(overlayAttached: Boolean)
}