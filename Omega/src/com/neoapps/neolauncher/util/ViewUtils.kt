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

package com.neoapps.neolauncher.util

import android.app.PendingIntent
import android.content.res.Resources
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val pendingIntentTagId =
    Resources.getSystem().getIdentifier("pending_intent_tag", "id", "android")

fun OnAttachStateChangeListener(callback: (isAttached: Boolean) -> Unit) =
    object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = callback(true)
        override fun onViewDetachedFromWindow(v: View) = callback(false)
    }

fun View.observeAttachedState(callback: (isAttached: Boolean) -> Unit): () -> Unit {
    var wasAttached = false
    val listener = OnAttachStateChangeListener { isAttached ->
        if (wasAttached != isAttached) {
            wasAttached = isAttached
            callback(isAttached)
        }
    }
    addOnAttachStateChangeListener(listener)
    if (isAttachedToWindow) {
        listener.onViewAttachedToWindow(this)
    }
    return { removeOnAttachStateChangeListener(listener) }
}

val View?.pendingIntent get() = this?.getTag(pendingIntentTagId) as? PendingIntent
fun View.repeatOnAttached(block: suspend CoroutineScope.() -> Unit) {
    var launchedJob: Job? = null

    val mutext = Mutex()
    observeAttachedState { isAttached ->
        if (isAttached) {
            launchedJob = MainScope().launch(
                context = Dispatchers.Main.immediate,
                start = CoroutineStart.UNDISPATCHED
            ) {
                mutext.withLock {
                    coroutineScope {
                        block()
                    }
                }
            }
            return@observeAttachedState
        }
        launchedJob?.cancel()
        launchedJob = null
    }
}
