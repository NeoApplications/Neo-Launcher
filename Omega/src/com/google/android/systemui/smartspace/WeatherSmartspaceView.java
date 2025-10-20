package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceAction;
import android.app.smartspace.SmartspaceTarget;
import android.app.smartspace.SmartspaceUtils;
import android.app.smartspace.uitemplatedata.BaseTemplateData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.android.internal.graphics.ColorUtils;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLogger;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggerUtil;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.utils.ContentDescriptionUtil;
import com.statix.android.systemui.res.R;

import java.util.List;

public class WeatherSmartspaceView extends LinearLayout
        implements BcSmartspaceDataPlugin.SmartspaceTargetListener,
                BcSmartspaceDataPlugin.SmartspaceView {
    public static final boolean DEBUG = Log.isLoggable("WeatherSmartspaceView", Log.DEBUG);

    private final ContentObserver mAodSettingsObserver;
    private Handler mBgHandler;
    private BcSmartspaceDataPlugin mDataProvider;
    private float mDozeAmount;
    private final DoubleShadowIconDrawable mIconDrawable;
    private final int mIconSize;
    private boolean mIsAodEnabled;
    private BcSmartspaceCardLoggingInfo mLoggingInfo;
    private int mPrimaryTextColor;
    private final boolean mRemoveTextDescent;
    private final int mTextDescentExtraPadding;
    private String mUiSurface;
    private DoubleShadowTextView mView;

    public WeatherSmartspaceView(Context context) {
        this(context, null);
    }

    public WeatherSmartspaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherSmartspaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mUiSurface = null;
        mDozeAmount = 0f;
        mLoggingInfo = null;
        mAodSettingsObserver = new WeatherSmartspaceViewObserver(this, new Handler());
        context.getTheme().applyStyle(R.style.Smartspace, false);

        TypedArray ta =
                context.getTheme()
                        .obtainStyledAttributes(attrs, R.styleable.WeatherSmartspaceView, 0, 0);
        try {
            mIconSize =
                    ta.getDimensionPixelSize(
                            1,
                            context.getResources()
                                    .getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_size));
            int iconInset =
                    ta.getDimensionPixelSize(
                            0,
                            context.getResources()
                                    .getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_inset));
            mRemoveTextDescent = ta.getBoolean(2, false);
            mTextDescentExtraPadding = ta.getDimensionPixelSize(3, 0);
            mIconDrawable = new DoubleShadowIconDrawable(mIconSize, iconInset, context);
        } finally {
            ta.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (TextUtils.equals(mUiSurface, "lockscreen")) {
            try {
                if (mBgHandler != null) {
                    mBgHandler.post(() -> registerAodObserver());
                } else {
                    throw new IllegalStateException(
                            "Must set background handler to avoid making binder calls on main"
                                + " thread");
                }
            } catch (Exception e) {
                Log.w(
                        "WeatherSmartspaceView",
                        "Unable to register DOZE_ALWAYS_ON content observer: ",
                        e);
            }
            ContentResolver resolver = getContext().getContentResolver();
            mIsAodEnabled =
                    Settings.Secure.getIntForUser(
                                    resolver, "doze_always_on", 0, getContext().getUserId())
                            == 1;
        }
        if (mDataProvider != null) {
            mDataProvider.registerListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBgHandler != null) {
            mBgHandler.post(() -> unregisterAodObserver());
            if (mDataProvider != null) {
                mDataProvider.unregisterListener(this);
            }
        } else {
            throw new IllegalStateException(
                    "Must set background handler to avoid making binder calls on main thread");
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mView = findViewById(R.id.weather_text_view);
    }

    @Override
    public void onSmartspaceTargetsUpdated(List<? extends Parcelable> targets) {
        List<SmartspaceTarget> smartspaceTargets =
                targets.stream()
                        .filter(t -> t instanceof SmartspaceTarget)
                        .map(t -> (SmartspaceTarget) t)
                        .collect(java.util.stream.Collectors.toList());
        if (smartspaceTargets.size() > 1
                || (smartspaceTargets.isEmpty() && TextUtils.equals(mUiSurface, "dream"))) {
            BcSmartspaceTemplateDataUtils.updateVisibility(mView, View.GONE);
            return;
        }
        if (smartspaceTargets.isEmpty()) {
            BcSmartspaceTemplateDataUtils.updateVisibility(mView, View.GONE);
            return;
        }
        mView.setVisibility(View.VISIBLE);
        SmartspaceTarget target = smartspaceTargets.get(0);
        if (target.getFeatureType() != 1) {
            BcSmartspaceTemplateDataUtils.updateVisibility(mView, View.GONE);
            return;
        }
        BaseTemplateData templateData = target.getTemplateData();
        boolean hasValidTemplate =
                BcSmartspaceCardLoggerUtil.containsValidTemplateType(templateData);
        if (!hasValidTemplate && target.getHeaderAction() == null) {
            BcSmartspaceTemplateDataUtils.updateVisibility(mView, View.GONE);
            return;
        }
        BcSmartspaceCardLoggingInfo.Builder builder =
                new BcSmartspaceCardLoggingInfo.Builder()
                        .setInstanceId(InstanceId.create(target))
                        .setFeatureType(target.getFeatureType())
                        .setDisplaySurface(
                                BcSmartSpaceUtil.getLoggingDisplaySurface(mUiSurface, mDozeAmount))
                        .setUid(-1)
                        .setDimensionalInfo(
                                BcSmartspaceCardLoggerUtil.createDimensionalLoggingInfo(
                                        templateData));
        mLoggingInfo = new BcSmartspaceCardLoggingInfo(builder);
        if (hasValidTemplate) {
            BaseTemplateData.SubItemInfo subItemInfo = templateData.getSubtitleItem();
            if (subItemInfo == null) {
                Log.d("WeatherSmartspaceView", "Passed-in item info is null");
                return;
            }
            BcSmartspaceTemplateDataUtils.setText(mView, subItemInfo.getText());
            if (subItemInfo.getIcon() != null) {
                Drawable iconDrawable =
                        BcSmartSpaceUtil.getIconDrawableWithCustomSize(
                                subItemInfo.getIcon().getIcon(), getContext(), mIconSize);
                mIconDrawable.setIcon(iconDrawable);
                mView.setCompoundDrawablesRelative(mIconDrawable, null, null, null);
            }
            CharSequence text =
                    subItemInfo.getText() != null && !SmartspaceUtils.isEmpty(subItemInfo.getText())
                            ? subItemInfo.getText().getText()
                            : "";
            CharSequence contentDescription =
                    subItemInfo.getIcon() != null
                            ? subItemInfo.getIcon().getContentDescription()
                            : "";
            ContentDescriptionUtil.setFormattedContentDescription(
                    "WeatherSmartspaceView", mView, text, contentDescription);
            if (subItemInfo.getTapAction() != null && isClickable()) {
                BcSmartspaceDataPlugin.SmartspaceEventNotifier notifier =
                        mDataProvider != null
                                ? event -> mDataProvider.notifySmartspaceEvent(event)
                                : null;
                BcSmartSpaceUtil.setOnClickListener(
                        mView,
                        target,
                        subItemInfo.getTapAction(),
                        notifier,
                        "WeatherSmartspaceView",
                        mLoggingInfo,
                        0);
            }
        } else {
            SmartspaceAction headerAction = target.getHeaderAction();
            if (headerAction == null) {
                Log.d("WeatherSmartspaceView", "Passed-in header action is null");
                return;
            }
            mView.setText(headerAction.getTitle().toString());
            ContentDescriptionUtil.setFormattedContentDescription(
                    "WeatherSmartspaceView",
                    mView,
                    headerAction.getTitle(),
                    headerAction.getContentDescription());
            Drawable iconDrawable =
                    BcSmartSpaceUtil.getIconDrawableWithCustomSize(
                            headerAction.getIcon(), getContext(), mIconSize);
            mIconDrawable.setIcon(iconDrawable);
            mView.setCompoundDrawablesRelative(mIconDrawable, null, null, null);
            BcSmartspaceDataPlugin.SmartspaceEventNotifier notifier =
                    mDataProvider != null
                            ? event -> mDataProvider.notifySmartspaceEvent(event)
                            : null;
            BcSmartSpaceUtil.setOnClickListener(
                    mView,
                    target,
                    headerAction,
                    notifier,
                    "WeatherSmartspaceView",
                    mLoggingInfo,
                    0);
        }
        if (mRemoveTextDescent) {
            int paddingBottom =
                    mTextDescentExtraPadding
                            - (int) Math.floor(mView.getPaint().getFontMetrics().descent);
            mView.setPaddingRelative(0, 0, 0, paddingBottom);
        }
    }

    @Override
    public void registerDataProvider(BcSmartspaceDataPlugin dataProvider) {
        if (mDataProvider != null) {
            mDataProvider.unregisterListener(this);
        }
        mDataProvider = dataProvider;
        if (isAttachedToWindow()) {
            mDataProvider.registerListener(this);
        }
    }

    @Override
    public void setBgHandler(Handler handler) {
        mBgHandler = handler;
    }

    @Override
    public void setDozeAmount(float dozeAmount) {
        mDozeAmount = dozeAmount;
        int blendedColor = ColorUtils.blendARGB(mPrimaryTextColor, -1, dozeAmount);
        mView.setTextColor(blendedColor);

        if (mLoggingInfo == null) {
            return;
        }
        int loggingSurface = BcSmartSpaceUtil.getLoggingDisplaySurface(mUiSurface, mDozeAmount);
        if (loggingSurface == -1 || (loggingSurface == 3 && !mIsAodEnabled)) {
            return;
        }

        if (DEBUG) {
            Log.d(
                    "WeatherSmartspaceView",
                    "@"
                            + Integer.toHexString(hashCode())
                            + ", setDozeAmount: Logging SMARTSPACE_CARD_SEEN, loggingSurface = "
                            + loggingSurface);
        }

        BcSmartspaceCardLoggingInfo.Builder builder =
                new BcSmartspaceCardLoggingInfo.Builder()
                        .setInstanceId(mLoggingInfo.mInstanceId)
                        .setFeatureType(mLoggingInfo.mFeatureType)
                        .setDisplaySurface(loggingSurface)
                        .setUid(mLoggingInfo.mUid);
        BcSmartspaceCardLogger.log(
                BcSmartspaceEvent.SMARTSPACE_CARD_SEEN, new BcSmartspaceCardLoggingInfo(builder));
    }

    @Override
    public void setFalsingManager(com.android.systemui.plugins.FalsingManager falsingManager) {
        BcSmartSpaceUtil.sFalsingManager = falsingManager;
    }

    @Override
    public void setIntentStarter(BcSmartspaceDataPlugin.IntentStarter intentStarter) {
        BcSmartSpaceUtil.sIntentStarter = intentStarter;
    }

    @Override
    public void setPrimaryTextColor(int color) {
        mPrimaryTextColor = color;
        mView.setTextColor(ColorUtils.blendARGB(color, -1, mDozeAmount));
    }

    @Override
    public void setUiSurface(String uiSurface) {
        if (!isAttachedToWindow()) {
            mUiSurface = uiSurface;
        } else {
            throw new IllegalStateException("Must call before attaching view to window.");
        }
    }

    private void registerAodObserver() {
        if (DEBUG) {
            Log.d("WeatherSmartspaceView", "Registering AOD observer");
        }
        ContentResolver resolver = getContext().getContentResolver();
        resolver.registerContentObserver(
                Settings.Secure.getUriFor("doze_always_on"), false, mAodSettingsObserver, -1);
    }

    private void unregisterAodObserver() {
        if (DEBUG) {
            Log.d("WeatherSmartspaceView", "Unregistering AOD observer");
        }
        ContentResolver resolver = getContext().getContentResolver();
        resolver.unregisterContentObserver(mAodSettingsObserver);
    }

    private class WeatherSmartspaceViewObserver extends ContentObserver {
        private final WeatherSmartspaceView outer;

        public WeatherSmartspaceViewObserver(WeatherSmartspaceView outer, Handler handler) {
            super(handler);
            this.outer = outer;
        }

        @Override
        public void onChange(boolean selfChange) {
            boolean isAodEnabled =
                    Settings.Secure.getIntForUser(
                                    outer.getContext().getContentResolver(),
                                    "doze_always_on",
                                    0,
                                    outer.getContext().getUserId())
                            == 1;
            if (outer.mIsAodEnabled != isAodEnabled) {
                outer.mIsAodEnabled = isAodEnabled;
            }
        }
    }
}
