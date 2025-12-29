package com.neoapps.neolauncher.iconpack

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Process
import androidx.core.content.getSystemService
import com.android.launcher3.R
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.ComponentKey
import com.neoapps.neolauncher.data.models.IconPickerItem
import com.neoapps.neolauncher.icons.ClockMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SystemIconPack(context: Context, pkg: String) : IconPack(context, pkg) {
    override val label = context.getString(R.string.icon_pack_default)
    private var appMap = run {
        val profiles = UserCache.INSTANCE.get(context).userProfiles
        val launcherApps: LauncherApps = context.getSystemService<LauncherApps>()!!
        profiles
            .flatMap { launcherApps.getActivityList(null, Process.myUserHandle()) }
            .associateBy { ComponentKey(it.componentName, it.user) }
    }

    init {
        startLoad()
    }

    override fun reloadAppMap() {
        appMap = run {
            val profiles = UserCache.INSTANCE.get(context).userProfiles
            val launcherApps = context.getSystemService<LauncherApps>()!!
            profiles
                .flatMap { launcherApps.getActivityList(null, it) }
                .associateBy { ComponentKey(it.componentName, it.user) }
        }
    }

    override fun getIcon(componentName: ComponentName) =
        IconEntry(
            packPackageName,
            ComponentKey(componentName, Process.myUserHandle()).toString(),
            IconType.Normal
        )

    override fun getIcon(iconEntry: IconEntry, iconDpi: Int): Drawable? {
        val key = ComponentKey.fromString(iconEntry.name)
        val app = appMap[key] ?: return null
        return app.getIcon(iconDpi)
    }

    override fun getIcon(shortcutInfo: ShortcutInfo, iconDpi: Int): Drawable? {
        val key = ComponentKey.fromString(shortcutInfo.`package`)
        val app = appMap[key] ?: return null
        return app.getIcon(iconDpi)
    }

    override fun getCalendar(componentName: ComponentName): IconEntry? = null
    override fun getClock(entry: IconEntry): ClockMetadata? = null

    override fun getCalendars(): MutableSet<ComponentName> = mutableSetOf()
    override fun getClocks(): MutableSet<ComponentName> = mutableSetOf()

    override fun loadInternal() {
    }

    override fun getAllIcons(): Flow<List<IconPickerCategory>> = flow {
        val items = appMap
            .map { (key, info) ->
                IconPickerItem(
                    packPackageName = packPackageName,
                    drawableName = key.toString(),
                    label = info.label.toString(),
                    IconType.Normal
                )
            }
        emit(categorize(items))
    }.flowOn(Dispatchers.IO)
}
