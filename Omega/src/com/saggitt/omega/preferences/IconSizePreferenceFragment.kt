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

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.iterator
import androidx.preference.PreferenceDialogFragmentCompat
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.applyAccent

class IconSizePreferenceFragment : PreferenceDialogFragmentCompat(), SeekBar.OnSeekBarChangeListener {

    private lateinit var iconSizePicker: SeekBar
    private lateinit var iconSizeLabel: TextView
    private lateinit var iconList: RelativeLayout
    private var iconScale = 0
    private var defaultIconScale = 1

    protected var min: Float = 0.toFloat()
    protected var max: Float = 0.toFloat()
    protected var current: Float = 0.toFloat()
    protected var defaultValue: Float = 0.toFloat()
    private var multiplier: Int = 0
    protected var steps: Int = 100
    private var format: String? = null
    open val allowResetToDefault = true
    private var lastPersist = Float.NaN

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        iconSizePicker = view.findViewById(R.id.iconSizePicker)
        iconSizePicker!!.max = steps
        iconSizePicker.setOnSeekBarChangeListener(this)
        iconSizeLabel = view.findViewById(R.id.iconSizeLabel)
        iconSizeLabel.text = "${iconSizePicker.progress}"

        //current = getPersistedFloat(defaultValue)
        val progress = ((current - min) / ((max - min) / steps))
        iconSizePicker!!.progress = Math.round(progress)
        if (allowResetToDefault) view.setOnCreateContextMenuListener(this)

        iconList = view.findViewById(R.id.icon_scale_list)
        init(view.context, null)
    }

    fun setValue(value: Float) {
        current = value
        persistFloat(value)
        updateDisplayedValue()
    }

    protected open fun updateDisplayedValue() {
        iconSizePicker?.setOnSeekBarChangeListener(null)
        val progress = ((current - min) / ((max - min) / steps))
        iconSizePicker!!.progress = Math.round(progress)
        //updateSummary()
        iconSizePicker?.setOnSeekBarChangeListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iconScale = savedInstanceState?.getInt(SingleDimensionGridSizeDialogFragmentCompat.SAVE_STATE_ROWS)
                ?: defaultIconScale
    }

    /*init {
        init(requireContext(), null)
    }*/

    private fun init(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference)
        min = ta.getFloat(R.styleable.SeekbarPreference_minValue, 0f)
        max = ta.getFloat(R.styleable.SeekbarPreference_maxValue, 100f)
        multiplier = ta.getInt(R.styleable.SeekbarPreference_summaryMultiplier, 1)
        format = ta.getString(R.styleable.SeekbarPreference_summaryFormat)
        defaultValue = ta.getFloat(R.styleable.SeekbarPreference_defaultSeekbarValue, min)
        steps = ta.getInt(R.styleable.SeekbarPreference_steps, 100)
        if (format == null) {
            format = "%.2f"
        }
        ta.recycle()
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        builder.setNeutralButton(R.string.dialog_default, { _, _ ->
            iconSizePicker.setProgress(defaultIconScale)
        })
        builder.setPositiveButton(R.string.dialog_ok, { _, _ ->
            iconScale = iconSizePicker.progress
        })
        builder.setNegativeButton(R.string.dialog_cancel) { _, _ ->
            dismiss()
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            Utilities.getOmegaPrefs(context).allAppsIconScale = iconScale.toFloat()
        }
    }

    override fun onStart() {
        super.onStart()
        val stateList = ColorStateList.valueOf(Utilities.getOmegaPrefs(context).accentColor)
        iconSizePicker.apply {
            thumbTintList = stateList
            progressTintList = stateList
            progressBackgroundTintList = stateList
        }

        (dialog as AlertDialog).applyAccent()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(SAVE_STATE_ICON_SCALE, iconSizePicker.progress)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        current = min + (max - min) / steps * progress
        current = Math.round(current * 100f) / 100f //round to .00 places

        iconSizeLabel.text = "$progress%"
        for (child in iconList) {
            child.scaleY = progress / 100F;
            child.scaleX = progress / 100F;
        }
        persistFloat(current)

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        persistFloat(current)
    }

    fun persistFloat(value: Float): Boolean {
        if (value == lastPersist) return true
        lastPersist = value
        return false
    }

    companion object {
        const val SAVE_STATE_ICON_SCALE = "icon_scale"

        fun newInstance(key: String?, context: Context) = IconSizePreferenceFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_KEY, key)
            }
        }
    }
}