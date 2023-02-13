package com.saggitt.omega.launcherclient

interface IScrollCallback {
    fun onOverlayScrollChanged(progress: Float)

    fun onServiceStateChanged(overlayAttached: Boolean)
}