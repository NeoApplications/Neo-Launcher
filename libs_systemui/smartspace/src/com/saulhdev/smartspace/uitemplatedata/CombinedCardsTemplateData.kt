package com.saulhdev.smartspace.uitemplatedata

import com.saulhdev.smartspace.SmartspaceTarget

class CombinedCardsTemplateData(
    @SmartspaceTarget.UiTemplateType override val templateType: Int,
    override val primaryItem: SubItemInfo?,
    override val subtitleItem: SubItemInfo?,
    override val subtitleSupplementalItem: SubItemInfo?,
    override val supplementalLineItem: SubItemInfo?,
    override val supplementalAlarmItem: SubItemInfo?,
    override val layoutWeight: Int,
    val combinedCardDataList: List<BaseTemplateData>,
) : BaseTemplateData(
    templateType,
    primaryItem,
    subtitleItem,
    subtitleSupplementalItem,
    supplementalLineItem,
    supplementalAlarmItem,
    layoutWeight
) {
}