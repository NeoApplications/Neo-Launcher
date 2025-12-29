package com.neoapps.neolauncher.smartspace.provider

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.provider.CalendarContract
import android.text.format.DateFormat
import com.android.launcher3.R
import com.neoapps.neolauncher.compose.navigation.Routes
import com.neoapps.neolauncher.preferences.PreferenceActivity
import com.neoapps.neolauncher.smartspace.model.SmartspaceScores
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class CalendarEventProvider(context: Context) : SmartspaceDataSource(
    context, R.string.smartspace_provider_calendar
) {
    override var internalTargets: Flow<List<SmartspaceTarget>> = flowOf(disabledTargets)

    private val requiredPermissions = listOf(Manifest.permission.READ_CALENDAR)
    private val calendarProjection = arrayOf(
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.CUSTOM_APP_PACKAGE
    )
    private val oneMinute = TimeUnit.MINUTES.toMillis(1)
    private val includeBehind = oneMinute * 15
    private val includeAhead = oneMinute * 60

    init {
        internalTargets = flow {
            while (true) {
                requiresSetup()
                emit(calendarTarget())
                delay(TimeUnit.MINUTES.toMillis(3))
            }
        }
    }

    override suspend fun requiresSetup(): Boolean =
        checkPermissionGranted().not()

    private fun calendarTarget(): List<SmartspaceTarget> {
        val events = getNextEvent()

        return if (events != null && events.isNotEmpty()) {
            val eventTargets = mutableListOf<SmartspaceTarget>()
            events.map { event ->
                val timeText = "${formatTime(event.start)} – ${formatTime(event.end)}"
                val subtitle = event.location?.let { "$it $timeText" } ?: timeText

                val target = SmartspaceTarget(
                    smartspaceTargetId = "CalendarEvent",
                    headerAction = SmartspaceAction(
                        id = "CalendarEvent",
                        icon = Icon.createWithResource(context, R.drawable.ic_calendar),
                        title = "${event.title} ${formatTimeRelative(event.start)}",
                        subtitle = subtitle,
                        pendingIntent = getPendingIntent(event)
                    ),
                    score = SmartspaceScores.SCORE_CALENDAR,
                    featureType = SmartspaceTarget.FEATURE_CALENDAR,
                )
                eventTargets.add(target)
            }
            eventTargets
        } else {
            disabledTargets
        }
    }

    private fun formatTime(time: Long) = DateFormat.getTimeFormat(context).format(Date(time))

    private fun formatTimeRelative(time: Long): String {
        val res = context.resources
        val currentTime = System.currentTimeMillis()
        if (time <= currentTime) {
            return res.getString(R.string.smartspace_now)
        }
        val minutesToEvent = ceil((time - currentTime).toDouble() / oneMinute).toInt()
        val timeString = if (minutesToEvent >= 60) {
            val hours = minutesToEvent / 60
            val minutes = minutesToEvent % 60
            val hoursString = res.getQuantityString(R.plurals.smartspace_hours, hours, hours)
            if (minutes <= 0) {
                hoursString
            } else {
                val minutesString =
                    res.getQuantityString(R.plurals.smartspace_minutes, minutes, minutes)
                res.getString(R.string.smartspace_hours_mins, hoursString, minutesString)
            }
        } else {
            res.getQuantityString(R.plurals.smartspace_minutes, minutesToEvent, minutesToEvent)
        }
        return res.getString(R.string.smartspace_in_time, timeString)
    }

    private fun getPendingIntent(event: CalendarEvent): PendingIntent? {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://com.android.calendar/events/${event.id}")
            `package` = event.appPackage
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getNextEvent(): MutableList<CalendarEvent>? {
        val currentTime = System.currentTimeMillis()
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            calendarProjection,
            "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?",
            arrayOf("${currentTime - includeBehind}", "${currentTime + includeAhead}"),
            "${CalendarContract.Events.DTSTART} ASC LIMIT 3"
        )
            ?.use {
                val targets = mutableListOf<CalendarEvent>()
                while (it.moveToNext()) {
                    val event = CalendarEvent(
                        id = it.getLong(0),
                        title = it.getString(1),
                        start = it.getLong(2),
                        end = it.getLong(3),
                        location = it.getString(4),
                        appPackage = it.getString(5)
                    )
                    targets.add(event)
                }
                return targets
            }
        return null
    }

    private fun checkPermissionGranted(): Boolean {
        return requiredPermissions.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }

    override suspend fun startSetup(activity: Activity) {
        val intent = PreferenceActivity.navigateIntent(activity, Routes.PREFS_WIDGETS)
        val message = activity.getString(
            R.string.event_provider_missing_notification_dots,
            activity.getString(providerName)
        )

        PreferenceActivity.startBlankActivityDialog(
            activity,
            intent,
            activity.getString(R.string.title_missing_notification_access),
            message,
            context.getString(R.string.title_change_settings),
        )
    }

    data class CalendarEvent(
        val id: Long,
        val title: String,
        val start: Long,
        val end: Long,
        val location: String?,
        val appPackage: String?
    )
}