package com.saggitt.omega.widget

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.util.concurrent.TimeUnit

abstract class PeriodicDataProvider(context: Context) :
    BaseDataProvider(context) {

    private val handlerThread =
        HandlerThread(this::class.java.simpleName).apply { if (!isAlive) start() }
    private val handler = Handler(handlerThread.looper)
    private val update = ::periodicUpdate

    open val timeout = TimeUnit.MINUTES.toMillis(30)

    override fun startListening() {
        super.startListening()
        handler.post(update)
    }

    private fun periodicUpdate() {
        try {
            updateData()
        } catch (e: Exception) {
            Log.d("PeriodicDataProvider", "failed to update data", e)
        }
        handler.postDelayed(update, timeout)
    }

    override fun stopListening() {
        super.stopListening()
        handlerThread.quit()
    }

    protected fun updateNow() {
        handler.removeCallbacks(update)
        handler.post(update)
    }

    open fun updateData() {
        updateData(queryWeatherData(), queryCardData())
    }

    open fun queryWeatherData(): WeatherData? {
        return null
    }

    open fun queryCardData(): CardData? {
        return null
    }

    override fun forceUpdate() {
        super.forceUpdate()
        updateNow()
    }
}