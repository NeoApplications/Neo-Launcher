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
package com.saggitt.omega.qsb.configs;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager.LayoutParams;
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsRecyclerView;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.icons.BitmapRenderer;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.google.protobuf.nano.MessageNano;
import com.saggitt.omega.OmegaLauncher;
import com.saggitt.omega.OmegaLauncherCallbacks;
import com.saggitt.omega.qsb.AllAppsQsbContainer;
import com.saggitt.omega.search.AppSearchProvider;

import java.util.ArrayList;

import static com.saggitt.omega.search.nano.SearchProto.AppIndex;
import static com.saggitt.omega.search.nano.SearchProto.Columns;
import static com.saggitt.omega.search.nano.SearchProto.SearchBase;
import static com.saggitt.omega.search.nano.SearchProto.SearchView;

public class ConfigurationBuilder {

    public final Bundle mBundle = new Bundle();
    public final SearchBase mNano = new SearchBase();
    public boolean mHasAllAppsDivider;
    public OmegaLauncher mLauncher;
    public BubbleTextView mBubbleTextView;
    public boolean mIsAllApps;
    public AllAppsQsbContainer mAllAppsQsbContainer;
    public UserManagerCompat mUserManager;

    public ConfigurationBuilder(AllAppsQsbContainer qsbContainer, boolean isAllApps) {
        mAllAppsQsbContainer = qsbContainer;
        mLauncher = mAllAppsQsbContainer.mLauncher;
        mIsAllApps = isAllApps;
        mUserManager = UserManagerCompat.getInstance(mLauncher);
    }

    public static Intent getSearchIntent(Rect sourceBounds, View gIcon, View micIcon) {
        Intent intent = new Intent("com.google.nexuslauncher.FAST_TEXT_SEARCH");
        intent.setSourceBounds(sourceBounds);
        if (micIcon.getVisibility() != 0) {
            intent.putExtra("source_mic_alpha", 0.0f);
        }
        return intent.putExtra("source_round_left", true)
                .putExtra("source_round_right", true)
                .putExtra("source_logo_offset", getCenter(gIcon, sourceBounds))
                .putExtra("source_mic_offset", getCenter(micIcon, sourceBounds))
                .putExtra("use_fade_animation", true)
                .setPackage(OmegaLauncherCallbacks.SEARCH_PACKAGE).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static Point getCenter(View view, Rect rect) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        Point point = new Point();
        point.x = (location[0] - rect.left) + (view.getWidth() / 2);
        point.y = (location[1] - rect.top) + (view.getHeight() / 2);
        return point;
    }

    public static Columns getViewBounds(View view) {
        Columns columns = new Columns();
        columns.iconDistance = view.getWidth();
        columns.height = view.getHeight();
        int[] array = new int[2];
        view.getLocationInWindow(array);
        columns.edgeMargin = array[0];
        columns.innerMargin = array[1];
        return columns;
    }

    public void updateHotseatSearchDimens() {
        if (mNano.appsView != null) {
            return;
        }
        final Columns apps = mNano.apps;
        final Columns appsView = new Columns();
        appsView.edgeMargin = apps.edgeMargin;
        appsView.innerMargin = apps.innerMargin + apps.height;
        appsView.height = apps.height;
        appsView.iconDistance = apps.iconDistance;
        mNano.appsView = appsView;
    }

    public AllAppsRecyclerView getAppsView() {
        return (AllAppsRecyclerView) mLauncher.findViewById(R.id.apps_list_view);
    }

    public int getBackgroundColor() {
        return ColorUtils.compositeColors(Themes.getAttrColor(mLauncher, R.attr.allAppsScrimColor),
                ColorUtils.setAlphaComponent(WallpaperColorInfo.getInstance(mLauncher).getMainColor(), 255));
    }

    public final AppIndex mo8435bZ(AppInfo appInfo, int n) {
        final AppIndex apps = new AppIndex();
        apps.label = appInfo.title.toString();
        apps.iconBitmap = "icon_bitmap_" + n;
        mBundle.putParcelable(apps.iconBitmap, appInfo.iconBitmap);
        Uri uri = AppSearchProvider.buildUri(appInfo, mUserManager);
        apps.searchUri = uri.toString();
        apps.predictionRank = new Intent("com.google.android.apps.nexuslauncher.search.APP_LAUNCH",
                uri.buildUpon().appendQueryParameter("predictionRank", Integer.toString(n)).build())
                .toUri(0);
        return apps;
    }

    public final RemoteViews searchIconTemplate() {
        RemoteViews remoteViews = new RemoteViews(mLauncher.getPackageName(), R.layout.apps_search_icon_template);
        int iconSize = mBubbleTextView.getIconSize();
        int horizontalPadding = (mBubbleTextView.getWidth() - iconSize) / 2;
        int paddingTop = mBubbleTextView.getPaddingTop();
        int paddingBottom = (mBubbleTextView.getHeight() - iconSize) - paddingTop;
        remoteViews.setViewPadding(android.R.id.icon, horizontalPadding, paddingTop, horizontalPadding, paddingBottom);
        int minPadding = Math.min((int) (((float) iconSize) * 0.12f), Math.min(horizontalPadding, Math.min(paddingTop, paddingBottom)));
        remoteViews.setViewPadding(R.id.click_feedback_wrapper, horizontalPadding - minPadding, paddingTop - minPadding, horizontalPadding - minPadding, paddingBottom - minPadding);
        remoteViews.setTextViewTextSize(android.R.id.title, 0, mLauncher.getDeviceProfile().allAppsIconTextSizePx);
        remoteViews.setViewPadding(android.R.id.title, mBubbleTextView.getPaddingLeft(), mBubbleTextView.getCompoundDrawablePadding() + mBubbleTextView.getIconSize(), mBubbleTextView.getPaddingRight(), 0);
        return remoteViews;
    }

    public final RemoteViews searchQsbTemplate() {
        int width;
        RemoteViews remoteViews = new RemoteViews(mLauncher.getPackageName(), R.layout.apps_search_qsb_template);
        int effectiveHeight = ((mAllAppsQsbContainer.getHeight() - mAllAppsQsbContainer.getPaddingTop()) - mAllAppsQsbContainer.getPaddingBottom()) + 20;
        Bitmap mShadowBitmap = mAllAppsQsbContainer.mShadowBitmap;
        if (mShadowBitmap != null) {
            int internalWidth = (mShadowBitmap.getWidth() - effectiveHeight) / 2;
            int verticalPadding = (mAllAppsQsbContainer.getHeight() - mShadowBitmap.getHeight()) / 2;
            remoteViews.setViewPadding(R.id.qsb_background_container, mAllAppsQsbContainer.getPaddingLeft() - internalWidth, verticalPadding, mAllAppsQsbContainer.getPaddingRight() - internalWidth, verticalPadding);
            Bitmap bitmap = Bitmap.createBitmap(mShadowBitmap, 0, 0, mShadowBitmap.getWidth() / 2, mShadowBitmap.getHeight());
            Bitmap bitmap2 = Bitmap.createBitmap(mShadowBitmap, (mShadowBitmap.getWidth() - 20) / 2, 0, 20, mShadowBitmap.getHeight());
            remoteViews.setImageViewBitmap(R.id.qsb_background_1, bitmap);
            remoteViews.setImageViewBitmap(R.id.qsb_background_2, bitmap2);
            remoteViews.setImageViewBitmap(R.id.qsb_background_3, bitmap);
        }
        if (mAllAppsQsbContainer.mSearchIcon.getVisibility() != View.VISIBLE) {
            remoteViews.setViewVisibility(R.id.mic_icon, View.INVISIBLE);
        }
        View gIcon = mAllAppsQsbContainer.findViewById(R.id.g_icon);
        if (mAllAppsQsbContainer.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            width = mAllAppsQsbContainer.getWidth() - gIcon.getRight();
        } else {
            width = gIcon.getLeft();
        }
        int horizontalPadding = width;
        remoteViews.setViewPadding(R.id.qsb_icon_container, horizontalPadding, 0, horizontalPadding, 0);
        return remoteViews;
    }

    public void createSearchTemplate() {
        mNano.searchTemplate = "search_box_template";
        mBundle.putParcelable(mNano.searchTemplate, searchQsbTemplate());
        mNano.gIcon = R.id.g_icon;
        mNano.micIcon = mAllAppsQsbContainer.mSearchIcon.getVisibility() == View.VISIBLE ? R.id.mic_icon : 0;
        Columns viewBounds = getViewBounds(mLauncher.getDragLayer());
        Columns appColumns = mNano.apps;
        int topShift = appColumns.innerMargin + (mHasAllAppsDivider ? 0 : appColumns.height);
        viewBounds.innerMargin += topShift;
        viewBounds.height -= topShift;
        mNano.viewBounds = viewBounds;
        int iconDistance = viewBounds.iconDistance;
        if (iconDistance > 0) {
            int height = viewBounds.height;
            if (height > 0) {
                mBundle.putParcelable(mNano.view, BitmapRenderer.createHardwareBitmap(iconDistance, height, new SearchBoxRenderer(this, topShift)));
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Invalid preview bitmap size. width: ");
        sb.append(viewBounds.iconDistance);
        sb.append("hight: ");
        sb.append(viewBounds.height);
        sb.append(" top shift: ");
        sb.append(topShift);
        Log.e("ConfigurationBuilder", sb.toString());
        viewBounds.height = 0;
        viewBounds.edgeMargin = 0;
        viewBounds.innerMargin = 0;
        viewBounds.iconDistance = 0;
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
        bitmap.setPixel(0, 0, 0);
        mBundle.putParcelable(mNano.view, bitmap);
    }

    public void updateView(int i, Canvas canvas) {
        int save = canvas.save();
        canvas.translate(0.0f, (float) (-i));
        updateMappings(canvas, mLauncher.getAppsView().getRecyclerViewContainer());
        updateMappings(canvas, (View) mLauncher.getAppsView().getFloatingHeaderView());
        canvas.restoreToCount(save);
    }

    public void updateMappings(Canvas canvas, View view) {
        int[] array = new int[]{0, 0};
        mLauncher.getDragLayer().mapCoordInSelfToDescendant(mLauncher.getAppsView(), array);
        mLauncher.getDragLayer().mapCoordInSelfToDescendant(view, array);
        canvas.translate((float) (-array[0]), (float) (-array[1]));
        view.draw(canvas);
        canvas.translate((float) array[0], (float) array[1]);
    }

    public void setAllAppsSearchView() {
        int appCols;
        View view = null;
        AllAppsRecyclerView appsView = getAppsView();
        SpanSizeLookup spanSizeLookup = ((GridLayoutManager) appsView.getLayoutManager()).getSpanSizeLookup();
        int allAppsCols = Math.min(mLauncher.getDeviceProfile().inv.numColumns, appsView.getChildCount());
        int childCount = appsView.getChildCount();
        BubbleTextView[] bubbleTextViewArr = new BubbleTextView[allAppsCols];
        int groupIndex = -1;
        for (appCols = 0; appCols < childCount; appCols++) {
            ViewHolder childViewHolder = appsView.getChildViewHolder(appsView.getChildAt(appCols));
            if (childViewHolder.itemView instanceof BubbleTextView) {
                int spanGroupIndex = spanSizeLookup.getSpanGroupIndex(childViewHolder.getLayoutPosition(), allAppsCols);
                if (spanGroupIndex >= 0) {
                    if (groupIndex >= 0 && spanGroupIndex != groupIndex) {
                        view = childViewHolder.itemView;
                        break;
                    } else {
                        groupIndex = spanGroupIndex;
                        bubbleTextViewArr[((LayoutParams) childViewHolder.itemView.getLayoutParams()).getSpanIndex()] = (BubbleTextView) childViewHolder.itemView;
                    }
                } else {
                    continue;
                }
            }
        }
        if (bubbleTextViewArr.length == 0 || bubbleTextViewArr[0] == null) {
            Log.e("ConfigBuilder", "No icons rendered in all apps");
            setHotseatSearchView();
            return;
        } else {
            mBubbleTextView = bubbleTextViewArr[0];
            mNano.allAppsCols = allAppsCols;
            appCols = 0;
            for (int allAppCols = 0; allAppCols < bubbleTextViewArr.length; allAppCols++) {
                if (bubbleTextViewArr[allAppCols] == null) {
                    appCols = allAppsCols - allAppCols;
                    allAppsCols = allAppCols;
                    break;
                }
            }
            mHasAllAppsDivider = appsView.getChildViewHolder(bubbleTextViewArr[0]).getItemViewType() == 4;
            Columns lastColumn = getViewBounds(bubbleTextViewArr[allAppsCols - 1]);
            Columns firstColumn = getViewBounds(bubbleTextViewArr[0]);
            if (Utilities.isRtl(mLauncher.getResources())) {
                Columns temp = lastColumn;
                lastColumn = firstColumn;
                firstColumn = temp;
            }
            int i3 = lastColumn.iconDistance;
            int totalIconDistance = lastColumn.edgeMargin - firstColumn.edgeMargin;
            int iconDistance = totalIconDistance / allAppsCols;
            firstColumn.iconDistance = i3 + totalIconDistance;
            if (Utilities.isRtl(mLauncher.getResources())) {
                firstColumn.edgeMargin -= appCols * i3;
                firstColumn.iconDistance += appCols * i3;
            } else {
                firstColumn.iconDistance += (iconDistance + i3) * appCols;
            }
            mNano.apps = firstColumn;
            if (!mHasAllAppsDivider) {
                firstColumn.innerMargin -= firstColumn.height;
            } else if (view != null) {
                Columns appView = getViewBounds(view);
                appView.iconDistance = firstColumn.iconDistance;
                mNano.appsView = appView;
            }
            updateHotseatSearchDimens();
        }
    }

    public void setHotseatSearchView() {
        mNano.allAppsCols = mLauncher.getDeviceProfile().inv.numColumns;
        int width = mLauncher.getHotseat().getWidth();
        int dimensionPixelSize = mLauncher.getResources().getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        Columns appCol = new Columns();
        appCol.edgeMargin = dimensionPixelSize;
        appCol.iconDistance = (width - dimensionPixelSize) - dimensionPixelSize;
        appCol.height = mLauncher.getDeviceProfile().allAppsCellHeightPx;
        mNano.apps = appCol;
        updateHotseatSearchDimens();
        AlphabeticalAppsList apps = getAppsView().getApps();
        mBubbleTextView = (BubbleTextView) mLauncher.getLayoutInflater().inflate(R.layout.all_apps_icon, getAppsView(), false);
        ViewGroup.LayoutParams layoutParams = mBubbleTextView.getLayoutParams();
        layoutParams.height = appCol.height;
        layoutParams.width = appCol.iconDistance / mNano.allAppsCols;
        if (!apps.getApps().isEmpty()) {
            mBubbleTextView.applyFromApplicationInfo((AppInfo) apps.getApps().get(0));
        }
        mBubbleTextView.measure(MeasureSpec.makeMeasureSpec(layoutParams.width, View.MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY));
        mBubbleTextView.layout(0, 0, layoutParams.width, layoutParams.height);
        ArrayList<AppIndex> list = new ArrayList(mNano.allAppsCols);
        mNano.index = (AppIndex[]) list.toArray(new AppIndex[list.size()]);
    }

    public byte[] build() {
        mNano.bgColor = getBackgroundColor();
        mNano.isDark = Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark);
        if (mIsAllApps) {
            setAllAppsSearchView();
        } else {
            setHotseatSearchView();
        }
        mNano.iconViewTemplate = "icon_view_template";
        mBundle.putParcelable(mNano.iconViewTemplate, searchIconTemplate());
        mNano.iconLongClick = "icon_long_click";
        mBundle.putParcelable(mNano.iconLongClick, PendingIntent.getBroadcast(
                mLauncher, 2055, new Intent().setComponent(new ComponentName(mLauncher, LongClickReceiver.class)),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT));
        LongClickReceiver.getWeakReference(mLauncher);
        mNano.bounds = getViewBounds(mAllAppsQsbContainer);
        mNano.isAllApps = mIsAllApps;
        if (mIsAllApps) {
            createSearchTemplate();
        }
        SearchView searchView = new SearchView();
        searchView.base = mNano;
        return MessageNano.toByteArray(searchView);
    }

    public Bundle getExtras() {
        return mBundle;
    }

    public class SearchBoxRenderer implements BitmapRenderer {
        private ConfigurationBuilder mConfigBuilder;
        private int mTopShift;

        public SearchBoxRenderer(ConfigurationBuilder configBuilder, int topShift) {
            mConfigBuilder = configBuilder;
            mTopShift = topShift;
        }

        public void draw(Canvas canvas) {
            mConfigBuilder.updateView(mTopShift, canvas);
        }
    }
}
