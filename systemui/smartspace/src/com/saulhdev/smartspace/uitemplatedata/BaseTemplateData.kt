package com.saulhdev.smartspace.uitemplatedata

import androidx.annotation.Nullable
import com.saulhdev.smartspace.SmartspaceTarget.UiTemplateType

open class BaseTemplateData(
    @UiTemplateType open val templateType: Int,
    @Nullable open val primaryItem: SubItemInfo?,
    @Nullable open val subtitleItem: SubItemInfo?,
    @Nullable open val subtitleSupplementalItem: SubItemInfo?,
    @Nullable open val supplementalLineItem: SubItemInfo?,
    @Nullable open val supplementalAlarmItem: SubItemInfo?,
    open val layoutWeight: Int
) {
    class SubItemInfo(
        val text: Text?,
        val icon: Icon?,
        val tapAction: TapAction?,
        val loggingInfo: SubItemLoggingInfo?
    )
}