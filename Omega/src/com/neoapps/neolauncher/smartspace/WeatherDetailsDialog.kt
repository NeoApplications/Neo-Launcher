/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
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

package com.neoapps.neolauncher.smartspace

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.neoapps.neolauncher.data.models.HourlyWeather
import com.neoapps.neolauncher.data.models.WeatherInfo
import com.neoapps.neolauncher.neoApp
import com.neoapps.neolauncher.theme.OmegaAppTheme
import com.neoapps.neolauncher.util.findActivity
import com.neoapps.neolauncher.util.getLauncherOrNull
import kotlin.math.roundToInt

private var isWeatherDialogShowing = false

fun showWeatherDetailsDialog(context: Context, weather: WeatherInfo) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        Handler(Looper.getMainLooper()).post {
            showWeatherDetailsDialog(context, weather)
        }
        return
    }

    if (isWeatherDialogShowing) return

    val launcher = context.getLauncherOrNull()
        ?: (context.findActivity() as? Launcher)
        ?: (context.neoApp.activityHandler.foregroundActivity as? Launcher)

    val root = launcher?.dragLayer
        ?: context.findActivity()?.findViewById<ViewGroup>(android.R.id.content)
        ?: return

    isWeatherDialogShowing = true
    val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    composeView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {}
        override fun onViewDetachedFromWindow(v: View) {
            isWeatherDialogShowing = false
        }
    })

    composeView.setContent {
        OmegaAppTheme {
            WeatherDetailsDialog(
                weather = weather,
                onDismiss = {
                    isWeatherDialogShowing = false
                    root.removeView(composeView)
                }
            )
        }
    }

    root.addView(composeView)
}

@Composable
fun WeatherDetailsDialog(
    weather: WeatherInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_weather_location),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = weather.cityName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            id = WeatherCode.getIconRes(weather.weatherCode, weather.isDay)
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = androidx.compose.ui.graphics.Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "${weather.temperature.roundToInt()}${weather.unit}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(id = WeatherCode.getDescriptionRes(weather.weatherCode)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(
                                id = R.string.weather_feels_like,
                                "${weather.apparentTemperature.roundToInt()}${weather.unit}"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherMetricItem(
                        iconRes = R.drawable.ic_weather_humidity,
                        value = "${weather.humidity}%"
                    )
                    WeatherMetricItem(
                        iconRes = R.drawable.ic_weather_wind,
                        value = "${weather.windSpeed.roundToInt()} km/h"
                    )
                    WeatherMetricItem(
                        iconRes = R.drawable.weather_10,
                        value = "${weather.precipitation} mm"
                    )
                    WeatherMetricItem(
                        iconRes = R.drawable.ic_weather_thermometer,
                        value = "${weather.maxTemp.roundToInt()}° / ${weather.minTemp.roundToInt()}°"
                    )
                }

                if (weather.hourly.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = stringResource(id = R.string.weather_forecast_hourly),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        weather.hourly.forEach { item ->
                            HourlyWeatherItem(item = item, unit = weather.unit)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun WeatherMetricItem(
    iconRes: Int,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = androidx.compose.ui.graphics.Color.Unspecified
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HourlyWeatherItem(
    item: HourlyWeather,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = item.time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Icon(
            painter = painterResource(
                id = WeatherCode.getIconRes(item.weatherCode, item.isDay)
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = androidx.compose.ui.graphics.Color.Unspecified
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${item.temperature.roundToInt()}$unit",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
