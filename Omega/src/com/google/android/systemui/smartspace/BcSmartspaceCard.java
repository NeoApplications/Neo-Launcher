package com.google.android.systemui.smartspace;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.launcher3.icons.GraphicsUtils;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.logging.BcSmartspaceCardMetadataLoggingInfo;
import com.google.android.systemui.smartspace.logging.BcSmartspaceSubcardLoggingInfo;
import com.google.android.systemui.smartspace.plugin.BcSmartspaceDataPlugin;
import com.google.android.systemui.smartspace.utils.ContentDescriptionUtil;
import com.saulhdev.smartspace.SmartspaceAction;
import com.saulhdev.smartspace.SmartspaceTarget;

import java.util.Locale;

public class BcSmartspaceCard extends ConstraintLayout implements SmartspaceCard {
    public final DoubleShadowIconDrawable mBaseActionIconDrawable;
    public Rect mBaseActionIconSubtitleHitRect;
    public DoubleShadowTextView mBaseActionIconSubtitleView;
    public float mDozeAmount;
    public BcSmartspaceDataPlugin.SmartspaceEventNotifier mEventNotifier;
    public final DoubleShadowIconDrawable mIconDrawable;
    public int mIconTintColor;
    public BcSmartspaceCardLoggingInfo mLoggingInfo;
    public BcSmartspaceCardSecondary mSecondaryCard;
    public ViewGroup mSecondaryCardGroup;
    public TextView mSubtitleTextView;
    public SmartspaceTarget mTarget;
    public ViewGroup mTextGroup;
    public TextView mTitleTextView;
    public boolean mTouchDelegateIsDirty;
    public String mUiSurface;
    public boolean mUsePageIndicatorUi;
    public boolean mValidSecondaryCard;

    public BcSmartspaceCard(Context context) {
        this(context, null);
    }

    public BcSmartspaceCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSecondaryCard = null;
        mIconTintColor = GraphicsUtils.getAttrColor(context, android.R.attr.textColorPrimary);
        mTextGroup = null;
        mSecondaryCardGroup = null;
        mTitleTextView = null;
        mSubtitleTextView = null;
        mBaseActionIconSubtitleView = null;
        mBaseActionIconSubtitleHitRect = null;
        mUiSurface = null;
        mTouchDelegateIsDirty = false;
        context.getTheme().applyStyle(R.style.Smartspace, false);
        mIconDrawable = new DoubleShadowIconDrawable(context);
        mBaseActionIconDrawable = new DoubleShadowIconDrawable(context);
        setDefaultFocusHighlightEnabled(false);
    }

    public static int getClickedIndex(BcSmartspaceCardLoggingInfo loggingInfo, int cardTypeId) {
        BcSmartspaceSubcardLoggingInfo subcardInfo = loggingInfo.mSubcardInfo;
        if (subcardInfo != null && subcardInfo.mSubcards != null) {
            for (int i = 0; i < subcardInfo.mSubcards.size(); i++) {
                BcSmartspaceCardMetadataLoggingInfo subCard = subcardInfo.mSubcards.get(i);
                if (subCard != null && subCard.mCardTypeId == cardTypeId) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    @Override
    public void bindData(
            SmartspaceTarget target,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
            BcSmartspaceCardLoggingInfo loggingInfo,
            boolean usePageIndicatorUi) {
        mLoggingInfo = null;
        mEventNotifier = null;
        BcSmartspaceTemplateDataUtils.updateVisibility(mSecondaryCardGroup, View.GONE);
        mIconDrawable.mIconDrawable = null;
        mBaseActionIconDrawable.mIconDrawable = null;
        setTitle(null, null, false);
        setSubtitle(null, null, false);
        updateIconTint();
        setOnClickListener(null);
        if (mTitleTextView != null) {
            mTitleTextView.setOnClickListener(null);
        }
        if (mSubtitleTextView != null) {
            mSubtitleTextView.setOnClickListener(null);
        }
        if (mBaseActionIconSubtitleView != null) {
            mBaseActionIconSubtitleView.setOnClickListener(null);
        }

        mTarget = target;
        mEventNotifier = eventNotifier;
        SmartspaceAction headerAction = target.getHeaderAction();
        SmartspaceAction baseAction = target.getBaseAction();
        mLoggingInfo = loggingInfo;
        mUsePageIndicatorUi = usePageIndicatorUi;
        mValidSecondaryCard = false;

        if (mTextGroup != null) {
            mTextGroup.setTranslationX(0f);
        }

        if (headerAction != null) {
            if (mSecondaryCard != null) {
                mSecondaryCard.reset(target.getSmartspaceTargetId());
                mValidSecondaryCard =
                        mSecondaryCard.setSmartspaceActions(target, eventNotifier, loggingInfo);
            }
            if (mSecondaryCardGroup != null) {
                mSecondaryCardGroup.setAlpha(1f);
                BcSmartspaceTemplateDataUtils.updateVisibility(
                        mSecondaryCardGroup,
                        mDozeAmount == 1f || !mValidSecondaryCard ? View.GONE : View.VISIBLE);
            }

            Icon icon = headerAction.getIcon();
            Drawable iconDrawable =
                    icon != null
                            ? BcSmartSpaceUtil.getIconDrawableWithCustomSize(
                                    icon,
                                    getContext(),
                                    getResources()
                                            .getDimensionPixelSize(
                                                    R.dimen.enhanced_smartspace_icon_size))
                            : null;
            boolean hasIcon = iconDrawable != null;
            mIconDrawable.setIcon(iconDrawable);

            CharSequence title = headerAction.getTitle();
            CharSequence subtitle = headerAction.getSubtitle();
            boolean hasTitle = target.getFeatureType() == 1 || !TextUtils.isEmpty(title);
            boolean hasSubtitle = !TextUtils.isEmpty(subtitle);
            CharSequence contentDescription = headerAction.getContentDescription();

            boolean useIconWithTitle = hasTitle != hasSubtitle && hasIcon;
            setTitle(hasTitle ? title : subtitle, contentDescription, useIconWithTitle);
            setSubtitle(hasTitle && hasSubtitle ? subtitle : null, contentDescription, hasIcon);
        }

        if (mBaseActionIconSubtitleView != null && baseAction != null) {
            Drawable baseIconDrawable =
                    baseAction.getIcon() != null
                            ? BcSmartSpaceUtil.getIconDrawableWithCustomSize(
                                    baseAction.getIcon(),
                                    getContext(),
                                    getResources()
                                            .getDimensionPixelSize(
                                                    R.dimen.enhanced_smartspace_icon_size))
                            : null;
            mBaseActionIconDrawable.setIcon(baseIconDrawable);

            if (baseIconDrawable == null) {
                BcSmartspaceTemplateDataUtils.updateVisibility(
                        mBaseActionIconSubtitleView, View.INVISIBLE);
                mBaseActionIconSubtitleView.setOnClickListener(null);
                mBaseActionIconSubtitleView.setContentDescription(null);
            } else {
                mBaseActionIconSubtitleView.setText(baseAction.getSubtitle());
                mBaseActionIconSubtitleView.setCompoundDrawablesRelative(
                        mBaseActionIconDrawable, null, null, null);
                BcSmartspaceTemplateDataUtils.updateVisibility(
                        mBaseActionIconSubtitleView, View.VISIBLE);

                int subcardType =
                        baseAction.getExtras() != null && !baseAction.getExtras().isEmpty()
                                ? baseAction.getExtras().getInt("subcardType", -1)
                                : -1;
                int clickedIndex =
                        subcardType != -1 ? getClickedIndex(loggingInfo, subcardType) : 0;
                if (clickedIndex == 0) {
                    Log.d(
                            "BcSmartspaceCard",
                            "Subcard expected but missing type. loggingInfo="
                                    + loggingInfo
                                    + ", baseAction="
                                    + baseAction);
                }
                BcSmartSpaceUtil.setOnClickListener(
                        mBaseActionIconSubtitleView,
                        target,
                        baseAction,
                        mEventNotifier,
                        "BcSmartspaceCard",
                        loggingInfo,
                        clickedIndex);
                ContentDescriptionUtil.setFormattedContentDescription(
                        "BcSmartspaceCard",
                        mBaseActionIconSubtitleView,
                        baseAction.getSubtitle(),
                        baseAction.getContentDescription());
            }
        }

        updateIconTint();

        if (headerAction != null
                && (headerAction.getIntent() != null || headerAction.getPendingIntent() != null)) {
            if (target.getFeatureType() == 1 && loggingInfo.mFeatureType == 39) {
                getClickedIndex(loggingInfo, 1);
            }
            setPrimaryOnClickListener(target, headerAction, loggingInfo);
        } else if (baseAction != null
                && (baseAction.getIntent() != null || baseAction.getPendingIntent() != null)) {
            setPrimaryOnClickListener(target, baseAction, loggingInfo);
        } else {
            setPrimaryOnClickListener(target, headerAction, loggingInfo);
        }

        if (mSecondaryCardGroup == null) {
            return;
        }

        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) mSecondaryCardGroup.getLayoutParams();
        params.matchConstraintMaxWidth =
                BcSmartSpaceUtil.getFeatureType(target) == -2 ? getWidth() * 3 / 4 : getWidth() / 2;
        mSecondaryCardGroup.setLayoutParams(params);
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
                        .setFeatureType(mTarget != null ? mTarget.getFeatureType() : 0)
                        .setUid(-1);
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
        mSecondaryCardGroup = findViewById(R.id.secondary_card_group);
        mTitleTextView = findViewById(R.id.title_text);
        mSubtitleTextView = findViewById(R.id.subtitle_text);
        mBaseActionIconSubtitleView = findViewById(R.id.base_action_icon_subtitle);
        if (mBaseActionIconSubtitleView != null) {
            mBaseActionIconSubtitleHitRect = new Rect();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed && !mTouchDelegateIsDirty) {
            return;
        }
        mTouchDelegateIsDirty = false;
        setTouchDelegate(null);
        if (mBaseActionIconSubtitleView == null
                || mBaseActionIconSubtitleView.getVisibility() != View.VISIBLE) {
            return;
        }

        int padding =
                getResources().getDimensionPixelSize(R.dimen.subtitle_hit_rect_height)
                        - mBaseActionIconSubtitleView.getHeight();
        padding = padding / 2;

        mBaseActionIconSubtitleView.getHitRect(mBaseActionIconSubtitleHitRect);
        View parent = (View) mBaseActionIconSubtitleView.getParent();
        offsetDescendantRectToMyCoords(parent, mBaseActionIconSubtitleHitRect);

        if (padding <= 0 && mBaseActionIconSubtitleHitRect.bottom == getHeight()) {
            return;
        }

        if (padding > 0) {
            mBaseActionIconSubtitleHitRect.top -= padding;
        }
        mBaseActionIconSubtitleHitRect.bottom = getHeight();
        setTouchDelegate(
                new TouchDelegate(mBaseActionIconSubtitleHitRect, mBaseActionIconSubtitleView));
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
        BcSmartspaceTemplateDataUtils.updateVisibility(
                mSecondaryCardGroup, secondaryCardVisibility);

        if (mTarget != null && mTarget.getFeatureType() == 30) {
            return;
        }

        if (mSecondaryCardGroup != null
                && mSecondaryCardGroup.getVisibility() == View.VISIBLE
                && mTextGroup != null) {
            int direction = isRtl() ? 1 : -1;
            float translation = mSecondaryCardGroup.getWidth() * direction;
            Interpolator interpolator = com.android.app.animation.Interpolators.EMPHASIZED;
            mTextGroup.setTranslationX(
                    translation * ((PathInterpolator) interpolator).getInterpolation(mDozeAmount));
            float alpha = Math.min(1f, Math.max(0f, (1f - mDozeAmount) * 9f - 6f));
            mSecondaryCardGroup.setAlpha(alpha);
        } else if (mTextGroup != null) {
            mTextGroup.setTranslationX(0f);
        }
    }

    public void setPrimaryOnClickListener(
            SmartspaceTarget target,
            SmartspaceAction action,
            BcSmartspaceCardLoggingInfo loggingInfo) {
        if (action != null) {
            BcSmartSpaceUtil.setOnClickListener(
                    this, target, action, mEventNotifier, "BcSmartspaceCard", loggingInfo, 0);
            if (mTitleTextView != null) {
                BcSmartSpaceUtil.setOnClickListener(
                        mTitleTextView,
                        target,
                        action,
                        mEventNotifier,
                        "BcSmartspaceCard",
                        loggingInfo,
                        0);
            }
            if (mSubtitleTextView != null) {
                BcSmartSpaceUtil.setOnClickListener(
                        mSubtitleTextView,
                        target,
                        action,
                        mEventNotifier,
                        "BcSmartspaceCard",
                        loggingInfo,
                        0);
            }
        }
    }

    public void setPrimaryTextColor(int color) {
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(color);
        }
        if (mSubtitleTextView != null) {
            mSubtitleTextView.setTextColor(color);
        }
        if (mBaseActionIconSubtitleView != null) {
            mBaseActionIconSubtitleView.setTextColor(color);
        }
        if (mSecondaryCard != null) {
            mSecondaryCard.setTextColor(color);
        }
        mIconTintColor = color;
        updateIconTint();
    }

    public void setScreenOn(boolean screenOn) {
        // No-op
    }

    public void setSecondaryCard(BcSmartspaceCardSecondary secondaryCard) {
        if (mSecondaryCardGroup == null) {
            return;
        }
        mSecondaryCard = secondaryCard;
        BcSmartspaceTemplateDataUtils.updateVisibility(mSecondaryCardGroup, View.GONE);
        mSecondaryCardGroup.removeAllViews();
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
            mSecondaryCardGroup.addView(secondaryCard, params);
        }
    }

    public void setSubtitle(CharSequence text, CharSequence contentDescription, boolean useIcon) {
        if (mSubtitleTextView == null) {
            Log.w("BcSmartspaceCard", "No subtitle view to update");
            return;
        }
        mSubtitleTextView.setText(text);
        mSubtitleTextView.setCompoundDrawablesRelative(
                useIcon ? mIconDrawable : null, null, null, null);
        mSubtitleTextView.setMaxLines(
                (mTarget != null && mTarget.getFeatureType() == 5 && !mUsePageIndicatorUi) ? 2 : 1);
        ContentDescriptionUtil.setFormattedContentDescription(
                "BcSmartspaceCard", mSubtitleTextView, text, contentDescription);
        BcSmartspaceTemplateDataUtils.offsetTextViewForIcon(
                mSubtitleTextView, useIcon ? mIconDrawable : null, isRtl());
    }

    public void setTitle(CharSequence text, CharSequence contentDescription, boolean useIcon) {
        if (mTitleTextView == null) {
            Log.w("BcSmartspaceCard", "No title view to update");
            return;
        }
        mTitleTextView.setText(text);
        Bundle extras =
                (mTarget != null && mTarget.getHeaderAction() != null)
                        ? mTarget.getHeaderAction().getExtras()
                        : null;
        if (extras != null && extras.containsKey("titleEllipsize")) {
            try {
                mTitleTextView.setEllipsize(
                        TextUtils.TruncateAt.valueOf(extras.getString("titleEllipsize")));
            } catch (IllegalArgumentException e) {
                Log.w(
                        "BcSmartspaceCard",
                        "Invalid TruncateAt value: " + extras.getString("titleEllipsize"));
            }
        } else if (mTarget != null
                && mTarget.getFeatureType() == 2
                && Locale.ENGLISH
                        .getLanguage()
                        .equals(getResources().getConfiguration().locale.getLanguage())) {
            mTitleTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        } else {
            mTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (extras != null && extras.getInt("titleMaxLines") != 0) {
            mTitleTextView.setMaxLines(extras.getInt("titleMaxLines"));
        }
        boolean showIcon = useIcon && !(extras != null && extras.getBoolean("disableTitleIcon"));
        if (showIcon) {
            ContentDescriptionUtil.setFormattedContentDescription(
                    "BcSmartspaceCard", mTitleTextView, text, contentDescription);
        }
        mTitleTextView.setCompoundDrawablesRelative(
                showIcon ? mIconDrawable : null, null, null, null);
        BcSmartspaceTemplateDataUtils.offsetTextViewForIcon(
                mTitleTextView, showIcon ? mIconDrawable : null, isRtl());
    }

    public void updateIconTint() {
        if (mTarget == null || mIconDrawable == null) {
            return;
        }
        mIconDrawable.setTint(mTarget.getFeatureType() != 1 ? mIconTintColor : 0);
        if (mBaseActionIconDrawable != null) {
            SmartspaceAction baseAction = mTarget.getBaseAction();
            int subcardType =
                    (baseAction != null
                                    && baseAction.getExtras() != null
                                    && !baseAction.getExtras().isEmpty())
                            ? baseAction.getExtras().getInt("subcardType", -1)
                            : -1;
            mBaseActionIconDrawable.setTint(subcardType != 1 ? mIconTintColor : 0);
        }
    }
}
