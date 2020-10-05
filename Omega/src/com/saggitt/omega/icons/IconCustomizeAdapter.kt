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

package com.saggitt.omega.icons

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import java.util.*

class IconCustomizeAdapter(context: Context) : RecyclerView.Adapter<IconCustomizeAdapter.Holder>() {
    val adapterItems = ArrayList<String>()
    val mContext = context

    init {
        val currentItems = context.resources.getStringArray(R.array.icon_shape_values)
        adapterItems.addAll(currentItems)
        adapterItems.removeAt(0);
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(adapterItems[position])
    }

    override fun getItemCount(): Int {
        return adapterItems.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return createHolder(parent, R.layout.item_icon_shape, ::Holder)
    }

    private inline fun createHolder(parent: ViewGroup, resource: Int, creator: (View) -> Holder): Holder {
        return creator(LayoutInflater.from(parent.context).inflate(resource, parent, false))
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconButton = itemView.findViewById<Button>(R.id.shape_icon)
        private val check = itemView.findViewById<ImageView>(R.id.check_mark)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(item: String) {
            var drawable = mContext.getDrawable(R.drawable.shape_circle)
            when (item) {
                TYPE_CIRLE -> drawable = mContext.getDrawable(R.drawable.shape_circle)
                TYPE_SQUARE -> drawable = mContext.getDrawable(R.drawable.shape_square)
                TYPE_ROUNDED -> drawable = mContext.getDrawable(R.drawable.shape_rounded)
                TYPE_SQUIRCLE -> drawable = mContext.getDrawable(R.drawable.shape_squircle)
                TYPE_TEARDROP -> drawable = mContext.getDrawable(R.drawable.shape_teardrop)
                TYPE_CYLINDER -> drawable = mContext.getDrawable(R.drawable.shape_cylinder)
            }
            iconButton.background = drawable
            check.visibility = View.INVISIBLE
        }
    }

    companion object {
        const val TYPE_CIRLE = "circle"
        const val TYPE_SQUARE = "square"
        const val TYPE_ROUNDED = "roundedSquare"
        const val TYPE_SQUIRCLE = "squircle"
        const val TYPE_TEARDROP = "teardrop"
        const val TYPE_CYLINDER = "cylinder"
    }
}