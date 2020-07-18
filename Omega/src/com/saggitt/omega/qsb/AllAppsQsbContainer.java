/*
 * Copyright (C) 2019 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saggitt.omega.qsb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsStore.OnUpdateListener;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.graphics.NinePatchDrawHelper;
import com.android.launcher3.icons.ShadowGenerator.Builder;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.uioverrides.WallpaperColorInfo.OnChangeListener;
import com.android.launcher3.util.Themes;
import com.android.launcher3.util.TransformingTouchDelegate;
import com.android.quickstep.WindowTransformSwipeHandler;
import com.saggitt.omega.OmegaLauncher;
import com.saggitt.omega.OmegaLauncherCallbacks;
import com.saggitt.omega.qsb.configs.ConfigurationBuilder;
import com.saggitt.omega.search.SearchHandler;
import com.saggitt.omega.util.Config;

import static com.android.launcher3.LauncherState.ALL_APPS;
import static com.android.launcher3.LauncherState.ALL_APPS_HEADER;
import static com.android.launcher3.LauncherState.HOTSEAT_SEARCH_BOX;

public class AllAppsQsbContainer extends FrameLayout implements Insettable, OnClickListener, OnChangeListener, OnUpdateListener, SearchUiManager {

    private static final long SEARCH_TASK_DELAY_MS = 450;
    private static final Rect mSrcRect = new Rect();
    private final Paint mShadowPaint = new Paint(1);
    private final NinePatchDrawHelper mShadowHelper = new NinePatchDrawHelper();
    public Bitmap mShadowBitmap;
    public OmegaLauncher mLauncher;
    public View mSearchIcon;
    protected int mResult;
    private AllAppsContainerView mAppsView;
    private Bitmap mQsbScroll;
    private Context mContext;
    private DefaultQsbContainer mDefaultQsb;
    private TransformingTouchDelegate mDelegate;
    private boolean mKeepDefaultQsb;
    private boolean mIsMainColorDark;
    private boolean mIsRtl;
    private boolean mUseDefaultQsb;
    private float mFixedTranslationY;
    private float mSearchIconStrokeWidth;
    private int mAlpha;
    private int mColor;
    private int mMarginAdjusting;
    private int mSearchIconWidth;
    private int mShadowMargin;
    private Paint mSearchIconStrokePaint = new Paint(1);

    public AllAppsQsbContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsQsbContainer(Context context, AttributeSet attributeSet, int res) {
        super(context, attributeSet, res);
        mContext = context;
        mResult = 0;
        mAlpha = 0;
        mLauncher = (OmegaLauncher) Launcher.getLauncher(context);
        setOnClickListener(this);
        mIsMainColorDark = Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark);
        mMarginAdjusting = mContext.getResources().getDimensionPixelSize(R.dimen.qsb_margin_top_adjusting);
        mSearchIconWidth = getResources().getDimensionPixelSize(R.dimen.qsb_mic_width);
        mShadowMargin = getResources().getDimensionPixelSize(R.dimen.qsb_shadow_margin);
        mIsRtl = Utilities.isRtl(getResources());
        mDelegate = new TransformingTouchDelegate(this);
        mShadowPaint.setColor(-1);
        mFixedTranslationY = Math.round(getTranslationY());
        setClipToPadding(false);
        setTranslationY(0);
    }

    @Override
    public void setInsets(Rect insets) {
        updateQsbType();
        ((MarginLayoutParams) getLayoutParams()).topMargin = (int) Math.max(-mFixedTranslationY, insets.top - mMarginAdjusting);
        requestLayout();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mSearchIcon = (View) findViewById(R.id.mic_icon);
        setTouchDelegate(mDelegate);
        requestLayout();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDelegate.setDelegateView(mSearchIcon);
        mLauncher.getAppsView().getAppsStore().addUpdateListener(this);
        WallpaperColorInfo instance = WallpaperColorInfo.getInstance(getContext());
        instance.addOnChangeListener(this);
        onExtractedColorsChanged(instance);
        updateConfiguration();
    }

    @Override
    public void onDetachedFromWindow() {
        mLauncher.getAppsView().getAppsStore().removeUpdateListener(this);
        WallpaperColorInfo.getInstance(getContext()).removeOnChangeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        setColor(ColorUtils.compositeColors(ColorUtils.compositeColors(
                Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark)
                        ? -650362813 : -855638017, Themes.getAttrColor(mLauncher, R.attr.allAppsScrimColor)), wallpaperColorInfo.getMainColor()));
    }

    @Override
    public void initialize(AllAppsContainerView allAppsContainerView) {
        mAppsView = allAppsContainerView;
        mAppsView.addElevationController(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                updateAlpha(((BaseRecyclerView) recyclerView).getCurrentScrollY());
            }
        });
        mAppsView.setRecyclerViewVerticalFadingEdgeEnabled(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            View gIcon = findViewById(R.id.g_icon);
            int result = 0;
            int newResult = 1;
            if (mIsRtl) {
                if (Float.compare(motionEvent.getX(), (float) (gIcon.getLeft())) >= 0) {
                    result = 1;
                }
            } else {
                if (Float.compare(motionEvent.getX(), (float) (gIcon.getRight())) <= 0) {
                    result = 1;
                }
            }
            if (result == 0) {
                newResult = 2;
            }
            mResult = newResult;
        }
        return super.onTouchEvent(motionEvent);
    }

    private void setColor(int color) {
        if (mColor != color) {
            mColor = color;
            mShadowBitmap = null;
            invalidate();
        }
    }

    private void addOrUpdateSearchPaint(float value) {
        mSearchIconStrokeWidth = TypedValue.applyDimension(1, value, getResources().getDisplayMetrics());
        mSearchIconStrokePaint.setStrokeWidth(mSearchIconStrokeWidth);
        mSearchIconStrokePaint.setStyle(Style.STROKE);
        mSearchIconStrokePaint.setColor(-4341306);
    }

    private void updateConfiguration() {
        addOrUpdateSearchPaint(0.0f);
        addOrUpdateSearchRipple();
    }

    @Override
    public void onClick(View view) {
        if (mLauncher.isInState(ALL_APPS)) {
            startSearch("", mResult);
        } else {
            mLauncher.getStateManager().goToState(ALL_APPS);
            new Handler().postDelayed(() -> startSearch("", mResult), SEARCH_TASK_DELAY_MS);
        }
    }

    public void startSearch(String initialQuery, int result) {
        ConfigurationBuilder config = new ConfigurationBuilder(this, true);
        if (mLauncher.getLauncherCallbacks().getClient().startSearch(config.build(), config.getExtras())) {
            mLauncher.getLauncherCallbacks().getQsbController().playQsbAnimation();
        } else {
            warmUpDefaultQsb(initialQuery);
        }
        mResult = 0;
    }

    private void warmUpDefaultQsb(String query) {
        if (mDefaultQsb == null) {
            initDefaultQsb();
        }
        mDefaultQsb.setText(query);
        mDefaultQsb.initKeyboard(mContext);
    }

    public void resetSearch() {
        updateAlpha(0);
        if (mUseDefaultQsb) {
            resetDefaultQsb();
        } else if (!mKeepDefaultQsb) {
            removeDefaultQsb();
        }
    }

    private void initDefaultQsb() {
        setOnClickListener(null);
        mDefaultQsb = (DefaultQsbContainer) mLauncher.getLayoutInflater().inflate(R.layout.search_container_all_apps, this, false);
        mDefaultQsb.mAllAppsQsb = this;
        mDefaultQsb.mApps = mAppsView.getApps();
        mDefaultQsb.mAppsView = mAppsView;
        SearchHandler handler = new SearchHandler(mDefaultQsb.getContext());
        mDefaultQsb.mController.initialize(handler, mDefaultQsb, Launcher.getLauncher(mDefaultQsb.getContext()), mDefaultQsb);
        addView(mDefaultQsb);
    }

    private void removeDefaultQsb() {
        if (mDefaultQsb != null) {
            mDefaultQsb.clearSearchResult();
            setOnClickListener(this);
            removeView(mDefaultQsb);
            mDefaultQsb = null;
        }
    }

    private void resetDefaultQsb() {
        if (mDefaultQsb != null) {
            mDefaultQsb.reset();
            mDefaultQsb.clearSearchResult();
        }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mSearchIcon.getHitRect(mSrcRect);
        if (mIsRtl) {
            mSrcRect.left -= mShadowMargin;
        } else {
            mSrcRect.right += mShadowMargin;
        }
        mDelegate.setBounds(mSrcRect.left, mSrcRect.top, mSrcRect.right, mSrcRect.bottom);
        View view = (View) getParent();
        setTranslationX((float) ((view.getPaddingLeft() + ((((view.getWidth() - view.getPaddingLeft()) - view.getPaddingRight()) - (right - left)) / 2)) - left));
        offsetTopAndBottom((int) mFixedTranslationY);
    }

    public int getMeasuredWidth(int width, DeviceProfile dp) {
        int leftRightPadding = dp.desiredWorkspaceLeftRightMarginPx
                + dp.cellLayoutPaddingLeftRightPx;
        int rowWidth = width - leftRightPadding * 2;
        return rowWidth;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DeviceProfile dp = mLauncher.getDeviceProfile();
        int round = Math.round(((float) dp.iconSizePx) * 0.92f);
        setMeasuredDimension(calculateMeasuredDimension(dp, round, widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            measureChildWithMargins(childAt, widthMeasureSpec, 0, heightMeasureSpec, 0);
            if (childAt.getWidth() <= round) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                int measuredWidth = (round - childAt.getWidth()) / 2;
                layoutParams.rightMargin = measuredWidth;
                layoutParams.leftMargin = measuredWidth;
            }
        }
    }

    public int calculateMeasuredDimension(DeviceProfile dp, int round, int widthMeasureSpec) {
        int width = getMeasuredWidth(MeasureSpec.getSize(widthMeasureSpec), dp);
        int calculateCellWidth = width - ((width / dp.inv.numHotseatIcons) - round);
        return getPaddingRight() + getPaddingLeft() + calculateCellWidth;
    }

    private void loadBitmap() {
        if (mShadowBitmap == null) {
            mShadowBitmap = getShadowBitmap(mColor);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mAlpha > 0) {
            if (mQsbScroll == null) {
                mQsbScroll = createBitmap(mContext.getResources().getDimension(
                        R.dimen.hotseat_qsb_scroll_shadow_blur_radius), mContext.getResources().getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset), 0);
            }
            mShadowHelper.paint.setAlpha(mAlpha);
            drawShadow(mQsbScroll, canvas);
            mShadowHelper.paint.setAlpha(255);
        }
        drawCanvas(canvas, getWidth());
        super.draw(canvas);
    }

    public void drawCanvas(Canvas canvas, int width) {
        Canvas qsb = canvas;
        loadBitmap();
        drawShadow(mShadowBitmap, qsb);
        if (mSearchIconStrokeWidth > WindowTransformSwipeHandler.SWIPE_DURATION_MULTIPLIER && mSearchIcon.getVisibility() == 0) {
            int paddingLeft = mIsRtl ? getPaddingLeft() : (width - getPaddingRight()) - getMicWidth();
            int paddingTop = getPaddingTop();
            int paddingRight = mIsRtl ? getPaddingLeft() + getMicWidth() : width - getPaddingRight();
            int paddingBottom = LauncherAppState.getIDP(getContext()).iconBitmapSize - getPaddingBottom();
            float height = ((float) (paddingBottom - paddingTop)) * 0.5f;
            int micStrokeWidth = (int) (mSearchIconStrokeWidth / 2.0f);
            if (mSearchIconStrokePaint == null) {
                mSearchIconStrokePaint = new Paint(1);
            }
            mSearchIconStrokePaint.setColor(-4341306);
            qsb.drawRoundRect((float) (paddingLeft + micStrokeWidth), (float) (paddingTop + micStrokeWidth), (float) (paddingRight - micStrokeWidth), (float) ((paddingBottom - micStrokeWidth) + 1), height, height, mSearchIconStrokePaint);
        }
    }

    private void drawShadow(Bitmap bitmap, Canvas canvas) {
        int shadowDimens = getShadowDimens(bitmap);
        int paddingTop = getPaddingTop() - ((bitmap.getHeight() - getHeightWithoutPadding()) / 2);
        int paddingLeft = getPaddingLeft() - shadowDimens;
        int width = (getWidth() - getPaddingRight()) + shadowDimens;
        if (mIsRtl) {
            paddingLeft += getRtlDimens();
        } else {
            width -= getRtlDimens();
        }
        mShadowHelper.draw(bitmap, canvas, (float) paddingLeft, (float) paddingTop, (float) width);
    }

    private Bitmap getShadowBitmap(int color) {
        int iconBitmapSize = LauncherAppState.getIDP(getContext()).iconBitmapSize;
        return createBitmap(((float) iconBitmapSize) / 96f, ((float) iconBitmapSize) / 48f, color);
    }

    private Bitmap createBitmap(float shadowBlur, float keyShadowDistance, int color) {
        int height = getHeightWithoutPadding();
        int heightSpec = height + 20;
        Builder builder = new Builder(color);
        builder.shadowBlur = shadowBlur;
        builder.keyShadowDistance = keyShadowDistance;
        builder.keyShadowAlpha = builder.ambientShadowAlpha;
        Bitmap pill = builder.createPill(heightSpec, height);
        if (Color.alpha(color) < 255) {
            Canvas canvas = new Canvas(pill);
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            canvas.drawRoundRect(builder.bounds, (float) (height / 2), (float) (height / 2), paint);
            paint.setXfermode(null);
            paint.setColor(color);
            canvas.drawRoundRect(builder.bounds, (float) (height / 2), (float) (height / 2), paint);
            canvas.setBitmap(null);
        }
        if (Utilities.ATLEAST_OREO) {
            return pill.copy(Bitmap.Config.HARDWARE, false);
        }
        return pill;
    }

    @Override
    public float getScrollRangeDelta(Rect insets) {
        if (mLauncher.getDeviceProfile().isVerticalBarLayout()) {
            return WindowTransformSwipeHandler.SWIPE_DURATION_MULTIPLIER;
        }
        DeviceProfile dp = mLauncher.getWallpaperDeviceProfile();
        int height = (dp.hotseatBarSizePx - dp.hotseatCellHeightPx) - getLayoutParams().height;
        int marginBottom = insets.bottom;
        return (float) (getLayoutParams().height + Math.max(-mFixedTranslationY, insets.top - mMarginAdjusting) + mFixedTranslationY + marginBottom + ((int) (((float) (height - marginBottom)) * 0.45f)));
    }

    private void addOrUpdateSearchRipple() {
        InsetDrawable insetDrawable = (InsetDrawable) getResources().getDrawable(R.drawable.qsb_icon_feedback_bg).mutate();
        RippleDrawable oldRipple = (RippleDrawable) insetDrawable.getDrawable();
        int width = mIsRtl ? getRtlDimens() : 0;
        int height = mIsRtl ? 0 : getRtlDimens();

        oldRipple.setLayerInset(0, width, 0, height, 0);
        setBackground(insetDrawable);
        RippleDrawable newRipple = (RippleDrawable) oldRipple.getConstantState().newDrawable().mutate();
        newRipple.setLayerInset(0, 0, mShadowMargin, 0, mShadowMargin);
        mSearchIcon.setBackground(newRipple);
        mSearchIcon.getLayoutParams().width = getMicWidth();

        int micWidth = mIsRtl ? 0 : getMicWidth() - mSearchIconWidth;
        int micHeight = mIsRtl ? getMicWidth() - mSearchIconWidth : 0;

        mSearchIcon.setPadding(micWidth, 0, micHeight, 0);
        mSearchIcon.requestLayout();
    }

    public void updateAlpha(int alpha) {
        alpha = Utilities.boundToRange(alpha, 0, 255);
        if (mAlpha != alpha) {
            mAlpha = alpha;
            invalidate();
        }
    }

    protected void updateQsbType() {
        boolean useDefaultQsb = !Config.hasPackageInstalled(Launcher.getLauncher(mContext), OmegaLauncherCallbacks.SEARCH_PACKAGE);
        if (useDefaultQsb != mUseDefaultQsb) {
            removeDefaultQsb();
            mUseDefaultQsb = useDefaultQsb;
            ((ImageView) findViewById(R.id.g_icon)).setImageResource(mUseDefaultQsb ? R.drawable.ic_allapps_search : R.drawable.ic_qsb_logo);
            if (mSearchIcon != null) {
                mSearchIcon.setAlpha(mUseDefaultQsb ? 0.0f : 1.0f);
            }
            if (mUseDefaultQsb) {
                initDefaultQsb();
            }
            requestLayout();
        }
    }

    public int getShadowDimens(Bitmap bitmap) {
        return (bitmap.getWidth() - (getHeightWithoutPadding() + 20)) / 2;
    }

    public int getHeightWithoutPadding() {
        return (getHeight() - getPaddingTop()) - getPaddingBottom();
    }

    public int getRtlDimens() {
        return 0;
    }

    public int getMicWidth() {
        return mSearchIconWidth;
    }

    @Override
    public void setContentVisibility(int visibleElements, PropertySetter setter, Interpolator interpolator) {
        boolean hasSearchBoxContent = (visibleElements & HOTSEAT_SEARCH_BOX) != 0 && (visibleElements & ALL_APPS_HEADER) != 0;
        float alpha = hasSearchBoxContent ? 1.0f : 0.0f;
        setter.setViewAlpha(this, 1, interpolator);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void preDispatchKeyEvent(KeyEvent keyEvent) {
    }

    @Override
    public void onAppsUpdated() {
        updateQsbType();
    }

    public void setKeepDefaultView(boolean canKeep) {
        mKeepDefaultQsb = canKeep;
    }
}
