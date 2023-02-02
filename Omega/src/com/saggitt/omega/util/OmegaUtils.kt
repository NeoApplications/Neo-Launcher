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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.R
import com.android.launcher3.allapps.AppInfoComparator
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.saggitt.omega.allapps.AppColorComparator
import com.saggitt.omega.allapps.AppUsageComparator
import com.saggitt.omega.allapps.InstallTimeComparator
import com.saggitt.omega.data.AppTrackerRepository
import java.text.Collator
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.stream.Stream

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

fun openURLinBrowser(context: Context, url: String?) {
    openURLinBrowser(context, url, null, null)
}

fun openURLinBrowser(context: Context, url: String?, sourceBounds: Rect?, options: Bundle?) {
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

fun Stream<AppInfo>.sortApps(context: Context, sortType: Int) {
    val pm: PackageManager = context.packageManager
    when (sortType) {
        Config.SORT_ZA              -> sorted(compareBy(Collator.getInstance().reversed()) {
            it.title.toString().lowercase()
        })

        Config.SORT_MOST_USED       -> {
            val repository = AppTrackerRepository.INSTANCE[context]
            val appsCounter = repository.getAppsCount()
            val mostUsedComparator = AppUsageComparator(appsCounter)
            sorted(mostUsedComparator)
        }

        Config.SORT_BY_COLOR        -> sorted(AppColorComparator(context))

        Config.SORT_BY_INSTALL_DATE -> sorted(InstallTimeComparator(pm))

        Config.SORT_AZ              -> sorted(compareBy(Collator.getInstance()) {
            it.title.toString().lowercase()
        })

        else                        -> sorted(AppInfoComparator(context))
    }
}
