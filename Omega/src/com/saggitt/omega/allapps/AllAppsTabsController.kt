package com.saggitt.omega.allapps

import android.view.View
import android.view.ViewGroup
import com.android.launcher3.BaseDraggingActivity
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.allapps.AllAppsStore
import com.android.launcher3.allapps.BaseAllAppsContainerView
import com.saggitt.omega.util.forEachChildIndexed

class AllAppsTabsController<T : BaseDraggingActivity>(
    val tabs: AllAppsTabs,
    private val container: BaseAllAppsContainerView<T>,
) {

    val tabsCount get() = tabs.count
    val shouldShowTabs get() = tabsCount > 1

    private var holders =
        mutableListOf<BaseAllAppsContainerView<T>.AdapterHolder>()

    private var horizontalPadding = 0
    private var bottomPadding = 0

    fun createHolders(): Array<BaseAllAppsContainerView<T>.AdapterHolder> {
        while (holders.size < tabsCount) holders.add(
            container.createHolder(BaseAllAppsContainerView.AdapterHolder.TYPE_MAIN)/*.apply {
            padding.bottom = bottomPadding
            padding.left = horizontalPadding
            padding.right = horizontalPadding
        }*/
        )
        return holders.toTypedArray()
    }

    fun reloadTabs() {
        tabs.reloadTabs()
    }

    fun registerIconContainers(allAppsStore: AllAppsStore) {
        holders.forEach { allAppsStore.registerIconContainer(it.mRecyclerView) }
    }

    fun unregisterIconContainers(allAppsStore: AllAppsStore) {
        holders.forEach { allAppsStore.unregisterIconContainer(it.mRecyclerView) }
    }

    fun setup(pagedView: AllAppsPagedView) {
        tabs.forEachIndexed { index, tab ->
            if (tab.isWork) holders[index].setType(BaseAllAppsContainerView.AdapterHolder.TYPE_WORK)
            holders[index].setup(pagedView.getChildAt(index), tab.matcher)
        }
    }

    fun setup(view: View) {
        holders.forEach { it.mRecyclerView = null }
        holders.getOrNull(0)?.setup(view, null)
    }

    fun bindButtons(buttonsContainer: ViewGroup, pagedView: AllAppsPagedView) {
        buttonsContainer.forEachChildIndexed { view, i ->
            view.setOnClickListener { pagedView.snapToPage(i) }
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