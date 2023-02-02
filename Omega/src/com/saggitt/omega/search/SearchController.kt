package com.saggitt.omega.search

import android.content.Context
import com.saggitt.omega.search.provider.AppsSearchProvider
import com.saggitt.omega.search.provider.BaiduSearchProvider
import com.saggitt.omega.search.provider.BingSearchProvider
import com.saggitt.omega.search.provider.CoolSearchSearchProvider
import com.saggitt.omega.search.provider.DuckDuckGoSearchProvider
import com.saggitt.omega.search.provider.EdgeSearchProvider
import com.saggitt.omega.search.provider.FirefoxSearchProvider
import com.saggitt.omega.search.provider.QwantSearchProvider
import com.saggitt.omega.search.provider.SFinderSearchProvider
import com.saggitt.omega.search.provider.SearchLiteSearchProvider
import com.saggitt.omega.search.provider.YandexSearchProvider
import com.saggitt.omega.search.webprovider.BaiduWebSearchProvider
import com.saggitt.omega.search.webprovider.BingWebSearchProvider
import com.saggitt.omega.search.webprovider.BraveWebSearchProvider
import com.saggitt.omega.search.webprovider.DDGWebSearchProvider
import com.saggitt.omega.search.webprovider.EcosiaWebSearchProvider
import com.saggitt.omega.search.webprovider.GoogleWebSearchProvider
import com.saggitt.omega.search.webprovider.MetagerWebSearchProvider
import com.saggitt.omega.search.webprovider.QwantWebSearchProvider
import com.saggitt.omega.search.webprovider.SearxWebSearchProvider
import com.saggitt.omega.search.webprovider.StartpageWebSearchProvider
import com.saggitt.omega.search.webprovider.YahooWebSearchProvider
import com.saggitt.omega.search.webprovider.YandexWebSearchProvider

fun getSearchProvidersMap(context: Context): Map<String, String> {
    val providers = listOf(
        AppsSearchProvider(context),
        SFinderSearchProvider(context),
        FirefoxSearchProvider(context),
        DuckDuckGoSearchProvider(context),
        BingSearchProvider(context),
        BaiduSearchProvider(context),
        YandexSearchProvider(context),
        QwantSearchProvider(context),
        SearchLiteSearchProvider(context),
        CoolSearchSearchProvider(context),
        EdgeSearchProvider(context),

        /*Web Providers*/
        BaiduWebSearchProvider(context),
        BraveWebSearchProvider(context),
        BingWebSearchProvider(context),
        DDGWebSearchProvider(context),
        EcosiaWebSearchProvider(context),
        MetagerWebSearchProvider(context),
        GoogleWebSearchProvider(context),
        QwantWebSearchProvider(context),
        StartpageWebSearchProvider(context),
        SearxWebSearchProvider(context),
        YahooWebSearchProvider(context),
        YandexWebSearchProvider(context)
    ).filter { it.isAvailable }

    val entries = providers.map { it.name }.toTypedArray()
    val entryValues = providers.map { it::class.java.name }.toTypedArray()
    return entryValues.zip(entries).toMap()
}