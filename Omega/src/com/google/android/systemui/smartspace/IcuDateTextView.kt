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

package com.google.android.systemui.smartspace

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.DateFormat
import android.icu.text.DisplayContext
import android.os.SystemClock
import android.text.format.DateFormat.is24HourFormat
import android.util.AttributeSet
import com.android.launcher3.R
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.smartspace.model.SmartspaceCalendar
import com.neoapps.neolauncher.smartspace.model.SmartspaceTimeFormat
import com.neoapps.neolauncher.util.broadcastReceiverFlow
import com.neoapps.neolauncher.util.repeatOnAttached
import com.neoapps.neolauncher.util.subscribeBlocking
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale

typealias FormatterFunction = (Long) -> String

class IcuDateTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : DoubleShadowTextView(context, attrs) {

    private val prefs = NeoPrefs.getInstance()
    private var calendar: SmartspaceCalendar? = null
    private lateinit var dateTimeOptions: DateTimeOptions
    private var formatterFunction: FormatterFunction? = null
    private val ticker = this::onTimeTick

    init {
        repeatOnAttached {
            val calendarSelectionEnabled = prefs.smartspaceDate.getValue()
            val calendarFlow =
                if (calendarSelectionEnabled) prefs.smartspaceCalendar.get()
                else flowOf(prefs.smartspaceCalendar.defaultValue)
            val optionsFlow = DateTimeOptions.fromPrefs(prefs)
            combine(calendarFlow, optionsFlow) { calendar, options -> calendar to options }
                .subscribeBlocking(this) {
                    calendar = SmartspaceCalendar.fromString(it.first)
                    dateTimeOptions = it.second
                    onTimeChanged(true)
                }

            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_TIME_CHANGED)
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
            broadcastReceiverFlow(context, intentFilter)
                .onEach { onTimeChanged(it.action != Intent.ACTION_TIME_TICK) }
                .launchIn(this)
        }
    }

    private fun onTimeChanged(updateFormatter: Boolean) {
        if (isShown) {
            val timeText = getTimeText(updateFormatter)
            if (text != timeText) {
                textAlignment =
                    if (shouldAlignToTextEnd()) TEXT_ALIGNMENT_TEXT_END else TEXT_ALIGNMENT_TEXT_START
                text = timeText
                contentDescription = timeText
            }
        } else if (updateFormatter) {
            formatterFunction = null
        }
    }

    private fun shouldAlignToTextEnd(): Boolean {
        val is24HourFormatManual =
            dateTimeOptions.timeFormat is SmartspaceTimeFormat.TwentyFourHourFormat
        val is24HourFormatOnSystem =
            dateTimeOptions.timeFormat is SmartspaceTimeFormat.FollowSystem && is24HourFormat(
                context
            )
        val is24HourFormat = is24HourFormatManual || is24HourFormatOnSystem
        val shouldNotAlignToEnd =
            dateTimeOptions.showTime && is24HourFormat && !dateTimeOptions.showDate
        return calendar == SmartspaceCalendar.Persian && !shouldNotAlignToEnd
    }

    private fun getTimeText(updateFormatter: Boolean): String {
        val formatter = getFormatterFunction(updateFormatter)
        return formatter(System.currentTimeMillis())
    }

    private fun getFormatterFunction(updateFormatter: Boolean): FormatterFunction {
        if (formatterFunction != null && !updateFormatter) {
            return formatterFunction!!
        }
        val formatter = createGregorianFormatter()
        formatterFunction = formatter
        return formatter
    }

    private fun createGregorianFormatter(): FormatterFunction {
        var format: String
        if (dateTimeOptions.showTime) {
            format = context.getString(
                when {
                    dateTimeOptions.timeFormat is SmartspaceTimeFormat.TwelveHourFormat -> R.string.smartspace_icu_date_pattern_gregorian_time_12h
                    dateTimeOptions.timeFormat is SmartspaceTimeFormat.TwentyFourHourFormat -> R.string.smartspace_icu_date_pattern_gregorian_time
                    is24HourFormat(context) -> R.string.smartspace_icu_date_pattern_gregorian_time
                    else -> R.string.smartspace_icu_date_pattern_gregorian_time_12h
                }
            )
            if (dateTimeOptions.showDate) format += context.getString(R.string.smartspace_icu_date_pattern_gregorian_date)
        } else {
            format =
                context.getString(R.string.smartspace_icu_date_pattern_gregorian_wday_month_day_no_year)
        }
        val formatter = DateFormat.getInstanceForSkeleton(format, Locale.getDefault())
        formatter.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE)
        return { formatter.format(it) }
    }

    private fun onTimeTick() {
        onTimeChanged(false)
        val uptimeMillis: Long = SystemClock.uptimeMillis()
        handler?.postAtTime(ticker, uptimeMillis + (1000 - uptimeMillis % 1000))
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        handler?.removeCallbacks(ticker)
        if (isVisible) {
            ticker()
        }
    }
}

data class DateTimeOptions(
    val showDate: Boolean,
    val showTime: Boolean,
    val timeFormat: SmartspaceTimeFormat,
) {
    companion object {
        fun fromPrefs(prefs: NeoPrefs) =
            combine(
                prefs.smartspaceDate.get(),
                prefs.smartspaceTime.get(),
                prefs.smartspaceTime24H.get(),
            ) { showDate, showTime, show24HTime ->
                val timeFormat: SmartspaceTimeFormat = if (show24HTime) {
                    SmartspaceTimeFormat.TwentyFourHourFormat
                } else {
                    SmartspaceTimeFormat.TwelveHourFormat
                }
                DateTimeOptions(showDate, showTime, timeFormat)
            }
    }
}

