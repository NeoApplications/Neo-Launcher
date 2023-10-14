package com.saulhdev.neolauncher.hotseat

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.InsettableFrameLayout
import com.android.launcher3.R
import com.android.launcher3.model.data.ItemInfo


class ExpandableHotseat(context: Context, attrs: AttributeSet? = null) :
    CustomHotseat(context, attrs) {
    private var hotseatTools: View

    private val handleBarBottomPadding =
        resources.getDimensionPixelSize(R.dimen.hotseat_handle_bar_padding_bottom)
    private val hotseatTopRowHeight =
        resources.getDimensionPixelSize(R.dimen.hotseat_top_row_height)
    private val settingButtonPaddingx =
        getResources().getDimensionPixelSize(R.dimen.hotseat_settings_button_padding_x);
    private val settingButtonPaddingy =
        getResources().getDimensionPixelSize(R.dimen.hotseat_settings_button_padding_y);

    private var hotseatTopRow: FrameLayout? = null
    private var handleBar: ImageView? = null
    var settingIcon: ImageView? = null

    var handler: Runnable? = null
    var handler2: Runnable? = null

    init {
        hotseatTools = LayoutInflater.from(context).inflate(R.layout.hotseat_tools, this, false)
        addView(hotseatTools)
        handler = Runnable {
            val childAt: View =
                shortcutsAndWidgets.getChildAt(0)
            if (childAt != null) {
                childAt.sendAccessibilityEvent(8)
            }
        }
        handler2 = Runnable {
            handleBar?.sendAccessibilityEvent(8);
        }
    }

    fun m(view: View): Boolean {
        if (view.tag is ItemInfo) {
            val itemInfo = view.tag as ItemInfo
            val invariantDeviceProfile = launcher.deviceProfile.inv
            return itemInfo.screenId >= invariantDeviceProfile.numHotseatIcons
        }
        return false
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        hotseatTopRow = findViewById(R.id.hotseat_top_row)
        handleBar = findViewById(R.id.hotseat_handle_bar)
        settingIcon = findViewById(R.id.hotseat_settings_icon)
    }

    private fun getDrawableHandler(): Drawable {
        return if (launcher.deviceProfile.isVerticalBarLayout) {
            AppCompatResources.getDrawable(context, R.drawable.hotseat_handle_bar_vertical)!!
        } else {
            AppCompatResources.getDrawable(context, R.drawable.hotseat_handle_bar)!!
        }
    }

    override fun setInsets(insets: Rect) {
        super.setInsets(insets)

        val lp = layoutParams as FrameLayout.LayoutParams
        val grid = mActivity.getDeviceProfile()

        if (grid.isVerticalBarLayout) {
            lp.height = LayoutParams.MATCH_PARENT
            if (grid.isSeascape) {
                lp.gravity = Gravity.START
                lp.width = grid.hotseatBarSizePx + insets.left
            } else {
                lp.gravity = Gravity.END
                lp.width = grid.hotseatBarSizePx + insets.right
            }
        } else {
            lp.gravity = Gravity.BOTTOM
            lp.width = LayoutParams.MATCH_PARENT
            lp.height =
                if (grid.isTaskbarPresent) grid.workspacePadding.bottom else grid.hotseatBarSizePx + insets.bottom
        }

        val padding = grid.getHotseatLayoutPadding(context)
        setPadding(padding.left, padding.top, padding.right, padding.bottom)
        setLayoutParams(lp)
        handleBar?.setImageDrawable(getDrawableHandler())
        handleBar?.visibility =
            if (prefs.dockExpandable.getValue()) View.VISIBLE else View.INVISIBLE
        InsettableFrameLayout.dispatchInsets(this, insets)
    }

    private fun setTopRowLayoutParams() {
        val layoutParams: FrameLayout.LayoutParams =
            hotseatTopRow!!.layoutParams as FrameLayout.LayoutParams
        val layoutParams2: FrameLayout.LayoutParams =
            handleBar?.layoutParams as FrameLayout.LayoutParams
        val layoutParams3: FrameLayout.LayoutParams =
            settingIcon?.layoutParams as FrameLayout.LayoutParams
        if (launcher.deviceProfile.isVerticalBarLayout) {
            if (launcher.deviceProfile.isSeascape) {
                layoutParams.gravity = 5
                layoutParams2.gravity = 19
                layoutParams3.gravity = 83
                layoutParams3.setMargins(settingButtonPaddingy, 0, 0, settingButtonPaddingx)
            } else {
                layoutParams.gravity = 3
                layoutParams2.gravity = 21
                layoutParams3.gravity = 85
                layoutParams3.setMargins(0, 0, settingButtonPaddingy, settingButtonPaddingx)
            }
            layoutParams.width = hotseatTopRowHeight
            layoutParams.height = -1
        } else {
            layoutParams.gravity = 48
            layoutParams.width = -1
            layoutParams.height = hotseatTopRowHeight
            layoutParams2.gravity = 81
            layoutParams3.gravity = 85
            layoutParams3.setMargins(0, 0, settingButtonPaddingx, settingButtonPaddingy)
        }
        hotseatTopRow?.setLayoutParams(layoutParams)
        handleBar?.setLayoutParams(layoutParams2)
        settingIcon?.setLayoutParams(layoutParams3)
    }

    fun getExpandableHotseatHeight(): Int {
        return hotseatTopRowHeight + getContentHeight()
    }

    fun getTopRowHeight(): Int {
        return hotseatTopRowHeight
    }

    private fun getContentHeight(): Int {
        return getContentRows(launcher.deviceProfile.inv.numHotseatRows) * launcher.deviceProfile.hotseatBarSizePx
    }

    private fun getContentRows(rows: Int): Int {
        return if (prefs.dockExpandable.getValue()) {
            rows
        } else 1
    }
}