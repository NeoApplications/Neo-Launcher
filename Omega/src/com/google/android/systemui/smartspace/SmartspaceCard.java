package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceTarget;
import android.view.View;

import com.android.systemui.plugins.BcSmartspaceDataPlugin;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;

public interface SmartspaceCard {
    void bindData(
            SmartspaceTarget smartspaceTarget,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier smartspaceEventNotifier,
            BcSmartspaceCardLoggingInfo bcSmartspaceCardLoggingInfo,
            boolean z);

    BcSmartspaceCardLoggingInfo getLoggingInfo();

    View getView();

    void setDozeAmount(float f);

    void setPrimaryTextColor(int i);

    void setScreenOn(boolean z);
}
