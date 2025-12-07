package com.google.android.systemui.smartspace;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.android.launcher3.R;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;
import com.android.systemui.plugins.FalsingManager;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLogger;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.saulhdev.smartspace.SmartspaceAction;
import com.saulhdev.smartspace.SmartspaceTarget;
import com.saulhdev.smartspace.SmartspaceTargetEvent;
import com.saulhdev.smartspace.uitemplatedata.TapAction;

import java.util.List;
import java.util.Map;
public abstract class BcSmartSpaceUtil {
    public static final Map<Integer, Integer> FEATURE_TYPE_TO_SECONDARY_CARD_RESOURCE_MAP =
            Map.ofEntries(
                    Map.entry(-1, R.layout.smartspace_card_combination),
                    Map.entry(-2, R.layout.smartspace_card_combination_at_store),
                    Map.entry(3, R.layout.smartspace_card_generic_landscape_image),
                    Map.entry(18, R.layout.smartspace_card_generic_landscape_image),
                    Map.entry(4, R.layout.smartspace_card_flight),
                    Map.entry(14, R.layout.smartspace_card_loyalty),
                    Map.entry(13, R.layout.smartspace_card_shopping_list),
                    Map.entry(9, R.layout.smartspace_card_sports),
                    Map.entry(10, R.layout.smartspace_card_weather_forecast),
                    Map.entry(30, R.layout.smartspace_card_doorbell),
                    Map.entry(20, R.layout.smartspace_card_doorbell));
    public static FalsingManager sFalsingManager;
    public static BcSmartspaceDataPlugin.IntentStarter sIntentStarter;
    public static String getDimensionRatio(Bundle bundle) {
        if (bundle.containsKey("imageRatioWidth") && bundle.containsKey("imageRatioHeight")) {
            int width = bundle.getInt("imageRatioWidth");
            int height = bundle.getInt("imageRatioHeight");
            if (width > 0 && height > 0) {
                return width + ":" + height;
            }
        }
        return null;
    }
    public static int getFeatureType(SmartspaceTarget target) {
        List<SmartspaceAction> actionChips = target.getActionChips();
        int featureType = target.getFeatureType();
        if (actionChips != null
                && !actionChips.isEmpty()
                && featureType == 13
                && actionChips.size() == 1) {
            return -2;
        }
        return actionChips != null && !actionChips.isEmpty() ? -1 : featureType;
    }
    public static Drawable getIconDrawableWithCustomSize(Icon icon, Context context, int size) {
        if (icon == null) {
            return null;
        }
        Drawable drawable;
        if (icon.getType() == Icon.TYPE_RESOURCE || icon.getType() == Icon.TYPE_URI) {
            drawable = icon.loadDrawable(context);
        } else {
            drawable = new BitmapDrawable(context.getResources(), icon.getBitmap());
        }
        if (drawable != null) {
            drawable.setBounds(0, 0, size, size);
        }
        return drawable;
    }
    public static int getLoggingDisplaySurface(String uiSurface, float dozeAmount) {
        if (uiSurface == null) {
            return 0;
        }
        switch (uiSurface) {
            case "home":
                return 1;
            case "dream":
                return 5;
            case "lockscreen":
                if (dozeAmount == 1.0f) {
                    return 3;
                } else if (dozeAmount == 0.0f) {
                    return 2;
                } else {
                    return -1;
                }
            default:
                return 0;
        }
    }
    public static Intent getOpenCalendarIntent() {
        return new Intent("android.intent.action.VIEW")
                .setData(
                        ContentUris.appendId(
                                        CalendarContract.CONTENT_URI.buildUpon().appendPath("time"),
                                        System.currentTimeMillis())
                                .build())
                .addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }
    public static void setOnClickListener(
            View view,
            SmartspaceTarget target,
            SmartspaceAction action,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
            String tag,
            BcSmartspaceCardLoggingInfo loggingInfo,
            int index) {
        if (view == null || action == null) {
            Log.e(tag, "No tap action can be set up");
            return;
        }
        boolean shouldShowOnLockscreen =
                action.getExtras() != null && action.getExtras().getBoolean("show_on_lockscreen");
        boolean noIntent = action.getIntent() == null && action.getPendingIntent() == null;
        BcSmartspaceDataPlugin.IntentStarter intentStarter =
                sIntentStarter != null ? sIntentStarter : new DefaultIntentStarter(tag);
        view.setOnClickListener(
                v -> {
                    if (sFalsingManager != null && sFalsingManager.isFalseTap(1)) {
                        return;
                    }
                    if (loggingInfo != null) {
                        if (loggingInfo.mSubcardInfo != null) {
                            loggingInfo.mSubcardInfo.mClickedSubcardIndex = index;
                        }
                        BcSmartspaceCardLogger.log(
                                BcSmartspaceEvent.SMARTSPACE_CARD_CLICK, loggingInfo);
                    }
                    if (!noIntent) {
                        intentStarter.startFromAction(action, v, shouldShowOnLockscreen);
                    }
                    if (eventNotifier != null) {
                        SmartspaceTargetEvent event =
                                new SmartspaceTargetEvent.Builder(1)
                                        .setSmartspaceTarget(target)
                                        .setSmartspaceActionId(action.getId())
                                        .build();
                        eventNotifier.notifySmartspaceEvent(event);
                    } else {
                        Log.w(
                                tag,
                                "Cannot notify target interaction smartspace event: event notifier"
                                        + " null.");
                    }
                });
    }
    public static void setOnClickListener(
            View view,
            SmartspaceTarget target,
            TapAction tapAction,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
            String tag,
            BcSmartspaceCardLoggingInfo loggingInfo,
            int index) {
        if (view == null || tapAction == null) {
            Log.e(tag, "No tap action can be set up");
            return;
        }
        boolean shouldShowOnLockscreen = tapAction.shouldShowOnLockscreen();
        view.setOnClickListener(
                v -> {
                    if (sFalsingManager != null && sFalsingManager.isFalseTap(1)) {
                        return;
                    }
                    if (loggingInfo != null) {
                        if (loggingInfo.mSubcardInfo != null) {
                            loggingInfo.mSubcardInfo.mClickedSubcardIndex = index;
                        }
                        BcSmartspaceCardLogger.log(
                                BcSmartspaceEvent.SMARTSPACE_CARD_CLICK, loggingInfo);
                    }
                    BcSmartspaceDataPlugin.IntentStarter intentStarter =
                            sIntentStarter != null ? sIntentStarter : new DefaultIntentStarter(tag);
                    if (tapAction.getIntent() != null || tapAction.getPendingIntent() != null) {
                        intentStarter.startFromAction(tapAction, v, shouldShowOnLockscreen);
                    }
                    if (eventNotifier != null) {
                        SmartspaceTargetEvent event =
                                new SmartspaceTargetEvent.Builder(1)
                                        .setSmartspaceTarget(target)
                                        .setSmartspaceActionId(tapAction.getId().toString())
                                        .build();
                        eventNotifier.notifySmartspaceEvent(event);
                    } else {
                        Log.w(
                                tag,
                                "Cannot notify target interaction smartspace event: event notifier"
                                        + " null.");
                    }
                });
    }
    public static class InteractionHandler implements RemoteViews.InteractionHandler {
        public final BcSmartspaceCardLoggingInfo loggingInfo;
        public final BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier;
        public final SmartspaceTarget target;
        public final SmartspaceAction action;
        public InteractionHandler(
                BcSmartspaceCardLoggingInfo loggingInfo,
                BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
                SmartspaceTarget target,
                SmartspaceAction action) {
            this.loggingInfo = loggingInfo;
            this.eventNotifier = eventNotifier;
            this.target = target;
            this.action = action;
        }
        @Override
        public boolean onInteraction(
                View view, PendingIntent pendingIntent, RemoteViews.RemoteResponse response) {
            BcSmartspaceDataPlugin.IntentStarter intentStarter =
                    sIntentStarter != null
                            ? sIntentStarter
                            : new DefaultIntentStarter("BcSmartspaceRemoteViewsCard");
            if (pendingIntent != null) {
                BcSmartspaceCardLogger.log(BcSmartspaceEvent.SMARTSPACE_CARD_CLICK, loggingInfo);
                if (eventNotifier != null) {
                    SmartspaceTargetEvent event =
                            new SmartspaceTargetEvent.Builder(1)
                                    .setSmartspaceTarget(target)
                                    .setSmartspaceActionId(action.getId())
                                    .build();
                    eventNotifier.notifySmartspaceEvent(event);
                }
                intentStarter.startPendingIntent(view, pendingIntent, false);
            }
            return true;
        }
    }
    public static class DefaultIntentStarter implements BcSmartspaceDataPlugin.IntentStarter {
        public final String tag;
        public DefaultIntentStarter(String tag) {
            this.tag = tag;
        }
        @Override
        public void startIntent(View view, Intent intent, boolean showOnLockscreen) {
            try {
                view.getContext().startActivity(intent);
            } catch (NullPointerException | ActivityNotFoundException | SecurityException e) {
                Log.e(tag, "Cannot invoke smartspace intent", e);
            }
        }
        @Override
        public void startPendingIntent(
                View view, PendingIntent pendingIntent, boolean showOnLockscreen) {
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Log.e(tag, "Cannot invoke canceled smartspace intent", e);
            }
        }
    }
}