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

package com.neoapps.neolauncher.smartspace.provider

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.BatteryManager
import androidx.core.content.getSystemService
import com.android.launcher3.R
import com.neoapps.neolauncher.smartspace.model.SmartspaceScores
import com.neoapps.neolauncher.util.broadcastReceiverFlow
import com.neoapps.neolauncher.util.formatShortElapsedTimeRoundingUpToMinutes
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.flow.map

class BatteryStatusProvider(context: Context) : SmartspaceDataSource(
    context, R.string.battery_status
) {
    private val batteryManager = context.getSystemService<BatteryManager>()

    override val internalTargets =
        broadcastReceiverFlow(context, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            .map { intent ->
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                val full = status == BatteryManager.BATTERY_STATUS_FULL
                val level = (100f
                        * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                        / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)).toInt()
                listOfNotNull(getSmartspaceTarget(charging, full, level))
            }

    private fun getSmartspaceTarget(
        charging: Boolean,
        full: Boolean,
        level: Int
    ): SmartspaceTarget? {
        val title = when {
            full || level == 100 -> context.getString(R.string.battery_full)
            charging -> context.getString(R.string.battery_charging)
            level <= 15 -> context.getString(R.string.battery_low)
            else -> return null
        }
        val chargingTimeRemaining = computeChargeTimeRemaining()
        val subtitle = if (charging && chargingTimeRemaining > 0) {
            val chargingTime =
                formatShortElapsedTimeRoundingUpToMinutes(context, chargingTimeRemaining)
            context.getString(
                R.string.battery_charging_percentage_charging_time, level, chargingTime
            )
        } else {
            context.getString(R.string.n_percent, level)
        }
        val iconResId = if (charging) R.drawable.ic_charging else R.drawable.ic_battery_low
        return SmartspaceTarget(
            smartspaceTargetId = "batteryStatus",
            headerAction = SmartspaceAction(
                id = "batteryStatusAction",
                icon = Icon.createWithResource(context, iconResId),
                title = title,
                subtitle = subtitle
            ),
            score = SmartspaceScores.SCORE_BATTERY,
            featureType = SmartspaceTarget.FEATURE_BATTERY
        )
    }

    private fun computeChargeTimeRemaining(): Long {
        return try {
            batteryManager?.computeChargeTimeRemaining() ?: -1
        } catch (t: Throwable) {
            -1
        }
    }
}
