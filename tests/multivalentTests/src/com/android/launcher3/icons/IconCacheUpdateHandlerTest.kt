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

package com.android.launcher3.icons

import android.content.ComponentName
import android.content.pm.PackageInfo
import android.database.Cursor
import android.os.UserHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.launcher3.icons.cache.BaseIconCache
import com.android.launcher3.icons.cache.CachingLogic
import com.android.launcher3.icons.cache.IconCacheUpdateHandler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn

@SmallTest
@RunWith(AndroidJUnit4::class)
class IconCacheUpdateHandlerTest {

    @Mock private lateinit var cursor: Cursor
    @Mock private lateinit var user: UserHandle
    @Mock private lateinit var cachingLogic: CachingLogic<String>
    @Mock private lateinit var baseIconCache: BaseIconCache

    private var componentMap: HashMap<ComponentName, String> = hashMapOf()
    private var ignorePackages: Set<String> = setOf()
    private var packageInfoMap: HashMap<String, PackageInfo> = hashMapOf()

    private val dummyRowData =
        IconCacheRowData(
            "com.android.fake/.FakeActivity",
            System.currentTimeMillis(),
            1,
            1.0.toLong(),
            "stateOfConfusion"
        )

    @Before
    fun setup() {

        MockitoAnnotations.initMocks(this)
        // Load in a specific row to the database
        doReturn(0).`when`(cursor).getColumnIndex(BaseIconCache.IconDB.COLUMN_COMPONENT)
        doReturn(1).`when`(cursor).getColumnIndex(BaseIconCache.IconDB.COLUMN_LAST_UPDATED)
        doReturn(2).`when`(cursor).getColumnIndex(BaseIconCache.IconDB.COLUMN_VERSION)
        doReturn(3).`when`(cursor).getColumnIndex(BaseIconCache.IconDB.COLUMN_ROWID)
        doReturn(4).`when`(cursor).getColumnIndex(BaseIconCache.IconDB.COLUMN_SYSTEM_STATE)
        doReturn(dummyRowData.component).`when`(cursor).getString(0)
        doReturn(dummyRowData.lastUpdated).`when`(cursor).getLong(1)
        doReturn(dummyRowData.version).`when`(cursor).getInt(2)
        doReturn(dummyRowData.row).`when`(cursor).getLong(3)
        doReturn(dummyRowData.systemState).`when`(cursor).getString(4)
    }

    @Test
    fun `IconCacheUpdateHandler returns null if the component name is malformed`() {
        val updateHandlerUnderTest = IconCacheUpdateHandler(packageInfoMap, baseIconCache)

        val result =
            updateHandlerUnderTest.updateOrDeleteIcon(
                cursor,
                componentMap,
                ignorePackages,
                user,
                cachingLogic
            )

        assert(result == null)
    }
}

data class IconCacheRowData(
    val component: String,
    val lastUpdated: Long,
    val version: Int,
    val row: Long,
    val systemState: String
)
