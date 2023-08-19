package com.saulhdev.smartspace.uitemplatedata

import com.saulhdev.smartspace.SmartspaceTarget

class SubListTemplateData(
    @SmartspaceTarget.UiTemplateType override val templateType: Int,
    override val primaryItem: SubItemInfo,
    override val subtitleItem: SubItemInfo,
    override val subtitleSupplementalItem: SubItemInfo,
    override val supplementalLineItem: SubItemInfo,
    override val supplementalAlarmItem: SubItemInfo,
    override val layoutWeight: Int,
    val subListIcon: Icon?,
    val subListTexts: List<Text>?,
    val subListAction: TapAction?

) : BaseTemplateData(
    templateType = templateType,
    primaryItem = primaryItem,
    subtitleItem = subtitleItem,
    subtitleSupplementalItem = subtitleSupplementalItem,
    supplementalLineItem = supplementalLineItem,
    supplementalAlarmItem = supplementalAlarmItem,
    layoutWeight = layoutWeight
)