package com.saggitt.omega.widget

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.view.View
import com.android.launcher3.Launcher
import com.saggitt.omega.util.checkPackagePermission

abstract class BaseDataProvider(protected val context: Context) {
    var listening = false
        private set

    var weatherUpdateListener: ((WeatherData?) -> Unit)? = null
    var cardUpdateListener: ((BaseDataProvider, CardData?) -> Unit)? =
        null

    private var currentWeather: WeatherData? = null
    private var currentCard: CardData? = null

    protected val resources: Resources = context.resources

    protected open val requiredPermissions = emptyList<String>()

    open fun requiresSetup() = !checkPermissionGranted()

    open fun startSetup(onFinish: (Boolean) -> Unit) {
        if (checkPermissionGranted()) {
            onFinish(true)
            return
        }

        /* TODO BlankActivity.requestPermissions(
            context,
            requiredPermissions.toTypedArray(),
            1031
        ) { _, _, results ->
            onFinish(results.all { it == PackageManager.PERMISSION_GRANTED })
        }*/
    }

    open fun startListening() {
        listening = true
    }

    open fun stopListening() {
        listening = false
        weatherUpdateListener = null
        cardUpdateListener = null
    }

    fun updateData(
        weather: WeatherData?,
        card: CardData?,
    ) {
        currentWeather = weather
        currentCard = card
        weatherUpdateListener?.invoke(weather)
        cardUpdateListener?.invoke(this, card)
    }

    open fun forceUpdate() {
        if (currentWeather != null || currentCard != null) {
            updateData(currentWeather, currentCard)
        }
    }

    protected fun getApp(name: String): CharSequence {
        val pm = context.packageManager
        try {
            return pm.getApplicationLabel(
                pm.getApplicationInfo(name, PackageManager.GET_META_DATA)
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        return name
    }

    protected fun getApp(sbn: StatusBarNotification): CharSequence {
        val subName = sbn.notification.extras.getString(EXTRA_SUBSTITUTE_APP_NAME)
        if (subName != null && context.checkPackagePermission(
                sbn.packageName,
                PERM_SUBSTITUTE_APP_NAME
            )
        ) {
            return subName
        }
        return getApp(sbn.packageName)
    }

    private fun checkPermissionGranted(): Boolean {
        return requiredPermissions.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }

    data class WeatherData(
        val icon: Bitmap,
        private val temperature: Temperature,
        val forecastUrl: String? = "https://www.google.com/search?q=weather",
        val forecastIntent: Intent? = null,
        val pendingIntent: PendingIntent? = null,
    ) {

        fun getTitle(unit: Temperature.Unit): String {
            return "${temperature.inUnit(unit)}${unit.suffix}"
        }
    }

    data class CardData(
        val icon: Bitmap? = null,
        val lines: List<Line>,
        val onClickListener: View.OnClickListener? = null,
        val forceSingleLine: Boolean = false,
    ) {

        constructor(
            icon: Bitmap? = null,
            lines: List<Line>,
            intent: PendingIntent? = null,
            forceSingleLine: Boolean = false,
        ) :
                this(icon, lines, intent?.let {
                    PendingIntentClickListener(it)
                }, forceSingleLine)

        constructor(
            icon: Bitmap? = null,
            lines: List<Line>,
            forceSingleLine: Boolean = false,
        ) :
                this(icon, lines, null as View.OnClickListener?, forceSingleLine)

        constructor(
            icon: Bitmap?,
            title: CharSequence,
            titleEllipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.END,
            subtitle: CharSequence,
            subtitleEllipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.END,
            pendingIntent: PendingIntent? = null,
        )
                : this(
            icon,
            listOf(
                Line(title, titleEllipsize),
                Line(subtitle, subtitleEllipsize)
            ),
            pendingIntent
        )

        val isDoubleLine = !forceSingleLine && lines.size >= 2

        val title: CharSequence?
        val titleEllipsize: TextUtils.TruncateAt?

        val subtitle: CharSequence?
        val subtitleEllipsize: TextUtils.TruncateAt?

        init {
            if (lines.isEmpty()) {
                error("Can't create card with zero lines")
            }
            if (forceSingleLine) {
                title = TextUtils.join(" – ", lines.map { it.text })!!
                titleEllipsize =
                    if (lines.size == 1) lines.first().ellipsize else TextUtils.TruncateAt.END
                subtitle = null
                subtitleEllipsize = null
            } else {
                title = lines.first().text
                titleEllipsize = lines.first().ellipsize
                subtitle = TextUtils.join(" – ", lines.subList(1, lines.size).map { it.text })!!
                subtitleEllipsize =
                    if (lines.size == 2) lines[1].ellipsize else TextUtils.TruncateAt.END
            }
        }
    }

    data class Line @JvmOverloads constructor(
        val text: CharSequence,
        val ellipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.MARQUEE,
    ) {

        constructor(context: Context, textRes: Int) : this(context.getString(textRes))
    }

    open class PendingIntentClickListener(private val pendingIntent: PendingIntent?) :
        View.OnClickListener {

        override fun onClick(v: View) {
            if (pendingIntent == null) return
            val launcher = Launcher.getLauncher(v.context)
            //val opts = launcher.getActivityLaunchOptions(v).toBundle()
            try {
                launcher.startIntentSender(
                    pendingIntent.intentSender, null,
                    Intent.FLAG_ACTIVITY_NEW_TASK,
                    //Intent.FLAG_ACTIVITY_NEW_TASK, 0, opts
                    Intent.FLAG_ACTIVITY_NEW_TASK, 0
                )
            } catch (e: ActivityNotFoundException) {
                // ignored
            }
        }
    }

    companion object {
        private const val PERM_SUBSTITUTE_APP_NAME =
            "android.permission.SUBSTITUTE_NOTIFICATION_APP_NAME"
        private const val EXTRA_SUBSTITUTE_APP_NAME = "android.substName"
    }
}

