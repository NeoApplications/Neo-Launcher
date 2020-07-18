/*
 * Copyright (C) 2019 Paranoid Android
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
package com.saggitt.omega.qsb;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.quickstep.inputconsumers.AssistantTouchConsumer;
import com.android.systemui.shared.system.ActivityManagerWrapper;

public class SearchIcon extends View implements SharedPreferences.OnSharedPreferenceChangeListener {

    public Drawable mIcon;
    public boolean mOpaEnabled;

    public SearchIcon(Context context) {
        this(context, null, 0);
    }

    public SearchIcon(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SearchIcon(Context context, AttributeSet attributeSet, int res) {
        super(context, attributeSet, res);
        updatePrefs(Utilities.getDevicePrefs(context));
        setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                startService(view);
            }
        });
    }

    public void startService(View view) {
        boolean hasGoogleApp;
        Context context = getContext();
        if (mOpaEnabled) {
            ContentResolver resolver = context.getContentResolver();
            String assistant = Settings.Secure.getString(resolver, "assistant");
            boolean isInSession = false;
            if (!TextUtils.isEmpty(assistant)) {
                hasGoogleApp = "com.google.android.googlequicksearchbox".equals(ComponentName.unflattenFromString(assistant).getPackageName());
            } else {
                String voiceInteraction = Settings.Secure.getString(resolver, "voice_interaction_service");
                if (!TextUtils.isEmpty(voiceInteraction)) {
                    hasGoogleApp = "com.google.android.googlequicksearchbox".equals(ComponentName.unflattenFromString(voiceInteraction).getPackageName());
                } else {
                    ResolveInfo activity = context.getPackageManager().resolveActivity(new Intent("android.intent.action.ASSIST"), 65536);
                    hasGoogleApp = activity != null ? "com.google.android.googlequicksearchbox".equals(activity.resolvePackageName) : false;
                }
            }
            if (hasGoogleApp) {
                Bundle opt = new Bundle();
                opt.putInt(AssistantTouchConsumer.OPA_BUNDLE_TRIGGER, 37);
                opt.putInt(AssistantTouchConsumer.INVOCATION_TYPE_KEY, 4);
                isInSession = ActivityManagerWrapper.getInstance().showVoiceSession(null, opt, 5);
            }
            if (isInSession) {
                return;
            }
        }
        try {
            context.startActivity(new Intent("android.intent.action.VOICE_ASSIST").addFlags(268468224).setPackage("com.google.android.googlequicksearchbox"));
        } catch (ActivityNotFoundException unused) {
            LauncherAppsCompat.getInstance(context).showAppDetailsForProfile(new ComponentName("com.google.android.googlequicksearchbox", ".SearchActivity"), Process.myUserHandle(), null, null);
        }
    }

    public void updatePrefs(SharedPreferences prefs) {
        mOpaEnabled = prefs.getBoolean("opa_enabled", true);
        mIcon = getContext().getDrawable(mOpaEnabled ? R.drawable.ic_qsb_assist : R.drawable.ic_qsb_mic);
        update();
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Utilities.getDevicePrefs(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetachedFromWindow() {
        Utilities.getDevicePrefs(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mIcon != null) {
            mIcon.draw(canvas);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String value) {
        if ("opa_enabled".equals(value)) {
            updatePrefs(prefs);
            invalidate();
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        update();
    }

    public void update() {
        if (mIcon != null) {
            int intrinsicWidth = mIcon.getIntrinsicWidth() / 2;
            int intrinsicHeight = mIcon.getIntrinsicHeight() / 2;
            int width = getWidth() / 2;
            int height = getHeight() / 2;
            mIcon.setBounds(width - intrinsicWidth, height - intrinsicHeight, width + intrinsicWidth, height + intrinsicHeight);
        }
    }
}
