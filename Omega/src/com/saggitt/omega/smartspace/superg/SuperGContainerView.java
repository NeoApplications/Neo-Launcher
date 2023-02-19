/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.smartspace.superg;

import static com.saggitt.omega.util.ContextExtensionsKt.getColorAttr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.saggitt.omega.util.OmegaUtilsKt;

public class SuperGContainerView extends BaseGContainerView {

    public SuperGContainerView(Context paramContext) {
        this(paramContext, null);
    }

    public SuperGContainerView(Context paramContext, AttributeSet paramAttributeSet) {
        this(paramContext, paramAttributeSet, 0);
    }

    public SuperGContainerView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        View.inflate(paramContext, R.layout.qsb_blocker_view, this);
    }

    @Override
    protected int getQsbView(boolean withMic) {
        return R.layout.qsb_without_mic;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int qsbOverlapMargin = -getResources().getDimensionPixelSize(R.dimen.qsb_overlap_margin);
        DeviceProfile deviceProfile = LauncherAppState.getIDP(getContext()).getDeviceProfile(getContext());
        int size = MeasureSpec.getSize(widthMeasureSpec) - qsbOverlapMargin;

        int qsbWidth;
        int marginStart;
        if (deviceProfile.isVerticalBarLayout()) {
            qsbWidth = size;
            marginStart = qsbOverlapMargin + getResources().getDimensionPixelSize(R.dimen.qsb_button_elevation);
        } else {
            Rect workspacePadding = deviceProfile.workspacePadding;
            int fullWidth = size - workspacePadding.left - workspacePadding.right;
            qsbWidth = DeviceProfile.calculateCellWidth(fullWidth, 0, deviceProfile.inv.numColumns) * deviceProfile.inv.numColumns;
            marginStart = 0;
        }

        if (mQsbView != null) {
            LayoutParams layoutParams = (LayoutParams) mQsbView.getLayoutParams();
            layoutParams.width = qsbWidth / deviceProfile.inv.numColumns;
            if (deviceProfile.isVerticalBarLayout()) {
                layoutParams.width = Math.max(layoutParams.width,
                        getResources().getDimensionPixelSize(R.dimen.qsb_min_width_with_mic));
            } else {
                layoutParams.width = Math.max(layoutParams.width,
                        getResources().getDimensionPixelSize(R.dimen.qsb_min_width_portrait));
            }
            layoutParams.setMarginStart(marginStart);
            layoutParams.resolveLayoutDirection(layoutParams.getLayoutDirection());
        }
        if (mConnectorView != null) {
            LayoutParams layoutParams = (LayoutParams) mConnectorView.getLayoutParams();
            layoutParams.width = marginStart + layoutParams.height / 2;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void setGoogleAnimationStart(Rect rect, Intent intent) {

    }

    @Override
    protected void applyQsbColor() {
        super.applyQsbColor();
        float radius = OmegaUtilsKt.dpToPx(100);
        mQsbView.setBackground(OmegaUtilsKt.createRipplePill(getContext(), getColorAttr(getContext(), R.attr.popupColorPrimary), radius));
    }

    @Override
    public void onClick(View v) {

    }
}