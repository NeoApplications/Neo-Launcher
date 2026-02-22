package com.neoapps.neolauncher.data

import android.content.Context
import com.android.launcher3.LauncherAppState
import com.android.launcher3.dagger.ApplicationContext
import com.android.launcher3.dagger.LauncherAppSingleton
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.MainThreadInitializedObject
import com.neoapps.neolauncher.data.models.IconOverride
import com.neoapps.neolauncher.data.models.IconPickerItem
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

@LauncherAppSingleton
class IconOverrideRepository  @Inject constructor(@ApplicationContext private val context: Context,) {

    private val scope = MainScope() + CoroutineName("IconOverrideRepository")
    private val dao = NeoLauncherDb.INSTANCE.get(context).iconOverrideDao()
    private var _overridesMap = mapOf<ComponentKey, IconPickerItem>()
    val overridesMap get() = _overridesMap
    private val updatePackageQueue = ConcurrentLinkedQueue<ComponentKey>()

    init {
        scope.launch {
            dao.observeAll()
                .flowOn(Dispatchers.Main)
                .collect { overrides ->
                    _overridesMap = overrides.associateBy(
                        keySelector = { it.target },
                        valueTransform = { it.iconPickerItem }
                    )
                    while (updatePackageQueue.isNotEmpty()) {
                        val target = updatePackageQueue.poll() ?: continue
                        updatePackageIcons(target)
                    }
                }
        }
    }

    suspend fun setOverride(target: ComponentKey, item: IconPickerItem) {
        dao.insert(IconOverride(target, item))
        updatePackageQueue.offer(target)
    }

    suspend fun deleteOverride(target: ComponentKey) {
        dao.delete(target)
        updatePackageQueue.offer(target)
    }

    fun observeTarget(target: ComponentKey) = dao.observeTarget(target)
    fun observeCount() = dao.observeCount()

    suspend fun deleteAll() {
        val targets = dao.getAll()
        dao.deleteAll()
        updatePackageIcons(targets)
    }

    private fun updatePackageIcons(target: ComponentKey) {
        val model = LauncherAppState.getInstance(context).model
        //TODO: Fix this
        //model.onPackageChanged(target.componentName.packageName, target.user)
    }

    private fun updatePackageIcons(target: List<IconOverride>) {
        val model = LauncherAppState.getInstance(context).model
        target.forEach {
            //TODO: Fix this
            //model.onPackageChanged(it.target.componentName.packageName, it.target.user)
        }
    }

    companion object {
        @JvmField
        val INSTANCE = MainThreadInitializedObject(::IconOverrideRepository)
    }
}
