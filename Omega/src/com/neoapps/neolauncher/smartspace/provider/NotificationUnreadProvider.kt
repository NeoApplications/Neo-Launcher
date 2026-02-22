package com.neoapps.neolauncher.smartspace.provider

import android.app.Notification
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import com.android.launcher3.R
import com.android.launcher3.util.PackageUserKey
import com.neoapps.neolauncher.flowerpot.Flowerpot
import com.neoapps.neolauncher.flowerpot.FlowerpotApps
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import com.saulhdev.smartspace.uitemplatedata.BaseTemplateData
import com.saulhdev.smartspace.uitemplatedata.BaseTemplateData.SubItemInfo
import com.saulhdev.smartspace.uitemplatedata.Icon
import com.saulhdev.smartspace.uitemplatedata.TapAction
import com.saulhdev.smartspace.uitemplatedata.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationUnreadProvider(context: Context) : SmartspaceDataSource(
    context, R.string.event_provider_unread_notifications
) {
    private val manager = NotificationsManager.INSTANCE.get(context)
    private var flowerpotLoaded = false
    private var flowerpotApps: FlowerpotApps? = null
    private val tmpKey = PackageUserKey(null, null)
    private var zenModeEnabled = false
    private var currentNotifications = emptyList<StatusBarNotification>()

    override val internalTargets: Flow<List<SmartspaceTarget>> = callbackFlow {
        val zenModeListener = ZenModeListener(context.contentResolver) {
            zenModeEnabled = it
            trySend(Unit)
        }
        zenModeListener.startListening()

        val job = launch {
            manager.notifications.collect {
                currentNotifications = it
                trySend(Unit)
            }
        }

        launch(Dispatchers.IO) {
            flowerpotApps = Flowerpot.Manager.getInstance(context)
                .getPot("COMMUNICATION", true)?.apps
            flowerpotLoaded = true
            withContext(Dispatchers.Main) {
                trySend(Unit)
            }
        }

        manager.onNotificationFullRefresh()

        awaitClose {
            zenModeListener.stopListening()
            job.cancel()
        }
    }.map {
        val sbn = findCurrentNotification()
        val target = if (zenModeEnabled) {
            getZenModeTarget()
        } else if (sbn != null) {
            getNotificationTarget(sbn)
        } else {
            null
        }
        if (target != null) listOf(target) else emptyList()
    }.flowOn(Dispatchers.Main)


    private fun isCommunicationApp(sbn: StatusBarNotification): Boolean {
        return tmpKey.updateFromNotification(sbn)
                && (flowerpotApps == null || flowerpotApps?.packageMatches?.contains(tmpKey) != false)
    }

    private fun findCurrentNotification(): StatusBarNotification? {
        if (!flowerpotLoaded) return null
        return currentNotifications
            .asSequence()
            .filter { !it.isOngoing }
            .filter { it.notification.priority >= Notification.PRIORITY_DEFAULT }
            .filter { isCommunicationApp(it) }
            .maxWithOrNull(
                compareBy(
                    { it.notification.priority },
                    { it.notification.`when` })
            )
    }

    private fun getNotificationTarget(sbn: StatusBarNotification): SmartspaceTarget {
        val notif = sbn.notification
        val extras = notif.extras
        val titleText = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val bodyText = extractNotificationText(extras)

        val splitted = splitTitle(titleText)
        val lines = mutableListOf<String>()
        if (bodyText.isNotBlank()) {
            lines.add(bodyText)
        }
        lines.addAll(splitted.reversed())

        val appName = context.getAppName(sbn.packageName).toString()
        if (!lines.contains(appName)) {
            lines.add(appName)
        }
        val fullText = lines.joinToString(" - ")

        val primaryText = Text(fullText, TextUtils.TruncateAt.END, 3)

        val icon = try {
            val smallIcon = sbn.notification.smallIcon
            if (smallIcon != null) {
                val drawable = smallIcon.loadDrawable(context)
                if (drawable is BitmapDrawable) {
                    Icon(
                        android.graphics.drawable.Icon.createWithBitmap(drawable.bitmap),
                        null,
                        true
                    )
                } else if (drawable != null) {
                    Icon(smallIcon, null, true)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        val clickIntent = sbn.notification.contentIntent

        val headerAction = SmartspaceAction(
            id = sbn.key,
            icon = sbn.notification.smallIcon,
            title = titleText,
            subtitle = bodyText,
            pendingIntent = clickIntent
        )

        val subItemInfo = SubItemInfo(
            primaryText,
            icon,
            TapAction(
                id = sbn.key,
                pendingIntent = clickIntent,
                userHandle = sbn.user,
                intent = null,
                extras = null,
                shouldShowOnLockscreen = false
            ),
            null
        )
        val templateData = BaseTemplateData(
            templateType = SmartspaceTarget.UI_TEMPLATE_DEFAULT,
            primaryItem = subItemInfo,
            subtitleItem = null,
            subtitleSupplementalItem = null,
            supplementalLineItem = null,
            supplementalAlarmItem = null,
            layoutWeight = 0
        )

        return SmartspaceTarget(
            smartspaceTargetId = context.getString(R.string.event_provider_unread_notifications),
            headerAction = headerAction,
            featureType = SmartspaceTarget.FEATURE_MISSED_CALL, // Using this as a placeholder for communication
            templateData = templateData,
            creationTimeMillis = sbn.notification.`when`
        )
    }

    private fun getZenModeTarget(): SmartspaceTarget {
        val primaryText =
            Text(context.getString(R.string.zen_mode_enabled), TextUtils.TruncateAt.END, 1)
        val icon = Icon(
            android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_zen_mode),
            "",
            true
        )
        val subItemInfo = SubItemInfo(primaryText, icon, null, null)
        val templateData = BaseTemplateData(
            templateType = SmartspaceTarget.UI_TEMPLATE_DEFAULT,
            primaryItem = subItemInfo,
            subtitleItem = null,
            subtitleSupplementalItem = null,
            supplementalLineItem = null,
            supplementalAlarmItem = null,
            layoutWeight = 0
        )
        return SmartspaceTarget(
            smartspaceTargetId = "zenModeTarget",
            featureType = SmartspaceTarget.FEATURE_UNDEFINED,
            templateData = templateData,
            score = 100
        )
    }

    private fun extractNotificationText(extras: android.os.Bundle): String {
        val candidates = mutableListOf<String?>()

        candidates.add(extras.getString(Notification.EXTRA_TEXT))
        candidates.add(extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
        candidates.add(extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString())
        candidates.add(extras.getString(Notification.EXTRA_BIG_TEXT))

        candidates.add(extras.getString(Notification.EXTRA_SUB_TEXT))
        candidates.add(extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString())
        candidates.add(extras.getString(Notification.EXTRA_INFO_TEXT))
        candidates.add(extras.getString(Notification.EXTRA_SUMMARY_TEXT))
        candidates.add(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString())

        try {
            for (key in extras.keySet()) {
                val value = extras.get(key)
                if (value is String && value.isNotBlank() && !value.equals(
                        extras.getString(Notification.EXTRA_TITLE),
                        ignoreCase = true
                    )
                ) {
                    candidates.add(value)
                } else if (value is CharSequence && value.isNotBlank()) {
                    candidates.add(value.toString())
                }
            }
        } catch (e: Exception) {
        }

        for (candidate in candidates) {
            if (candidate != null) {
                val text = candidate.trim().split("\n").firstOrNull()?.trim()
                if (!text.isNullOrBlank() && text.length > 3) {
                    return text
                }
            }
        }

        return ""
    }

    private fun splitTitle(title: String): Array<String> {
        val delimiters = arrayOf(": ", " - ", " • ")
        for (del in delimiters) {
            if (title.contains(del)) {
                return title.split(del.toRegex(), 2).toTypedArray()
            }
        }
        return arrayOf(title)
    }
}
