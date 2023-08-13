package com.saggitt.omega.smartspace.uitemplatedata

import com.saggitt.omega.smartspace.model.SmartspaceTarget.FeatureType

data class SubItemLoggingInfo(
    val instanceInfo: SubItemInfo?,
    @FeatureType val featureType: Int,
    val packageName: CharSequence?
)