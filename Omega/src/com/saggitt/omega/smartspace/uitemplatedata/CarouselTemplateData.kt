package com.saggitt.omega.smartspace.uitemplatedata


data class CarouselTemplateData(
    val templateType: Int,
    val primaryItem: SubItemInfo?,
    val subtitleItem: SubItemInfo?,
    val subtitleSupplementalItem: SubItemInfo?,
    val supplementalLineItem: SubItemInfo?,
    val supplementalAlarmItem: SubItemInfo?,
    val layoutWeight: Int,
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
}