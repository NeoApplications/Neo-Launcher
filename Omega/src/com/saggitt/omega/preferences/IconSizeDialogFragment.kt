/*
 *  Copyright (c) 2020 Omega Launcher
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
 *
 */

package com.saggitt.omega.preferences

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.android.launcher3.R
import com.saggitt.omega.util.applyAccent

class IconSizeDialogFragment : PreferenceDialogFragmentCompat(), SeekBar.OnSeekBarChangeListener {
    private val minValue = 30
    private val maxValue = 200

    private var iconScale = 100

    private lateinit var iconScalePicker: SeekBar
    private lateinit var iconScaleLabel: TextView

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        iconScalePicker = view.findViewById(R.id.iconSize)
        iconScaleLabel = view.findViewById(R.id.numRowsLabel)

        iconScalePicker.max = maxValue - minValue
        iconScalePicker.progress = iconScale - minValue
        iconScalePicker.setOnSeekBarChangeListener(this)

        iconScaleLabel.text = "${iconScalePicker.progress + minValue}"
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            //gridSizePreference.setSize(numRowsPicker.progress + minValue)
        }
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        builder.setNeutralButton(R.string.theme_default, { _, _ ->
            //gridSizePreference.setSize(0)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_ROWS, iconScalePicker.progress)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        iconScaleLabel.text = "${progress + minValue}"
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).applyAccent()
    }

    companion object {
        const val SAVE_STATE_ROWS = "iconSize"

        fun newInstance(key: String?) = IconSizeDialogFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_KEY, key)
            }
        }
    }
}