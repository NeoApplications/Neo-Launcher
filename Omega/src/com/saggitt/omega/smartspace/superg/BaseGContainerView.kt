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

import android.animation.ObjectAnimator
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.saggitt.omega.search.SearchProviderController
import com.saggitt.omega.search.SearchProviderController.OnProviderChangeListener
import com.saggitt.omega.util.Config

abstract class BaseGContainerView(
    paramContext: Context?,
    paramAttributeSet: AttributeSet?,
    paramInt: Int
) :
    FrameLayout(paramContext!!, paramAttributeSet, paramInt), View.OnClickListener,
    OnProviderChangeListener {
    private var mObjectAnimator: ObjectAnimator? = null
    var mQsbView: View? = null
    protected var mConnectorView: QsbConnector? = null
    private val mADInterpolator: Interpolator = AccelerateDecelerateInterpolator()
    private val mElevationAnimator: ObjectAnimator?
    private var qsbHidden = false
    private var mQsbViewId = 0
    private var mWindowHasFocus = false
    protected abstract fun getQsbView(withMic: Boolean): Int

    init {
        mElevationAnimator = ObjectAnimator()
    }

    private fun applyOpaPreference() {
        val qsbViewId = getQsbView(false)
        if (qsbViewId != mQsbViewId) {
            mQsbViewId = qsbViewId
            if (mQsbView != null) {
                removeView(mQsbView)
            }
            mQsbView = LayoutInflater.from(context).inflate(mQsbViewId, this, false)
            val mQsbButtonElevation =
                resources.getDimensionPixelSize(R.dimen.qsb_button_elevation).toFloat()
            addView(mQsbView)
            mObjectAnimator = ObjectAnimator.ofFloat(mQsbView, "elevation", 0f, mQsbButtonElevation)
                .setDuration(200L)
            mObjectAnimator!!.interpolator = mADInterpolator
            if (qsbHidden) {
                hideQsbImmediately()
            }
            mQsbView!!.setOnClickListener(this)
        }
        loadIcon()
        applyQsbColor()
    }

    protected open fun applyQsbColor() {}

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyOpaPreference()
        applyMinusOnePreference()
        applyVisibility()
        SearchProviderController.getInstance(context).addOnProviderChangeListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SearchProviderController.getInstance(context).removeOnProviderChangeListener(this)
    }

    private fun applyMinusOnePreference() {
        if (mConnectorView != null) {
            removeView(mConnectorView)
            mConnectorView = null
        }
    }

    override fun onClick(paramView: View) {
        val controller = SearchProviderController.getInstance(
            context
        )
        if (controller.isGoogle) {
            context.sendOrderedBroadcast(
                pillAnimationIntent,
                null,
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        if (resultCode == 0) {
                            startQsbActivity()
                        } else {
                            loadWindowFocus()
                        }
                    }
                },
                null,
                0,
                null,
                null
            )
        } else {
            val provider = controller.searchProvider
            provider.startSearch { intent: Intent? ->
                context.startActivity(
                    intent, ActivityOptionsCompat.makeClipRevealAnimation(
                        mQsbView!!, 0, 0, mQsbView!!.width, mQsbView!!.width
                    ).toBundle()
                )
            }
        }
    }

    private val pillAnimationIntent: Intent
        get() {
            val qsbLocation = IntArray(2)
            mQsbView!!.getLocationOnScreen(qsbLocation)
            val rect = Rect(
                qsbLocation[0],
                qsbLocation[1],
                qsbLocation[0] + mQsbView!!.width,
                qsbLocation[1] + mQsbView!!.height
            )
            val intent = Intent("com.google.nexuslauncher.FAST_TEXT_SEARCH")
            setGoogleAnimationStart(rect, intent)
            intent.sourceBounds = rect
            return intent.putExtra("source_round_left", true)
                .putExtra("source_round_right", true)
                .putExtra("source_logo_offset", midLocation(findViewById(R.id.g_icon), rect))
                .setPackage(Config.GOOGLE_QSB)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    private fun midLocation(view: View, rect: Rect): Point {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val point = Point()
        point.x = location[0] - rect.left + view.width / 2
        point.y = location[1] - rect.top + view.height / 2
        return point
    }

    protected open fun setGoogleAnimationStart(rect: Rect?, intent: Intent?) {}

    private fun loadWindowFocus() {
        if (hasWindowFocus()) {
            mWindowHasFocus = true
        } else {
            hideQsbImmediately()
        }
    }

    override fun onWindowFocusChanged(newWindowHasFocus: Boolean) {
        super.onWindowFocusChanged(newWindowHasFocus)
        if (!newWindowHasFocus && mWindowHasFocus) {
            hideQsbImmediately()
        } else if (newWindowHasFocus && !mWindowHasFocus) {
            changeVisibility(true)
        }
    }

    override fun onWindowVisibilityChanged(paramInt: Int) {
        super.onWindowVisibilityChanged(paramInt)
        changeVisibility(false)
    }

    private fun hideQsbImmediately() {
        mWindowHasFocus = false
        qsbHidden = true
        if (mQsbView != null) {
            mQsbView!!.alpha = 0f
            if (mElevationAnimator != null && mElevationAnimator.isRunning) {
                mElevationAnimator.end()
            }
        }
        if (mConnectorView != null) {
            if (mObjectAnimator != null && mObjectAnimator!!.isRunning) {
                mObjectAnimator!!.end()
            }
            mConnectorView!!.alpha = 0f
        }
    }

    private fun changeVisibility(makeVisible: Boolean) {
        mWindowHasFocus = false
        if (qsbHidden) {
            qsbHidden = false
            if (mQsbView != null) {
                mQsbView!!.alpha = 1f
                if (mElevationAnimator != null) {
                    mElevationAnimator.start()
                    if (!makeVisible) {
                        mElevationAnimator.end()
                    }
                }
            }
            if (mConnectorView != null) {
                mConnectorView!!.alpha = 1f
                mConnectorView!!.changeVisibility(makeVisible)
            }
        }
    }

    private fun startQsbActivity() {
        val context = context
        try {
            context.startActivity(
                Intent(TEXT_ASSIST)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setPackage(Config.GOOGLE_QSB)
            )
        } catch (ignored: ActivityNotFoundException) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")),
                    Launcher.getLauncher(context).getActivityLaunchOptions(mQsbView).toBundle()
                )
            } catch (ignored: ActivityNotFoundException) {
            }
        }
    }

    private fun applyVisibility() {
        if (mQsbView != null) {
            mQsbView!!.visibility = VISIBLE
        }
        if (mConnectorView != null) {
            mConnectorView!!.visibility = VISIBLE
        }
    }

    override fun onSearchProviderChanged() {
        loadIcon()
    }

    private fun loadIcon() {
        if (mQsbView != null) {
            val provider = SearchProviderController.getInstance(context)
                .searchProvider
            val gIcon = mQsbView!!.findViewById<ImageView>(R.id.g_icon)
            gIcon.setImageDrawable(provider.icon)
        }
    }

    companion object {
        private const val TEXT_ASSIST = "com.google.android.googlequicksearchbox.TEXT_ASSIST"
    }
}