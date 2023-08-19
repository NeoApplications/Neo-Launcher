package com.saulhdev.smartspace

import android.content.ComponentName
import android.os.Process
import android.os.UserHandle
import androidx.annotation.IntDef
import com.saulhdev.smartspace.uitemplatedata.BaseTemplateData

data class SmartspaceTarget(
    val smartspaceTargetId: String,
    val headerAction: SmartspaceAction? = null,
    val baseAction: SmartspaceAction? = null,
    val creationTimeMillis: Long = 0,
    val score: Int = 0,
    val featureType: Int,
    val templateData: BaseTemplateData? = null,
    val actionChips: List<SmartspaceAction> = listOf(),
    val iconGrid: List<SmartspaceAction> = listOf(),
    val componentName: ComponentName? = null,
) {
    @IntDef(
        value = [FEATURE_UNDEFINED, FEATURE_WEATHER, FEATURE_CALENDAR, FEATURE_COMMUTE_TIME, FEATURE_FLIGHT, FEATURE_TIPS, FEATURE_REMINDER, FEATURE_ALARM, FEATURE_ONBOARDING, FEATURE_SPORTS, FEATURE_WEATHER_ALERT, FEATURE_CONSENT, FEATURE_STOCK_PRICE_CHANGE, FEATURE_SHOPPING_LIST, FEATURE_LOYALTY_CARD, FEATURE_MEDIA, FEATURE_BEDTIME_ROUTINE, FEATURE_FITNESS_TRACKING, FEATURE_ETA_MONITORING, FEATURE_MISSED_CALL, FEATURE_PACKAGE_TRACKING, FEATURE_TIMER, FEATURE_STOPWATCH, FEATURE_UPCOMING_ALARM, FEATURE_GAS_STATION_PAYMENT, FEATURE_PAIRED_DEVICE_STATE, FEATURE_DRIVING_MODE, FEATURE_SLEEP_SUMMARY, FEATURE_FLASHLIGHT, FEATURE_TIME_TO_LEAVE, FEATURE_DOORBELL, FEATURE_MEDIA_RESUME, FEATURE_CROSS_DEVICE_TIMER, FEATURE_SEVERE_WEATHER_ALERT, FEATURE_HOLIDAY_ALARM, FEATURE_SAFETY_CHECK, FEATURE_MEDIA_HEADS_UP, FEATURE_STEP_COUNTING, FEATURE_EARTHQUAKE_ALERT, FEATURE_STEP_DATE, FEATURE_BLAZE_BUILD_PROGRESS, FEATURE_EARTHQUAKE_OCCURRED, FEATURE_BATTERY]
    )
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class FeatureType {}

    @IntDef(
        value = [UI_TEMPLATE_UNDEFINED, UI_TEMPLATE_DEFAULT, UI_TEMPLATE_SUB_IMAGE, UI_TEMPLATE_SUB_LIST, UI_TEMPLATE_CAROUSEL, UI_TEMPLATE_HEAD_TO_HEAD, UI_TEMPLATE_COMBINED_CARDS, UI_TEMPLATE_SUB_CARD]
    )
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class UiTemplateType {}

    class Builder(
        val smartspaceTargetId: String,
        val componentName: ComponentName,
        val user: UserHandle = Process.myUserHandle()
    ) {
        private var mFeatureType: Int = -1
        private val score = 0
        private val actionChips: List<SmartspaceAction> = ArrayList()
        private val iconGrid: List<SmartspaceAction> = ArrayList()
        private val headerAction: SmartspaceAction? = null
        private val baseAction: SmartspaceAction? = null
        private val templateData: BaseTemplateData? = null
        fun setFeatureType(featureType: Int): Builder {
            mFeatureType = featureType
            return this
        }

        fun build(): SmartspaceTarget {
            check(!(smartspaceTargetId == null || componentName == null || user == null)) { "Please assign a value to all @NonNull args." }
            return SmartspaceTarget(
                smartspaceTargetId = smartspaceTargetId,
                headerAction = headerAction,
                baseAction = baseAction,
                score = score,
                featureType = mFeatureType,
                templateData = templateData,
                actionChips = actionChips,
                iconGrid = iconGrid
            )
        }
    }

    companion object {
        //FeatureType
        const val FEATURE_UNDEFINED = 0
        const val FEATURE_WEATHER = 1
        const val FEATURE_CALENDAR = 2
        const val FEATURE_COMMUTE_TIME = 3
        const val FEATURE_FLIGHT = 4
        const val FEATURE_TIPS = 5
        const val FEATURE_REMINDER = 6
        const val FEATURE_ALARM = 7
        const val FEATURE_ONBOARDING = 8
        const val FEATURE_SPORTS = 9
        const val FEATURE_WEATHER_ALERT = 10
        const val FEATURE_CONSENT = 11
        const val FEATURE_STOCK_PRICE_CHANGE = 12
        const val FEATURE_SHOPPING_LIST = 13
        const val FEATURE_LOYALTY_CARD = 14
        const val FEATURE_MEDIA = 15
        const val FEATURE_BEDTIME_ROUTINE = 16
        const val FEATURE_FITNESS_TRACKING = 17
        const val FEATURE_ETA_MONITORING = 18
        const val FEATURE_MISSED_CALL = 19
        const val FEATURE_PACKAGE_TRACKING = 20
        const val FEATURE_TIMER = 21
        const val FEATURE_STOPWATCH = 22
        const val FEATURE_UPCOMING_ALARM = 23
        const val FEATURE_GAS_STATION_PAYMENT = 24
        const val FEATURE_PAIRED_DEVICE_STATE = 25
        const val FEATURE_DRIVING_MODE = 26
        const val FEATURE_SLEEP_SUMMARY = 27
        const val FEATURE_FLASHLIGHT = 28
        const val FEATURE_TIME_TO_LEAVE = 29
        const val FEATURE_DOORBELL = 30
        const val FEATURE_MEDIA_RESUME = 31
        const val FEATURE_CROSS_DEVICE_TIMER = 32
        const val FEATURE_SEVERE_WEATHER_ALERT = 33
        const val FEATURE_HOLIDAY_ALARM = 34
        const val FEATURE_SAFETY_CHECK = 35
        const val FEATURE_MEDIA_HEADS_UP = 36
        const val FEATURE_STEP_COUNTING = 37
        const val FEATURE_EARTHQUAKE_ALERT = 38
        const val FEATURE_STEP_DATE = 39
        const val FEATURE_BLAZE_BUILD_PROGRESS = 40
        const val FEATURE_EARTHQUAKE_OCCURRED = 41
        const val FEATURE_BATTERY = 42


        //UiTemplateType
        const val UI_TEMPLATE_UNDEFINED = 0
        const val UI_TEMPLATE_DEFAULT = 1
        const val UI_TEMPLATE_SUB_IMAGE = 2
        const val UI_TEMPLATE_SUB_LIST = 3
        const val UI_TEMPLATE_CAROUSEL = 4
        const val UI_TEMPLATE_HEAD_TO_HEAD = 5
        const val UI_TEMPLATE_COMBINED_CARDS = 6
        const val UI_TEMPLATE_SUB_CARD = 7
    }
}
