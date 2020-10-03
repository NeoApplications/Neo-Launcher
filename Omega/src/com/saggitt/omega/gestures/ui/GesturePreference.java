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

package com.saggitt.omega.gestures.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import com.android.launcher3.R;
import com.saggitt.omega.gestures.BlankGestureHandler;
import com.saggitt.omega.gestures.GestureController;
import com.saggitt.omega.gestures.GestureHandler;
import com.saggitt.omega.gestures.gestures.NavSwipeUpGesture;

import java.util.Objects;

public class GesturePreference extends DialogPreference implements SharedPreferences.OnSharedPreferenceChangeListener {

    public String defaultValue = "";
    public String value = "";

    private GestureHandler handler;
    private BlankGestureHandler blankGestureHandler;
    public boolean isSwipeUp = false;

    public GesturePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GesturePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        blankGestureHandler = new BlankGestureHandler(getContext(), null);
        handler = GestureController.Companion.createGestureHandler(getContext(), value, blankGestureHandler);

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.GesturePreference);
        String className = "";

        if (ta.getString(R.styleable.GesturePreference_gestureClass) != null) {
            className = ta.getString(R.styleable.GesturePreference_gestureClass);
        }

        if (Objects.equals(className, NavSwipeUpGesture.class.getName())) {
            isSwipeUp = true;
        }

        ta.recycle();
    }

    public void onAttached() {
        super.onAttached();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onDetached() {
        super.onDetached();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getKey())) {
            value = getPersistedString(defaultValue);
            notifyChanged();
        }
    }

    public String getSummary() {
        return handler.getDisplayName();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            value = getPersistedString((String) defaultValue) != null ? getPersistedString((String) defaultValue) : "";
        } else {
            value = defaultValue != null ? (String) defaultValue : "";
        }
    }

    public String onGetDefaultValue(TypedArray a, int index) {
        defaultValue = a.getString(index);
        return defaultValue;
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.dialog_preference_recyclerview;
    }
}
