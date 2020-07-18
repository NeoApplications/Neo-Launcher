/*
 * Copyright (C) 2019 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saggitt.omega.search;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.Parcelable;
import android.util.Log;

import com.android.launcher3.AppFilter;
import com.android.launcher3.AppInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherModel.ModelUpdateTask;
import com.android.launcher3.allapps.AppInfoComparator;
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm;
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm.StringMatcher;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.model.AllAppsList;
import com.android.launcher3.model.BgDataModel;
import com.android.launcher3.model.LoaderResults;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.LooperExecutor;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;

public class AppSearchProvider extends ContentProvider {

    public static final String[] sQuerySuggest = new String[]{
            "_id",
            "suggest_text_1",
            "suggest_icon_1",
            "suggest_intent_action",
            "suggest_intent_data"
    };
    public final PipeDataWriter<Future> mPipeDataWriter;
    public LauncherAppState mApp;
    public AppFilter mBaseFilter;
    public LooperExecutor mLooper;

    public AppSearchProvider() {
        mPipeDataWriter = new PipeDataWriter<Future>() {
            @Override
            public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType, Bundle opts, Future args) {
                AutoCloseOutputStream outStream = null;
                try {
                    outStream = new AutoCloseOutputStream(output);
                    ((Bitmap) args.get()).compress(CompressFormat.PNG, 100, outStream);
                } catch (Throwable e) {
                    Log.w("AppSearchProvider", "fail to write to pipe", e);
                }
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (Throwable ignored) {
                    }
                }
            }
        };
    }

    public static ComponentKey uriToComponent(Uri uri, Context context) {
        return new ComponentKey(ComponentName.unflattenFromString(uri.getQueryParameter("component")), UserManagerCompat.getInstance(context).getUserForSerialNumber(Long.parseLong(uri.getQueryParameter("user"))));
    }

    public static Uri buildUri(AppInfo appInfo, UserManagerCompat userManagerCompat) {
        return new Builder().scheme("content").authority("com.aosp.launcher.appssearch").appendQueryParameter("component", appInfo.componentName.flattenToShortString()).appendQueryParameter("user", Long.toString(userManagerCompat.getSerialNumberForUser(appInfo.user))).build();
    }

    public final Cursor listToCursor(List<AppInfo> list) {
        MatrixCursor matrixCursor = new MatrixCursor(sQuerySuggest, list.size());
        UserManagerCompat instance = UserManagerCompat.getInstance(getContext());
        int n = 0;
        for (AppInfo appInfo : list) {
            String uri = buildUri(appInfo, instance).toString();
            int n2 = n + 1;
            matrixCursor.newRow().add(Integer.valueOf(n)).add(appInfo.title.toString()).add(uri).add("com.google.android.apps.nexuslauncher.search.APP_LAUNCH").add(uri);
            n = n2;
        }
        return matrixCursor;
    }

    public Bundle call(String s, String s2, Bundle bundle) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.d("AppSearchProvider", "Content provider accessed on main thread");
            return null;
        } else if (!"loadIcon".equals(s)) {
            return super.call(s, s2, bundle);
        } else {
            try {
                final ComponentKey dl = uriToComponent(Uri.parse(s2), getContext());
                Callable<Bitmap> g = new Callable<Bitmap>() {
                    public Bitmap call() {
                        AppItemInfoWithIcon info = new AppItemInfoWithIcon(dl);
                        mApp.getIconCache().getTitleAndIcon(info, false);
                        return info.iconBitmap;
                    }
                };
                Bundle bundle2 = new Bundle();
                bundle2.putParcelable("suggest_icon_1", (Parcelable) mLooper.submit(g).get());
                return bundle2;
            } catch (Exception ex) {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to load icon ");
                sb.append(ex);
                Log.e("AppSearchProvider", sb.toString());
                return null;
            }
        }
    }

    public int delete(Uri uri, String s, String[] array) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.android.search.suggest";
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        mLooper = new LooperExecutor(MODEL_EXECUTOR.getLooper());
        mApp = LauncherAppState.getInstance(getContext());
        return true;
    }

    public ParcelFileDescriptor openFile(Uri uri, String s) throws FileNotFoundException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.e("AppSearchProvider", "Content provider accessed on main thread");
            return null;
        }
        try {
            final ComponentKey dl = uriToComponent(uri, getContext());
            String str = "image/png";
            return openPipeHelper(uri, "image/png", null, mLooper.submit(new Callable<Bitmap>() {
                public Bitmap call() {
                    AppItemInfoWithIcon info = new AppItemInfoWithIcon(dl);
                    mApp.getIconCache().getTitleAndIcon(info, false);
                    return info.iconBitmap;
                }
            }), mPipeDataWriter);
        } catch (Exception ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    public Cursor query(Uri uri, String[] array, String s, String[] array2, String s2) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.e("AppSearchProvider", "Content provider accessed on main thread");
            return new MatrixCursor(sQuerySuggest, 0);
        }
        List<AppInfo> list;
        try {
            SearchTask task = new SearchTask(uri.getLastPathSegment());
            mApp.getModel().enqueueModelUpdateTask(task);
            list = (List) task.mSearch.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("AppSearchProvider", "Error searching apps", ex);
            list = new ArrayList<>();
        }
        return listToCursor(list);
    }

    public int update(Uri uri, ContentValues contentValues, String s, String[] array) {
        throw new UnsupportedOperationException();
    }

    public AppFilter getBaseFilter() {
        if (mBaseFilter == null) {
            mBaseFilter = new AppFilter();
        }
        return mBaseFilter;
    }

    class SearchTask implements Callable<List<AppInfo>>, ModelUpdateTask {

        public final FutureTask<List<AppInfo>> mSearch = new FutureTask(this);
        public AllAppsList mAllAppsList;
        public LauncherAppState mApp;
        public BgDataModel mBgDataModel;
        public LauncherModel mModel;
        public String mQuery;

        public SearchTask(String query) {
            mQuery = query.toLowerCase();
        }

        @Override
        public void init(LauncherAppState appState, LauncherModel model, BgDataModel bgModel, AllAppsList allApps, Executor executor) {
            mApp = appState;
            mModel = model;
            mBgDataModel = bgModel;
            mAllAppsList = allApps;
        }

        public List<AppInfo> call() {
            if (!mModel.isModelLoaded()) {
                Log.d("AppSearchProvider", "Workspace not loaded, loading now");
                mModel.startLoaderForResults(new LoaderResults(mApp, mBgDataModel, mAllAppsList, 0, null));
            }
            if (mModel.isModelLoaded()) {
                ArrayList<AppInfo> list = new ArrayList();
                List<AppInfo> data = mAllAppsList.data;
                StringMatcher instance = StringMatcher.getInstance();
                for (AppInfo appInfo : data) {
                    if (DefaultAppSearchAlgorithm.matches(appInfo, mQuery, instance)) {
                        list.add(appInfo);
                        if (appInfo.usingLowResIcon()) {
                            mApp.getIconCache().getTitleAndIcon(appInfo, false);
                        }
                    }
                }
                Collections.sort(list, new AppInfoComparator(mApp.getContext()));
                return list;
            }
            Log.d("AppSearchProvider", "Loading workspace failed");
            return Collections.emptyList();
        }

        @Override
        public void run() {
            mSearch.run();
        }
    }
}
