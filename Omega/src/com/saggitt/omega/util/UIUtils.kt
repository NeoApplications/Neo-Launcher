package com.saggitt.omega.util

import android.content.Context
import android.graphics.drawable.Drawable

fun Context.getIcon(): Drawable = packageManager.getApplicationIcon(applicationInfo)