package com.saggitt.omega.smartspace.uitemplatedata

import androidx.annotation.Nullable
import com.saggitt.omega.smartspace.model.SmartspaceTarget.UiTemplateType

open class BaseTemplateData(
    @UiTemplateType private val templateType: Int,
    @Nullable private val primaryItem: SubItemInfo?,
    @Nullable private val subtitleItem: SubItemInfo?,
    @Nullable private val subtitleSupplementalItem: SubItemInfo?,
    @Nullable private val supplementalLineItem: SubItemInfo?,
    @Nullable private val supplementalAlarmItem: SubItemInfo?,
    private val layoutWeight: Int
)

data class SubItemInfo(
    val text: Text?,
    val icon: Icon?,
    val action: TapAction?,
    val loggingInfo: SubItemLoggingInfo?
)