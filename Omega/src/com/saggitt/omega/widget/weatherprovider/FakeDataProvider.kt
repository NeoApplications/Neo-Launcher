/*
 *     This file was part of Lawnchair Launcher.
 *     And then was modified as part of Neo Launcher (under the same license)
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.widget.weatherprovider

import android.content.Context
import android.text.TextUtils
import androidx.annotation.Keep
import com.saggitt.omega.widget.BaseDataProvider
import com.saggitt.omega.widget.Temperature

@Keep
class FakeDataProvider(context: Context) :
    BaseDataProvider(context) {

    private val iconProvider = WeatherIconProvider(context)
    private val weather = WeatherData(
        iconProvider.getIcon(WeatherIconProvider.CONDITION_UNKNOWN),
        Temperature(0, Temperature.Unit.Celsius), ""
    )
    private val card = CardData(
        iconProvider.getIcon(WeatherIconProvider.CONDITION_UNKNOWN),
        "Title", TextUtils.TruncateAt.END, "Subtitle", TextUtils.TruncateAt.END
    )

    init {
        updateData(weather, card)
    }
}
