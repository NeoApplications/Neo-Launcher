package com.saggitt.omega.widget.weatherprovider

import android.content.Context
import androidx.annotation.Keep
import com.saggitt.omega.widget.BaseDataProvider

@Keep
class BlankDataProvider(context: Context) :
    BaseDataProvider(context) {

    override fun startListening() {
        super.startListening()

        updateData(null, null)
    }
}