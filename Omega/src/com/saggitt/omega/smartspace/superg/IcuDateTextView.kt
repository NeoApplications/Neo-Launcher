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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.DateFormat
import android.icu.text.DisplayContext
import android.util.AttributeSet
import com.android.launcher3.R
import com.android.launcher3.Utilities
import java.util.Locale

class IcuDateTextView @JvmOverloads constructor(context: Context?, set: AttributeSet? = null) :
    DoubleShadowTextView(context!!, set, 0) {
    private var mDateFormat: DateFormat? = null
    private val mTimeChangeReceiver: BroadcastReceiver
    private var mIsVisible = false

    init {
        mTimeChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                reloadDateFormat(Intent.ACTION_TIME_TICK != intent.action)
            }
        }
    }

    fun reloadDateFormat(forcedChange: Boolean) {
        mDateFormat = getDateFormat(context, forcedChange, mDateFormat, id == R.id.time_above)
        val format = mDateFormat!!.format(System.currentTimeMillis())
        text = format
        contentDescription = format
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_TIME_TICK)
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        context.registerReceiver(mTimeChangeReceiver, intentFilter)
    }

    private fun unregisterReceiver() {
        context.unregisterReceiver(mTimeChangeReceiver)
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (!mIsVisible && isVisible) {
            mIsVisible = true
            registerReceiver()
            reloadDateFormat(true)
        } else if (mIsVisible && !isVisible) {
            unregisterReceiver()
            mIsVisible = false
        }
    }

    companion object {
        fun getDateFormat(
            context: Context,
            forcedChange: Boolean,
            mOldFormat: DateFormat?,
            isTimeAbove: Boolean
        ): DateFormat? {
            var oldFormat = mOldFormat
            if (oldFormat == null || forcedChange) {
                DateFormat.getInstanceForSkeleton(
                    context
                        .getString(R.string.full_wday_month_day_no_year), Locale.getDefault()
                ).also {
                    oldFormat = it
                }
                    .setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE)
            }
            val prefs = Utilities.getOmegaPrefs(context)
            val showTime = prefs.smartspaceTime.getValue()
            val timeAbove = prefs.smartspaceTimeLarge.getValue()
            val show24h = prefs.smartspaceTime24H.getValue()
            val showDate = prefs.smartspaceDate.getValue()
            if (showTime && !timeAbove || isTimeAbove) {
                var format =
                    context.getString(if (show24h) R.string.icu_abbrev_time else R.string.icu_abbrev_time_12h)
                if (showDate && !isTimeAbove) format += context.getString(R.string.icu_abbrev_date)
                DateFormat.getInstanceForSkeleton(format, Locale.getDefault()).also {
                    oldFormat = it
                }
                    .setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE)
            }
            return oldFormat
        }
    }
}
