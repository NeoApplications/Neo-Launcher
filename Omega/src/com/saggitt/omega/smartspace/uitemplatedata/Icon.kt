package com.saggitt.omega.smartspace.uitemplatedata

data class Icon(
    val icon: android.graphics.drawable.Icon?,
    val contentDescription: CharSequence?,
    val shouldTint: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Icon) return false
        return icon.toString() == other.icon.toString() && SmartspaceUtils.isEqual(
            contentDescription,
            other.contentDescription
        ) && shouldTint == other.shouldTint
    }
}