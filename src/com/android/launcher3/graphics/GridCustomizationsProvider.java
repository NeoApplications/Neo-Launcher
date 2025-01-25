/*
 * Copyright (C) 2023 The Android Open Source Project
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
package com.android.launcher3.graphics;

import static com.android.launcher3.LauncherPrefs.THEMED_ICONS;
import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR;
import static com.android.launcher3.util.Themes.isThemedIconEnabled;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.InvariantDeviceProfile.GridOption;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherPrefs;
import com.android.launcher3.model.BgDataModel;
import com.android.launcher3.util.Executors;
import com.android.launcher3.util.Preconditions;
import com.android.launcher3.util.RunnableList;
import com.android.systemui.shared.Flags;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Exposes various launcher grid options and allows the caller to change them.
 * APIs:
 *      /list_options: List the various available grip options, has following columns
 *          name: name of the grid
 *          rows: number of rows in the grid
 *          cols: number of columns in the grid
 *          preview_count: number of previews available for this grid option. The preview uri
 *                         looks like /preview/<grid-name>/<preview index starting with 0>
 *          is_default: true if this grid is currently active
 *
 *     /preview: Opens a file stream for the grid preview
 *
 *     /default_grid: Call update to set the current grid, with values
 *          name: name of the grid to apply
 */
public class GridCustomizationsProvider extends ContentProvider {

    private static final String TAG = "GridCustomizationsProvider";

    private static final String KEY_NAME = "name";
    private static final String KEY_ROWS = "rows";
    private static final String KEY_COLS = "cols";
    private static final String KEY_PREVIEW_COUNT = "preview_count";
    private static final String KEY_IS_DEFAULT = "is_default";

    private static final String KEY_LIST_OPTIONS = "/list_options";
    private static final String KEY_DEFAULT_GRID = "/default_grid";

    private static final String METHOD_GET_PREVIEW = "get_preview";

    private static final String GET_ICON_THEMED = "/get_icon_themed";
    private static final String SET_ICON_THEMED = "/set_icon_themed";
    private static final String ICON_THEMED = "/icon_themed";
    private static final String BOOLEAN_VALUE = "boolean_value";

    private static final String KEY_SURFACE_PACKAGE = "surface_package";
    private static final String KEY_CALLBACK = "callback";
    public static final String KEY_HIDE_BOTTOM_ROW = "hide_bottom_row";
    public static final String KEY_GRID_NAME = "grid_name";

    private static final int MESSAGE_ID_UPDATE_PREVIEW = 1337;
    private static final int MESSAGE_ID_UPDATE_GRID = 7414;

    // Set of all active previews used to track duplicate memory allocations
    private final Set<PreviewLifecycleObserver> mActivePreviews =
            Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        switch (uri.getPath()) {
            case KEY_LIST_OPTIONS: {
                MatrixCursor cursor = new MatrixCursor(new String[]{
                        KEY_NAME, KEY_ROWS, KEY_COLS, KEY_PREVIEW_COUNT, KEY_IS_DEFAULT});
                InvariantDeviceProfile idp = InvariantDeviceProfile.INSTANCE.get(getContext());
                for (GridOption gridOption : idp.parseAllGridOptions(getContext())) {
                    cursor.newRow()
                            .add(KEY_NAME, gridOption.name)
                            .add(KEY_ROWS, gridOption.numRows)
                            .add(KEY_COLS, gridOption.numColumns)
                            .add(KEY_PREVIEW_COUNT, 1)
                            .add(KEY_IS_DEFAULT, idp.numColumns == gridOption.numColumns
                                    && idp.numRows == gridOption.numRows);
                }
                return cursor;
            }
            case GET_ICON_THEMED:
            case ICON_THEMED: {
                MatrixCursor cursor = new MatrixCursor(new String[]{BOOLEAN_VALUE});
                cursor.newRow().add(BOOLEAN_VALUE, isThemedIconEnabled(getContext()) ? 1 : 0);
                return cursor;
            }
            default:
                return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/launcher_grid";
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String path = uri.getPath();
        Context context = getContext();
        if (path == null || context == null) {
            return 0;
        }
        switch (path) {
            case KEY_DEFAULT_GRID: {
                String gridName = values.getAsString(KEY_NAME);
                InvariantDeviceProfile idp = InvariantDeviceProfile.INSTANCE.get(context);
                // Verify that this is a valid grid option
                GridOption match = null;
                for (GridOption option : idp.parseAllGridOptions(context)) {
                    String name = option.name;
                    if (name != null && name.equals(gridName)) {
                        match = option;
                        break;
                    }
                }
                if (match == null) {
                    return 0;
                }

                idp.setCurrentGrid(context, gridName);
                if (Flags.newCustomizationPickerUi()) {
                    try {
                        // Wait for device profile to be fully reloaded and applied to the launcher
                        loadModelSync(context);
                    } catch (ExecutionException | InterruptedException e) {
                        Log.e(TAG, "Fail to load model", e);
                    }
                }
                context.getContentResolver().notifyChange(uri, null);
                return 1;
            }
            case ICON_THEMED:
            case SET_ICON_THEMED: {
                LauncherPrefs.get(context)
                        .put(THEMED_ICONS, values.getAsBoolean(BOOLEAN_VALUE));
                context.getContentResolver().notifyChange(uri, null);
                return 1;
            }
            default:
                return 0;
        }
    }

    /**
     * Loads the model in memory synchronously
     */
    private void loadModelSync(Context context) throws ExecutionException, InterruptedException {
        Preconditions.assertNonUiThread();
        BgDataModel.Callbacks emptyCallbacks = new BgDataModel.Callbacks() { };
        LauncherModel launcherModel = LauncherAppState.getInstance(context).getModel();
        MAIN_EXECUTOR.submit(
                () -> launcherModel.addCallbacksAndLoad(emptyCallbacks)
        ).get();

        Executors.MODEL_EXECUTOR.submit(() -> { }).get();
        MAIN_EXECUTOR.submit(
                () -> launcherModel.removeCallbacks(emptyCallbacks)
        ).get();
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (getContext().checkPermission("android.permission.BIND_WALLPAPER",
                Binder.getCallingPid(), Binder.getCallingUid())
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        if (!METHOD_GET_PREVIEW.equals(method)) {
            return null;
        }
        return getPreview(extras);
    }

    private synchronized Bundle getPreview(Bundle request) {
        RunnableList lifeCycleTracker = new RunnableList();
        try {
            PreviewSurfaceRenderer renderer = new PreviewSurfaceRenderer(
                    getContext(), lifeCycleTracker, request);
            PreviewLifecycleObserver observer =
                    new PreviewLifecycleObserver(lifeCycleTracker, renderer);

            // Destroy previous renderers to avoid any duplicate memory
            mActivePreviews.stream().filter(observer::isSameRenderer).forEach(o ->
                    MAIN_EXECUTOR.execute(o.lifeCycleTracker::executeAllAndDestroy));

            renderer.loadAsync();
            lifeCycleTracker.add(() -> renderer.getHostToken().unlinkToDeath(observer, 0));
            renderer.getHostToken().linkToDeath(observer, 0);

            Bundle result = new Bundle();
            result.putParcelable(KEY_SURFACE_PACKAGE, renderer.getSurfacePackage());

            Messenger messenger =
                    new Messenger(new Handler(UI_HELPER_EXECUTOR.getLooper(), observer));
            Message msg = Message.obtain();
            msg.replyTo = messenger;
            result.putParcelable(KEY_CALLBACK, msg);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Unable to generate preview", e);
            MAIN_EXECUTOR.execute(lifeCycleTracker::executeAllAndDestroy);
            return null;
        }
    }

    private static class PreviewLifecycleObserver implements Handler.Callback, DeathRecipient {

        public final RunnableList lifeCycleTracker;
        public final PreviewSurfaceRenderer renderer;
        public boolean destroyed = false;

        PreviewLifecycleObserver(RunnableList lifeCycleTracker, PreviewSurfaceRenderer renderer) {
            this.lifeCycleTracker = lifeCycleTracker;
            this.renderer = renderer;
            lifeCycleTracker.add(() -> destroyed = true);
        }

        @Override
        public boolean handleMessage(Message message) {
            if (destroyed) {
                return true;
            }

            switch (message.what) {
                case MESSAGE_ID_UPDATE_PREVIEW:
                    renderer.hideBottomRow(message.getData().getBoolean(KEY_HIDE_BOTTOM_ROW));
                    break;
                case MESSAGE_ID_UPDATE_GRID:
                    String gridName = message.getData().getString(KEY_GRID_NAME);
                    if (!TextUtils.isEmpty(gridName)) {
                        renderer.updateGrid(gridName);
                    }
                    break;
                default:
                    // Unknown command, destroy lifecycle
                    Log.d(TAG, "Unknown preview command: " + message.what + ", destroying preview");
                    MAIN_EXECUTOR.execute(lifeCycleTracker::executeAllAndDestroy);
                    break;
            }

            return true;
        }

        @Override
        public void binderDied() {
            MAIN_EXECUTOR.execute(lifeCycleTracker::executeAllAndDestroy);
        }

        /**
         * Two renderers are considered same if they have the same host token and display Id
         */
        public boolean isSameRenderer(PreviewLifecycleObserver plo) {
            return plo != null
                    && plo.renderer.getHostToken().equals(renderer.getHostToken())
                    && plo.renderer.getDisplayId() == renderer.getDisplayId();
        }
    }
}
