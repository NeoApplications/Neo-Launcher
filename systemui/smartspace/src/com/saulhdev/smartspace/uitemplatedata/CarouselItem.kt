package com.saulhdev.smartspace.uitemplatedata

class CarouselItem(
    val upperText: Text,
    val image: Icon?,
    val lowerText: Text,
    val tapAction: TapAction
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CarouselItem) return false
        return upperText == other.upperText && image == other.image && lowerText == other.lowerText && tapAction == other.tapAction
    }
}