package com.google.android.systemui.smartspace.utils;

import android.util.Log;
import android.view.View;

import com.statix.android.systemui.res.R;

import java.util.Arrays;

public abstract class ContentDescriptionUtil {
    public static final void setFormattedContentDescription(
            String str, View view, CharSequence charSequence, CharSequence charSequence2) {
        CharSequence string =
                (charSequence == null || charSequence.length() == 0)
                        ? charSequence2
                        : (charSequence2 == null || charSequence2.length() == 0)
                                ? charSequence
                                : view.getContext()
                                        .getString(
                                                R.string.generic_smartspace_concatenated_desc,
                                                charSequence2,
                                                charSequence);
        Log.i(
                str,
                String.format(
                        "setFormattedContentDescription: text=%s, iconDescription=%s,"
                            + " contentDescription=%s",
                        Arrays.copyOf(new Object[] {charSequence, charSequence2, string}, 3)));
        view.setContentDescription(string);
    }
}
