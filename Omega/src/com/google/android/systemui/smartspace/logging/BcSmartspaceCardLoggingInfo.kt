package com.google.android.systemui.smartspace.logging

class BcSmartspaceCardLoggingInfo(builder: Builder) {
    var cardinality = 0
    var displaySurface = 0
    var featureType = 0
    var instanceId = 0
    var rank = 0
    var receivedLatency = 0
    var subcardInfo: BcSmartspaceCardLoggingInfo? = null
    var uId = 0

    init {
        cardinality = builder.cardinality
        displaySurface = builder.displaySurface
        featureType = builder.featureType
        instanceId = builder.instanceId
        rank = builder.rank
        receivedLatency = builder.receivedLatency
        subcardInfo = builder.subCardInfo
        uId = builder.uId
    }

    override fun equals(obj: Any?): Boolean {
        var z = true
        if (this === obj) {
            return true
        }
        if (obj !is BcSmartspaceCardLoggingInfo) {
            return false
        }
        if (instanceId != obj.instanceId ||
            displaySurface != obj.displaySurface ||
            rank != obj.rank ||
            cardinality != obj.cardinality ||
            featureType != obj.featureType ||
            receivedLatency != obj.receivedLatency ||
            uId != obj.uId ||
            subcardInfo != obj.subcardInfo
        ) {
            z = false
        }
        return z
    }

    class Builder(
        val cardinality: Int,
        val displaySurface: Int,
        val featureType: Int,
        val instanceId: Int,
        val rank: Int,
        val receivedLatency: Int,
        val subCardInfo: BcSmartspaceCardLoggingInfo?,
        val uId: Int
    )
}