package com.saggitt.omega.search

import android.content.Context
import androidx.lifecycle.asLiveData
import com.android.launcher3.LauncherAppState
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm
import com.android.launcher3.model.AllAppsList
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.search.SearchCallback
import com.android.launcher3.search.StringMatcherUtility
import com.neoapps.neolauncher.nLauncher
import com.saggitt.omega.util.prefs
import com.neoapps.neolauncher.search.SearchProviderController
import com.saggitt.omega.preferences.NeoPrefs
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio
import java.util.Locale

class NeoAppSearchAlgorithm(val context: Context) : DefaultAppSearchAlgorithm(context) {

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
        mAppState.model.enqueueModelUpdateTask(object : BaseModelUpdateTask() {
            override fun execute(app: LauncherAppState, dataModel: BgDataModel, apps: AllAppsList) {
                val result = getSearchResult(apps.data, query)
                var suggestions = emptyList<String>()

                /*if (prefs.searchContacts.onGetValue()) { TODO
                    val repository = PeopleRepository.INSTANCE.get(app.context)
                    val contacts = repository.findPeople(query)
                    val total = result.size
                    var position = total + 1
                    if (contacts.isNotEmpty()) {
                        result.add(AdapterItem.asAllAppsDivider(position))
                        position++
                        result.add(
                            AdapterItem.asSectionHeader(
                                position,
                                context.getString(R.string.section_contacts)
                            )
                        )
                        position++
                        contacts.forEach {
                            result.add(AdapterItem.asContact(position, it))
                            position++
                        }
                    }
                }*/

                mResultHandler.post {
                    callback?.onSearchResult(
                        query,
                        result,
                        suggestions
                    )
                }

                /*if (callback!!.showWebResult()) { TODO
                    suggestions = getSuggestions(query)
                    callback.setShowWebResult(false)
                }
                mResultHandler.post {
                    callback.onSearchResult(
                        query,
                        result,
                        suggestions
                    )
                }*/
            }
        })
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
        val mApps = apps
        if (searchHiddenAppsEnable) {
            mApps.clear()
            mApps.addAll(context.nLauncher.allApps)
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
            mApps.addAll(context.nLauncher.allApps)
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

    private fun getSuggestions(query: String): List<String?> {
        if (!NeoPrefs.getInstance().searchGlobal.getValue()) {
            return emptyList<String>()
        }
        val provider = SearchProviderController
            .getInstance(context).activeSearchProvider
        return if (!provider.suggestionUrl.isNullOrEmpty()) {
            provider.getSuggestions(query)
        } else emptyList<String>()
    }
}