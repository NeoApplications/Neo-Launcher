package com.saulhdev.smartspace

import android.text.TextUtils
import androidx.annotation.Nullable
import com.saulhdev.smartspace.uitemplatedata.Text

class SmartspaceUtils {

    companion object {
        @JvmStatic
        fun isEmpty(@Nullable text: Text?): Boolean {
            return text == null || TextUtils.isEmpty(text.text)
        }

        @JvmStatic
        fun isEqual(@Nullable text1: Text?, @Nullable text2: Text?): Boolean {
            if (text1 == null && text2 == null) return true
            return if (text1 == null || text2 == null) false else text1 == text2
        }

        @JvmStatic
        fun isEqual(@Nullable cs1: CharSequence?, @Nullable cs2: CharSequence?): Boolean {
            if (cs1 == null && cs2 == null) return true
            return if (cs1 == null || cs2 == null) false else cs1.toString().contentEquals(cs2)
        }
    }
}