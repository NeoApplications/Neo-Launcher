/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.dagger

import android.content.Context
import android.os.Looper
import android.os.Process
import com.android.launcher3.MainProcessInitializer
import com.android.launcher3.R
import com.android.launcher3.concurrent.annotations.Background
import com.android.launcher3.concurrent.annotations.LightweightBackground
import com.android.launcher3.concurrent.annotations.LightweightBackgroundPriority
import com.android.launcher3.concurrent.annotations.Ui
import com.android.launcher3.dragndrop.SystemDragController
import com.android.launcher3.dragndrop.SystemDragControllerStub
import com.android.launcher3.folder.FolderNameProvider
import com.android.launcher3.homescreenfiles.HomeScreenFilesNoOpProvider
import com.android.launcher3.homescreenfiles.HomeScreenFilesProvider
import com.android.launcher3.popup.PopupDataProvider
import com.android.launcher3.util.InstantAppResolver
import com.android.launcher3.util.LooperExecutor
import com.android.launcher3.util.ResourceBasedOverride.Overrides
import com.android.launcher3.util.window.RefreshRateTracker
import com.android.launcher3.util.window.RefreshRateTracker.RefreshRateTrackerImpl
import com.android.launcher3.views.ActivityContext
import com.android.launcher3.widget.LauncherWidgetHolder.WidgetHolderFactory
import com.android.launcher3.widget.LauncherWidgetHolder.WidgetHolderFactoryImpl
import com.android.launcher3.widget.LocalColorExtractor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private object Modules {}

@Module
@InstallIn(SingletonComponent::class)
abstract class WindowManagerProxyModule {}

@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityContextModule {}

@Module
@InstallIn(SingletonComponent::class)
abstract class ApiWrapperModule {}

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {
    @Binds
    abstract fun bindWidgetHolderFactory(factor: WidgetHolderFactoryImpl): WidgetHolderFactory
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PluginManagerWrapperModule {}

@Module
@InstallIn(SingletonComponent::class)
abstract class StaticObjectModule {
    @Binds abstract fun bindRefreshRateTracker(tracker: RefreshRateTrackerImpl): RefreshRateTracker
}

@Module
@InstallIn(SingletonComponent::class)
object SystemDragModule {
    @Provides
    @LauncherAppSingleton
    fun provideSystemDragController(): SystemDragController = SystemDragControllerStub()
}


// Module containing bindings for the final derivative app
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @LauncherAppSingleton
    fun provideInstantAppResolver(@ApplicationContext context: Context): InstantAppResolver =
        InstantAppResolver.newInstance(context)

    @Provides
    @LauncherAppSingleton
    fun provideMainProcessInitializer(@ApplicationContext context: Context): MainProcessInitializer =
        Overrides.getObject(
            MainProcessInitializer::class.java,
            context,
            R.string.main_process_initializer_class
        )

    @Provides
    @LauncherAppSingleton
    fun provideFolderNameProvider(@ApplicationContext context: Context): FolderNameProvider =
        FolderNameProvider.newInstance(context)

    @Provides
    @LauncherAppSingleton
    fun provideLocalColorExtractor(@ApplicationContext context: Context): LocalColorExtractor =
        LocalColorExtractor.newInstance(context)
}

// Module containing bindings of [ActivityContext] for the final derivative app
@Module
@DisableInstallInCheck
object AppActivityContextModule {
    @Provides
    fun providePopupDataProvider(activityContext: ActivityContext): PopupDataProvider =
        PopupDataProvider(activityContext)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PerDisplayModule {}

@Module
@InstallIn(SingletonComponent::class)
object LauncherConcurrencyModule {
    @Provides
    @LauncherAppSingleton
    @Ui
    fun provideUiExecutor(): LooperExecutor =
        LooperExecutor(Looper.getMainLooper(), Process.THREAD_PRIORITY_DEFAULT)

    @Provides
    @LauncherAppSingleton
    @Ui
    fun provideUiExecutorService(@Ui executor: LooperExecutor): ExecutorService = executor

    @Provides
    @LauncherAppSingleton
    @LightweightBackground(priority = LightweightBackgroundPriority.UI)
    fun provideUiLightweightBackgroundExecutor(): LooperExecutor =
        LooperExecutor("LightweightBackgroundUi", Process.THREAD_PRIORITY_FOREGROUND)

    @Provides
    @LauncherAppSingleton
    @Background
    fun provideBackgroundExecutorService(): ExecutorService = Executors.newCachedThreadPool()
}

/** A dagger module responsible for managing files on the home screen. */
@Module
@InstallIn(SingletonComponent::class)
object HomeScreenFilesModule {
    @Provides
    @LauncherAppSingleton
    fun provideHomeScreenFilesProvider(): HomeScreenFilesProvider = HomeScreenFilesNoOpProvider()
}
