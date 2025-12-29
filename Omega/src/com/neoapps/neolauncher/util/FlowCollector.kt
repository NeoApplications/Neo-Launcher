package com.neoapps.neolauncher.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FlowCollector<T>(
    private val flow: Flow<T>,
    private val callback: (T) -> Unit
) {
    private val scope = MainScope()
    private var job: Job? = null

    fun start() {
        job = scope.launch {
            flow.collect(callback)
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}

