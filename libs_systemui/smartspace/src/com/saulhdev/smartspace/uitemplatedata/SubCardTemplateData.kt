package com.saulhdev.smartspace.uitemplatedata

class SubCardTemplateData(
    override val templateType: Int,
    override val primaryItem: SubItemInfo,
    override val subtitleItem: SubItemInfo,
    override val subtitleSupplementalItem: SubItemInfo,
    override val supplementalLineItem: SubItemInfo,
    override val supplementalAlarmItem: SubItemInfo,
    override val layoutWeight: Int,
    val subCardIcon: Icon?,
    val subCardText: Text?,
    val subCardAction: TapAction?

) : BaseTemplateData(
    templateType = templateType,
    primaryItem = primaryItem,
    subtitleItem = subtitleItem,
    subtitleSupplementalItem = subtitleSupplementalItem,
    supplementalLineItem = supplementalLineItem,
    supplementalAlarmItem = supplementalAlarmItem,
    layoutWeight = layoutWeight
)