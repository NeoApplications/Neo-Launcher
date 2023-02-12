package com.saggitt.omega.smartspace.provider

import android.app.Activity
import android.content.Context
import com.saggitt.omega.preferences.NLPrefs
import com.saggitt.omega.smartspace.model.SmartspaceTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

abstract class SmartspaceDataSource(
    val context: Context,
    val providerName: Int
) {
    val prefs = NLPrefs.getInstance(context)

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
        .distinctUntilChanged()
        .flatMapLatest { isEnabled ->
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
