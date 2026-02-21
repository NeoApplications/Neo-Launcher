package com.neoapps.neolauncher.allapps.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.asLiveData
import com.android.launcher3.Launcher
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm
import com.android.launcher3.model.AllAppsList
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.ModelTaskController
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.search.SearchCallback
import com.android.launcher3.search.StringMatcherUtility
import com.neoapps.neolauncher.launcher
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.search.SearchProviderController
import com.neoapps.neolauncher.util.prefs
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio
import java.util.Locale

class NeoAppSearchAlgorithm(val context: Context, addNoResultsMessage: Boolean) :
    DefaultAppSearchAlgorithm(context, addNoResultsMessage) {

    private val prefs = context.prefs
    private var searchHiddenAppsEnable = false

    init {
        prefs.searchHiddenApps.get().asLiveData().observeForever {
            searchHiddenAppsEnable = it
        }
    }

    override fun destroy() {
        super.destroy()
        prefs.searchHiddenApps.get().asLiveData().removeObserver {
            searchHiddenAppsEnable = false
        }
    }

    override fun doSearch(query: String, callback: SearchCallback<AdapterItem>?) {
        Log.d("NeoAppSearchAlgorithm", "doSearch: $query")
        Launcher.getLauncher(context).model.enqueueModelUpdateTask { taskController: ModelTaskController?, dataModel: BgDataModel?, apps: AllAppsList? ->
            val result = getSearchResult(apps!!.data, query)
            var suggestions: ArrayList<String> = arrayListOf()
            if (mAddNoResultsMessage && result.isEmpty()) {
                result.add(getEmptyMessageAdapterItem(query))
            }
            mResultHandler.post(Runnable { callback!!.onSearchResult(query, result, suggestions) })
            if (callback!!.showWebResults()) {
                suggestions = getSuggestions(query)
                callback.setShowWebResults(false)
            }
            mResultHandler.post(Runnable { callback.onSearchResult(query, result, suggestions) })
        }
    }

    private fun getSearchResult(apps: MutableList<AppInfo>, query: String): ArrayList<AdapterItem> {
        return if (prefs.searchFuzzy.getValue()) {
            getFuzzySearchResult(apps, query)
        } else {
            getTitleMatchResultKT(apps, query)
        }
    }

    private fun getFuzzySearchResult(
        apps: MutableList<AppInfo>,
        query: String,
    ): ArrayList<AdapterItem> {
        val result = ArrayList<AdapterItem>()
        if (searchHiddenAppsEnable) {
            apps.clear()
            apps.addAll(context.launcher.allApps)
        }

        val matcher = FuzzySearch.extractSorted(
            query.lowercase(Locale.getDefault()), apps,
            { it!!.title.toString() }, WeightedRatio(), 65
        )
        var resultCount = 0
        val total = matcher.size
        var i = 0
        while (i < total && resultCount < MAX_RESULTS_COUNT) {
            val info = matcher[i].referent
            val appItem = AdapterItem.asApp(info)
            result.add(appItem)
            resultCount++
            i++
        }

        return result
    }


    private fun getTitleMatchResultKT(
        apps: MutableList<AppInfo>,
        query: String?,
    ): ArrayList<AdapterItem> {

        // Do an intersection of the words in the query and each title, and filter out all the
        // apps that don't match all of the words in the query.
        val queryTextLower = query!!.lowercase(Locale.getDefault())
        val result = ArrayList<AdapterItem>()

        val matcher = StringMatcherUtility.StringMatcher.getInstance()

        var resultCount = 0
        val mApps = apps
        if (searchHiddenAppsEnable) {
            mApps.clear()
            mApps.addAll(context.launcher.allApps)
        }

        var i = 0

        val total = mApps.size
        while (i < total && resultCount < MAX_RESULTS_COUNT) {
            val info = mApps[i]
            if (StringMatcherUtility.matches(queryTextLower, info.title.toString(), matcher)) {
                val appItem = AdapterItem.asApp(info)
                result.add(appItem)
                resultCount++
            }

            i++
        }
        return result
    }

    private fun getSuggestions(query: String): ArrayList<String> {
        if (!NeoPrefs.getInstance().searchGlobal.getValue()) {
            return arrayListOf()
        }
        val provider = SearchProviderController
            .getInstance(context).activeSearchProvider
        return if (!provider.suggestionUrl.isNullOrEmpty()) {
            provider.getSuggestions(query)
        } else arrayListOf()
    }
}