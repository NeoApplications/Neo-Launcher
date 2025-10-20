package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceAction;
import android.app.smartspace.SmartspaceTarget;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.android.systemui.plugins.BcSmartspaceDataPlugin;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;

public class BcSmartspaceRemoteViewsCard extends AppWidgetHostView implements SmartspaceCard {
    public BcSmartspaceDataPlugin.SmartspaceEventNotifier mEventNotifier;
    public BcSmartspaceCardLoggingInfo mLoggingInfo;
    public SmartspaceTarget mTarget;
    public String mUiSurface;

    public BcSmartspaceRemoteViewsCard(Context context) {
        super(context);
        setOnLongClickListener(null);
        if ("lockscreen".equals(mUiSurface)) {
            super.setInteractionHandler(null);
        }
    }

    @Override
    public void bindData(
            SmartspaceTarget target,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
            BcSmartspaceCardLoggingInfo loggingInfo,
            boolean usePageIndicatorUi) {
        mTarget = target;
        mLoggingInfo = loggingInfo;
        mEventNotifier = eventNotifier;

        RemoteViews remoteViews = target.getRemoteViews();
        updateAppWidget(remoteViews);

        SmartspaceAction headerAction = target.getHeaderAction();
        if (headerAction == null) {
            setOnClickListener(null);
            super.setInteractionHandler(null);
            return;
        }

        BcSmartSpaceUtil.setOnClickListener(
                this,
                target,
                headerAction,
                mEventNotifier,
                "BcSmartspaceRemoteViewsCard",
                loggingInfo,
                0);
        if ("lockscreen".equals(mUiSurface)) {
            super.setInteractionHandler(
                    new BcSmartSpaceUtil.InteractionHandler(
                            loggingInfo, mEventNotifier, target, headerAction));
        }
    }

    @Override
    public BcSmartspaceCardLoggingInfo getLoggingInfo() {
        if (mLoggingInfo == null) {
            BcSmartspaceCardLoggingInfo.Builder builder =
                    new BcSmartspaceCardLoggingInfo.Builder()
                            .setDisplaySurface(
                                    BcSmartSpaceUtil.getLoggingDisplaySurface(mUiSurface, 0f))
                            .setFeatureType(mTarget != null ? mTarget.getFeatureType() : 0)
                            .setUid(-1);
            return new BcSmartspaceCardLoggingInfo(builder);
        }
        return mLoggingInfo;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setDozeAmount(float dozeAmount) {
        // No-op
    }

    @Override
    public void setPrimaryTextColor(int color) {
        // No-op
    }

    @Override
    public void setScreenOn(boolean screenOn) {
        // No-op
    }
}
