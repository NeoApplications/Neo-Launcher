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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore.Images
import android.text.TextUtils
import android.util.Log
import com.google.protobuf.ByteString
import com.saggit.omega.smartspace.SmartspaceProto.CardWrapper
import com.saggit.omega.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard
import com.saggitt.omega.util.Config
import java.io.ByteArrayOutputStream

class CardInfo(
    smartSpaceCard: SmartSpaceCard,
    intent: Intent,
    val isPrimary: Boolean,
    val publishTime: Long,
    val packageInfo: PackageInfo
) {
    val card: SmartSpaceCard = smartSpaceCard
    private val mIntent: Intent = intent

    fun retrieveIcon(context: Context): Bitmap? {
        val image = card.icon ?: return null
        val bitmap = retrieveFromIntent(image.key, mIntent) as Bitmap
        if (bitmap != null) {
            return bitmap
        }
        try {
            if (!TextUtils.isEmpty(image.uri)) {
                return Images.Media.getBitmap(context.contentResolver, Uri.parse(image.uri))
            }
            if (!TextUtils.isEmpty(image.gsaResourceName)) {
                val shortcutIconResource = ShortcutIconResource()
                shortcutIconResource.packageName = Config.GOOGLE_QSB
                shortcutIconResource.resourceName = image.gsaResourceName
                return createIconBitmap(shortcutIconResource, context)
            }
        } catch (unused: Exception) {
            val sb = "retrieving bitmap uri=" +
                    image.uri +
                    " gsaRes=" +
                    image.gsaResourceName
            Log.e("NewCardInfo", sb)
        }
        return null
    }

    fun toWrapper(context: Context?): CardWrapper? {
        val cardWrapper = CardWrapper.newBuilder()
        val retrieveIcon = retrieveIcon(context!!)
        if (retrieveIcon != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            retrieveIcon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            cardWrapper.icon = ByteString.copyFrom(byteArrayOutputStream.toByteArray())
        }
        cardWrapper.card = card
        cardWrapper.publishTime = publishTime
        val packageInfo = packageInfo
        cardWrapper.gsaVersionCode = packageInfo.versionCode
        cardWrapper.gsaUpdateTime = packageInfo.lastUpdateTime
        return cardWrapper.build()
    }

    private fun retrieveFromIntent(str: String, intent: Intent): Parcelable? {
        return if (!TextUtils.isEmpty(str)) {
            intent.getParcelableExtra(str)
        } else null
    }

    @SuppressLint("DiscouragedApi")
    private fun createIconBitmap(
        shortcutIconResource: ShortcutIconResource,
        context: Context
    ): Bitmap? {
        try {
            val resourcesForApplication = context.packageManager
                .getResourcesForApplication(shortcutIconResource.packageName)
            return BitmapFactory.decodeResource(
                resourcesForApplication,
                resourcesForApplication
                    .getIdentifier(shortcutIconResource.resourceName, null, null)
            )
        } catch (ignored: Exception) {
        }
        return null
    }

    fun getUserId(): Int {
        return mIntent.getIntExtra("uid", -1)
    }

    fun shouldDiscard(): Boolean {
        return card.shouldDiscard
    }
}