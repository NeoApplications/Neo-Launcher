package com.neoapps.neolauncher.smartspace.provider

import android.app.Activity
import android.content.Context
import com.neoapps.neolauncher.util.prefs
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

abstract class SmartspaceDataSource(
    val context: Context,
    val providerName: Int
) {
    val prefs = context.prefs

    open val isAvailable: Boolean = true
    abstract val internalTargets: Flow<List<SmartspaceTarget>>
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

    open suspend fun startSetup(activity: Activity) {} // TODO implement in the respective providers

    suspend fun onSetupDone() {
        if (!requiresSetup()) {
            restart()
        }
    }

    private fun restart() {
        restartSignal.value++
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
