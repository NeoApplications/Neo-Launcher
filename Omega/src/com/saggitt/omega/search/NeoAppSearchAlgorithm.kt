package com.saggitt.omega.search

import android.content.Context
import com.android.launcher3.LauncherAppState
import com.android.launcher3.Utilities
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm
import com.android.launcher3.model.AllAppsList
import com.android.launcher3.model.BaseModelUpdateTask
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.search.SearchCallback
import com.android.launcher3.search.StringMatcherUtility
import com.saggitt.omega.nLauncher
import com.saggitt.omega.util.prefs
import java.util.Locale

class NeoAppSearchAlgorithm(val context: Context) : DefaultAppSearchAlgorithm(context) {

    private val prefs = context.prefs

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
        return getTitleMatchResult(apps, query)
        /*if (prefs.searchFuzzy.getValue()) { TODO
            getFuzzySearchResult(apps, query)
        } else {
            getTitleMatchResult(apps, query)
        }*/
    }

    private fun getFuzzySearchResult(apps: List<AppInfo>, query: String): ArrayList<AdapterItem> {
        val result = ArrayList<AdapterItem>()
        val mApps = if (prefs.searchHiddenApps.getValue()) {
            context.nLauncher.allApps
        } else {
            apps
        }

        /*val matcher = FuzzySearch.extractSorted( TODO
            query.lowercase(Locale.getDefault()), mApps,
            { it!!.title.toString() }, WeightedRatio(), 65
        )
        var resultCount = 0
        val total = matcher.size
        var i = 0
        while (i < total && resultCount < DefaultAppSearchAlgorithm.MAX_RESULTS_COUNT) {
            val info = matcher!![i]
            val appItem = AdapterItem.asApp(resultCount, "", info.referent, resultCount)
            result.add(appItem)
            resultCount++
            i++
        }*/

        return result
    }

    override fun getTitleMatchResult(
        apps: MutableList<AppInfo>,
        query: String?,
    ): ArrayList<AdapterItem> {

        // Do an intersection of the words in the query and each title, and filter out all the
        // apps that don't match all of the words in the query.
        val queryTextLower = query!!.lowercase(Locale.getDefault())
        val result = ArrayList<AdapterItem>()

        val matcher = StringMatcherUtility.StringMatcher.getInstance()

        var resultCount = 0
        val total = apps.size
        val mApps = if (prefs.searchHiddenApps.getValue()) {
            context.nLauncher.allApps
        } else {
            apps
        }
        var i = 0

        while (i < total && resultCount < DefaultAppSearchAlgorithm.MAX_RESULTS_COUNT) {
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
        if (!Utilities.getOmegaPrefs(context).searchGlobal.getValue()) {
            return emptyList<String>()
        }
        val provider = SearchProviderController
            .getInstance(context).searchProvider
        return if (provider is WebSearchProvider) {
            provider.getSuggestions(query)
        } else emptyList<String>()
    }
}