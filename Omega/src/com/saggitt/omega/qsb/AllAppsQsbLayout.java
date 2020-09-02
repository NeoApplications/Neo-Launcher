/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.saggitt.omega.qsb;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.qsb.QsbContainerView;
import com.android.launcher3.qsb.QsbWidgetHostView;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.search.SearchProvider;
import com.saggitt.omega.search.SearchProviderController;
import com.saggitt.omega.search.SearchThread;
import com.saggitt.omega.search.providers.AppSearchSearchProvider;
import com.saggitt.omega.search.providers.GoogleSearchProvider;
import com.saggitt.omega.search.webproviders.WebSearchProvider;

import java.util.Objects;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.android.launcher3.LauncherState.ALL_APPS;
import static com.android.launcher3.LauncherState.ALL_APPS_CONTENT;
import static com.android.launcher3.icons.IconNormalizer.ICON_VISIBLE_AREA_FACTOR;

public class AllAppsQsbLayout extends AbstractQsbLayout implements SearchUiManager, QsbChangeListener {

    private final QsbConfiguration configuration;
    private final int marginTop;
    public float appsVerticalOffset;
    boolean mDoNotRemoveFallback;
    private boolean mLowPerformanceMode;
    private int mShadowAlpha;
    private Bitmap Dv;
    private boolean mUseFallbackSearch;
    private FallbackAppsSearchView mFallback;
    private TextView mHint;
    private AllAppsContainerView mAppsView;
    private OmegaPreferences prefs;

    // This value was used to position the QSB. We store it here for translationY animations.
    private final float mFixedTranslationY;
    private final float mMarginTopAdjusting;
    private final int[] currentPadding = new int[2];
    // Delegate views.
    private View mSearchWrapperView;

    public AllAppsQsbLayout(Context context) {
        this(context, null);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        mShadowAlpha = 0;
        setOnClickListener(this);
        configuration = QsbConfiguration.getInstance(context);
        marginTop = getResources().getDimensionPixelSize(R.dimen.qsb_margin_top_adjusting);
        appsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset);
        setClipToPadding(false);
        prefs = OmegaPreferences.Companion.getInstanceNoCreate();

        mLowPerformanceMode = prefs.getLowPerformanceMode();

        mFixedTranslationY = getTranslationY();
        mMarginTopAdjusting = mFixedTranslationY - getPaddingTop();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mHint = findViewById(R.id.qsb_hint);
        mSearchWrapperView = findViewById(R.id.search_wrapper_view);
        mSearchWrapperView.setVisibility(shouldHideDockSearch()
                ? View.GONE
                : View.VISIBLE);
    }

    private boolean shouldHideDockSearch() {
        return !Utilities.getOmegaPrefs(getContext()).getDockSearchBar();
    }

    public void setInsets(Rect insets) {
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.topMargin = Math.round(Math.max(-mFixedTranslationY, insets.top - mMarginTopAdjusting));
        requestLayout();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateConfiguration();
        configuration.addListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        if (key.equals("pref_allAppsGoogleSearch")) {
            loadPreferences(sharedPreferences);
        }
    }

    @Override
    protected Drawable getIcon(boolean colored) {
        if (prefs.getAllAppsGlobalSearch()) {
            return super.getIcon(colored);
        } else {
            return new AppSearchSearchProvider(getContext()).getIcon(colored);
        }
    }

    @Override
    protected boolean logoCanOpenFeed() {
        return super.logoCanOpenFeed() && prefs.getAllAppsGlobalSearch();
    }

    @Override
    protected Drawable getMicIcon(boolean colored) {
        if (prefs.getAllAppsGlobalSearch()) {
            mMicIconView.setVisibility(View.VISIBLE);
            return super.getMicIcon(colored);
        } else {
            mMicIconView.setVisibility(View.GONE);
            return null;
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        configuration.removeListener(this);
    }

    public final void initialize(AllAppsContainerView allAppsContainerView) {
        mAppsView = allAppsContainerView;
        mAppsView.addElevationController(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                setShadowAlpha(((BaseRecyclerView) recyclerView).getCurrentScrollY());
            }
        });
        mAppsView.setRecyclerViewVerticalFadingEdgeEnabled(!mLowPerformanceMode);
    }

    public final void onChange() {
        updateConfiguration();
        invalidate();
    }

    private void updateConfiguration() {
        az(mColor);
        addOrUpdateSearchPaint(configuration.micStrokeWidth());
        Dh = configuration.hintIsForAssistant();
        mUseTwoBubbles = useTwoBubbles();
        setHintText(configuration.hintTextValue(), mHint);
        addOrUpdateSearchRipple();
    }

    public void onClick(View view) {
        super.onClick(view);
        startSearch("", mResult);
    }

    public final void l(String str) {
        startSearch(str, 0);
    }

    @Override
    public void startSearch() {
        post(() -> startSearch("", mResult));
    }

    @Override
    public final void startSearch(String str, int i) {
        SearchProviderController controller = SearchProviderController.Companion.getInstance(mLauncher);
        SearchProvider provider = controller.getSearchProvider();

        if (Launcher.getLauncher(getContext()).getStateManager().getState() != ALL_APPS) {
            Launcher.getLauncher(getContext()).getStateManager().goToState(ALL_APPS);
        }

        if (shouldUseFallbackSearch(provider)) {
            searchFallback(str);
        } else if (controller.isGoogle()) {
            final ConfigBuilder f = new ConfigBuilder(this, true);
            if (!Objects.requireNonNull(mLauncher.getGoogleNow()).startSearch(f.build(), f.getExtras())) {
                searchFallback(str);
                if (mFallback != null) {
                    mFallback.setHint(null);
                }
            }
        } else {
            provider.startSearch(intent -> {
                mLauncher.startActivity(intent);
                return null;
            });
        }
    }

    private boolean shouldUseFallbackSearch() {
        SearchProviderController controller = SearchProviderController.Companion.getInstance(mLauncher);
        SearchProvider provider = controller.getSearchProvider();
        return shouldUseFallbackSearch(provider);
    }

    private boolean shouldUseFallbackSearch(SearchProvider provider) {
        return !Utilities
                .getOmegaPrefs(getContext()).getAllAppsGlobalSearch()
                || provider instanceof AppSearchSearchProvider
                || provider instanceof WebSearchProvider
                || (!Utilities.ATLEAST_NOUGAT && provider instanceof GoogleSearchProvider);
    }

    public void searchFallback(String query) {
        ensureFallbackView();
        mFallback.setText(query);
        mFallback.showKeyboard();
    }

    public final void resetSearch() {
        setShadowAlpha(0);
        if (mUseFallbackSearch) {
            resetFallbackView();
        } else if (!mDoNotRemoveFallback) {
            removeFallbackView();
        }
    }

    @Override
    public void preDispatchKeyEvent(KeyEvent event) {
    }

    private void ensureFallbackView() {
        if (mFallback == null) {
            setOnClickListener(null);
            mFallback = (FallbackAppsSearchView) this.mLauncher.getLayoutInflater()
                    .inflate(R.layout.all_apps_google_search_fallback, this, false);
            AllAppsContainerView allAppsContainerView = this.mAppsView;
            mFallback.DJ = this;
            mFallback.mApps = allAppsContainerView.getApps();
            mFallback.mAppsView = allAppsContainerView;
            mFallback.mSearchBarController.initialize(new SearchThread(mFallback.getContext()), mFallback,
                    Launcher.getLauncher(mFallback.getContext()), mFallback);
            addView(mFallback);
        }
    }

    private void removeFallbackView() {
        if (mFallback != null) {
            mFallback.clearSearchResult();
            setOnClickListener(this);
            removeView(mFallback);
            mFallback = null;
        }
    }

    private void resetFallbackView() {
        if (mFallback != null) {
            mFallback.reset();
            mFallback.clearSearchResult();
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Update the width to match the grid padding
        int myRequestedWidth = getSize(widthMeasureSpec);
        int myRequestedHeight = getSize(heightMeasureSpec);

        DeviceProfile dp = mLauncher.getDeviceProfile();
        int rowWidth = myRequestedWidth - mAppsView.getActiveRecyclerView().getPaddingLeft()
                - mAppsView.getActiveRecyclerView().getPaddingRight();

        int cellWidth = DeviceProfile.calculateCellWidth(rowWidth, dp.inv.numHotseatIcons);
        int iconVisibleSize = Math.round(ICON_VISIBLE_AREA_FACTOR * dp.iconSizePx);
        int iconPadding = cellWidth - iconVisibleSize;

        int myWidth = rowWidth - iconPadding + getPaddingLeft() + getPaddingRight();

        int widgetPad = getResources().getDimensionPixelSize(R.dimen.qsb_widget_padding);

        if (mFallback != null) {
            mFallback.measure(makeMeasureSpec(myWidth, EXACTLY),
                    makeMeasureSpec(myRequestedHeight - widgetPad, EXACTLY));
        }

        currentPadding[0] = 0;
        currentPadding[1] = 0;
        calcPaddingRecursive(mSearchWrapperView, 2);

        mSearchWrapperView.setPadding(
                mSearchWrapperView.getPaddingLeft() + widgetPad - currentPadding[0],
                mSearchWrapperView.getPaddingTop(),
                mSearchWrapperView.getPaddingRight() + widgetPad - currentPadding[1],
                mSearchWrapperView.getPaddingBottom());

        mSearchWrapperView.measure(makeMeasureSpec(myWidth + 2 * widgetPad, EXACTLY),
                makeMeasureSpec(myRequestedHeight, EXACTLY));

    }

    private void calcPaddingRecursive(View view, int lvl) {
        currentPadding[0] += view.getPaddingLeft();
        currentPadding[1] += view.getPaddingRight();
        if (view instanceof ViewGroup && lvl > 0) {
            ViewGroup group = (ViewGroup) view;
            if (group.getChildCount() == 1) {
                calcPaddingRecursive(group.getChildAt(0), lvl - 1);
            }
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        View parent = (View) getParent();
        int availableWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        int myWidth = right - left;
        int expectedLeft = parent.getPaddingLeft() + (availableWidth - myWidth) / 2;
        int shift = expectedLeft - left;
        setTranslationX(shift);
    }

    public void draw(Canvas canvas) {
        if (this.mShadowAlpha > 0) {
            if (this.Dv == null) {
                this.Dv = createBitmap(
                        getResources().getDimension(R.dimen.hotseat_qsb_scroll_shadow_blur_radius),
                        getResources().getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset),
                        0, true);
            }
            mShadowHelper.paint.setAlpha(this.mShadowAlpha);
            drawShadow(Dv, canvas);
            mShadowHelper.paint.setAlpha(255);
        }
        super.draw(canvas);
    }

    final void setShadowAlpha(int i) {
        i = Utilities.boundToRange(i, 0, 255);
        if (this.mShadowAlpha != i) {
            this.mShadowAlpha = i;
            invalidate();
        }
    }

    protected final boolean dK() {
        if (mFallback != null) {
            return false;
        }
        return super.dK();
    }

    protected final void c(SharedPreferences sharedPreferences) {
        if (mUseFallbackSearch) {
            removeFallbackView();
            mUseFallbackSearch = false;
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    //Used when search bar is disabled
    public int getTopMargin(Rect rect) {
        return Math.max(Math.round(-this.appsVerticalOffset), rect.top - this.marginTop);
    }

    @Override
    public float getScrollRangeDelta(Rect insets) {
        if (mLauncher.getDeviceProfile().isVerticalBarLayout() || shouldHideDockSearch()) {
            return 0;
        } else {
            int topMargin = Math.round(Math.max(
                    -mFixedTranslationY, insets.top - mMarginTopAdjusting));

            DeviceProfile dp = mLauncher.getWallpaperDeviceProfile();
            int searchPadding = getLayoutParams().height;
            int hotseatPadding = dp.hotseatBarBottomPaddingPx - searchPadding;

            return insets.bottom + topMargin + mFixedTranslationY + searchPadding
                    + hotseatPadding * 0.65f;
        }
    }

    @Override
    public void setContentVisibility(int visibleElements, PropertySetter setter, Interpolator interpolator) {
        boolean showAllApps = (visibleElements & ALL_APPS_CONTENT) != 0;
        if (showAllApps)
            setter.setViewAlpha(mSearchWrapperView, showAllApps ? 0f : (shouldHideDockSearch() ? 0f : 1f), Interpolators.LINEAR);
        else
            setter.setViewAlpha(mSearchWrapperView, !showAllApps ? 0f : (!shouldHideDockSearch() ? 1f : 0f), Interpolators.LINEAR);
    }

    @Nullable
    @Override
    protected String getClipboardText() {
        return shouldUseFallbackSearch() ? super.getClipboardText() : null;
    }

    @Override
    protected void clearMainPillBg(Canvas canvas) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            drawPill(mClearShadowHelper, mClearBitmap, canvas);
        }
    }

    @Override
    protected void clearPillBg(Canvas canvas, int left, int top, int right) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            mClearShadowHelper.draw(mClearBitmap, canvas, left, top, right);
        }
    }

    public static class HotseatQsbFragment extends QsbContainerView.QsbFragment {
        @Override
        public boolean isQsbEnabled() {
            return true;
        }

        @Override
        protected QsbContainerView.QsbWidgetHost createHost() {
            return new QsbContainerView.QsbWidgetHost(getContext(), QSB_WIDGET_HOST_ID,
                    (c) -> new QsbWidgetHostView(c));
        }
    }
}
