/*
 * This file is part of Neo Launcher
 * Copyright (c) 2023   Neo Launcher Team
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

package com.saggitt.omega.smartspace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Process
import android.util.Log
import com.saggit.omega.smartspace.SmartspaceProto.SmartSpaceUpdate
import com.saggit.omega.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard

class SmartSpaceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val str = "SmartSpaceReceiver"
        val myUserId = Process.myUserHandle().hashCode()
        val str2 = "uid"
        if (myUserId != 0) {
            val str3 = "rebroadcast"
            if (!intent.getBooleanExtra(str3, false)) {
                intent.putExtra(str3, true)
                intent.putExtra(str2, myUserId)
                context.sendBroadcast(intent)
                return
            }
            return
        }
        if (!intent.hasExtra(str2)) {
            intent.putExtra(str2, myUserId)
        }
        val byteArrayExtra =
            intent.getByteArrayExtra("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CARD")
        if (byteArrayExtra != null) {
            val smartspaceUpdate = SmartSpaceUpdate.newBuilder().build()
            try {
                for (smartspaceCard in smartspaceUpdate.cardList) {
                    val isPrimary = smartspaceCard.cardPriority == 1
                    val z2 = smartspaceCard.cardPriority == 2
                    if (!isPrimary) {
                        if (!z2) {
                            val sb = "unrecognized card priority: " + smartspaceCard.cardPriority
                            Log.w(str, sb)
                        }
                    }
                    notify(smartspaceCard, context, intent, isPrimary)
                }
            } catch (e: Exception) {
                Log.e(str, "proto", e)
            }
        } else {
            val sb2 = "receiving update with no proto: " + intent.extras
            Log.e(str, sb2)
        }
    }

    private fun notify(
        smartspaceCard: SmartSpaceCard,
        context: Context,
        intent: Intent,
        isPrimary: Boolean
    ) {
        val currentTimeMillis = System.currentTimeMillis()
        val packageInfo: PackageInfo? = try {
            context.packageManager.getPackageInfo("com.google.android.googlequicksearchbox", 0)
        } catch (e: NameNotFoundException) {
            Log.w("SmartSpaceReceiver", "Cannot find GSA", e)
            null
        }
        val newCardInfo =
            NewCardInfo(
                smartspaceCard,
                intent,
                isPrimary,
                currentTimeMillis,
                packageInfo
            )
        SmartSpaceController.get(context).onNewCard(newCardInfo)
    }
}