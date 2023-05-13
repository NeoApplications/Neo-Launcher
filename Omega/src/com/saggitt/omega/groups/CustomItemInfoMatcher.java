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

package com.saggitt.omega.groups;

import android.content.ComponentName;
import android.os.UserHandle;

import com.android.launcher3.LauncherSettings;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.shortcuts.ShortcutKey;
import com.android.launcher3.util.IntSet;

import java.util.HashSet;
import java.util.Set;

public interface CustomItemInfoMatcher {

    boolean matches(ItemInfo info, ComponentName cn);

    /**
     * Returns true if the itemInfo matches this check
     */
    default boolean matchesInfo(ItemInfo info) {
        if (info != null) {
            ComponentName cn = info.getTargetComponent();
            return cn != null && matches(info, cn);
        } else {
            return false;
        }
    }

    /**
     * Returns a new matcher with returns true if either this or {@param matcher} returns true.
     */
    default CustomItemInfoMatcher or(CustomItemInfoMatcher matcher) {
        return (info, cn) -> matches(info, cn) || matcher.matches(info, cn);
    }

    /**
     * Returns a new matcher with returns true if both this and {@param matcher} returns true.
     */
    default CustomItemInfoMatcher and(CustomItemInfoMatcher matcher) {
        return (info, cn) -> matches(info, cn) && matcher.matches(info, cn);
    }

    /**
     * Returns a new matcher with returns the opposite value of this.
     */
    default CustomItemInfoMatcher negate() {
        return (info, cn) -> !matches(info, cn);
    }

    static CustomItemInfoMatcher ofUser(UserHandle user) {
        return (info, cn) -> info.user.equals(user);
    }

    static CustomItemInfoMatcher ofComponents(HashSet<ComponentName> components, UserHandle user) {
        return (info, cn) -> components.contains(cn) && info.user.equals(user);
    }

    static CustomItemInfoMatcher ofPackages(Set<String> packageNames, UserHandle user) {
        return (info, cn) -> packageNames.contains(cn.getPackageName()) && info.user.equals(user);
    }

    static CustomItemInfoMatcher ofShortcutKeys(Set<ShortcutKey> keys) {
        return (info, cn) -> info.itemType == LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT &&
                keys.contains(ShortcutKey.fromItemInfo(info));
    }

    /**
     * Returns a matcher for items with provided ids
     */
    static CustomItemInfoMatcher ofItemIds(IntSet ids) {
        return (info, cn) -> ids.contains(info.id);
    }
}