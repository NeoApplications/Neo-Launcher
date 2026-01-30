package com.neoapps.neolauncher.data.models

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.launcher3.R
import com.neoapps.neolauncher.util.toArrayList
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

@Entity
data class SearchProvider(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,
    val name: String,
    @DrawableRes val iconId: Int,
    val searchUrl: String, // first %s for search term, second %s for language
    val suggestionUrl: String?, // first %s for search term, second %s for language
    val enabled: Boolean,
    val order: Int,
) {

    fun getSuggestions(query: String): ArrayList<String> {
        val client = OkHttpClient()
        if (suggestionUrl == null) return arrayListOf()
        if (query.isEmpty()) return arrayListOf()
        val request = Request.Builder()
            .url(suggestionUrl.format(query))
            .build()
        try {
            val response = client.newCall(request).execute()
            val result = JSONArray(response.body.string())
                .getJSONArray(1)
                .toArrayList<String>()
                .take(MAX_SUGGESTIONS)
            response.close()
            Log.d("WebSearchProvider", "Websearch Query: $query")
            return result as ArrayList
        } catch (ex: Exception) {
            Log.e("WebSearchProvider", ex.message ?: "", ex)
        }

        return arrayListOf()
    }

    companion object {
        const val MAX_SUGGESTIONS = 5

        fun offlineSearchProvider(context: Context) = SearchProvider(
            id = 1,
            name = context.getString(R.string.search_provider_appsearch),
            iconId = R.drawable.ic_search,
            searchUrl = "",
            suggestionUrl = "",
            enabled = true,
            order = -1,
        )

        private fun defaultProvider(
            name: String,
            iconId: Int,
            searchUrl: String,
            suggestionUrl: String?,
        ) = SearchProvider(
            id = 0,
            name = name,
            iconId = iconId,
            searchUrl = searchUrl,
            suggestionUrl = suggestionUrl,
            enabled = false,
            order = -1,
        )

        private val ALTERNATIVE_TO = defaultProvider(
            name = "AlternativeTo",
            iconId = R.drawable.ic_alternative_to,
            searchUrl = "https://alternativeto.net/browse/search/?q=%s",
            suggestionUrl = null,
        )

        private val BAIDU = defaultProvider(
            name = "Baidu",
            iconId = R.drawable.ic_baidu,
            searchUrl = "https://www.baidu.com/s?wd=%s",
            suggestionUrl = "https://m.baidu.com/su?action=opensearch&ie=UTF-8&wd=%s",
        )

        private val BING = defaultProvider(
            name = "Bing",
            iconId = R.drawable.ic_bing,
            searchUrl = "https://www.bing.com/search?q=%s",
            suggestionUrl = "https://www.bing.com/osjson.aspx?query=%s",
        )

        private val BRAVE = defaultProvider(
            name = "Brave",
            iconId = R.drawable.ic_brave,
            searchUrl = "https://search.brave.com/search?q=%s",
            suggestionUrl = "https://search.brave.com/api/suggest?q=%s&rich=false",
        )

        private val DUCKDUCKGO = defaultProvider(
            name = "DuckDuckGo",
            iconId = R.drawable.ic_ddg,
            searchUrl = "https://duckduckgo.com/?q=%s",
            suggestionUrl = "https://ac.duckduckgo.com/ac/?q=%s&type=list",
        )

        private val ECOSIA = defaultProvider(
            name = "Ecosia",
            iconId = R.drawable.ic_ecosia,
            searchUrl = "https://www.ecosia.org/search?q=%s",
            suggestionUrl = "https://ac.ecosia.org/autocomplete?q=%s&type=list",
        )

        private val GOOGLE = defaultProvider(
            name = "Google",
            iconId = R.drawable.ic_super_g_color,
            searchUrl = "https://www.google.com/search?q=%s",
            suggestionUrl = "https://www.google.com/complete/search?client=chrome&q=%s",
        )

        private val METAGER_ORG = defaultProvider(
            name = "Metager (English)",
            iconId = R.drawable.ic_metager_search,
            searchUrl = "https://metager.org/meta/meta.ger3?eingabe=%s",
            suggestionUrl = null,
        )

        private val METAGER_DE = defaultProvider(
            name = "Metager (German)",
            iconId = R.drawable.ic_metager_search,
            searchUrl = "https://metager.de/meta/meta.ger3?eingabe=%s",
            suggestionUrl = null,
        )

        private val METAGER_ES = defaultProvider(
            name = "Metager (Spanish)",
            iconId = R.drawable.ic_metager_search,
            searchUrl = "https://metager.es/meta/meta.ger3?eingabe=%s",
            suggestionUrl = null,
        )

        private val OPENVERSE = defaultProvider(
            name = "Openverse",
            iconId = R.drawable.ic_openverse,
            searchUrl = "https://openverse.org/search/?q=%s",
            suggestionUrl = null,
        )

        private val PERPLEXITY = defaultProvider(
            name = "Perplexity",
            iconId = R.drawable.ic_perplexity,
            searchUrl = "https://www.perplexity.ai/search?q=%s",
            suggestionUrl = null,
        )

        private val QWANT = defaultProvider(
            name = "Qwant",
            iconId = R.drawable.ic_qwant,
            searchUrl = "https://www.qwant.com/?q=%s",
            suggestionUrl = "https://api.qwant.com/api/suggest/?q=%s&client=opensearch&lang=en"
        )

        private val REDDIT = defaultProvider(
            name = "Reddit",
            iconId = R.drawable.ic_reddit,
            searchUrl = "https://www.reddit.com/search/?q=%s",
            suggestionUrl = null,
        )

        private val SEARX_INFO = defaultProvider(
            name = "Searx.info",
            iconId = R.drawable.ic_searx_search,
            searchUrl = "https://searxng.site/search?q=%s",
            suggestionUrl = "https://searxng.site/autocompleter?q=%s",
        )

        private val STARTPAGE = defaultProvider(
            name = "Startpage",
            iconId = R.drawable.ic_startpage_search,
            searchUrl = "https://www.startpage.com/search?q=%s",
            suggestionUrl = "https://www.startpage.com/suggestions?q=%s&limit=$MAX_SUGGESTIONS&format=json",
        )

        private val SWISSCOWS = defaultProvider(
            name = "Swisscows",
            iconId = R.drawable.ic_swisscows,
            searchUrl = "https://swisscows.com/en/web?query=%s",
            suggestionUrl = "https://swisscows.com/api/suggest?query=%s&itemsCount=$MAX_SUGGESTIONS",
        )

        private val WOLFRAM_ALPHA = defaultProvider(
            name = "Wolfram Alpha",
            iconId = R.drawable.ic_wolfram_alpha,
            searchUrl = "https://www.wolframalpha.com/input?i==%s",
            suggestionUrl = null,
        )

        private val YAHOO = defaultProvider(
            name = "Yahoo",
            iconId = R.drawable.ic_yahoo,
            searchUrl = "https://search.yahoo.com/search?q=%s",
            suggestionUrl = "https://ff.search.yahoo.com/gossip?output=fxjson&command=%s",
        )

        private val YANDEX = defaultProvider(
            name = "Yandex",
            iconId = R.drawable.ic_yandex,
            searchUrl = "https://yandex.ru/search/?text=%s",
            suggestionUrl = "https://suggest.yandex.com/suggest-ff.cgi?part=%s",
        )

        private val YOU = defaultProvider(
            name = "You",
            iconId = R.drawable.ic_you_com,
            searchUrl = "https://you.com/search?q=%s",
            suggestionUrl = null,
        )

        val defaultProviders = listOf(
            ALTERNATIVE_TO, BAIDU, BING, BRAVE, DUCKDUCKGO,
            ECOSIA, GOOGLE, METAGER_ORG, METAGER_DE, METAGER_ES,
            OPENVERSE, PERPLEXITY, QWANT, REDDIT, SEARX_INFO,
            STARTPAGE, SWISSCOWS, WOLFRAM_ALPHA, YAHOO, YANDEX, YOU,
        )

        val addedProvidersV6 = listOf(
            ALTERNATIVE_TO, OPENVERSE, REDDIT, SWISSCOWS
        )
    }
}