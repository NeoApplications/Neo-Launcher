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

import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;
import static com.saggit.omega.smartspace.SmartspaceProto.CardWrapper;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.android.launcher3.Alarm;
import com.saggitt.omega.util.Config;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SmartSpaceController implements Handler.Callback {
    enum Store {
        WEATHER("smartspace_weather"),
        CURRENT("smartspace_current");

        final String filename;

        Store(final String filename) {
            this.filename = filename;
        }
    }

    private static SmartSpaceController sInstance;
    private final SmartSpaceData mData;
    private final ProtoStore mProtoStore;
    private final Context mContext;
    private final Handler mUiHandler;
    private final Handler mWorker;

    private final ArrayList<SmartSpaceUpdateListener> mListeners = new ArrayList<>();
    public int mCurrentUserId;
    private boolean mHidePrivateData;
    private final Handler mBackgroundHandler;

    private final Alarm mAlarm;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public SmartSpaceController(Context context) {
        mContext = context;
        mProtoStore = new ProtoStore(mContext);
        mWorker = new Handler(MODEL_EXECUTOR.getLooper());
        mUiHandler = new Handler(Looper.getMainLooper());

        mCurrentUserId = Process.myUserHandle().hashCode();
        HandlerThread handlerThread = new HandlerThread("smartspace-background");
        handlerThread.start();
        mBackgroundHandler = new Handler(handlerThread.getLooper());

        mData = new SmartSpaceData();

        (mAlarm = new Alarm()).setOnAlarmListener(alarm -> onExpire());
        updateGsa();
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateGsa();
            }
        }, ActionIntentFilter.Companion.googleInstance(
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_CHANGED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_DATA_CLEARED));
    }

    public static SmartSpaceController get(final Context context) {
        if (sInstance == null) {
            sInstance = new SmartSpaceController(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onNewCard(final NewCardInfo newCardInfo) {
        if (newCardInfo != null) {
            if (newCardInfo.getUserId() != mCurrentUserId) {
                return;
            }
            mBackgroundHandler.post(() -> {
                final CardWrapper wrapper = newCardInfo.toWrapper(mContext);
                if (!mHidePrivateData) {
                    String sb = "smartspace_" +
                            mCurrentUserId +
                            "_" +
                            newCardInfo.isPrimary();
                    mProtoStore.store(wrapper, sb);
                }
                mUiHandler.post(() -> {
                    SmartSpaceCardView smartSpaceCard = newCardInfo.shouldDiscard() ? null :
                            SmartSpaceCardView.fromWrapper(mContext, wrapper, newCardInfo.isPrimary());
                    if (newCardInfo.isPrimary()) {
                        mData.setCurrentCard(smartSpaceCard);
                    } else {
                        mData.setWeatherCard(smartSpaceCard);
                    }
                    mData.handleExpire();
                    update();
                });
            });
        }
    }

    private SmartSpaceCardView loadSmartSpaceData(boolean z) {
        CardWrapper cardWrapper = CardWrapper.newBuilder().build();
        StringBuilder sb = new StringBuilder();
        sb.append("smartspace_");
        sb.append(mCurrentUserId);
        sb.append("_");
        sb.append(z);
        if (mProtoStore.load(sb.toString(), cardWrapper)) {
            return SmartSpaceCardView.fromWrapper(mContext, cardWrapper, !z);
        }
        return null;
    }

    public void reloadData() {
        mData.setCurrentCard(loadSmartSpaceData(true));
        mData.setWeatherCard(loadSmartSpaceData(false));
        update();
    }

    public void addListener(SmartSpaceUpdateListener smartSpaceUpdateListener) {
        mListeners.add(smartSpaceUpdateListener);
        SmartSpaceData smartSpaceData = mData;

        if (smartSpaceData != null && smartSpaceUpdateListener != null) {
            smartSpaceUpdateListener.onSmartSpaceUpdated(smartSpaceData);
        }
    }

    public void removeListener(SmartSpaceUpdateListener smartSpaceUpdateListener) {
        mListeners.remove(smartSpaceUpdateListener);
    }

    private void onExpire() {
        boolean hasWeather = mData.hasWeather();
        boolean hasCurrent = mData.hasCurrent();
        mData.handleExpire();

        if (hasWeather && !mData.hasWeather()) {
            df(null, SmartSpaceController.Store.WEATHER);
        }

        if (hasCurrent && !mData.hasCurrent()) {
            df(null, SmartSpaceController.Store.CURRENT);
            mContext.sendBroadcast(new Intent("com.google.android.apps.gsa.smartspace.EXPIRE_EVENT")
                    .setPackage(Config.GOOGLE_QSB)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public void setHideSensitiveData(boolean hide) {
        mHidePrivateData = hide;
        ArrayList<SmartSpaceUpdateListener> arrayList = new ArrayList<>(mListeners);
        for (int i = 0; i < arrayList.size(); i++) {
            arrayList.get(i).onSensitiveModeChanged(hide);
        }
        if (mHidePrivateData) {
            clearStore();
        }
    }

    private void clearStore() {
        String str = "smartspace_";
        String sb = "smartspace_" + mCurrentUserId + "_true";
        mProtoStore.store(null, sb);
        String sb2 = str + mCurrentUserId + "_false";
        mProtoStore.store(null, sb2);
    }

    private void update() {
        mAlarm.cancelAlarm();

        long expiresAtMillis = mData.getExpiresAtMillis();

        if (expiresAtMillis > 0) {
            mAlarm.setAlarm(expiresAtMillis);
        }

        ArrayList<SmartSpaceUpdateListener> listeners = new ArrayList<>(mListeners);
        for (SmartSpaceUpdateListener listener : listeners) {
            listener.onSmartSpaceUpdated(mData);
        }
    }

    public boolean handleMessage(Message message) {
        SmartSpaceCardView cardView = null;

        switch (message.what) {
            case 1:
                CardWrapper data = CardWrapper.newBuilder().build();
                SmartSpaceCardView weatherCard = mProtoStore.load(SmartSpaceController.Store.WEATHER.filename, data) ?
                        SmartSpaceCardView.fromWrapper(mContext, data, true) :
                        null;

                data = CardWrapper.newBuilder().build();
                SmartSpaceCardView eventCard = mProtoStore.load(SmartSpaceController.Store.CURRENT.filename, data) ?
                        SmartSpaceCardView.fromWrapper(mContext, data, false) :
                        null;

                Message.obtain(mUiHandler, 101, new SmartSpaceCardView[]{weatherCard, eventCard}).sendToTarget();
                break;
            case 2:
                //mProtoStore.store(SmartSpaceCardView.cQ(mContext, (NewCardInfo) message.obj), SmartSpaceController.Store.values()[message.arg1].filename);
                Message.obtain(mUiHandler, 1).sendToTarget();
                break;
            case 101:
                SmartSpaceCardView[] cardViews = (SmartSpaceCardView[]) message.obj;
                if (cardViews != null) {
                    mData.setWeatherCard(cardViews.length > 0 ?
                            cardViews[0] : null);

                    if (cardViews.length > 1) {
                        cardView = cardViews[1];
                    }

                    mData.setCurrentCard(cardView);
                }
                mData.handleExpire();
                update();
                break;
        }

        return true;
    }

    private Intent getIntent() {
        return new Intent("com.google.android.apps.gsa.smartspace.SETTINGS")
                .setPackage(Config.GOOGLE_QSB)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }


    private void updateGsa() {
        ArrayList<SmartSpaceUpdateListener> listeners = new ArrayList<>(mListeners);
        for (SmartSpaceUpdateListener listener : listeners) {
            listener.onGsaChanged();
        }
        onGsaChanged();
    }


    private void onGsaChanged() {
        Log.d("SmartSpaceController", "onGsaChanged");
        mContext.sendBroadcast(new Intent("com.google.android.apps.gsa.smartspace.ENABLE_UPDATE")
                .setPackage(Config.GOOGLE_QSB)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void df(NewCardInfo newCardInfo, SmartSpaceController.Store controller) {
        Message.obtain(mWorker, 2, controller.ordinal(), 0, newCardInfo).sendToTarget();
    }


    public void cW() {
        Message.obtain(this.mWorker, 1).sendToTarget();
    }

    public void printWriter(final String s, final PrintWriter printWriter) {
        printWriter.println();
        printWriter.println(s + "SmartspaceController");
        printWriter.println(s + "  weather " + mData.getCurrentCard());
        printWriter.println(s + "  current " + mData.getWeatherCard());
    }

    public boolean cY() {
        boolean b = false;
        final List<ResolveInfo> queryBroadcastReceivers = mContext.getPackageManager().queryBroadcastReceivers(getIntent(), 0);
        if (queryBroadcastReceivers != null) {
            b = !queryBroadcastReceivers.isEmpty();
        }
        return b;
    }
}
