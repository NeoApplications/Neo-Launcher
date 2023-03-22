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

package com.saggitt.omega.smartspace;

import static com.saggit.omega.smartspace.SmartspaceProto.CardWrapper;
import static com.saggit.omega.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard;
import static com.saggit.omega.smartspace.SmartspaceProto.SmartSpaceUpdate.SmartSpaceCard.Image;

import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;

public class NewCardInfo {
    public final SmartSpaceCard mCard;
    public final boolean mIsPrimary;
    public final PackageInfo mPackageInfo;
    public final long mPublishTime;
    public final Intent mIntent;

    public NewCardInfo(SmartSpaceCard smartspaceCard, Intent intent, boolean isPrimary, long publishTime, PackageInfo packageInfo) {
        mCard = smartspaceCard;
        mIsPrimary = isPrimary;
        mIntent = intent;
        mPublishTime = publishTime;
        mPackageInfo = packageInfo;
    }

    public boolean isPrimary() {
        return mIsPrimary;
    }

    public Bitmap retrieveIcon(Context context) {
        Image image = mCard.getIcon();
        if (image == null) {
            return null;
        }
        Bitmap bitmap = (Bitmap) retrieveFromIntent(image.getKey(), mIntent);
        if (bitmap != null) {
            return bitmap;
        }
        try {
            if (!TextUtils.isEmpty(image.getUri())) {
                return Media.getBitmap(context.getContentResolver(), Uri.parse(image.getUri()));
            }
            if (!TextUtils.isEmpty(image.getGsaResourceName())) {
                ShortcutIconResource shortcutIconResource = new ShortcutIconResource();
                shortcutIconResource.packageName = "com.google.android.googlequicksearchbox";
                shortcutIconResource.resourceName = image.getGsaResourceName();
                return createIconBitmap(shortcutIconResource, context);
            }
        } catch (Exception unused) {
            String sb = "retrieving bitmap uri=" +
                    image.getUri() +
                    " gsaRes=" +
                    image.getGsaResourceName();
            Log.e("NewCardInfo", sb);
        }
        return null;
    }

    public CardWrapper toWrapper(Context context) {
        CardWrapper.Builder cardWrapper = CardWrapper.newBuilder();
        Bitmap retrieveIcon = retrieveIcon(context);
        if (retrieveIcon != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            retrieveIcon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            cardWrapper.setIcon(ByteString.copyFrom(byteArrayOutputStream.toByteArray()));
        }
        cardWrapper.setCard(mCard);
        cardWrapper.setPublishTime(mPublishTime);
        PackageInfo packageInfo = mPackageInfo;
        if (packageInfo != null) {
            cardWrapper.setGsaVersionCode(packageInfo.versionCode);
            cardWrapper.setGsaUpdateTime(packageInfo.lastUpdateTime);
        }
        return cardWrapper.build();
    }

    private static Parcelable retrieveFromIntent(String str, Intent intent) {
        if (!TextUtils.isEmpty(str)) {
            return intent.getParcelableExtra(str);
        }
        return null;
    }

    static Bitmap createIconBitmap(ShortcutIconResource shortcutIconResource, Context context) {
        try {
            Resources resourcesForApplication = context.getPackageManager()
                    .getResourcesForApplication(shortcutIconResource.packageName);
            if (resourcesForApplication != null) {
                return BitmapFactory.decodeResource(resourcesForApplication,
                        resourcesForApplication
                                .getIdentifier(shortcutIconResource.resourceName, null, null));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public int getUserId() {
        return mIntent.getIntExtra("uid", -1);
    }

    public boolean shouldDiscard() {
        return mCard == null || mCard.getShouldDiscard();
    }
}
