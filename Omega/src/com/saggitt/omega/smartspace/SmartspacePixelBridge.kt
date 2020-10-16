/*
 * Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.saggitt.omega.smartspace

import android.util.Log
import com.android.launcher3.util.Executors.MODEL_EXECUTOR

class SmartspacePixelBridge(controller: OmegaSmartspaceController) :
        OmegaSmartspaceController.DataProvider(controller), ISmartspace, Runnable {

    private val smartspaceController = SmartspaceController.get(controller.context)
    private val handler = MODEL_EXECUTOR.handler
    private var data: SmartspaceDataContainer? = null
    private var ds = false

    override fun startListening() {
        super.startListening()

        updateData(null, null)
        smartspaceController.da(this)
    }

    override fun stopListening() {
        super.stopListening()
        smartspaceController.da(null)
    }

    override fun onGsaChanged() {
        ds = smartspaceController.cY()
        if (data != null) {
            postUpdate(data)
        } else {
            Log.d("SmartspacePixelBridge", "onGsaChanged but no data present")
        }
    }

    override fun postUpdate(data: SmartspaceDataContainer?) {
        this.data = data?.also { initListeners(it) }
    }

    private fun initListeners(e: SmartspaceDataContainer) {
        val weatherData: OmegaSmartspaceController.WeatherData? = if (e.isWeatherAvailable) {
            SmartspaceDataWidget.parseWeatherData(e.dO.icon, e.dO.title)
        } else {
            null
        }
        val cardData: OmegaSmartspaceController.CardData? = if (e.cS()) {
            val dp = e.dP
            OmegaSmartspaceController.CardData(dp.icon, dp.title, dp.cx(true), dp.cy(), dp.cx(false))
        } else {
            null
        }

        handler.removeCallbacks(this)
        if (e.cS() && e.dP.cv()) {
            val cw = e.dP.cw()
            var min = 61000L - System.currentTimeMillis() % 60000L
            if (cw > 0L) {
                min = Math.min(min, cw)
            }
            handler.postDelayed(this, min)
        }

        updateData(weatherData, cardData)
    }

    override fun run() {
        data?.let { initListeners(it) }
    }
}