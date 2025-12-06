package com.saulhdev.launcherclient

interface ISerializableScrollCallback : IScrollCallback {
    fun setPersistentFlags(myFlags: Int)
}