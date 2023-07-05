package com.saggitt.omega.allapps

import com.android.launcher3.BaseDraggingActivity
import com.android.launcher3.allapps.ActivityAllAppsContainerView
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.allapps.AllAppsStore

class AllAppsPagesController<T : BaseDraggingActivity>(
    val pages: AllAppsPages,
    private val container: ActivityAllAppsContainerView<T>,
) {
    private var holders =
        mutableListOf<ActivityAllAppsContainerView<T>.AdapterHolder>()
    val pagesCount get() = pages.count

    private var horizontalPadding = 0
    private var bottomPadding = 0

    fun createHolders(): Array<ActivityAllAppsContainerView<T>.AdapterHolder> {
        while (holders.size < pagesCount) {
            /*holders.add(
                container.createHolder(ActivityAllAppsContainerView.AdapterHolder.MAIN).apply {
                /*padding.bottom = bottomPadding
                padding.left = horizontalPadding
                padding.right = horizontalPadding*/
            }
            )*/
        }
        return holders.toTypedArray()
    }

    fun registerIconContainers(allAppsStore: AllAppsStore) {
        //holders.forEach { allAppsStore.registerIconContainer(it.mRecyclerView) }
    }

    fun unregisterIconContainers(allAppsStore: AllAppsStore) {
        //holders.forEach { allAppsStore.unregisterIconContainer(it.mRecyclerView) }
    }

    fun setup(pagedView: AllAppsPagedView) {
        pages.forEachIndexed { index, page ->
            /*if (page.isWork) holders[index].setType(ActivityAllAppsContainerView.AdapterHolder.WORK)
            holders[index].setup(pagedView.getChildAt(index), page.filter.matcher)*/
        }
    }

    fun setPadding(horizontal: Int, bottom: Int) {
        horizontalPadding = horizontal
        bottomPadding = bottom

        /*holders.forEach {
            it.padding.bottom = bottomPadding
            it.padding.left = horizontalPadding
            it.padding.right = horizontalPadding
            it.applyPadding()
        }*/
    }
}