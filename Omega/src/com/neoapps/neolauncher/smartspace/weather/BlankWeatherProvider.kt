package com.neoapps.neolauncher.smartspace.weather

import android.content.Context
import com.android.launcher3.R
import com.neoapps.neolauncher.smartspace.provider.SmartspaceDataSource
import com.saulhdev.smartspace.SmartspaceTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class BlankWeatherProvider(context: Context) : SmartspaceDataSource(
    context, R.string.title_disabled
) {
    override val isAvailable = true

    override val disabledTargets = listOf(GoogleWeatherProvider.dummyTarget)
    override val internalTargets: Flow<List<SmartspaceTarget>> = listOf(disabledTargets).asFlow()

}