package com.google.android.systemui.smartspace;

import android.app.smartspace.SmartspaceTarget;
import android.app.smartspace.SmartspaceTargetEvent;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.android.systemui.plugins.BcSmartspaceConfigPlugin;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;
import com.android.systemui.plugins.FalsingManager;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLogger;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggerUtil;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.logging.BcSmartspaceSubcardLoggingInfo;
import com.google.android.systemui.smartspace.uitemplate.BaseTemplateCard;
import com.statix.android.systemui.res.R;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BcSmartspaceView extends FrameLayout
        implements BcSmartspaceDataPlugin.SmartspaceTargetListener,
                BcSmartspaceDataPlugin.SmartspaceView {
    public static final boolean DEBUG = Log.isLoggable("BcSmartspaceView", Log.DEBUG);

    public CardAdapter mAdapter;
    public final ContentObserver mAodObserver;
    public Handler mBgHandler;
    public int mCardPosition;
    public BcSmartspaceConfigPlugin mConfigProvider;
    public BcSmartspaceDataPlugin mDataProvider;
    public boolean mHasPerformedLongPress;
    public boolean mHasPostedLongPress;
    public boolean mIsAodEnabled;
    public final Set<String> mLastReceivedTargets = new ArraySet<>();
    public final Runnable mLongPressCallback;
    public PageIndicator mPageIndicator;
    public List<SmartspaceTarget> mPendingTargets;
    public float mPreviousDozeAmount;
    public int mScrollState;
    public boolean mSplitShadeEnabled;
    public Integer mSwipedCardPosition;
    public ViewPager mViewPager;
    public ViewPager2 mViewPager2;
    public final ViewPager2.OnPageChangeCallback mViewPager2OnPageChangeCallback;
    public final ViewPager.OnPageChangeListener mViewPagerOnPageChangeListener;

    public BcSmartspaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mConfigProvider = new DefaultBcSmartspaceConfigProvider();
        mLastReceivedTargets.clear();
        mIsAodEnabled = false;
        mCardPosition = 0;
        mPreviousDozeAmount = 0f;
        mScrollState = 0;
        mSplitShadeEnabled = false;
        mAodObserver = new AodObserver(this, new Handler());
        mViewPager2OnPageChangeCallback = new ViewPager2OnPageChangeCallback(this);
        mViewPagerOnPageChangeListener = new ViewPagerOnPageChangeListener(this);
        mLongPressCallback =
                () -> {
                    if (mViewPager2 != null && !mHasPerformedLongPress) {
                        mHasPerformedLongPress = true;
                        if (mViewPager2.performLongClick()) {
                            mViewPager2.setPressed(false);
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                };
        getContext().getTheme().applyStyle(R.style.DefaultSmartspaceView, false);
    }

    public void onViewPagerPageSelected(int position) {
        SmartspaceTarget previousTarget = mAdapter.getTargetAtPosition(mCardPosition);
        mCardPosition = position;
        SmartspaceTarget currentTarget = mAdapter.getTargetAtPosition(position);
        if (currentTarget != null) {
            logSmartspaceEvent(currentTarget, position, BcSmartspaceEvent.SMARTSPACE_CARD_SEEN);
        }
        if (mDataProvider == null) {
            Log.w(
                    "BcSmartspaceView",
                    "Cannot notify target hidden/shown smartspace events: data provider null");
            return;
        }
        if (previousTarget == null) {
            Log.w(
                    "BcSmartspaceView",
                    "Cannot notify target hidden smartspace event: previous target is null.");
        } else {
            SmartspaceTargetEvent.Builder builder = new SmartspaceTargetEvent.Builder(3);
            builder.setSmartspaceTarget(previousTarget);
            if (previousTarget.getBaseAction() != null) {
                builder.setSmartspaceActionId(previousTarget.getBaseAction().getId());
            }
            mDataProvider.notifySmartspaceEvent(builder.build());
        }
        if (currentTarget == null) {
            Log.w(
                    "BcSmartspaceView",
                    "Cannot notify target shown smartspace event: shown card smartspace target"
                        + " null.");
        } else {
            SmartspaceTargetEvent.Builder builder = new SmartspaceTargetEvent.Builder(2);
            builder.setSmartspaceTarget(currentTarget);
            if (currentTarget.getBaseAction() != null) {
                builder.setSmartspaceActionId(currentTarget.getBaseAction().getId());
            }
            mDataProvider.notifySmartspaceEvent(builder.build());
        }
    }

    public void cancelScheduledLongPress() {
        if (mViewPager2 != null && mHasPostedLongPress) {
            mHasPostedLongPress = false;
            mViewPager2.removeCallbacks(mLongPressCallback);
        }
    }

    public int getCurrentCardTopPadding() {
        int position = getSelectedPage();
        BcSmartspaceCard legacyCard = mAdapter.getLegacyCardAtPosition(position);
        if (legacyCard != null) {
            return legacyCard.getPaddingTop();
        }
        BaseTemplateCard templateCard = mAdapter.getTemplateCardAtPosition(position);
        if (templateCard != null) {
            return templateCard.getPaddingTop();
        }
        BcSmartspaceRemoteViewsCard remoteViewsCard =
                mAdapter.getRemoteViewsCardAtPosition(position);
        if (remoteViewsCard != null) {
            return remoteViewsCard.getPaddingTop();
        }
        return 0;
    }

    public int getSelectedPage() {
        if (mViewPager != null) {
            return mViewPager.getCurrentItem();
        } else if (mViewPager2 != null) {
            return mViewPager2.getCurrentItem();
        }
        return 0;
    }

    public boolean handleTouchOverride(MotionEvent event, ViewPager2 viewPager2, int classId) {
        if (viewPager2 == null) {
            return false;
        }
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mHasPerformedLongPress = false;
                if (viewPager2.isLongClickable()) {
                    cancelScheduledLongPress();
                    mHasPostedLongPress = true;
                    viewPager2.postDelayed(
                            mLongPressCallback, ViewConfiguration.getLongPressTimeout());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelScheduledLongPress();
                break;
        }
        if (mHasPerformedLongPress) {
            cancelScheduledLongPress();
            return true;
        }
        boolean result =
                (classId == 0)
                        ? viewPager2.onTouchEvent(event)
                        : viewPager2.onInterceptTouchEvent(event);
        if (result) {
            cancelScheduledLongPress();
            return true;
        }
        return false;
    }

    public void logSmartspaceEvent(SmartspaceTarget target, int rank, BcSmartspaceEvent event) {
        int receivedLatencyMillis = -1;
        if (event == BcSmartspaceEvent.SMARTSPACE_CARD_RECEIVED) {
            try {
                receivedLatencyMillis =
                        (int)
                                Instant.now()
                                        .minusMillis(target.getCreationTimeMillis())
                                        .toEpochMilli();
            } catch (Exception e) {
                Log.e(
                        "BcSmartspaceView",
                        "received_latency_millis will be -1 due to exception ",
                        e);
            }
        }
        boolean hasValidTemplate =
                BcSmartspaceCardLoggerUtil.containsValidTemplateType(target.getTemplateData());
        BcSmartspaceCardLoggingInfo.Builder loggingInfoBuilder =
                new BcSmartspaceCardLoggingInfo.Builder()
                        .setInstanceId(InstanceId.create(target))
                        .setFeatureType(target.getFeatureType())
                        .setDisplaySurface(
                                BcSmartSpaceUtil.getLoggingDisplaySurface(
                                        mAdapter.getUiSurface(), mAdapter.getDozeAmount()))
                        .setRank(rank)
                        .setCardinality(mAdapter.getCount())
                        .setReceivedLatency(receivedLatencyMillis)
                        .setUid(-1);
        BcSmartspaceSubcardLoggingInfo subcardInfo =
                hasValidTemplate
                        ? BcSmartspaceCardLoggerUtil.createSubcardLoggingInfo(
                                target.getTemplateData())
                        : BcSmartspaceCardLoggerUtil.createSubcardLoggingInfo(target);
        loggingInfoBuilder.setSubcardInfo(subcardInfo);
        loggingInfoBuilder.setDimensionalInfo(
                BcSmartspaceCardLoggerUtil.createDimensionalLoggingInfo(target.getTemplateData()));
        BcSmartspaceCardLoggingInfo loggingInfo =
                new BcSmartspaceCardLoggingInfo(loggingInfoBuilder);
        if (hasValidTemplate) {
            BcSmartspaceCardLoggerUtil.tryForcePrimaryFeatureTypeOrUpdateLogInfoFromTemplateData(
                    loggingInfo, target.getTemplateData());
        } else {
            BcSmartspaceCardLoggerUtil.tryForcePrimaryFeatureTypeAndInjectWeatherSubcard(
                    loggingInfo, target);
        }
        BcSmartspaceCardLogger.log(event, loggingInfo);
    }

    @Override
    public void onSmartspaceTargetsUpdated(List<? extends Parcelable> targets) {
        List<SmartspaceTarget> smartspaceTargets =
                targets.stream()
                        .filter(t -> t instanceof SmartspaceTarget)
                        .map(t -> (SmartspaceTarget) t)
                        .collect(Collectors.toList());
        if (DEBUG) {
            Log.d(
                    "BcSmartspaceView",
                    "@"
                            + Integer.toHexString(hashCode())
                            + ", onTargetsAvailable called. Callers = "
                            + Debug.getCallers(5));
            Log.d("BcSmartspaceView", " targets.size() = " + targets.size());
            Log.d("BcSmartspaceView", " targets = " + targets.toString());
        }
        if (mViewPager != null && mScrollState != 0 && mAdapter.getCount() > 1) {
            mPendingTargets = smartspaceTargets;
            return;
        }
        mPendingTargets = null;
        boolean isRtl = isLayoutRtl();
        int selectedPage = getSelectedPage();
        int position = isRtl ? mAdapter.getCount() - selectedPage : selectedPage;
        List<SmartspaceTarget> updatedTargets =
                isRtl ? new ArrayList<>(smartspaceTargets) : smartspaceTargets;
        if (isRtl) {
            Collections.reverse(updatedTargets);
        }
        mAdapter.setTargets(updatedTargets);
        int count = mAdapter.getCount();
        if (mPageIndicator != null) {
            mPageIndicator.setNumPages(count, isRtl);
        }
        if (isRtl) {
            int newPosition = Math.min(count - 1, count - position);
            position = Math.max(0, newPosition);
            if (mViewPager != null) {
                mViewPager.setCurrentItem(position, false);
            } else if (mViewPager2 != null) {
                mViewPager2.setCurrentItem(position, false);
            }
            mPageIndicator.setPageOffset(0f, position);
        }
        for (int i = 0; i < count; i++) {
            SmartspaceTarget target = mAdapter.getTargetAtPosition(i);
            if (!mLastReceivedTargets.contains(target.getSmartspaceTargetId())) {
                logSmartspaceEvent(target, i, BcSmartspaceEvent.SMARTSPACE_CARD_RECEIVED);
                SmartspaceTargetEvent.Builder builder = new SmartspaceTargetEvent.Builder(8);
                builder.setSmartspaceTarget(target);
                if (target.getBaseAction() != null) {
                    builder.setSmartspaceActionId(target.getBaseAction().getId());
                }
                mDataProvider.notifySmartspaceEvent(builder.build());
            }
        }
        mLastReceivedTargets.clear();
        mLastReceivedTargets.addAll(
                mAdapter.getSmartspaceTargets().stream()
                        .filter(t -> t instanceof SmartspaceTarget)
                        .map(t -> ((SmartspaceTarget) t).getSmartspaceTargetId())
                        .collect(Collectors.toList()));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mViewPager != null && mAdapter instanceof CardPagerAdapter) {
            mViewPager.setAdapter((CardPagerAdapter) mAdapter);
            mViewPager.addOnPageChangeListener(mViewPagerOnPageChangeListener);
        } else if (mViewPager2 != null && mAdapter instanceof CardRecyclerViewAdapter) {
            mViewPager2.setAdapter((CardRecyclerViewAdapter) mAdapter);
            mViewPager2.registerOnPageChangeCallback(mViewPager2OnPageChangeCallback);
        } else {
            Log.w("BcSmartspaceView", "Unable to attach the view pager adapter");
        }
        mPageIndicator.setNumPages(mAdapter.getCount(), isLayoutRtl());
        if ("lockscreen".equals(mAdapter.getUiSurface())) {
            try {
                if (mBgHandler == null) {
                    throw new IllegalStateException(
                            "Must set background handler to avoid making binder calls on main"
                                + " thread");
                }
                mBgHandler.post(
                        () -> {
                            ContentResolver resolver = getContext().getContentResolver();
                            int userId = getContext().getUserId();
                            mIsAodEnabled =
                                    Settings.Secure.getIntForUser(
                                                    resolver, "doze_always_on", 0, userId)
                                            == 1;
                            resolver.registerContentObserver(
                                    Settings.Secure.getUriFor("doze_always_on"),
                                    false,
                                    mAodObserver,
                                    -1);
                        });
            } catch (Exception e) {
                Log.w("BcSmartspaceView", "Unable to register Doze Always on content observer.", e);
            }
        }
        if (mDataProvider != null) {
            registerDataProvider(mDataProvider);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBgHandler == null) {
            throw new IllegalStateException(
                    "Must set background handler to avoid making binder calls on main thread");
        }
        mBgHandler.post(
                () -> getContext().getContentResolver().unregisterContentObserver(mAodObserver));
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(mViewPagerOnPageChangeListener);
        } else if (mViewPager2 != null) {
            mViewPager2.unregisterOnPageChangeCallback(mViewPager2OnPageChangeCallback);
        }
        if (mDataProvider != null) {
            mDataProvider.unregisterListener(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View pager = findViewById(R.id.smartspace_card_pager);
        if (pager instanceof ViewPager) {
            mViewPager = (ViewPager) pager;
            mAdapter = new CardPagerAdapter(this, mConfigProvider);
        } else if (pager instanceof ViewPager2) {
            mViewPager2 = (ViewPager2) pager;
            mAdapter = new CardRecyclerViewAdapter(this, mConfigProvider);
        } else {
            throw new IllegalStateException("smartspace_card_pager is an invalid view type");
        }
        mPageIndicator = findViewById(R.id.smartspace_page_indicator);
        int paddingStart =
                getResources().getDimensionPixelSize(R.dimen.non_remoteviews_card_padding_start);
        mPageIndicator.setPaddingRelative(
                paddingStart,
                mPageIndicator.getPaddingTop(),
                mPageIndicator.getPaddingEnd(),
                mPageIndicator.getPaddingBottom());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mViewPager2 != null) {
            boolean handled = handleTouchOverride(event, mViewPager2, 1);
            return handled || super.onInterceptTouchEvent(event) || mHasPerformedLongPress;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int desiredHeight =
                getResources().getDimensionPixelSize(R.dimen.enhanced_smartspace_height);
        if (height > 0 && height < desiredHeight) {
            float scale = (float) height / desiredHeight;
            int width = (int) (MeasureSpec.getSize(widthMeasureSpec) / scale);
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY));
            setScaleX(scale);
            setScaleY(scale);
            setPivotX(0);
            setPivotY(desiredHeight / 2f);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setScaleX(1f);
            setScaleY(1f);
            resetPivot();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mViewPager2 != null) {
            return handleTouchOverride(event, mViewPager2, 0);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (mDataProvider != null) {
            SmartspaceTargetEvent.Builder builder =
                    new SmartspaceTargetEvent.Builder(isVisible ? 6 : 7);
            mDataProvider.notifySmartspaceEvent(builder.build());
        }
        if (mViewPager != null && mScrollState != 0) {
            mScrollState = 0;
            if (mPendingTargets != null) {
                onSmartspaceTargetsUpdated(mPendingTargets);
            }
        }
    }

    public void registerConfigProvider(BcSmartspaceConfigPlugin configProvider) {
        mConfigProvider = configProvider;
        mAdapter.setConfigProvider(configProvider);
    }

    public void registerDataProvider(BcSmartspaceDataPlugin dataProvider) {
        if (mDataProvider != null) {
            mDataProvider.unregisterListener(this);
        }
        mDataProvider = dataProvider;
        dataProvider.registerListener(this);
        mAdapter.setDataProvider(dataProvider);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            cancelScheduledLongPress();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void setBgHandler(Handler handler) {
        mBgHandler = handler;
        mAdapter.setBgHandler(handler);
    }

    public void setDozeAmount(float dozeAmount) {
        List<SmartspaceTarget> previousTargets = mAdapter.getSmartspaceTargets();
        mAdapter.setDozeAmount(dozeAmount);
        if (!mAdapter.getSmartspaceTargets().isEmpty()) {
            BcSmartspaceTemplateDataUtils.updateVisibility(this, View.VISIBLE);
        }
        float alpha = 1f;
        if (mAdapter.getHasAodLockscreenTransition()) {
            if (dozeAmount == mPreviousDozeAmount) {
                alpha = getAlpha();
            } else if (mPreviousDozeAmount > dozeAmount) {
                alpha = 1f - dozeAmount;
            } else {
                alpha = dozeAmount;
            }
            float threshold = 0.36f;
            if (alpha < threshold) {
                alpha = (threshold - alpha) / threshold;
            } else {
                alpha = (alpha - threshold) / 0.64f;
            }
        }
        setAlpha(alpha);
        if (mPageIndicator != null) {
            mPageIndicator.setNumPages(mAdapter.getCount(), isLayoutRtl());
            mPageIndicator.setAlpha(alpha);
            if (mPageIndicator.getVisibility() != View.GONE) {
                BcSmartspaceTemplateDataUtils.updateVisibility(
                        mPageIndicator, dozeAmount == 1f ? View.INVISIBLE : View.VISIBLE);
            }
        }
        mPreviousDozeAmount = dozeAmount;
        if (mAdapter.getHasDifferentTargets()
                && mAdapter.getSmartspaceTargets() != previousTargets
                && mAdapter.getCount() > 0) {
            int position = isLayoutRtl() ? mAdapter.getCount() - 1 : 0;
            if (mViewPager != null) {
                mViewPager.setCurrentItem(position, false);
            } else if (mViewPager2 != null) {
                mViewPager2.setCurrentItem(position, false);
            }
            mPageIndicator.setPageOffset(0f, position);
        }
        int displaySurface =
                BcSmartSpaceUtil.getLoggingDisplaySurface(
                        mAdapter.getUiSurface(), mAdapter.getDozeAmount());
        if (displaySurface == -1 || (displaySurface == 3 && !mIsAodEnabled)) {
            return;
        }
        if (DEBUG) {
            Log.d(
                    "BcSmartspaceView",
                    "@"
                            + Integer.toHexString(hashCode())
                            + ", setDozeAmount: Logging SMARTSPACE_CARD_SEEN, currentSurface = "
                            + displaySurface);
        }
        SmartspaceTarget target = mAdapter.getTargetAtPosition(mCardPosition);
        if (target == null) {
            Log.w("BcSmartspaceView", "Current card is not present in the Adapter; cannot log.");
            return;
        }
        logSmartspaceEvent(target, mCardPosition, BcSmartspaceEvent.SMARTSPACE_CARD_SEEN);
    }

    public void setDozing(boolean dozing) {
        if (!dozing
                && mSplitShadeEnabled
                && mAdapter.getHasAodLockscreenTransition()
                && mAdapter.getLockscreenTargets().isEmpty()) {
            BcSmartspaceTemplateDataUtils.updateVisibility(this, View.GONE);
        }
    }

    public void setFalsingManager(FalsingManager falsingManager) {
        BcSmartSpaceUtil.sFalsingManager = falsingManager;
    }

    public void setHorizontalPaddings(int padding) {
        if (mPageIndicator != null) {
            mPageIndicator.setPaddingRelative(
                    padding,
                    mPageIndicator.getPaddingTop(),
                    padding,
                    mPageIndicator.getPaddingBottom());
        }
        mAdapter.setNonRemoteViewsHorizontalPadding(padding);
    }

    public void setIntentStarter(BcSmartspaceDataPlugin.IntentStarter intentStarter) {
        BcSmartSpaceUtil.sIntentStarter = intentStarter;
    }

    public void setKeyguardBypassEnabled(boolean enabled) {
        mAdapter.setKeyguardBypassEnabled(enabled);
    }

    public void setMediaTarget(SmartspaceTarget target) {
        mAdapter.setMediaTarget(target);
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        if (mViewPager != null) {
            mViewPager.setOnLongClickListener(listener);
        } else if (mViewPager2 != null) {
            mViewPager2.setOnLongClickListener(listener);
        }
    }

    public void setPrimaryTextColor(int color) {
        mAdapter.setPrimaryTextColor(color);
        mPageIndicator.mPrimaryColor = color;
        for (int i = 0; i < mPageIndicator.getChildCount(); i++) {
            ImageView dot = (ImageView) mPageIndicator.getChildAt(i);
            Drawable drawable = dot.getDrawable();
            drawable.setTint(color);
        }
    }

    public void setScreenOn(boolean screenOn) {
        if (mViewPager != null && mScrollState != 0) {
            mScrollState = 0;
            if (mPendingTargets != null) {
                onSmartspaceTargetsUpdated(mPendingTargets);
            }
        }
        mAdapter.setScreenOn(screenOn);
    }

    public void setSplitShadeEnabled(boolean enabled) {
        mSplitShadeEnabled = enabled;
    }

    public void setTimeChangedDelegate(BcSmartspaceDataPlugin.TimeChangedDelegate delegate) {
        mAdapter.setTimeChangedDelegate(delegate);
    }

    public void setUiSurface(String uiSurface) {
        if (isAttachedToWindow()) {
            throw new IllegalStateException("Must call before attaching view to window.");
        }
        if ("home".equals(uiSurface)) {
            getContext().getTheme().applyStyle(R.style.LauncherSmartspaceView, true);
        }
        mAdapter.setUiSurface(uiSurface);
    }

    public static class AodObserver extends ContentObserver {
        public final BcSmartspaceView this$0;

        public AodObserver(BcSmartspaceView view, Handler handler) {
            super(handler);
            this.this$0 = view;
        }

        @Override
        public void onChange(boolean selfChange) {
            ContentResolver resolver = this$0.getContext().getContentResolver();
            int userId = this$0.getContext().getUserId();
            this$0.mIsAodEnabled =
                    Settings.Secure.getIntForUser(resolver, "doze_always_on", 0, userId) == 1;
        }
    }

    public static class ViewPager2OnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
        public final BcSmartspaceView this$0;

        public ViewPager2OnPageChangeCallback(BcSmartspaceView view) {
            this.this$0 = view;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            this$0.mScrollState = state;
            if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                this$0.mSwipedCardPosition = this$0.mViewPager2.getCurrentItem();
            }
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                if (this$0.mConfigProvider.isSwipeEventLoggingEnabled()
                        && this$0.mSwipedCardPosition != null) {
                    int swipedPosition = this$0.mSwipedCardPosition;
                    if (swipedPosition != this$0.mViewPager2.getCurrentItem()) {
                        SmartspaceCard card = this$0.mAdapter.getCardAtPosition(swipedPosition);
                        if (card != null) {
                            BcSmartspaceCardLogger.log(
                                    BcSmartspaceEvent.SMARTSPACE_CARD_SWIPE, card.getLoggingInfo());
                        }
                    }
                }
                this$0.mSwipedCardPosition = null;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (this$0.mPageIndicator != null) {
                this$0.mPageIndicator.setPageOffset(positionOffset, position);
            }
        }

        @Override
        public void onPageSelected(int position) {
            this$0.onViewPagerPageSelected(position);
        }
    }

    public static class ViewPagerOnPageChangeListener implements ViewPager.OnPageChangeListener {
        public final BcSmartspaceView this$0;

        public ViewPagerOnPageChangeListener(BcSmartspaceView view) {
            this.this$0 = view;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            this$0.mScrollState = state;
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                this$0.mSwipedCardPosition = this$0.mViewPager.getCurrentItem();
            }
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (this$0.mConfigProvider.isSwipeEventLoggingEnabled()
                        && this$0.mSwipedCardPosition != null) {
                    int swipedPosition = this$0.mSwipedCardPosition;
                    if (swipedPosition != this$0.mViewPager.getCurrentItem()) {
                        SmartspaceCard card = this$0.mAdapter.getCardAtPosition(swipedPosition);
                        if (card != null) {
                            BcSmartspaceCardLogger.log(
                                    BcSmartspaceEvent.SMARTSPACE_CARD_SWIPE, card.getLoggingInfo());
                        }
                    }
                }
                this$0.mSwipedCardPosition = null;
                if (this$0.mPendingTargets != null) {
                    this$0.onSmartspaceTargetsUpdated(this$0.mPendingTargets);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (this$0.mPageIndicator != null) {
                this$0.mPageIndicator.setPageOffset(positionOffset, position);
            }
        }

        @Override
        public void onPageSelected(int position) {
            this$0.onViewPagerPageSelected(position);
        }
    }
}
