/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.preferences

import androidx.annotation.StringRes
import com.neoapps.neolauncher.util.JavaField

open class GridSize(
    @StringRes val titleId: Int,
    val numColumnsPref: IdpIntPref,
    columnsKey: String,
    targetObject: Any,
    private val onChangeListener: () -> Unit
) {
    var numColumns by JavaField<Int>(targetObject, columnsKey)
    private val numColumnsOriginal by JavaField<Int>(targetObject, "${columnsKey}Original")
    protected val onChange = {
        applyCustomization()
        onChangeListener.invoke()
    }

    init {
        applyNumColumns()
    }

    protected open fun applyCustomization() {
        applyNumColumns()
    }

    private fun applyNumColumns() {
        numColumns = fromPref(numColumnsPref.getValue(), numColumnsOriginal)
    }

    fun resetDefaultColumn(): Int = numColumnsOriginal

    fun fromPref(value: Int, default: Int) = if (value != 0) value else default
}

class GridSize2D(
    @StringRes titleId: Int,
    numColumnsPref: IdpIntPref,
    val numRowsPref: IdpIntPref,
    columnsKey: String,
    rowsKey: String,
    targetObject: Any,
    onChangeListener: () -> Unit
) : GridSize(
    titleId,
    numColumnsPref,
    columnsKey,
    targetObject,
    onChangeListener
) {
    var numRows by JavaField<Int>(targetObject, rowsKey)
    private val numRowsOriginal by JavaField<Int>(targetObject, "${rowsKey}Original")

    init {
        applyNumRows()
    }

    override fun applyCustomization() {
        super.applyCustomization()
        applyNumRows()
    }

    private fun applyNumRows() {
        numRows = fromPref(numRowsPref.getValue(), numRowsOriginal)
    }

    fun resetDefaultRow(): Int = numRowsOriginal
}