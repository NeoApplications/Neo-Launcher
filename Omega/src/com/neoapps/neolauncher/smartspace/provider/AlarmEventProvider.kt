package com.neoapps.neolauncher.smartspace.provider

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.AlarmClock
import com.android.launcher3.R
import com.neoapps.neolauncher.smartspace.model.SmartspaceScores
import com.neoapps.neolauncher.util.formatTime
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AlarmEventProvider(context: Context) : SmartspaceDataSource(
    context, R.string.name_provider_alarm_events
) {
    override var internalTargets: Flow<List<SmartspaceTarget>> = flowOf(disabledTargets)

    init {
        internalTargets = flow {
            while (true) {
                emit(alarmTarget())
                delay(TimeUnit.MINUTES.toMillis(2))
            }
        }
    }

    private fun alarmTarget(): List<SmartspaceTarget> {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return emptyList()
            }
        }

        val alarmClock = alarmManager.nextAlarmClock
        if (alarmClock != null &&
            alarmClock.showIntent.isActivity &&
            alarmClock.triggerTime - System.currentTimeMillis() <= TimeUnit.MINUTES.toMillis(30)
        ) {
            val title = context.getString(R.string.resuable_text_alarm)
            val calendarTrigerTime = Calendar.getInstance()
            calendarTrigerTime.timeInMillis = alarmClock.triggerTime
            val subTitle = formatTime(calendarTrigerTime, context)

            val target = SmartspaceTarget(
                smartspaceTargetId = "AlarmEvent",
                headerAction = SmartspaceAction(
                    id = "AlarmEvent",
                    icon = Icon.createWithResource(context, R.drawable.ic_alarm_on_black_24dp),
                    title = title,
                    subtitle = subTitle,
                    pendingIntent = getPendingIntent()
                ),
                score = SmartspaceScores.SCORE_ALARM,
                featureType = SmartspaceTarget.FEATURE_UPCOMING_ALARM
            )

            return listOf(target)
        }

        return emptyList()
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}
