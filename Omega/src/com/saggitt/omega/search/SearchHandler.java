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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;

import com.android.launcher3.allapps.search.AllAppsSearchBarController.Callbacks;
import com.android.launcher3.allapps.search.SearchAlgorithm;

public class SearchHandler implements SearchAlgorithm, Callback {

    public static HandlerThread handlerThread;
    public Context mContext;
    public Handler mHandler;
    public boolean mInterruptActiveRequests;
    public Handler mUiHandler = new Handler(this);

    public SearchHandler(Context context) {
        mContext = context;
        if (handlerThread == null) {
            handlerThread = new HandlerThread("search-thread", -2);
            handlerThread.start();
        }
        mHandler = new Handler(handlerThread.getLooper(), this);
    }

    public void queryResult(SearchResult componentList) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(new Builder()
                    .scheme("content")
                    .authority("com.saggitt.launcher.appssearch")
                    .appendPath(componentList.mQuery)
                    .build(), null, null, null, null);
            int suggestIntentData = cursor.getColumnIndex("suggest_intent_data");
            while (cursor.moveToNext()) {
                componentList.mApps.add(AppSearchProvider.uriToComponent(Uri.parse(cursor.getString(suggestIntentData)), mContext));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Message.obtain(mUiHandler, 200, componentList).sendToTarget();
    }

    public void cancel(boolean interruptActiveRequests) {
        mInterruptActiveRequests = interruptActiveRequests;
        mHandler.removeMessages(100);
        if (interruptActiveRequests) {
            mUiHandler.removeMessages(200);
        }
    }

    public void doSearch(String query, Callbacks callback) {
        mHandler.removeMessages(100);
        Message.obtain(mHandler, 100, new SearchResult(query, callback)).sendToTarget();
    }

    public boolean handleMessage(Message message) {
        int i = message.what;
        if (i == 100) {
            queryResult((SearchResult) message.obj);
        } else if (i != 200) {
            return false;
        } else {
            if (!mInterruptActiveRequests) {
                SearchResult result = (SearchResult) message.obj;
                result.mCallbacks.onSearchResult(result.mQuery, result.mApps);
            }
        }
        return true;
    }
}
