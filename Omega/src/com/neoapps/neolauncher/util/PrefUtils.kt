package com.neoapps.neolauncher.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import com.neoapps.neolauncher.groups.AppGroupsManager
import com.neoapps.neolauncher.preferences.PREFS_LANGUAGE_DEFAULT_CODE
import com.neoapps.neolauncher.preferences.PREFS_LANGUAGE_DEFAULT_NAME
import java.util.Locale

fun Context.languageOptions(): Map<String, String> {

    val langCodes = BuildConfig.DETECTED_ANDROID_LOCALES
    val languages: ArrayList<String> = ArrayList()
    val contextUtils = Config(this)

    if (langCodes.isNotEmpty()) {
        for (langId in langCodes) {
            val locale: Locale = contextUtils.getLocaleByAndroidCode(langId)
            languages.add(summarizeLocale(locale, langId) + ";" + langId)
        }
    }

    // Sort languages naturally
    languages.sort()

    val mEntries = arrayOfNulls<String>(languages.size + 2)
    val mEntryValues = arrayOfNulls<String>(languages.size + 2)

    for (i in languages.indices) {
        mEntries[i + 2] = languages[i].split(";").toTypedArray()[0]
        mEntryValues[i + 2] = languages[i].split(";").toTypedArray()[1]
    }

    mEntryValues[0] = ""
    mEntries[0] = "$PREFS_LANGUAGE_DEFAULT_NAME » " + summarizeLocale(
        resources.configuration.locales.get(0), ""
    )

    mEntryValues[1] = PREFS_LANGUAGE_DEFAULT_CODE
    mEntries[1] = summarizeLocale(
        contextUtils.getLocaleByAndroidCode(PREFS_LANGUAGE_DEFAULT_CODE),
        PREFS_LANGUAGE_DEFAULT_NAME
    )

    return mEntryValues.filterNotNull()
        .zip(mEntries.filterNotNull())
        .toMap()
}

private fun summarizeLocale(locale: Locale, localeAndroidCode: String): String {
    val country = locale.getDisplayCountry(locale)
    val language = locale.getDisplayLanguage(locale)
    var ret = (locale.getDisplayLanguage(Locale.ENGLISH)
        .toString() + " (" + language.substring(0, 1)
        .uppercase(Locale.getDefault()) + language.substring(1)
            + (if (country.isNotEmpty() && country.lowercase(Locale.getDefault()) != language.lowercase(
            Locale.getDefault()
        )
    ) ", $country" else "")
            + ")")
    if (localeAndroidCode == "zh-rCN") {
        ret = ret.substring(
            0,
            ret.indexOf(" ") + 1
        ) + "Simplified" + ret.substring(ret.indexOf(" "))
    } else if (localeAndroidCode == "zh-rTW") {
        ret = ret.substring(
            0,
            ret.indexOf(" ") + 1
        ) + "Traditional" + ret.substring(ret.indexOf(" "))
    }
    return ret
}

fun Context.getFeedProviders(): Map<String, String> {
    val feeds = listOf(
        ProviderInfo(getString(R.string.none), "", getIcon())
    ) + availableFeedProviders().map {
        ProviderInfo(
            it.loadLabel(packageManager).toString(),
            it.packageName,
            it.loadIcon(packageManager)
        )
    }

    val entries = feeds.map { it.displayName }.toTypedArray()
    val entryValues = feeds.map { it.packageName }.toTypedArray()
    return entryValues.zip(entries).toMap()
}

val Context.drawerCategorizationOptions: Map<String, String>
    get() = listOfNotNull(
        AppGroupsManager.Category.NONE,
        AppGroupsManager.Category.FOLDER,
        AppGroupsManager.Category.TAB
    ).associate { Pair(it.key, getString(it.titleId)) }

fun Context.availableFeedProviders(): List<ApplicationInfo> {
    val packageManager = packageManager
    val intent = Intent("com.android.launcher3.WINDOW_OVERLAY")
        .setData(Uri.parse("app://$packageName"))
    val feedList: MutableList<ApplicationInfo> = ArrayList()
    for (resolveInfo in packageManager.queryIntentServices(
        intent,
        PackageManager.GET_RESOLVED_FILTER
    )) {
        if (resolveInfo.serviceInfo != null) {
            val applicationInfo = resolveInfo.serviceInfo.applicationInfo
            feedList.add(applicationInfo)
        }
    }
    return feedList
}

data class ProviderInfo(
    val displayName: String,
    val packageName: String,
    val icon: Drawable?,
)