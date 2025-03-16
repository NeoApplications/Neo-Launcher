/*
 * This file is part of Neo Launcher
 * Copyright (c) 2021   Neo Launcher Team
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
 */

package com.neoapps.launcher.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import com.android.launcher3.allapps.AppInfoComparator
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.neoapps.launcher.allapps.AppColorComparator
import com.neoapps.launcher.allapps.AppUsageComparator
import com.neoapps.launcher.allapps.InstallTimeComparator
import com.neoapps.launcher.data.repository.AppTrackerRepository
import java.text.Collator
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

@JvmOverloads
fun makeBasicHandler(preferMyLooper: Boolean = false, callback: Handler.Callback? = null): Handler =
    if (preferMyLooper)
        Handler(Looper.myLooper() ?: Looper.getMainLooper(), callback)
    else
        Handler(Looper.getMainLooper(), callback)

val mainHandler by lazy { makeBasicHandler() }

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

fun getAllAppsComparator(context: Context, sortType: Int): Comparator<AppInfo> {
    val pm: PackageManager = context.packageManager
    return when (sortType) {
        Config.SORT_ZA -> compareBy(Collator.getInstance().reversed()) {
            it.title.toString().lowercase()
        }

        Config.SORT_MOST_USED -> {
            val repository = AppTrackerRepository.INSTANCE[context]
            val appsCounter = repository.getAllApps()
            val mostUsedComparator = AppUsageComparator(appsCounter)
            mostUsedComparator
        }

        Config.SORT_BY_COLOR -> AppColorComparator(context)

        Config.SORT_BY_INSTALL_DATE -> InstallTimeComparator(pm)

        Config.SORT_AZ -> compareBy(Collator.getInstance()) {
            it.title.toString().lowercase()
        }

        else -> AppInfoComparator(context)
    }
}
