/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher3.model.data;

import static android.text.TextUtils.isEmpty;

import static androidx.core.util.Preconditions.checkNotNull;

import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APP_PAIR;
import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT;
import static com.android.launcher3.folder.FolderIcon.inflateIcon;
import static com.android.launcher3.logger.LauncherAtom.Attribute.EMPTY_LABEL;
import static com.android.launcher3.logger.LauncherAtom.Attribute.MANUAL_LABEL;
import static com.android.launcher3.logger.LauncherAtom.Attribute.SUGGESTED_LABEL;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.text.TextUtils;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.Item;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.folder.Folder;
import com.android.launcher3.folder.FolderNameInfos;
import com.android.launcher3.icons.BitmapRenderer;
import com.android.launcher3.logger.LauncherAtom;
import com.android.launcher3.logger.LauncherAtom.Attribute;
import com.android.launcher3.logger.LauncherAtom.FolderIcon;
import com.android.launcher3.logger.LauncherAtom.FromState;
import com.android.launcher3.logger.LauncherAtom.ToState;
import com.android.launcher3.model.ModelWriter;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ContentWriter;
import com.saggitt.omega.data.GestureItemInfoRepository;
import com.saggitt.omega.data.models.GestureItemInfo;
import com.saggitt.omega.folder.FirstItemProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Represents a folder containing shortcuts or apps.
 */
public class FolderInfo extends CollectionInfo {

    /**
     * The multi-page animation has run for this folder
     */
    public static final int FLAG_MULTI_PAGE_ANIMATION = 0x00000004;

    public static final int FLAG_MANUAL_FOLDER_NAME = 0x00000008;

    public static final int FLAG_COVER_MODE = 0x00000010;

    /**
     * Different states of folder label.
     */
    public enum LabelState {
        // Folder's label is not yet assigned( i.e., title == null). Eligible for auto-labeling.
        UNLABELED(Attribute.UNLABELED),

        // Folder's label is empty(i.e., title == ""). Not eligible for auto-labeling.
        EMPTY(EMPTY_LABEL),

        // Folder's label is one of the non-empty suggested values.
        SUGGESTED(SUGGESTED_LABEL),

        // Folder's label is non-empty, manually entered by the user
        // and different from any of suggested values.
        MANUAL(MANUAL_LABEL);

        private final LauncherAtom.Attribute mLogAttribute;

        LabelState(Attribute logAttribute) {
            this.mLogAttribute = logAttribute;
        }
    }

    public int options;

    public FolderNameInfos suggestedFolderNames;

    /**
     * The apps and shortcuts
     */
    public final ArrayList<ItemInfo> contents = new ArrayList<>();

    public String swipeUpAction;
    private ArrayList<FolderListener> mListeners = new ArrayList<>();
    public FirstItemProvider firstItemProvider = new FirstItemProvider(this);

    public FolderInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
        user = Process.myUserHandle();

        swipeUpAction = "";
    }

    @Override
    public void add(@NonNull ItemInfo item) {
        if (!willAcceptItemType(item.itemType)) {
            throw new RuntimeException("tried to add an illegal type into a folder");
        }
        add(item, contents.size(), true);
    }

    public void add(ItemInfo item, int rank, boolean animate) {
        rank = Utilities.boundToRange(rank, 0, contents.size());
        contents.add(rank, item);
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onAdd(item, rank);
        }
        itemsChanged(animate);
    }

    /**
     * Remove an app or shortcut. Does not change the DB.
     *
     * @param item
     */
    public void remove(ItemInfo item, boolean animate) {
        removeAll(Collections.singletonList(item), animate);
    }

    public void removeAll(List<ItemInfo> items, boolean animate) {
        contents.removeAll(items);
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onRemove(items);
        }
        itemsChanged(animate);
    }

    public void itemsChanged(boolean animate) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onItemsChanged(animate);
        }
    }
    /**
     * Returns the folder's contents as an unsorted ArrayList of {@link ItemInfo}. Includes
     * {@link WorkspaceItemInfo} and {@link AppPairInfo}s.
     */
    @NonNull
    @Override
    public ArrayList<ItemInfo> getContents() {
        return contents;
    }

    /**
     * Returns the folder's contents as an ArrayList of {@link WorkspaceItemInfo}. Note: Does not
     * return any {@link AppPairInfo}s contained in the folder, instead collects *their* contents
     * and adds them to the ArrayList.
     */
    @Override
    public ArrayList<WorkspaceItemInfo> getAppContents()  {
        ArrayList<WorkspaceItemInfo> workspaceItemInfos = new ArrayList<>();
        for (ItemInfo item : contents) {
            if (item instanceof WorkspaceItemInfo wii) {
                workspaceItemInfos.add(wii);
            } else if (item instanceof AppPairInfo api) {
                workspaceItemInfos.addAll(api.getAppContents());
            }
        }
        return workspaceItemInfos;
    }

    @Override
    public void onAddToDatabase(@NonNull ContentWriter writer) {
        super.onAddToDatabase(writer);
        writer.put(LauncherSettings.Favorites.OPTIONS, options);
    }

    public void addListener(FolderListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(FolderListener listener) {
        mListeners.remove(listener);
    }

    public interface FolderListener {
        void onAdd(ItemInfo item, int rank);

        void onRemove(List<ItemInfo> item);

        void onItemsChanged(boolean animate);

        void onTitleChanged(CharSequence title);

        default void onIconChanged() {
            // do nothing
        }
    }

    public boolean hasOption(int optionFlag) {
        return (options & optionFlag) != 0;
    }

    /**
     * @param option flag to set or clear
     * @param isEnabled whether to set or clear the flag
     * @param writer if not null, save changes to the db.
     */
    public void setOption(int option, boolean isEnabled, ModelWriter writer) {
        int oldOptions = options;
        if (isEnabled) {
            options |= option;
        } else {
            options &= ~option;
        }
        if (writer != null && oldOptions != options) {
            writer.updateItemInDatabase(this);
        }
    }

    public boolean isCoverMode() {
        return hasOption(FLAG_COVER_MODE);
    }

    public boolean isInDrawer() {
        return container == ItemInfo.NO_ID;
    }

    public void setCoverMode(boolean enable, ModelWriter modelWriter) {
        setOption(FLAG_COVER_MODE, enable, modelWriter);
        onIconChanged();
    }

    public ItemInfo getCoverInfo() {
        return firstItemProvider.getFirstItem();
    }

    public void setSwipeUpAction(@NonNull Context context, @Nullable String action) {
        swipeUpAction = action;
        GestureItemInfoRepository repository = new GestureItemInfoRepository(context);
        GestureItemInfo gestureItemInfo = repository.find(toComponentKey());
        if (gestureItemInfo != null && gestureItemInfo.getSwipeUp() != null) {
            gestureItemInfo.setSwipeUp(swipeUpAction);
            repository.update(gestureItemInfo);
        } else {
            gestureItemInfo = new GestureItemInfo(toComponentKey(), swipeUpAction, "");
            repository.insert(gestureItemInfo);
        }
    }

    public CharSequence getIconTitle(Folder folder) {
        if (!isCoverMode()) {
            if (!TextUtils.equals(folder.getDefaultFolderName(), title)) {
                return title;
            } else {
                return folder.getDefaultFolderName();
            }
        } else {
            WorkspaceItemInfo info = getCoverInfo();
            if (info.customTitle != null) {
                return info.customTitle;
            }
            return info.title;
        }
    }

    public ComponentKey toComponentKey() {
        return new ComponentKey(new ComponentName("com.neoapps.neolauncher.folder", String.valueOf(id)), Process.myUserHandle());
    }

    public void onIconChanged() {
        for (FolderListener listener : mListeners) {
            listener.onIconChanged();
        }
    }

    @Override
    protected String dumpProperties() {
        return String.format("%s; labelState=%s", super.dumpProperties(), getLabelState());
    }

    @NonNull
    @Override
    public LauncherAtom.ItemInfo buildProto(@Nullable CollectionInfo cInfo, Context context) {
        FolderIcon.Builder folderIcon = FolderIcon.newBuilder()
                .setCardinality(getContents().size());
        if (LabelState.SUGGESTED.equals(getLabelState())) {
            folderIcon.setLabelInfo(title.toString());
        }
        return getDefaultItemInfoBuilder(context)
                .setFolderIcon(folderIcon)
                .setRank(rank)
                .addItemAttributes(getLabelState().mLogAttribute)
                .setContainerInfo(getContainerInfo())
                .build();
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onTitleChanged(title);
        }
    }

    public void setTitle(@Nullable CharSequence title, ModelWriter modelWriter) {
        // Updating label from null to empty is considered as false touch.
        // Retaining null title(ie., UNLABELED state) allows auto-labeling when new items added.
        if (isEmpty(title) && this.title == null) {
            return;
        }

        // Updating title to same value does not change any states.
        if (title != null && title.equals(this.title)) {
            return;
        }

        this.title = title;
        LabelState newLabelState =
                title == null ? LabelState.UNLABELED
                        : title.length() == 0 ? LabelState.EMPTY :
                                getAcceptedSuggestionIndex().isPresent() ? LabelState.SUGGESTED
                                        : LabelState.MANUAL;

        if (newLabelState.equals(LabelState.MANUAL)) {
            options |= FLAG_MANUAL_FOLDER_NAME;
        } else {
            options &= ~FLAG_MANUAL_FOLDER_NAME;
        }
        if (modelWriter != null) {
            modelWriter.updateItemInDatabase(this);
        }
    }

    /**
     * Returns current state of the current folder label.
     */
    public LabelState getLabelState() {
        return title == null ? LabelState.UNLABELED
                : title.length() == 0 ? LabelState.EMPTY :
                        hasOption(FLAG_MANUAL_FOLDER_NAME) ? LabelState.MANUAL
                                : LabelState.SUGGESTED;
    }

    @NonNull
    @Override
    public ItemInfo makeShallowCopy() {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.copyFrom(this);
        return folderInfo;
    }

    @Override
    public void copyFrom(@NonNull ItemInfo info) {
        super.copyFrom(info);
        if (info instanceof FolderInfo fi) {
            contents.addAll(fi.getContents());
        }
    }

    /**
     * Returns index of the accepted suggestion.
     */
    public OptionalInt getAcceptedSuggestionIndex() {
        String newLabel = checkNotNull(title,
                "Expected valid folder label, but found null").toString();
        if (suggestedFolderNames == null || !suggestedFolderNames.hasSuggestions()) {
            return OptionalInt.empty();
        }
        CharSequence[] labels = suggestedFolderNames.getLabels();
        return IntStream.range(0, labels.length)
                .filter(index -> !isEmpty(labels[index])
                        && newLabel.equalsIgnoreCase(
                        labels[index].toString()))
                .sequential()
                .findFirst();
    }

    /**
     * Returns {@link FromState} based on current {@link #title}.
     */
    public LauncherAtom.FromState getFromLabelState() {
        switch (getLabelState()){
            case EMPTY:
                return LauncherAtom.FromState.FROM_EMPTY;
            case MANUAL:
                return LauncherAtom.FromState.FROM_CUSTOM;
            case SUGGESTED:
                return LauncherAtom.FromState.FROM_SUGGESTED;
            case UNLABELED:
            default:
                return LauncherAtom.FromState.FROM_STATE_UNSPECIFIED;
        }
    }

    /**
     * Returns {@link ToState} based on current {@link #title}.
     */
    public LauncherAtom.ToState getToLabelState() {
        if (title == null) {
            return LauncherAtom.ToState.TO_STATE_UNSPECIFIED;
        }

        // TODO: if suggestedFolderNames is null then it infrastructure issue, not
        // ranking issue. We should log these appropriately.
        if (suggestedFolderNames == null || !suggestedFolderNames.hasSuggestions()) {
            return title.length() > 0
                    ? LauncherAtom.ToState.TO_CUSTOM_WITH_EMPTY_SUGGESTIONS
                    : LauncherAtom.ToState.TO_EMPTY_WITH_EMPTY_SUGGESTIONS;
        }

        boolean hasValidPrimary = suggestedFolderNames != null && suggestedFolderNames.hasPrimary();
        if (title.length() == 0) {
            return hasValidPrimary ? LauncherAtom.ToState.TO_EMPTY_WITH_VALID_PRIMARY
                    : LauncherAtom.ToState.TO_EMPTY_WITH_VALID_SUGGESTIONS_AND_EMPTY_PRIMARY;
        }

        OptionalInt accepted_suggestion_index = getAcceptedSuggestionIndex();
        if (!accepted_suggestion_index.isPresent()) {
            return hasValidPrimary ? LauncherAtom.ToState.TO_CUSTOM_WITH_VALID_PRIMARY
                    : LauncherAtom.ToState.TO_CUSTOM_WITH_VALID_SUGGESTIONS_AND_EMPTY_PRIMARY;
        }

        switch (accepted_suggestion_index.getAsInt()) {
            case 0:
                return LauncherAtom.ToState.TO_SUGGESTION0;
            case 1:
                return hasValidPrimary ? LauncherAtom.ToState.TO_SUGGESTION1_WITH_VALID_PRIMARY
                        : LauncherAtom.ToState.TO_SUGGESTION1_WITH_EMPTY_PRIMARY;
            case 2:
                return hasValidPrimary ? LauncherAtom.ToState.TO_SUGGESTION2_WITH_VALID_PRIMARY
                        : LauncherAtom.ToState.TO_SUGGESTION2_WITH_EMPTY_PRIMARY;
            case 3:
                return hasValidPrimary ? LauncherAtom.ToState.TO_SUGGESTION3_WITH_VALID_PRIMARY
                        : LauncherAtom.ToState.TO_SUGGESTION3_WITH_EMPTY_PRIMARY;
            default:
                // fall through
        }
        return LauncherAtom.ToState.TO_STATE_UNSPECIFIED;
    }

    /**
     * Checks if {@code itemType} is a type that can be placed in folders.
     */
    public static boolean willAcceptItemType(int itemType) {
        return itemType == ITEM_TYPE_APPLICATION
                || itemType == ITEM_TYPE_DEEP_SHORTCUT
                || itemType == ITEM_TYPE_APP_PAIR;
    }

    public boolean useIconMode(Context context) {
        return isCoverMode();
    }

    public boolean usingCustomIcon(Context context) {
        return !isCoverMode();
    }

    public Drawable getIcon(Context context) {
        Launcher launcher = Launcher.getLauncher(context);
        if (isCoverMode()) return getCoverInfo().newIcon(context);
        return getFolderIcon(launcher);
    }

    public Drawable getFolderIcon(Launcher launcher) {
        int iconSize = launcher.getDeviceProfile().iconSizePx;
        LinearLayout dummy = new LinearLayout(launcher, null);
        com.android.launcher3.folder.FolderIcon icon =  inflateIcon(R.layout.folder_icon, launcher, dummy, this);
        icon.isCustomIcon = false;
        icon.getFolderBackground().setStartOpacity(1f);
        Bitmap b = BitmapRenderer.createHardwareBitmap(iconSize, iconSize, out -> {
            out.translate(iconSize / 2f, 0);
            icon.draw(out);
        });
        icon.unbind();
        return new BitmapDrawable(launcher.getResources(), b);
    }
}
