package com.saggitt.omega.util

import android.content.Context
import com.android.launcher3.BuildConfig
import com.saggitt.omega.preferences.PREFS_LANGUAGE_DEFAULT_CODE
import com.saggitt.omega.preferences.PREFS_LANGUAGE_DEFAULT_NAME
import java.util.*

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