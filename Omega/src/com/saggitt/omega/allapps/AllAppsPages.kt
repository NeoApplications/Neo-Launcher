package com.saggitt.omega.allapps

import android.content.Context
import android.content.pm.LauncherActivityInfo
import com.android.launcher3.AppFilter
import com.android.launcher3.LauncherAppState
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.groups.CustomFilter
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.getAllAppsComparator
import com.saggitt.omega.util.prefs

class AllAppsPages(
    val context: Context,
) : Iterable<AllAppsPages.Page> {
    var pages = ArrayList<Page>()
    var count = 1
    private val idp = LauncherAppState.getIDP(context)
    private var mNumRows = idp.numRows // TODO change to allApps Row instead of desktop row
    private var mNumColumns = idp.numAllAppsColumns
    private var activityInfoList = listOf<LauncherActivityInfo>()
    private var appList = ArrayList<AppInfo>()
    private val config = Config(context)

    init {
        activityInfoList = config.getAppsList(AppFilter())

        activityInfoList.forEach {
            appList.add(AppInfo(it, it.user, false))
        }
        reloadPages()
    }

    private fun reloadPages() {
        pages.clear()
        val appsPerPage = mNumColumns * mNumRows
        var pageCount = (appList.size / appsPerPage) + if (appList.size % appsPerPage <= 0) 0 else 1
        if (pageCount == 0) {
            pageCount++
        }

        appList.sortWith(getAllAppsComparator(context, context.prefs.drawerSortMode.getValue()))
        var initialApp = 0
        var endApp = appsPerPage
        for (page in 0 until pageCount) {
            if (endApp > appList.lastIndex) {
                endApp = appList.lastIndex + 1
            }

            val addedApps = HashSet<ComponentKey>()
            for (appIndex in initialApp until endApp) {
                addedApps.add(appList[appIndex].toComponentKey())
            }

            pages.add(Page(false, CustomFilter(context, addedApps)))
            initialApp = ((page + 1) * appsPerPage)
            endApp = initialApp + appsPerPage
        }
        count = pages.size
    }

    override fun iterator(): Iterator<Page> {
        return pages.iterator()
    }

    operator fun get(index: Int) = pages[index]

    class Page(
        val isWork: Boolean = false,
        val filter: CustomFilter,
    )
}