package com.saulhdev.smartspace.uitemplatedata

import com.saulhdev.smartspace.SmartspaceTarget.FeatureType

data class SubItemLoggingInfo(
    val instanceId: Int?,
    @FeatureType val featureType: Int,
    val packageName: CharSequence?
)