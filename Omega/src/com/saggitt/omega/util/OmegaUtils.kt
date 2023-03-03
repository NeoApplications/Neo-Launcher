/*
 *  This file is part of Omega Launcher.
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.util

import android.app.Activity
import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.StatusBarNotification
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR
import com.android.launcher3.views.OptionsPopupView
import java.lang.reflect.Field
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class JavaField<T>(
    private val targetObject: Any,
    fieldName: String,
    targetClass: Class<*> = targetObject::class.java
) {
    private val field: Field = targetClass.getDeclaredField(fieldName).apply { isAccessible = true }
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = field.get(targetObject) as T
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
        field.set(targetObject, value)
}

@JvmOverloads
fun makeBasicHandler(preferMyLooper: Boolean = false, callback: Handler.Callback? = null): Handler =
    if (preferMyLooper)
        Handler(Looper.myLooper() ?: Looper.getMainLooper(), callback)
    else
        Handler(Looper.getMainLooper(), callback)

fun <T, A> ensureOnMainThread(creator: (A) -> T): (A) -> T {
    return { it ->
        if (Looper.myLooper() == Looper.getMainLooper()) {
            creator(it)
        } else {
            try {
                MAIN_EXECUTOR.submit(Callable { creator(it) }).get()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }

        }
    }
}

val mainHandler by lazy { makeBasicHandler() }
val uiWorkerHandler: Handler by lazy { UI_HELPER_EXECUTOR.handler }
fun runOnUiWorkerThread(r: () -> Unit) {
    runOnThread(uiWorkerHandler, r)
}

fun runOnMainThread(r: () -> Unit) {
    runOnThread(mainHandler, r)
}

fun runOnThread(handler: Handler, r: () -> Unit) {
    if (handler.looper.thread.id == Looper.myLooper()?.thread?.id) {
        r()
    } else {
        handler.post(r)
    }
}

fun formatTime(calendar: Calendar, context: Context? = null): String {
    return when (context) {
        null -> String.format(
            "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.HOUR_OF_DAY)
        )

        else -> if (DateFormat.is24HourFormat(context)) String.format(
            "%02d:%02d", calendar.get(
                Calendar.HOUR_OF_DAY
            ), calendar.get(Calendar.MINUTE)
        ) else String.format(
            "%02d:%02d %s",
            if (calendar.get(
                    Calendar.HOUR_OF_DAY
                ) % 12 == 0
            ) 12 else calendar.get(
                Calendar.HOUR_OF_DAY
            ) % 12,
            calendar.get(
                Calendar.MINUTE
            ),
            if (calendar.get(
                    Calendar.HOUR_OF_DAY
                ) < 12
            ) "AM" else "PM"
        )
    }
}

inline val Calendar.hourOfDay get() = get(Calendar.HOUR_OF_DAY)
inline val Calendar.dayOfYear get() = get(Calendar.DAY_OF_YEAR)

fun StatusBarNotification.loadSmallIcon(context: Context): Drawable? {
    return notification.smallIcon?.loadDrawable(context)
}

operator fun PreferenceGroup.get(index: Int): Preference = getPreference(index)

fun openPopupMenu(view: View, rect: RectF?, vararg items: OptionsPopupView.OptionItem) {
    val launcher = Launcher.getLauncher(view.context)
    OptionsPopupView.show(
        launcher,
        rect ?: RectF(launcher.getViewBounds(view)),
        items.toList(),
        true
    )
}

fun createRipplePill(context: Context, color: Int, radius: Float): Drawable {
    return RippleDrawable(
        ContextCompat.getColorStateList(context, R.color.focused_background)!!,
        createPill(color, radius), createPill(color, radius)
    )
}

fun createPill(color: Int, radius: Float): Drawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = radius
    }
}

fun String.toTitleCase(): String = splitToSequence(" ").map {
    it.replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(
            Locale.getDefault()
        ) else ch.toString()
    }
}.joinToString(" ")

inline fun <T> listWhileNotNull(generator: () -> T?): List<T> = mutableListOf<T>().apply {
    while (true) {
        add(generator() ?: break)
    }
}

val isBlackTheme: Boolean = false //TODO add black theme support

fun openURLInBrowser(context: Context, url: String?) {
    openURLInBrowser(context, url, null, null)
}

fun openURLInBrowser(context: Context, url: String?, sourceBounds: Rect?, options: Bundle?) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (context !is AppCompatActivity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.sourceBounds = sourceBounds
        if (options == null) {
            context.startActivity(intent)
        } else {
            context.startActivity(intent, options)
        }
    } catch (exc: ActivityNotFoundException) {
        Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_SHORT).show()
    }
}

fun dpToPx(size: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        size,
        Resources.getSystem().displayMetrics
    )
}

fun pxToDp(size: Float): Float {
    return size / dpToPx(1f)
}

fun Activity.recreateAnimated() = startActivity(
    Intent.makeRestartActivityTask(
        ComponentName(this, this::class.java)
    ), ActivityOptions.makeCustomAnimation(
        this, android.R.anim.fade_in, android.R.anim.fade_out
    ).toBundle()
)

fun <T, U : Comparable<U>> comparing(extractKey: (T) -> U): Comparator<T> {
    return Comparator { o1, o2 -> extractKey(o1).compareTo(extractKey(o2)) }
}

fun <T, U : Comparable<U>> Comparator<T>.then(extractKey: (T) -> U): Comparator<T> {
    return kotlin.Comparator { o1, o2 ->
        val res = compare(o1, o2)
        if (res != 0) res else extractKey(o1).compareTo(extractKey(o2))
    }
}