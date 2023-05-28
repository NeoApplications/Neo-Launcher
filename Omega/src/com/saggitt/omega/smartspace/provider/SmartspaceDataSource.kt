package com.saggitt.omega.smartspace.provider

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.saggitt.omega.preferences.NeoPrefs
import com.saggitt.omega.smartspace.model.SmartspaceTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.TimeUnit

abstract class SmartspaceDataSource(
    val context: Context,
    val providerName: Int
) {
    val prefs = NeoPrefs.getInstance(context)

    open val isAvailable: Boolean = true
    protected abstract val internalTargets: Flow<List<SmartspaceTarget>>
    open val disabledTargets: List<SmartspaceTarget> = emptyList()

    private val restartSignal = MutableStateFlow(0)

    private val enabledTargets
        get() = internalTargets
            .onStart {
                if (requiresSetup()) throw RequiresSetupException()
            }
            .map { State(targets = it) }
            .catch {
                if (it is RequiresSetupException) {
                    emit(
                        State(
                            targets = disabledTargets,
                            requiresSetup = listOf(this@SmartspaceDataSource)
                        )
                    )
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    val targets = prefs.smartspaceEventProviders.get()
        .flatMapLatest {
            if (isAvailable)
                restartSignal.flatMapLatest { enabledTargets }
            else
                flowOf(State(targets = disabledTargets))
        }


    open suspend fun requiresSetup(): Boolean = false

    open suspend fun startSetup(activity: Activity) {}

    suspend fun onSetupDone() {
        if (!requiresSetup()) {
            restart()
        }
    }

    private fun restart() {
        restartSignal.value++
    }

    private val handlerThread =
        HandlerThread(this::class.java.simpleName).apply { if (!isAlive) start() }
    private val handler = Handler(handlerThread.looper)
    private val update = ::periodicUpdate

    open val timeout = TimeUnit.MINUTES.toMillis(30)
    fun startListening() {
        handler.post(update)
    }

    private fun periodicUpdate() {
        try {
            updateData()
        } catch (e: Exception) {
            Log.d("PeriodicDataProvider", "failed to update data", e)
        }
        handler.postDelayed(update, timeout)
    }

    fun stopListening() {
        handlerThread.quit()
    }

    open fun updateData() {
    }

    private class RequiresSetupException : RuntimeException()

    data class State(
        val targets: List<SmartspaceTarget> = emptyList(),
        val requiresSetup: List<SmartspaceDataSource> = emptyList()
    ) {
        operator fun plus(other: State): State {
            return State(
                targets = this.targets + other.targets,
                requiresSetup = this.requiresSetup + other.requiresSetup
            )
        }
    }
}
