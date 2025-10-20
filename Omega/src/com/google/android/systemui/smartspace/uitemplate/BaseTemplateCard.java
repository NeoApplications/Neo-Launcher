package com.google.android.systemui.smartspace.uitemplate;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.launcher3.R;
import com.android.launcher3.icons.GraphicsUtils;

import com.google.android.systemui.smartspace.BcSmartSpaceUtil;
import com.google.android.systemui.smartspace.BcSmartspaceCardSecondary;
import com.google.android.systemui.smartspace.BcSmartspaceTemplateDataUtils;
import com.google.android.systemui.smartspace.DoubleShadowIconDrawable;
import com.google.android.systemui.smartspace.DoubleShadowTextView;
import com.google.android.systemui.smartspace.IcuDateTextView;
import com.google.android.systemui.smartspace.SmartspaceCard;
import com.google.android.systemui.smartspace.TouchDelegateComposite;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggerUtil;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardMetadataLoggingInfo;
import com.google.android.systemui.smartspace.plugin.BcSmartspaceDataPlugin;
import com.google.android.systemui.smartspace.utils.ContentDescriptionUtil;
import com.saulhdev.smartspace.SmartspaceTarget;
import com.saulhdev.smartspace.SmartspaceUtils;
import com.saulhdev.smartspace.uitemplatedata.BaseTemplateData;
import com.saulhdev.smartspace.uitemplatedata.Icon;
import com.saulhdev.smartspace.uitemplatedata.TapAction;
import com.saulhdev.smartspace.uitemplatedata.Text;

import java.util.UUID;

public class BaseTemplateCard extends ConstraintLayout implements SmartspaceCard {
    public Handler mBgHandler;
    public IcuDateTextView mDateView;
    public float mDozeAmount;
    public ViewGroup mExtrasGroup;
    public int mFeatureType;
    public int mIconTintColor;
    public BcSmartspaceCardLoggingInfo mLoggingInfo;
    public BcSmartspaceCardSecondary mSecondaryCard;
    public ViewGroup mSecondaryCardPane;
    public boolean mShouldShowPageIndicator;
    public ViewGroup mSubtitleGroup;
    public Rect mSubtitleHitRect;
    public Rect mSubtitleSupplementalHitRect;
    public DoubleShadowTextView mSubtitleSupplementalView;
    public DoubleShadowTextView mSubtitleTextView;
    public DoubleShadowTextView mSupplementalLineTextView;
    public SmartspaceTarget mTarget;
    public BaseTemplateData mTemplateData;
    public ViewGroup mTextGroup;
    public DoubleShadowTextView mTitleTextView;
    public final TouchDelegateComposite mTouchDelegateComposite;
    public boolean mTouchDelegateIsDirty;
    public String mUiSurface;
    public boolean mValidSecondaryCard;

    public BaseTemplateCard(Context context) {
        this(context, null);
    }

    public BaseTemplateCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSecondaryCard = null;
        mFeatureType = 0;
        mLoggingInfo = null;
        mIconTintColor = GraphicsUtils.getAttrColor(context, android.R.attr.textColorPrimary);
        mTextGroup = null;
        mSecondaryCardPane = null;
        mDateView = null;
        mTitleTextView = null;
        mSubtitleGroup = null;
        mSubtitleTextView = null;
        mSubtitleSupplementalView = null;
        mSubtitleHitRect = null;
        mSubtitleSupplementalHitRect = null;
        mExtrasGroup = null;
        mSupplementalLineTextView = null;
        mTouchDelegateComposite = new TouchDelegateComposite(this);
        mTouchDelegateIsDirty = false;
        context.getTheme().applyStyle(R.style.Smartspace, false);
        setDefaultFocusHighlightEnabled(false);
    }

    public static boolean shouldTint(BaseTemplateData.SubItemInfo subItemInfo) {
        if (subItemInfo == null || subItemInfo.getIcon() == null) {
            return false;
        }
        return subItemInfo.getIcon().shouldTint();
    }

    @Override
    public void bindData(
            SmartspaceTarget target,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
            BcSmartspaceCardLoggingInfo loggingInfo,
            boolean shouldShowPageIndicator) {
        mTarget = null;
        mTemplateData = null;
        mFeatureType = 0;
        mLoggingInfo = null;
        setOnClickListener(null);
        if (mDateView != null) {
            mDateView.setOnClickListener(null);
        }
        if (mSubtitleGroup != null) {
            mSubtitleGroup.setOnClickListener(null);
        }
        resetTextView(mTitleTextView);
        resetTextView(mSubtitleTextView);
        resetTextView(mSubtitleSupplementalView);
        resetTextView(mSupplementalLineTextView);
        BcSmartspaceTemplateDataUtils.updateVisibility(mTitleTextView, View.GONE);
        BcSmartspaceTemplateDataUtils.updateVisibility(mSubtitleGroup, View.GONE);
        BcSmartspaceTemplateDataUtils.updateVisibility(mSubtitleTextView, View.GONE);
        BcSmartspaceTemplateDataUtils.updateVisibility(mSubtitleSupplementalView, View.GONE);
        BcSmartspaceTemplateDataUtils.updateVisibility(mSecondaryCardPane, View.GONE);
        BcSmartspaceTemplateDataUtils.updateVisibility(mExtrasGroup, View.GONE);
        mTarget = target;
        mTemplateData = target.getTemplateData();
        mFeatureType = target.getFeatureType();
        mLoggingInfo = loggingInfo;
        mShouldShowPageIndicator = shouldShowPageIndicator;
        mValidSecondaryCard = false;

        if (mTextGroup != null) {
            mTextGroup.setTranslationX(0f);
        }
        if (mTemplateData == null) {
            return;
        }
        mLoggingInfo = getLoggingInfo();

        // STX comment-out // Handle secondary card
        if (mSecondaryCard != null) {
            Log.i("SsBaseTemplateCard", "Secondary card is not null");
            mSecondaryCard.reset(target.getSmartspaceTargetId());
            mValidSecondaryCard =
                    mSecondaryCard.setSmartspaceActions(target, eventNotifier, mLoggingInfo);
            // STX comment-out BcSmartspaceTemplateDataUtils.updateVisibility(mSecondaryCardPane,
            // mValidSecondaryCard ? View.VISIBLE : View.GONE);
            // STX comment-out } else {
            // STX comment-out Log.i("SsBaseTemplateCard", "Secondary card is null");
            // STX comment-out BcSmartspaceTemplateDataUtils.updateVisibility(mSecondaryCardPane,
            // View.GONE);
        }

        // STX comment-out // Handle date view for primary card
        // STX comment-out if (mDateView != null && mFeatureType == 1) {
        if (mDateView != null) {
            String tapActionId = UUID.randomUUID().toString();
            TapAction tapAction =
                    new TapAction.Builder(tapActionId)
                            .setIntent(BcSmartSpaceUtil.getOpenCalendarIntent())
                            .build();
            BcSmartSpaceUtil.setOnClickListener(
                    this, mTarget, tapAction, eventNotifier, "SsBaseTemplateCard", mLoggingInfo, 0);
            BcSmartSpaceUtil.setOnClickListener(
                    mDateView,
                    mTarget,
                    tapAction,
                    eventNotifier,
                    "SsBaseTemplateCard",
                    mLoggingInfo,
                    0);
            // STX comment-out BcSmartspaceTemplateDataUtils.updateVisibility(mDateView,
            // View.VISIBLE);
            // STX comment-out } else if (mDateView != null) {
            // STX comment-out BcSmartspaceTemplateDataUtils.updateVisibility(mDateView, View.GONE);
        }

        setUpTextView(mTitleTextView, mTemplateData.getPrimaryItem(), eventNotifier);
        setUpTextView(mSubtitleTextView, mTemplateData.getSubtitleItem(), eventNotifier);
        setUpTextView(
                mSubtitleSupplementalView,
                mTemplateData.getSubtitleSupplementalItem(),
                eventNotifier);
        setUpTextView(
                mSupplementalLineTextView, mTemplateData.getSupplementalLineItem(), eventNotifier);

        if (mExtrasGroup != null) {
            if (mSupplementalLineTextView != null
                    && mSupplementalLineTextView.getVisibility() == View.VISIBLE
                    && (!mShouldShowPageIndicator || mDateView != null)) {
                BcSmartspaceTemplateDataUtils.updateVisibility(mExtrasGroup, View.VISIBLE);
                updateZenColors();
            } else {
                BcSmartspaceTemplateDataUtils.updateVisibility(mExtrasGroup, View.GONE);
            }
        }

        boolean subtitleVisible =
                mSubtitleTextView.getVisibility() != View.GONE
                        || mSubtitleSupplementalView.getVisibility() != View.GONE;
        BcSmartspaceTemplateDataUtils.updateVisibility(
                mSubtitleGroup, subtitleVisible ? View.VISIBLE : View.GONE);

        if (mTarget.getFeatureType() == 1
                && mSubtitleSupplementalView != null
                && mSubtitleSupplementalView.getVisibility() == View.VISIBLE) {
            mSubtitleTextView.setEllipsize(null);
        }

        if (mTemplateData.getPrimaryItem() != null
                && mTemplateData.getPrimaryItem().getTapAction() != null) {
            BcSmartSpaceUtil.setOnClickListener(
                    this,
                    mTarget,
                    mTemplateData.getPrimaryItem().getTapAction(),
                    eventNotifier,
                    "SsBaseTemplateCard",
                    mLoggingInfo,
                    0);
        }

        if (mSecondaryCardPane != null) {
            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) mSecondaryCardPane.getLayoutParams();
            params.matchConstraintMaxWidth = getWidth() / 2;
            mSecondaryCardPane.setLayoutParams(params);
        }
        mTouchDelegateIsDirty = true;
    }

    @Override
    public AccessibilityNodeInfo createAccessibilityNodeInfo() {
        AccessibilityNodeInfo info = super.createAccessibilityNodeInfo();
        info.getExtras().putCharSequence("AccessibilityNodeInfo.roleDescription", " ");
        return info;
    }

    @Override
    public BcSmartspaceCardLoggingInfo getLoggingInfo() {
        if (mLoggingInfo != null) {
            return mLoggingInfo;
        }
        BcSmartspaceCardLoggingInfo.Builder builder =
                new BcSmartspaceCardLoggingInfo.Builder()
                        .setDisplaySurface(
                                BcSmartSpaceUtil.getLoggingDisplaySurface(mUiSurface, mDozeAmount))
                        .setFeatureType(mFeatureType)
                        .setUid(-1);
        builder.setDimensionalInfo(
                BcSmartspaceCardLoggerUtil.createDimensionalLoggingInfo(mTarget.getTemplateData()));
        return new BcSmartspaceCardLoggingInfo(builder);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int paddingStart =
                getResources().getDimensionPixelSize(R.dimen.non_remoteviews_card_padding_start);
        setPaddingRelative(paddingStart, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
        mTextGroup = findViewById(R.id.text_group);
        mSecondaryCardPane = findViewById(R.id.secondary_card_group);
        mDateView = findViewById(R.id.date);
        mTitleTextView = findViewById(R.id.title_text);
        mSubtitleGroup = findViewById(R.id.smartspace_subtitle_group);
        mSubtitleTextView = findViewById(R.id.subtitle_text);
        mSubtitleSupplementalView = findViewById(R.id.base_action_icon_subtitle);
        mExtrasGroup = findViewById(R.id.smartspace_extras_group);
        if (mSubtitleTextView != null) {
            mSubtitleHitRect = new Rect();
        }
        if (mSubtitleSupplementalView != null) {
            mSubtitleSupplementalHitRect = new Rect();
        }
        if (mSubtitleTextView != null || mSubtitleSupplementalView != null) {
            setTouchDelegate(mTouchDelegateComposite);
        }
        if (mExtrasGroup != null) {
            mSupplementalLineTextView = mExtrasGroup.findViewById(R.id.supplemental_line_text);
        }
        if (mBgHandler != null && mDateView != null) {
            mDateView.mBgHandler = mBgHandler;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed && !mTouchDelegateIsDirty) {
            return;
        }
        mTouchDelegateIsDirty = false;
        TouchDelegate touchDelegate = getTouchDelegate();
        if (touchDelegate == null || !(touchDelegate instanceof TouchDelegateComposite)) {
            return;
        }
        TouchDelegateComposite composite = (TouchDelegateComposite) touchDelegate;
        composite.mDelegates.clear();
        if (mSubtitleGroup == null || mSubtitleGroup.getVisibility() != View.VISIBLE) {
            return;
        }
        boolean subtitleTextVisible =
                mSubtitleTextView != null && mSubtitleTextView.getVisibility() == View.VISIBLE;
        boolean subtitleSupplementalVisible =
                mSubtitleSupplementalView != null
                        && mSubtitleSupplementalView.getVisibility() == View.VISIBLE;
        if (!subtitleTextVisible && !subtitleSupplementalVisible) {
            return;
        }
        int padding =
                getResources().getDimensionPixelSize(R.dimen.subtitle_hit_rect_height)
                        - mSubtitleGroup.getHeight();
        padding = Math.max(padding / 2, 0);
        if (padding <= 0 && mSubtitleGroup.getBottom() == getHeight()) {
            return;
        }
        if (subtitleTextVisible) {
            mSubtitleTextView.getHitRect(mSubtitleHitRect);
            offsetDescendantRectToMyCoords(mSubtitleGroup, mSubtitleHitRect);
            if (padding > 0) {
                mSubtitleHitRect.top -= padding;
            }
            mSubtitleHitRect.bottom = getBottom();
            composite.mDelegates.add(new TouchDelegate(mSubtitleHitRect, mSubtitleTextView));
        }
        if (subtitleSupplementalVisible) {
            mSubtitleSupplementalView.getHitRect(mSubtitleSupplementalHitRect);
            offsetDescendantRectToMyCoords(mSubtitleGroup, mSubtitleSupplementalHitRect);
            if (padding > 0) {
                mSubtitleSupplementalHitRect.top -= padding;
            }
            mSubtitleSupplementalHitRect.bottom = getBottom();
            composite.mDelegates.add(
                    new TouchDelegate(mSubtitleSupplementalHitRect, mSubtitleSupplementalView));
        }
    }

    public void resetTextView(DoubleShadowTextView textView) {
        if (textView == null) {
            return;
        }
        textView.setCompoundDrawablesRelative(null, null, null, null);
        textView.setOnClickListener(null);
        textView.setContentDescription(null);
        textView.setText(null);
        textView.setTranslationX(0f);
    }

    public void setDozeAmount(float dozeAmount) {
        mDozeAmount = dozeAmount;
        if (mTarget != null
                && mTarget.getBaseAction() != null
                && mTarget.getBaseAction().getExtras() != null) {
            Bundle extras = mTarget.getBaseAction().getExtras();
            if (mTitleTextView != null && extras.getBoolean("hide_title_on_aod")) {
                mTitleTextView.setAlpha(1f - dozeAmount);
            }
            if (mSubtitleTextView != null && extras.getBoolean("hide_subtitle_on_aod")) {
                mSubtitleTextView.setAlpha(1f - dozeAmount);
            }
        }
        int secondaryCardVisibility =
                (mDozeAmount == 1f || !mValidSecondaryCard) ? View.GONE : View.VISIBLE;
        BcSmartspaceTemplateDataUtils.updateVisibility(mSecondaryCardPane, secondaryCardVisibility);
        if (mSecondaryCardPane != null
                && mSecondaryCardPane.getVisibility() == View.VISIBLE
                && mTextGroup != null) {
            int direction = isRtl() ? 1 : -1;
            float translation = mSecondaryCardPane.getWidth() * direction;
            Interpolator interpolator = com.android.app.animation.Interpolators.EMPHASIZED;
            mTextGroup.setTranslationX(
                    translation * ((PathInterpolator) interpolator).getInterpolation(mDozeAmount));
            float alpha = Math.min(1f, Math.max(0f, (1f - mDozeAmount) * 9f - 6f));
            mSecondaryCardPane.setAlpha(alpha);
        } else if (mTextGroup != null) {
            mTextGroup.setTranslationX(0f);
        }
    }

    public void setPrimaryTextColor(int color) {
        mIconTintColor = color;
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(color);
            if (mTemplateData != null) {
                updateTextViewIconTint(mTitleTextView, shouldTint(mTemplateData.getPrimaryItem()));
            }
        }
        if (mDateView != null) {
            mDateView.setTextColor(color);
        }
        if (mSubtitleTextView != null) {
            mSubtitleTextView.setTextColor(color);
            if (mTemplateData != null) {
                updateTextViewIconTint(
                        mSubtitleTextView, shouldTint(mTemplateData.getSubtitleItem()));
            }
        }
        if (mSubtitleSupplementalView != null) {
            mSubtitleSupplementalView.setTextColor(color);
            if (mTemplateData != null) {
                updateTextViewIconTint(
                        mSubtitleSupplementalView,
                        shouldTint(mTemplateData.getSubtitleSupplementalItem()));
            }
        }
        updateZenColors();
    }

    public void setScreenOn(boolean screenOn) {
        if (mDateView != null) {
            mDateView.mIsInteractive = screenOn;
            mDateView.rescheduleTicker();
        }
    }

    public void setSecondaryCard(BcSmartspaceCardSecondary secondaryCard) {
        if (mSecondaryCardPane == null) {
            return;
        }
        mSecondaryCard = secondaryCard;
        BcSmartspaceTemplateDataUtils.updateVisibility(mSecondaryCardPane, View.GONE);
        mSecondaryCardPane.removeAllViews();
        if (secondaryCard != null) {
            ConstraintLayout.LayoutParams params =
                    new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            getResources()
                                    .getDimensionPixelSize(
                                            R.dimen.enhanced_smartspace_card_height));
            params.setMarginStart(
                    getResources()
                            .getDimensionPixelSize(
                                    R.dimen.enhanced_smartspace_secondary_card_start_margin));
            params.startToStart = 0;
            params.topToTop = 0;
            params.bottomToBottom = 0;
            mSecondaryCardPane.addView(secondaryCard, params);
        }
    }

    public void setUpTextView(
            DoubleShadowTextView textView,
            BaseTemplateData.SubItemInfo subItemInfo,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier) {
        if (textView == null) {
            Log.d("SsBaseTemplateCard", "No text view can be set up");
            return;
        }
        resetTextView(textView);
        if (subItemInfo == null) {
            Log.d("SsBaseTemplateCard", "Passed-in item info is null");
            BcSmartspaceTemplateDataUtils.updateVisibility(textView, View.GONE);
            return;
        }
        Text text = subItemInfo.getText();
        BcSmartspaceTemplateDataUtils.setText(textView, text);
        if (!SmartspaceUtils.isEmpty(text)) {
            textView.setTextColor(mIconTintColor);
        }
        Icon icon = subItemInfo.getIcon();
        if (icon != null) {
            DoubleShadowIconDrawable drawable = new DoubleShadowIconDrawable(getContext());
            int iconSize =
                    getResources().getDimensionPixelSize(R.dimen.enhanced_smartspace_icon_size);
            Drawable iconDrawable =
                    BcSmartSpaceUtil.getIconDrawableWithCustomSize(
                            icon.getIcon(), getContext(), iconSize);
            drawable.setIcon(iconDrawable);
            textView.setCompoundDrawablesRelative(drawable, null, null, null);
            CharSequence contentDescription = SmartspaceUtils.isEmpty(text) ? "" : text.getText();
            ContentDescriptionUtil.setFormattedContentDescription(
                    "SsBaseTemplateCard",
                    textView,
                    contentDescription,
                    icon.getContentDescription());
            updateTextViewIconTint(textView, icon.shouldTint());
            BcSmartspaceTemplateDataUtils.offsetTextViewForIcon(textView, drawable, isRtl());
        }
        BcSmartspaceTemplateDataUtils.updateVisibility(textView, View.VISIBLE);
        TapAction tapAction = subItemInfo.getTapAction();
        int subCardRank = 0;
        if (mLoggingInfo != null
                && mLoggingInfo.mSubcardInfo != null
                && !mLoggingInfo.mSubcardInfo.mSubcards.isEmpty()
                && subItemInfo.getLoggingInfo() != null) {
            int targetFeatureType = subItemInfo.getLoggingInfo().getFeatureType();
            if (targetFeatureType != mLoggingInfo.mFeatureType) {
                for (int i = 0; i < mLoggingInfo.mSubcardInfo.mSubcards.size(); i++) {
                    BcSmartspaceCardMetadataLoggingInfo subCard =
                            mLoggingInfo.mSubcardInfo.mSubcards.get(i);
                    if (subCard.mInstanceId == subItemInfo.getLoggingInfo().getInstanceId()
                            && subCard.mCardTypeId == targetFeatureType) {
                        subCardRank = i + 1;
                        break;
                    }
                }
            }
        }
        BcSmartSpaceUtil.setOnClickListener(
                textView,
                mTarget,
                tapAction,
                eventNotifier,
                "SsBaseTemplateCard",
                mLoggingInfo,
                subCardRank);
    }

    public void updateTextViewIconTint(DoubleShadowTextView textView, boolean shouldTint) {
        Drawable[] drawables = textView.getCompoundDrawablesRelative();
        for (Drawable drawable : drawables) {
            if (drawable != null) {
                if (shouldTint) {
                    drawable.setTint(mIconTintColor);
                } else {
                    drawable.setTintList(null);
                }
            }
        }
    }

    public void updateZenColors() {
        if (mSupplementalLineTextView != null) {
            mSupplementalLineTextView.setTextColor(mIconTintColor);
            if (BcSmartspaceCardLoggerUtil.containsValidTemplateType(mTemplateData)) {
                updateTextViewIconTint(
                        mSupplementalLineTextView,
                        shouldTint(mTemplateData.getSupplementalLineItem()));
            }
        }
    }
}
