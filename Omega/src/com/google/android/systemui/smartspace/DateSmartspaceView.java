package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceAction;
import android.app.smartspace.SmartspaceTarget;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper; // STX edit
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.internal.graphics.ColorUtils;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;
import com.android.systemui.plugins.FalsingManager;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLogger;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.statix.android.systemui.res.R;

public class DateSmartspaceView extends LinearLayout
        implements BcSmartspaceDataPlugin.SmartspaceView {
    public static final boolean DEBUG = Log.isLoggable("DateSmartspaceView", Log.DEBUG);
    private final ContentObserver mAodSettingsObserver;
    private Handler mBgHandler;
    private int mCurrentTextColor;
    private BcSmartspaceDataPlugin mDataProvider;
    private final SmartspaceAction mDateAction;
    private final SmartspaceTarget mDateTarget;
    private IcuDateTextView mDateView;
    private final DoubleShadowIconDrawable mDndIconDrawable;
    private ImageView mDndImageView;
    private float mDozeAmount;
    private boolean mIsAodEnabled;
    private BcSmartspaceCardLoggingInfo mLoggingInfo;
    private final BcNextAlarmData mNextAlarmData;
    private final DoubleShadowIconDrawable mNextAlarmIconDrawable;
    private DoubleShadowTextView mNextAlarmTextView;
    private int mPrimaryTextColor;
    private String mUiSurface;

    public DateSmartspaceView(Context context) {
        this(context, null);
    }

    public DateSmartspaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateSmartspaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mUiSurface = null;
        mDozeAmount = 0f;
        mDateTarget =
                new SmartspaceTarget.Builder(
                                "date_card_794317_92634",
                                new ComponentName(context, DateSmartspaceView.class),
                                context.getUser())
                        .setFeatureType(1)
                        .build();
        mDateAction =
                new SmartspaceAction.Builder("dateId", "Date")
                        .setIntent(BcSmartSpaceUtil.getOpenCalendarIntent())
                        .build();
        mNextAlarmData = new BcNextAlarmData();
        mAodSettingsObserver = new DateSmartspaceViewObserver(this, new Handler());
        context.getTheme().applyStyle(R.style.Smartspace, false);
        mNextAlarmIconDrawable = new DoubleShadowIconDrawable(context);
        mDndIconDrawable = new DoubleShadowIconDrawable(context);
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
                        "DateSmartspaceView",
                        "Unable to register DOZE_ALWAYS_ON content observer: ",
                        e);
            }
            ContentResolver resolver = getContext().getContentResolver();
            mIsAodEnabled =
                    Settings.Secure.getIntForUser(
                                    resolver, "doze_always_on", 0, getContext().getUserId())
                            == 1;
        }
        BcSmartspaceCardLoggingInfo.Builder builder =
                new BcSmartspaceCardLoggingInfo.Builder()
                        .setInstanceId(InstanceId.create(mDateTarget))
                        .setFeatureType(mDateTarget.getFeatureType())
                        .setDisplaySurface(
                                BcSmartSpaceUtil.getLoggingDisplaySurface(mUiSurface, mDozeAmount))
                        .setUid(-1);
        mLoggingInfo = new BcSmartspaceCardLoggingInfo(builder);
        BcSmartspaceDataPlugin.SmartspaceEventNotifier notifier =
                mDataProvider != null ? event -> mDataProvider.notifySmartspaceEvent(event) : null;
        BcSmartSpaceUtil.setOnClickListener(
                mDateView,
                mDateTarget,
                mDateAction,
                notifier,
                "DateSmartspaceView",
                mLoggingInfo,
                0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBgHandler != null) {
            mBgHandler.post(() -> unregisterAodObserver());
        } else {
            throw new IllegalStateException(
                    "Must set background handler to avoid making binder calls on main thread");
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDateView = findViewById(R.id.date);
        mNextAlarmTextView = findViewById(R.id.alarm_text_view);
        mDndImageView = findViewById(R.id.dnd_icon);
        // STX comment-out BcSmartspaceTemplateDataUtils.updateVisibility(mDateView, View.VISIBLE);
        // // Show date view for primary display
    }

    @Override
    public void registerDataProvider(BcSmartspaceDataPlugin dataProvider) {
        mDataProvider = dataProvider;
    }

    @Override
    public void setBgHandler(Handler handler) {
        mBgHandler = handler;
        if (mDateView != null) {
            mDateView.mBgHandler = handler;
        }
    }

    @Override
    public void setDnd(Drawable drawable, String description) {
        new Handler(Looper.getMainLooper())
                .post(
                        () -> { // STX edit
                            if (drawable == null) {
                                BcSmartspaceTemplateDataUtils.updateVisibility(
                                        mDndImageView, View.GONE);
                            } else {
                                mDndIconDrawable.setIcon(drawable.mutate());
                                mDndImageView.setImageDrawable(mDndIconDrawable);
                                mDndImageView.setContentDescription(description);
                                BcSmartspaceTemplateDataUtils.updateVisibility(
                                        mDndImageView, View.VISIBLE);
                                // STX comment-out
                                // BcSmartspaceTemplateDataUtils.updateVisibility(mDateView,
                                // View.GONE); // Hide date view when DND is present
                            }
                            updateColorForExtras();
                        }); // STX edit
    }

    @Override
    public void setDozeAmount(float dozeAmount) {
        mDozeAmount = dozeAmount;
        mCurrentTextColor = ColorUtils.blendARGB(mPrimaryTextColor, -1, dozeAmount);
        mDateView.setTextColor(mCurrentTextColor);
        updateColorForExtras();
        if (mLoggingInfo == null) {
            return;
        }
        int loggingSurface = BcSmartSpaceUtil.getLoggingDisplaySurface(mUiSurface, mDozeAmount);
        if (loggingSurface == -1 || (loggingSurface == 3 && !mIsAodEnabled)) {
            return;
        }
        if (DEBUG) {
            Log.d(
                    "DateSmartspaceView",
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
        if (mNextAlarmData.mImage != null) {
            BcSmartspaceCardLoggingInfo.Builder alarmBuilder =
                    new BcSmartspaceCardLoggingInfo.Builder()
                            .setInstanceId(InstanceId.create("upcoming_alarm_card_94510_12684"))
                            .setFeatureType(23)
                            .setDisplaySurface(loggingSurface)
                            .setUid(mLoggingInfo.mUid);
            BcSmartspaceCardLogger.log(
                    BcSmartspaceEvent.SMARTSPACE_CARD_SEEN,
                    new BcSmartspaceCardLoggingInfo(alarmBuilder));
        }
    }

    @Override
    public void setFalsingManager(FalsingManager falsingManager) {
        BcSmartSpaceUtil.sFalsingManager = falsingManager;
    }

    @Override
    public void setIntentStarter(BcSmartspaceDataPlugin.IntentStarter intentStarter) {
        BcSmartSpaceUtil.sIntentStarter = intentStarter;
    }

    @Override
    public void setNextAlarm(Drawable drawable, String description) {
        new Handler(Looper.getMainLooper())
                .post(
                        () -> { // STX edit
                            mNextAlarmData.mImage = drawable != null ? drawable.mutate() : null;
                            mNextAlarmData.mDescription = description;
                            if (mNextAlarmData.mImage == null) {
                                BcSmartspaceTemplateDataUtils.updateVisibility(
                                        mNextAlarmTextView, View.GONE);
                                // STX comment-out
                                // BcSmartspaceTemplateDataUtils.updateVisibility(mDateView,
                                // View.VISIBLE); // Show date view when no alarm data
                            } else {
                                mNextAlarmTextView.setContentDescription(
                                        getContext()
                                                .getString(
                                                        R.string.accessibility_next_alarm,
                                                        description));
                                String displayText =
                                        TextUtils.isEmpty(null)
                                                ? description
                                                : description + " · null";
                                mNextAlarmTextView.setText(displayText);
                                int iconSize =
                                        getResources()
                                                .getDimensionPixelSize(
                                                        R.dimen.enhanced_smartspace_icon_size);
                                mNextAlarmData.mImage.setBounds(0, 0, iconSize, iconSize);
                                mNextAlarmIconDrawable.setIcon(mNextAlarmData.mImage);
                                mNextAlarmTextView.setCompoundDrawablesRelative(
                                        mNextAlarmIconDrawable, null, null, null);
                                BcSmartspaceTemplateDataUtils.updateVisibility(
                                        mNextAlarmTextView, View.VISIBLE);
                                // STX comment-out
                                // BcSmartspaceTemplateDataUtils.updateVisibility(mDateView,
                                // View.GONE); // Hide date view when alarm data is present
                                BcSmartspaceDataPlugin.SmartspaceEventNotifier notifier =
                                        mDataProvider != null
                                                ? event ->
                                                        mDataProvider.notifySmartspaceEvent(event)
                                                : null;
                                int loggingSurface =
                                        BcSmartSpaceUtil.getLoggingDisplaySurface(
                                                mUiSurface, mDozeAmount);
                                BcNextAlarmData.setOnClickListener(
                                        mNextAlarmTextView, notifier, loggingSurface);
                            }
                            updateColorForExtras();
                        }); // STX edit
    }

    @Override
    public void setPrimaryTextColor(int color) {
        mPrimaryTextColor = color;
        mCurrentTextColor = ColorUtils.blendARGB(color, -1, mDozeAmount);
        mDateView.setTextColor(mCurrentTextColor);
        updateColorForExtras();
    }

    @Override
    public void setScreenOn(boolean screenOn) {
        if (mDateView != null) {
            mDateView.mIsInteractive = screenOn;
            mDateView.rescheduleTicker();
        }
    }

    @Override
    public void setTimeChangedDelegate(BcSmartspaceDataPlugin.TimeChangedDelegate delegate) {
        if (mDateView != null && !mDateView.isAttachedToWindow()) {
            mDateView.mTimeChangedDelegate = delegate;
        } else {
            throw new IllegalStateException("Must call before attaching view to window.");
        }
    }

    @Override
    public void setUiSurface(String uiSurface) {
        if (!isAttachedToWindow()) {
            mUiSurface = uiSurface;
            if (TextUtils.equals(uiSurface, "lockscreen")
                    && mDateView != null
                    && !mDateView.isAttachedToWindow()) {
                mDateView.mUpdatesOnAod = true;
            } else if (mDateView != null && mDateView.isAttachedToWindow()) {
                throw new IllegalStateException("Must call before attaching view to window.");
            }
        } else {
            throw new IllegalStateException("Must call before attaching view to window.");
        }
    }

    public void updateColorForExtras() {
        if (mNextAlarmTextView != null) {
            mNextAlarmTextView.setTextColor(mCurrentTextColor);
            mNextAlarmIconDrawable.setTint(mCurrentTextColor);
        }
        if (mDndImageView != null && mDndImageView.getDrawable() != null) {
            mDndImageView.getDrawable().setTint(mCurrentTextColor);
            mDndImageView.invalidate();
        }
    }

    private void registerAodObserver() {
        if (DEBUG) {
            Log.d("DateSmartspaceView", "Registering AOD observer");
        }
        ContentResolver resolver = getContext().getContentResolver();
        resolver.registerContentObserver(
                Settings.Secure.getUriFor("doze_always_on"), false, mAodSettingsObserver, -1);
    }

    private void unregisterAodObserver() {
        if (DEBUG) {
            Log.d("DateSmartspaceView", "Unregistering AOD observer");
        }
        ContentResolver resolver = getContext().getContentResolver();
        resolver.unregisterContentObserver(mAodSettingsObserver);
    }

    private class DateSmartspaceViewObserver extends ContentObserver {
        private final DateSmartspaceView outer;

        public DateSmartspaceViewObserver(DateSmartspaceView outer, Handler handler) {
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
