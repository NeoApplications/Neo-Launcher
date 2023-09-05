package com.saggitt.omega.launcherclient

interface ISerializableScrollCallback : IScrollCallback {
    fun setPersistentFlags(myFlags: Int)
}