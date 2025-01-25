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

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageInstaller
import android.content.pm.ShortcutInfo
import android.os.UserHandle
import android.platform.test.annotations.EnableFlags
import android.util.LongSparseArray
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.launcher3.Flags.FLAG_ENABLE_SUPPORT_FOR_ARCHIVING
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherSettings.Favorites
import com.android.launcher3.LauncherSettings.Favorites.CONTAINER_DESKTOP
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.IconRequestInfo
import com.android.launcher3.model.data.LauncherAppWidgetInfo
import com.android.launcher3.model.data.LauncherAppWidgetInfo.FLAG_RESTORE_STARTED
import com.android.launcher3.model.data.LauncherAppWidgetInfo.FLAG_UI_NOT_READY
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.shortcuts.ShortcutKey
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.PackageManagerHelper
import com.android.launcher3.util.PackageUserKey
import com.android.launcher3.util.UserIconInfo
import com.android.launcher3.widget.WidgetInflater
import com.android.launcher3.widget.WidgetSections
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

@RunWith(AndroidJUnit4::class)
class WorkspaceItemProcessorExtraTest {

    @Mock private lateinit var mockIconRequestInfo: IconRequestInfo<WorkspaceItemInfo>
    @Mock private lateinit var mockWorkspaceInfo: WorkspaceItemInfo
    @Mock private lateinit var mockBgDataModel: BgDataModel
    @Mock private lateinit var mockContext: Context
    @Mock private lateinit var mockAppState: LauncherAppState
    @Mock private lateinit var mockPmHelper: PackageManagerHelper
    @Mock private lateinit var mockLauncherApps: LauncherApps
    @Mock private lateinit var mockCursor: LoaderCursor
    @Mock private lateinit var mockUserCache: UserCache
    @Mock private lateinit var mockUserManagerState: UserManagerState
    @Mock private lateinit var mockWidgetInflater: WidgetInflater

    private var intent: Intent = Intent()
    private var mUserHandle: UserHandle = UserHandle(0)
    private var mIconRequestInfos: MutableList<IconRequestInfo<WorkspaceItemInfo>> = mutableListOf()
    private var mComponentName: ComponentName = ComponentName("package", "class")
    private var mUnlockedUsersArray: LongSparseArray<Boolean> = LongSparseArray()
    private var mKeyToPinnedShortcutsMap: MutableMap<ShortcutKey, ShortcutInfo> = mutableMapOf()
    private var mInstallingPkgs: HashMap<PackageUserKey, PackageInstaller.SessionInfo> = hashMapOf()
    private var mAllDeepShortcuts: MutableList<ShortcutInfo> = mutableListOf()
    private var mWidgetProvidersMap: MutableMap<ComponentKey, AppWidgetProviderInfo?> =
        mutableMapOf()
    private var mPendingPackages: MutableSet<PackageUserKey> = mutableSetOf()

    private lateinit var itemProcessorUnderTest: WorkspaceItemProcessor

    @Before
    fun setup() {
        mUserHandle = UserHandle(0)
        mockIconRequestInfo = mock<IconRequestInfo<WorkspaceItemInfo>>()
        mockWorkspaceInfo = mock<WorkspaceItemInfo>()
        mockBgDataModel = mock<BgDataModel>()
        mComponentName = ComponentName("package", "class")
        mUnlockedUsersArray = LongSparseArray<Boolean>(1).apply { put(101, true) }
        intent =
            Intent().apply {
                component = mComponentName
                `package` = "pkg"
                putExtra(ShortcutKey.EXTRA_SHORTCUT_ID, "")
            }
        mockContext =
            mock<Context>().apply {
                whenever(packageManager).thenReturn(mock())
                whenever(packageManager.getUserBadgedLabel(any(), any())).thenReturn("")
                whenever(applicationContext).thenReturn(ApplicationProvider.getApplicationContext())
            }
        mockAppState =
            mock<LauncherAppState>().apply {
                whenever(context).thenReturn(mockContext)
                whenever(iconCache).thenReturn(mock())
                whenever(iconCache.getShortcutIcon(any(), any(), any())).then {}
            }
        mockPmHelper =
            mock<PackageManagerHelper>().apply {
                whenever(getAppLaunchIntent(mComponentName.packageName, mUserHandle))
                    .thenReturn(intent)
            }
        mockLauncherApps =
            mock<LauncherApps>().apply {
                whenever(isPackageEnabled("package", mUserHandle)).thenReturn(true)
                whenever(isActivityEnabled(mComponentName, mUserHandle)).thenReturn(true)
            }
        mockCursor =
            Mockito.mock(LoaderCursor::class.java, RETURNS_DEEP_STUBS).apply {
                user = mUserHandle
                itemType = ITEM_TYPE_APPLICATION
                id = 1
                restoreFlag = 1
                serialNumber = 101
                whenever(parseIntent()).thenReturn(intent)
                whenever(markRestored()).doAnswer { restoreFlag = 0 }
                whenever(updater().put(Favorites.INTENT, intent.toUri(0)).commit()).thenReturn(1)
                whenever(getAppShortcutInfo(any(), any(), any(), any()))
                    .thenReturn(mockWorkspaceInfo)
                whenever(createIconRequestInfo(any(), any())).thenReturn(mockIconRequestInfo)
            }
        mockUserCache =
            mock<UserCache>().apply {
                val userIconInfo =
                    mock<UserIconInfo>().apply { whenever(isPrivate).thenReturn(false) }
                whenever(getUserInfo(any())).thenReturn(userIconInfo)
            }

        mockUserManagerState = mock<UserManagerState>()
        mockWidgetInflater = mock<WidgetInflater>()
        mKeyToPinnedShortcutsMap = mutableMapOf()
        mInstallingPkgs = hashMapOf()
        mAllDeepShortcuts = mutableListOf()
        mWidgetProvidersMap = mutableMapOf()
        mIconRequestInfos = mutableListOf()
        mPendingPackages = mutableSetOf()
    }

    @Test
    fun `When Pending App Widget has not started restore then update db and add item`() {

        val mockitoSession =
            ExtendedMockito.mockitoSession()
                .strictness(Strictness.LENIENT)
                .mockStatic(WidgetSections::class.java)
                .startMocking()
        try {
            // Given
            val expectedProvider = "com.google.android.testApp/com.android.testApp.testAppProvider"
            val expectedComponentName =
                ComponentName.unflattenFromString(expectedProvider)!!.flattenToString()
            val expectedRestoreStatus = FLAG_UI_NOT_READY or FLAG_RESTORE_STARTED
            val expectedAppWidgetId = 0
            mockCursor.apply {
                itemType = ITEM_TYPE_APPWIDGET
                user = mUserHandle
                restoreFlag = FLAG_UI_NOT_READY
                container = CONTAINER_DESKTOP
                whenever(isOnWorkspaceOrHotseat).thenCallRealMethod()
                whenever(appWidgetProvider).thenReturn(expectedProvider)
                whenever(appWidgetId).thenReturn(expectedAppWidgetId)
                whenever(spanX).thenReturn(2)
                whenever(spanY).thenReturn(1)
                whenever(options).thenReturn(0)
                whenever(appWidgetSource).thenReturn(20)
                whenever(applyCommonProperties(any())).thenCallRealMethod()
                whenever(
                        updater()
                            .put(Favorites.APPWIDGET_PROVIDER, expectedComponentName)
                            .put(Favorites.APPWIDGET_ID, expectedAppWidgetId)
                            .put(Favorites.RESTORED, expectedRestoreStatus)
                            .commit()
                    )
                    .thenReturn(1)
            }
            val inflationResult =
                WidgetInflater.InflationResult(
                    type = WidgetInflater.TYPE_PENDING,
                    widgetInfo = null
                )
            mockWidgetInflater =
                mock<WidgetInflater>().apply {
                    whenever(inflateAppWidget(any())).thenReturn(inflationResult)
                }
            val packageUserKey = PackageUserKey("com.google.android.testApp", mUserHandle)
            mInstallingPkgs[packageUserKey] = PackageInstaller.SessionInfo()

            // When
            itemProcessorUnderTest =
                createWorkspaceItemProcessorUnderTest(widgetProvidersMap = mWidgetProvidersMap)
            itemProcessorUnderTest.processItem()

            // Then
            val expectedWidgetInfo =
                LauncherAppWidgetInfo().apply {
                    appWidgetId = expectedAppWidgetId
                    providerName = ComponentName.unflattenFromString(expectedProvider)
                    restoreStatus = expectedRestoreStatus
                }
            verify(
                    mockCursor
                        .updater()
                        .put(Favorites.APPWIDGET_PROVIDER, expectedProvider)
                        .put(Favorites.APPWIDGET_ID, expectedAppWidgetId)
                        .put(Favorites.RESTORED, expectedRestoreStatus)
                )
                .commit()
            val widgetInfoCaptor = ArgumentCaptor.forClass(LauncherAppWidgetInfo::class.java)
            verify(mockCursor).checkAndAddItem(widgetInfoCaptor.capture(), eq(mockBgDataModel))
            val actualWidgetInfo = widgetInfoCaptor.value
            with(actualWidgetInfo) {
                assertThat(providerName).isEqualTo(expectedWidgetInfo.providerName)
                assertThat(restoreStatus).isEqualTo(expectedWidgetInfo.restoreStatus)
                assertThat(targetComponent).isEqualTo(expectedWidgetInfo.targetComponent)
                assertThat(appWidgetId).isEqualTo(expectedWidgetInfo.appWidgetId)
            }
        } finally {
            mockitoSession.finishMocking()
        }
    }

    @Test
    @EnableFlags(FLAG_ENABLE_SUPPORT_FOR_ARCHIVING)
    fun `When Archived Pending App Widget then checkAndAddItem`() {
        val mockitoSession =
            ExtendedMockito.mockitoSession().mockStatic(Utilities::class.java).startMocking()
        try {
            // Given
            val expectedProvider = "com.google.android.testApp/com.android.testApp.testAppProvider"
            val expectedComponentName = ComponentName.unflattenFromString(expectedProvider)
            val expectedPackage = expectedComponentName!!.packageName
            mockPmHelper =
                mock<PackageManagerHelper>().apply {
                    whenever(isAppArchived(expectedPackage)).thenReturn(true)
                }
            mockCursor =
                mock<LoaderCursor>().apply {
                    itemType = ITEM_TYPE_APPWIDGET
                    id = 1
                    user = UserHandle(1)
                    restoreFlag = FLAG_UI_NOT_READY
                    container = CONTAINER_DESKTOP
                    whenever(isOnWorkspaceOrHotseat).thenCallRealMethod()
                    whenever(appWidgetProvider).thenReturn(expectedProvider)
                    whenever(appWidgetId).thenReturn(0)
                    whenever(spanX).thenReturn(2)
                    whenever(spanY).thenReturn(1)
                    whenever(options).thenReturn(0)
                    whenever(appWidgetSource).thenReturn(20)
                    whenever(applyCommonProperties(any())).thenCallRealMethod()
                }
            mInstallingPkgs = hashMapOf()
            val inflationResult =
                WidgetInflater.InflationResult(
                    type = WidgetInflater.TYPE_PENDING,
                    widgetInfo = null
                )
            mockWidgetInflater =
                mock<WidgetInflater>().apply {
                    whenever(inflateAppWidget(any())).thenReturn(inflationResult)
                }
            itemProcessorUnderTest =
                createWorkspaceItemProcessorUnderTest(widgetProvidersMap = mWidgetProvidersMap)

            // When
            itemProcessorUnderTest.processItem()

            // Then
            verify(mockCursor).checkAndAddItem(any(), any())
        } finally {
            mockitoSession.finishMocking()
        }
    }

    private fun createWorkspaceItemProcessorUnderTest(
        cursor: LoaderCursor = mockCursor,
        memoryLogger: LoaderMemoryLogger? = null,
        userCache: UserCache = mockUserCache,
        userManagerState: UserManagerState = mockUserManagerState,
        launcherApps: LauncherApps = mockLauncherApps,
        shortcutKeyToPinnedShortcuts: Map<ShortcutKey, ShortcutInfo> = mKeyToPinnedShortcutsMap,
        app: LauncherAppState = mockAppState,
        bgDataModel: BgDataModel = mockBgDataModel,
        widgetProvidersMap: MutableMap<ComponentKey, AppWidgetProviderInfo?> = mWidgetProvidersMap,
        widgetInflater: WidgetInflater = mockWidgetInflater,
        pmHelper: PackageManagerHelper = mockPmHelper,
        iconRequestInfos: MutableList<IconRequestInfo<WorkspaceItemInfo>> = mIconRequestInfos,
        isSdCardReady: Boolean = false,
        pendingPackages: MutableSet<PackageUserKey> = mPendingPackages,
        unlockedUsers: LongSparseArray<Boolean> = mUnlockedUsersArray,
        installingPkgs: HashMap<PackageUserKey, PackageInstaller.SessionInfo> = mInstallingPkgs,
        allDeepShortcuts: MutableList<ShortcutInfo> = mAllDeepShortcuts
    ) =
        WorkspaceItemProcessor(
            c = cursor,
            memoryLogger = memoryLogger,
            userCache = userCache,
            userManagerState = userManagerState,
            launcherApps = launcherApps,
            app = app,
            bgDataModel = bgDataModel,
            widgetProvidersMap = widgetProvidersMap,
            widgetInflater = widgetInflater,
            pmHelper = pmHelper,
            unlockedUsers = unlockedUsers,
            iconRequestInfos = iconRequestInfos,
            pendingPackages = pendingPackages,
            isSdCardReady = isSdCardReady,
            shortcutKeyToPinnedShortcuts = shortcutKeyToPinnedShortcuts,
            installingPkgs = installingPkgs,
            allDeepShortcuts = allDeepShortcuts
        )
}
