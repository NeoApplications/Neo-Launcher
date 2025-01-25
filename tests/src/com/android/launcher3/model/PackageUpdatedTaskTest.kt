/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.launcher3.model

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.platform.test.annotations.EnableFlags
import android.platform.test.flag.junit.SetFlagsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.launcher3.AppFilter
import com.android.launcher3.Flags.FLAG_ENABLE_PRIVATE_SPACE
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherSettings
import com.android.launcher3.icons.IconCache
import com.android.launcher3.model.PackageUpdatedTask.OP_ADD
import com.android.launcher3.model.PackageUpdatedTask.OP_REMOVE
import com.android.launcher3.model.PackageUpdatedTask.OP_SUSPEND
import com.android.launcher3.model.PackageUpdatedTask.OP_UNAVAILABLE
import com.android.launcher3.model.PackageUpdatedTask.OP_UNSUSPEND
import com.android.launcher3.model.PackageUpdatedTask.OP_UPDATE
import com.android.launcher3.model.PackageUpdatedTask.OP_USER_AVAILABILITY_CHANGE
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.util.Executors
import com.android.launcher3.util.LauncherModelHelper
import com.android.launcher3.util.LauncherModelHelper.SandboxModelContext
import com.android.launcher3.util.TestUtil
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class PackageUpdatedTaskTest {

    @get:Rule(order = 0) val setFlagsRule = SetFlagsRule()
    @get:Rule(order = 1) val modelTestRule = ModelTestRule()

    private val mUser = UserHandle(0)
    private val mDataModel: BgDataModel = BgDataModel()
    private val mLauncherModelHelper = LauncherModelHelper()
    private val mContext: SandboxModelContext = spy(mLauncherModelHelper.sandboxContext)
    private val mAppState: LauncherAppState = spy(LauncherAppState.getInstance(mContext))

    private val expectedPackage = "Test.Package"
    private val expectedComponent = ComponentName(expectedPackage, "TestClass")
    private val expectedActivityInfo: LauncherActivityInfo = mock<LauncherActivityInfo>()
    private val expectedWorkspaceItem = spy(WorkspaceItemInfo())

    private val mockIconCache: IconCache = mock()
    private val mockTaskController: ModelTaskController = mock<ModelTaskController>()
    private val mockAppFilter: AppFilter = mock<AppFilter>()
    private val mockApplicationInfo: ApplicationInfo = mock<ApplicationInfo>()
    private val mockActivityInfo: ActivityInfo = mock<ActivityInfo>()

    private lateinit var mAllAppsList: AllAppsList

    @Before
    fun setup() {
        mAllAppsList = spy(AllAppsList(mockIconCache, mockAppFilter))
        mLauncherModelHelper.sandboxContext.spyService(LauncherApps::class.java).apply {
            whenever(getActivityList(expectedPackage, mUser))
                .thenReturn(listOf(expectedActivityInfo))
        }
        whenever(mAppState.iconCache).thenReturn(mockIconCache)
        whenever(mockTaskController.app).thenReturn(mAppState)
        whenever(mockAppFilter.shouldShowApp(expectedComponent)).thenReturn(true)
        mockApplicationInfo.apply {
            uid = 1
            isArchived = false
        }
        mockActivityInfo.isArchived = false
        expectedActivityInfo.apply {
            whenever(applicationInfo).thenReturn(mockApplicationInfo)
            whenever(activityInfo).thenReturn(mockActivityInfo)
            whenever(componentName).thenReturn(expectedComponent)
        }
        expectedWorkspaceItem.apply {
            itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
            container = LauncherSettings.Favorites.CONTAINER_DESKTOP
            user = mUser
            whenever(targetPackage).thenReturn(expectedPackage)
            whenever(targetComponent).thenReturn(expectedComponent)
        }
    }

    @After
    fun tearDown() {
        mLauncherModelHelper.destroy()
    }

    @Test
    fun `OP_ADD triggers model callbacks and adds new items to AllAppsList`() {
        // Given
        val taskUnderTest = PackageUpdatedTask(OP_ADD, mUser, expectedPackage)
        // When
        mDataModel.addItem(mContext, expectedWorkspaceItem, true)
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {
            taskUnderTest.execute(mockTaskController, mDataModel, mAllAppsList)
        }
        mLauncherModelHelper.loadModelSync()
        // Then
        verify(mockIconCache).updateIconsForPkg(expectedPackage, mUser)
        verify(mAllAppsList).addPackage(mContext, expectedPackage, mUser)
        verify(mockTaskController).bindUpdatedWorkspaceItems(listOf(expectedWorkspaceItem))
        verify(mockTaskController).bindUpdatedWidgets(mDataModel)
        assertThat(mAllAppsList.data.firstOrNull()?.componentName)
            .isEqualTo(AppInfo(mContext, expectedActivityInfo, mUser).componentName)
    }

    @Test
    fun `OP_UPDATE triggers model callbacks and updates items in AllAppsList`() {
        // Given
        val taskUnderTest = PackageUpdatedTask(OP_UPDATE, mUser, expectedPackage)
        // When
        mDataModel.addItem(mContext, expectedWorkspaceItem, true)
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {
            taskUnderTest.execute(mockTaskController, mDataModel, mAllAppsList)
        }
        mLauncherModelHelper.loadModelSync()
        // Then
        verify(mockIconCache).updateIconsForPkg(expectedPackage, mUser)
        verify(mAllAppsList).updatePackage(mContext, expectedPackage, mUser)
        verify(mockTaskController).bindUpdatedWorkspaceItems(listOf(expectedWorkspaceItem))
        assertThat(mAllAppsList.data.firstOrNull()?.componentName)
            .isEqualTo(AppInfo(mContext, expectedActivityInfo, mUser).componentName)
    }

    @Test
    fun `OP_REMOVE triggers model callbacks and removes packages and icons`() {
        // Given
        val taskUnderTest = PackageUpdatedTask(OP_REMOVE, mUser, expectedPackage)
        // When
        mDataModel.addItem(mContext, expectedWorkspaceItem, true)
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {
            taskUnderTest.execute(mockTaskController, mDataModel, mAllAppsList)
        }
        mLauncherModelHelper.loadModelSync()
        // Then
        verify(mockIconCache).removeIconsForPkg(expectedPackage, mUser)
        verify(mAllAppsList).removePackage(expectedPackage, mUser)
        verify(mockTaskController).bindUpdatedWorkspaceItems(listOf(expectedWorkspaceItem))
        assertThat(mAllAppsList.data).isEmpty()
    }

    @Test
    fun `OP_UNAVAILABLE triggers model callbacks and removes package from AllAppsList`() {
        // Given
        val taskUnderTest = PackageUpdatedTask(OP_UNAVAILABLE, mUser, expectedPackage)
        // When
        mDataModel.addItem(mContext, expectedWorkspaceItem, true)
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {
            taskUnderTest.execute(mockTaskController, mDataModel, mAllAppsList)
        }
        mLauncherModelHelper.loadModelSync()
        // Then
        verify(mAllAppsList).removePackage(expectedPackage, mUser)
        verify(mockTaskController).bindUpdatedWorkspaceItems(listOf(expectedWorkspaceItem))
        assertThat(mAllAppsList.data).isEmpty()
    }

    @Test
    fun `OP_SUSPEND triggers model callbacks and updates flags in AllAppsList`() {
        // Given
        val taskUnderTest = PackageUpdatedTask(OP_SUSPEND, mUser, expectedPackage)
        // When
        mDataModel.addItem(mContext, expectedWorkspaceItem, true)
        mAllAppsList.add(AppInfo(mContext, expectedActivityInfo, mUser), expectedActivityInfo)
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {
            taskUnderTest.execute(mockTaskController, mDataModel, mAllAppsList)
        }
        mLauncherModelHelper.loadModelSync()
        // Then
        verify(mAllAppsList).updateDisabledFlags(any(), any())
        verify(mockTaskController).bindUpdatedWorkspaceItems(listOf(expectedWorkspaceItem))
        assertThat(mAllAppsList.getAndResetChangeFlag()).isTrue()
    }

    @Test
    fun `OP_UNSUSPEND triggers no callbacks when app not suspended`() {
        // Given
        val taskUnderTest = PackageUpdatedTask(OP_UNSUSPEND, mUser, expectedPackage)
        // When
        mDataModel.addItem(mContext, expectedWorkspaceItem, true)
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {
            taskUnderTest.execute(mockTaskController, mDataModel, mAllAppsList)
        }
        mLauncherModelHelper.loadModelSync()
        // Then
        verify(mAllAppsList).updateDisabledFlags(any(), any())
        verify(mockTaskController).bindUpdatedWorkspaceItems(emptyList())
        assertThat(mAllAppsList.getAndResetChangeFlag()).isFalse()
    }

    @EnableFlags(FLAG_ENABLE_PRIVATE_SPACE)
    @Test
    fun `OP_USER_AVAILABILITY_CHANGE triggers no callbacks if current user not work or private`() {
        // Given
        val taskUnderTest = PackageUpdatedTask(OP_USER_AVAILABILITY_CHANGE, mUser, expectedPackage)
        // When
        mDataModel.addItem(mContext, expectedWorkspaceItem, true)
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {
            taskUnderTest.execute(mockTaskController, mDataModel, mAllAppsList)
        }
        mLauncherModelHelper.loadModelSync()
        // Then
        verify(mAllAppsList).updateDisabledFlags(any(), any())
        verify(mockTaskController).bindUpdatedWorkspaceItems(emptyList())
        assertThat(mAllAppsList.data).isEmpty()
    }
}
