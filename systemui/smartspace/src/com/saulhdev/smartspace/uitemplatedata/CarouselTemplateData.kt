package com.saulhdev.smartspace.uitemplatedata

class CarouselTemplateData(
    override val templateType: Int,
    override val primaryItem: SubItemInfo?,
    override val subtitleItem: SubItemInfo?,
    override val subtitleSupplementalItem: SubItemInfo?,
    override val supplementalLineItem: SubItemInfo?,
    override val supplementalAlarmItem: SubItemInfo?,
    override val layoutWeight: Int,
    val carouselItems: List<CarouselItem>,
    val carouselAction: TapAction?,
) : BaseTemplateData(
    templateType, primaryItem, subtitleItem, subtitleSupplementalItem,
    supplementalLineItem, supplementalAlarmItem, layoutWeight
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CarouselTemplateData) return false
        if (!super.equals(other)) return false
        return carouselItems == other.carouselItems && carouselAction == other.carouselAction
    }

    class CarouselItem(
        val upperText: Text?,
        val image: Icon?,
        val lowerText: Text?,
        val tapAction: TapAction?,
    )
}