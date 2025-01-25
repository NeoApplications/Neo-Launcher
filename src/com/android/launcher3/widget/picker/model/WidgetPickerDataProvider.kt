/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.widget.picker.model

import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.widget.model.WidgetsListBaseEntry
import com.android.launcher3.widget.picker.model.data.WidgetPickerData
import com.android.launcher3.widget.picker.model.data.WidgetPickerDataUtils.withRecommendedWidgets
import com.android.launcher3.widget.picker.model.data.WidgetPickerDataUtils.withWidgets
import java.io.PrintWriter

/**
 * Provides [WidgetPickerData] to various views such as widget picker, app-specific widget picker,
 * widgets shortcut.
 */
class WidgetPickerDataProvider {
    /** All the widgets data provided for the views */
    private var mWidgetPickerData: WidgetPickerData = WidgetPickerData()

    private var changeListener: WidgetPickerDataChangeListener? = null

    /** Sets a listener to be called back when widget data is updated. */
    fun setChangeListener(changeListener: WidgetPickerDataChangeListener?) {
        this.changeListener = changeListener
    }

    /** Returns the current snapshot of [WidgetPickerData]. */
    fun get(): WidgetPickerData {
        return mWidgetPickerData
    }

    /**
     * Updates the widgets available to the widget picker.
     *
     * Generally called when the widgets model has new data.
     */
    @JvmOverloads
    fun setWidgets(
        allWidgets: List<WidgetsListBaseEntry>,
        defaultWidgets: List<WidgetsListBaseEntry> = listOf()
    ) {
        mWidgetPickerData =
            mWidgetPickerData.withWidgets(allWidgets = allWidgets, defaultWidgets = defaultWidgets)
        changeListener?.onWidgetsBound()
    }

    /**
     * Makes the widget recommendations available to the widget picker
     *
     * Generally called when new widget predictions are available.
     */
    fun setWidgetRecommendations(recommendations: List<ItemInfo>) {
        mWidgetPickerData = mWidgetPickerData.withRecommendedWidgets(recommendations)
        changeListener?.onRecommendedWidgetsBound()
    }

    /** Writes the current state to the provided writer. */
    fun dump(prefix: String, writer: PrintWriter) {
        writer.println(prefix + "WidgetPickerDataProvider:")
        writer.println("$prefix\twidgetPickerData:$mWidgetPickerData")
    }

    interface WidgetPickerDataChangeListener {
        /** A callback to get notified when widgets are bound. */
        fun onWidgetsBound()

        /** A callback to get notified when recommended widgets are bound. */
        fun onRecommendedWidgetsBound()
    }
}
