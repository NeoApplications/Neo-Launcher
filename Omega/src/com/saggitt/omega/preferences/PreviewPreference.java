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

package com.saggitt.omega.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.launcher3.R;

public class PreviewPreference extends Preference {

    private int layoutId = 0;
    private View previewView = null;

    public PreviewPreference(Context context) {
        this(context, null, 0);
    }

    public PreviewPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        int layoutResource = R.layout.preference_preview;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PreviewPreference);
        layoutId = ta.getResourceId(R.styleable.PreviewPreference_previewLayout, 0);
        ta.recycle();
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        ViewGroup parent = (ViewGroup) holder.itemView;
        parent.removeAllViews();
        parent.addView(getPreviewView(parent));
    }

    private View getPreviewView(ViewGroup parent) {
        if (previewView == null)
            previewView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);

        return previewView;
    }

    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        setVisible(isEnabled());
    }
}
