package com.neoapps.launcherclient

interface ISerializableScrollCallback : IScrollCallback {
    fun setPersistentFlags(myFlags: Int)
}