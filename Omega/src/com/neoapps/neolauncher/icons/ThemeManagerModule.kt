package com.neoapps.neolauncher.icons

import com.android.launcher3.dagger.LauncherAppSingleton
import com.android.launcher3.graphics.ThemeManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeManagerModule {
    @Binds
    @LauncherAppSingleton
    abstract fun bindThemeManager(impl: CustomThemeManager): ThemeManager
}
