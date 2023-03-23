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

package com.saggitt.omega.smartspace.superg

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.util.AttributeSet
import com.android.launcher3.DeviceProfile
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.saggitt.omega.util.createRipplePill
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.getColorAttr
import kotlin.math.max

class SuperGContainerView @JvmOverloads constructor(
    paramContext: Context?,
    paramAttributeSet: AttributeSet? = null,
    paramInt: Int = 0
) :
    BaseGContainerView(paramContext, paramAttributeSet, paramInt) {
    init {
        inflate(paramContext, R.layout.qsb_blocker_view, this)
    }

    override fun getQsbView(withMic: Boolean): Int {
        return R.layout.qsb_without_mic
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(0, 0, 0, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val qsbOverlapMargin = -resources.getDimensionPixelSize(R.dimen.qsb_overlap_margin)
        val deviceProfile = LauncherAppState.getIDP(context).getDeviceProfile(
            context
        )
        val size = MeasureSpec.getSize(widthMeasureSpec) - qsbOverlapMargin
        val qsbWidth: Int
        val marginStart: Int
        if (deviceProfile.isVerticalBarLayout) {
            qsbWidth = size
            marginStart =
                qsbOverlapMargin + resources.getDimensionPixelSize(R.dimen.qsb_button_elevation)
        } else {
            val workspacePadding = deviceProfile.workspacePadding
            val fullWidth = size - workspacePadding.left - workspacePadding.right
            qsbWidth = DeviceProfile.calculateCellWidth(
                fullWidth,
                0,
                deviceProfile.inv.numColumns
            ) * deviceProfile.inv.numColumns
            marginStart = 0
        }
        if (mQsbView != null) {
            val layoutParams = mQsbView!!.layoutParams as LayoutParams
            layoutParams.width = qsbWidth / deviceProfile.inv.numColumns
            if (deviceProfile.isVerticalBarLayout) {
                layoutParams.width = max(
                    layoutParams.width,
                    resources.getDimensionPixelSize(R.dimen.qsb_min_width_with_mic)
                )
            } else {
                layoutParams.width = max(
                    layoutParams.width,
                    resources.getDimensionPixelSize(R.dimen.qsb_min_width_portrait)
                )
            }
            layoutParams.marginStart = marginStart
            layoutParams.resolveLayoutDirection(layoutParams.layoutDirection)
        }
        if (mConnectorView != null) {
            val layoutParams = mConnectorView!!.layoutParams as LayoutParams
            layoutParams.width = marginStart + layoutParams.height / 2
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun setGoogleAnimationStart(rect: Rect?, intent: Intent?) {}
    override fun applyQsbColor() {
        super.applyQsbColor()
        val radius = dpToPx(100f)
        mQsbView!!.background =
            createRipplePill(context, context.getColorAttr(R.attr.popupColorPrimary), radius)
    }
}