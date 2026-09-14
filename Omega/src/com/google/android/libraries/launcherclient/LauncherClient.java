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

package com.google.android.libraries.launcherclient;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.neoapps.neolauncher.preferences.NeoPrefs;

import java.lang.ref.WeakReference;

public class LauncherClient {
    private static int apiVersion = -1;

    private ILauncherOverlay overlay;
    public final IScrollCallback scrollCallback;

    public final BaseClientService baseClientService;
    public final LauncherClientService launcherClientService;

    public final BroadcastReceiver googleInstallListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            baseClientService.disconnect();
            launcherClientService.disconnect();
            LauncherClient.loadApiVersion(context);
            if ((activityState & 2) != 0) {
                connect();
            }
        }
    };

    private int activityState = 0;
    private int serviceState = 0;
    public int flags;

    public LayoutParams layoutParams;
    public OverlayCallback overlayCallback;
    public final Activity mActivity;

    public boolean isDestroyed = false;
    private Bundle layoutBundle;

    public static class OverlayCallback extends ILauncherOverlayCallback.Stub implements Callback {
        public LauncherClient launcherClient;
        private final Handler uiHandler = new Handler(Looper.getMainLooper(), this);
        public Window window;
        private boolean windowHidden = false;
        public WindowManager windowManager;
        int windowShift;

        @Override
        public final void overlayScrollChanged(float f) {
            uiHandler.removeMessages(2);
            Message.obtain(uiHandler, 2, f).sendToTarget();
            if (f > 0f && windowHidden) {
                windowHidden = false;
            }
        }

        @Override
        public final void overlayStatusChanged(int i) {
            Message.obtain(uiHandler, 4, i, 0).sendToTarget();
        }

        @Override
        public boolean handleMessage(Message message) {
            if (launcherClient == null) {
                return true;
            }

            switch (message.what) {
                case 2:
                    if ((launcherClient.serviceState & 1) != 0) {
                        float floatValue = (float) message.obj;
                        launcherClient.scrollCallback.onOverlayScrollChanged(floatValue);
                    }
                    return true;
                case 3:
                    WindowManager.LayoutParams attributes = window.getAttributes();
                    if ((Boolean) message.obj) {
                        attributes.x = windowShift;
                        attributes.flags |= FLAG_LAYOUT_NO_LIMITS;
                    } else {
                        attributes.x = 0;
                        attributes.flags &= -513;
                    }
                    windowManager.updateViewLayout(window.getDecorView(), attributes);
                    return true;
                case 4:
                    launcherClient.setServiceState(message.arg1);
                    if (launcherClient.scrollCallback instanceof ISerializableScrollCallback) {
                        ((ISerializableScrollCallback) launcherClient.scrollCallback).setPersistentFlags(message.arg1);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    public LauncherClient(Activity activity, IScrollCallback scrollCallback, StaticInteger flags) {
        mActivity = activity;
        this.scrollCallback = scrollCallback;
        baseClientService = new BaseClientService(activity, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
        this.flags = flags.mData;

        launcherClientService = LauncherClientService.getInstance(activity);
        launcherClientService.mClient = new WeakReference<>(this);
        overlay = launcherClientService.mOverlay;

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        intentFilter.addDataSchemeSpecificPart("com.google.android.googlequicksearchbox", 0);
        mActivity.registerReceiver(googleInstallListener, intentFilter);

        if (apiVersion <= 0) {
            loadApiVersion(activity);
        }

        connect();
        if (mActivity.getWindow() != null &&
                mActivity.getWindow().peekDecorView() != null &&
                mActivity.getWindow().peekDecorView().isAttachedToWindow()) {
            onAttachedToWindow();
        }
    }

    public void setEnableFeed(boolean enable) {
        if (enable) {
            flags |= 1;
        } else {
            flags &= ~1;
        }
    }

    public final void onAttachedToWindow() {
        if (!isDestroyed) {
            setLayoutParams(mActivity.getWindow().getAttributes());
        }
    }

    public final void onDetachedFromWindow() {
        if (!isDestroyed) {
            setLayoutParams(null);
        }
    }

    public final void onResume() {
        if (!isDestroyed) {
            activityState |= 2;
            if (overlay != null && layoutParams != null) {
                try {
                    if (apiVersion < 4) {
                        overlay.onResume();
                    } else {
                        overlay.setActivityState(activityState);
                    }
                } catch (RemoteException ignored) {
                }
            }
        }
    }

    public final void onPause() {
        if (!isDestroyed) {
            activityState &= -3;
            if (overlay != null && layoutParams != null) {
                try {
                    if (apiVersion < 4) {
                        overlay.onPause();
                    } else {
                        overlay.setActivityState(activityState);
                    }
                } catch (RemoteException ignored) {
                }
            }
        }
    }

    public final void onStart() {
        if (!isDestroyed) {
            launcherClientService.setStopped(false);
            connect();
            activityState |= 1;
            if (overlay != null && layoutParams != null) {
                try {
                    overlay.setActivityState(activityState);
                } catch (RemoteException ignored) {
                }
            }
        }
    }

    public final void onStop() {
        if (!isDestroyed) {
            launcherClientService.setStopped(true);
            baseClientService.disconnect();
            activityState &= -2;
            if (!(overlay == null || layoutParams == null)) {
                try {
                    overlay.setActivityState(activityState);
                } catch (RemoteException ignored) {
                }
            }
        }
    }

    public void onDestroy() {
        if (!isDestroyed) {
            try {
                mActivity.unregisterReceiver(googleInstallListener);
            } catch (IllegalArgumentException ignored) {
            }
            isDestroyed = true;
            baseClientService.disconnect();
            launcherClientService.disconnect();
        }
    }

    private void connect() {
        if (!isDestroyed && (!launcherClientService.connect() || !baseClientService.connect())) {
            mActivity.runOnUiThread(() -> setServiceState(0));
        }
    }

    public void reconnect() {
        baseClientService.disconnect();
        launcherClientService.disconnect();
        LauncherClient.loadApiVersion(mActivity);
        if ((activityState & 2) != 0) {
            connect();
        }
    }

    public final void setLayoutParams(LayoutParams layoutParams) {
        if (this.layoutParams != layoutParams) {
            this.layoutParams = layoutParams;
            if (this.layoutParams != null) {
                exchangeConfig();
            } else if (overlay != null) {
                try {
                    overlay.windowDetached(mActivity.isChangingConfigurations());
                } catch (RemoteException ignored) {
                }
                overlay = null;
            }
        }
    }

    public final void exchangeConfig() {
        if (overlay != null) {
            try {
                if (overlayCallback == null) {
                    overlayCallback = new OverlayCallback();
                }
                OverlayCallback overlayCallback = this.overlayCallback;
                overlayCallback.launcherClient = this;
                overlayCallback.windowManager = mActivity.getWindowManager();
                Point point = new Point();
                overlayCallback.windowManager.getDefaultDisplay().getRealSize(point);
                overlayCallback.windowShift = -Math.max(point.x, point.y);
                overlayCallback.window = mActivity.getWindow();
                if (apiVersion < 3) {
                    overlay.windowAttached(layoutParams, this.overlayCallback, flags);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("layout_params", layoutParams);
                    bundle.putParcelable("configuration", mActivity.getResources().getConfiguration());
                    bundle.putInt("client_options", flags);
                    if (layoutBundle != null) {
                        bundle.putAll(layoutBundle);
                    }
                    overlay.windowAttached2(bundle, this.overlayCallback);
                }
                if (apiVersion >= 4) {
                    overlay.setActivityState(activityState);
                } else if ((activityState & 2) != 0) {
                    overlay.onResume();
                } else {
                    overlay.onPause();
                }
            } catch (RemoteException ignored) {
            }
        }
    }

    public boolean isConnected() {
        return overlay != null;
    }

    public final void startScroll() {
        if (isConnected()) {
            try {
                overlay.startScroll();
            } catch (RemoteException ignored) {
            }
        }
    }

    public final void endScroll() {
        if (isConnected()) {
            try {
                overlay.endScroll();
            } catch (RemoteException ignored) {
            }
        }
    }

    public final void setScroll(float f) {
        if (isConnected()) {
            try {
                overlay.onScroll(f);
            } catch (RemoteException ignored) {
            }
        }
    }

    private int verifyAndGetAnimationFlags(int duration) {
        if ((duration <= 0) || (duration > 2047)) {
            throw new IllegalArgumentException("Invalid duration");
        }
        return 0x1 | duration << 2;
    }

    public final void hideOverlay(boolean feedRunning) {
        if (overlay != null) {
            try {
                overlay.closeOverlay(feedRunning ? 1 : 0);
            } catch (RemoteException ignored) {
            }
        }
    }

    public final void hideOverlay(int duration) {
        if (overlay != null) {
            try {
                overlay.closeOverlay(verifyAndGetAnimationFlags(duration));
            } catch (RemoteException ignored) {
            }
        }
    }

    public final void showOverlay(boolean feedRunning) {
        if (overlay != null) {
            try {
                overlay.openOverlay(feedRunning ? 1 : 0);
            } catch (RemoteException ignored) {
            }
        }
    }

    public final boolean startSearch(byte[] bArr, Bundle bundle) {
        if (apiVersion >= 6 && overlay != null) {
            try {
                return overlay.startSearch(bArr, bundle);
            } catch (Throwable e) {
                Log.e("DrawerOverlayClient", "Error starting session for search", e);
            }
        }
        return false;
    }

    public final void redraw() {
        if (layoutParams != null && apiVersion >= 7) {
            exchangeConfig();
        }
    }

    final void setOverlay(ILauncherOverlay overlay) {
        this.overlay = overlay;
        if (this.overlay == null) {
            setServiceState(0);
        } else if (layoutParams != null) {
            exchangeConfig();
        }
    }

    private void setServiceState(int serviceState) {
        if (this.serviceState != serviceState) {
            this.serviceState = serviceState;
            boolean isAttached = (serviceState & 1) != 0;
            boolean hotwordActive = (serviceState & 2) != 0;
            if (scrollCallback instanceof LauncherClientCallbacks) {
                ((LauncherClientCallbacks) scrollCallback).onServiceStateChanged(isAttached, hotwordActive);
            } else {
                scrollCallback.onServiceStateChanged(isAttached);
            }
        }
    }

    protected static Intent getIntent(Context context, boolean proxy) {
        String pkg = context.getPackageName();
        NeoPrefs prefs = NeoPrefs.getInstance();
        return new Intent("com.android.launcher3.WINDOW_OVERLAY")
                .setPackage(prefs.getFeedProvider().getStringValue())
                .setData(Uri.parse("app://" +
                                pkg +
                                ":" +
                                Process.myUid())
                        .buildUpon()
                        .appendQueryParameter("v", Integer.toString(7))
                        .appendQueryParameter("cv", Integer.toString(9))
                        .build());
    }

    private static void loadApiVersion(Context context) {
        ResolveInfo resolveService = context.getPackageManager().resolveService(getIntent(context, false),
                PackageManager.GET_META_DATA);
        apiVersion = resolveService == null || resolveService.serviceInfo.metaData == null ? 1
                : resolveService.serviceInfo.metaData.getInt("service.api.version", 1);
    }
}
