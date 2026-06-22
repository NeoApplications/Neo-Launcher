package com.neoapps.neolauncher.backup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileLoader(private val backupManager: BackupManager) {
    var callback: Callback? = null
    var meta: FileInfo? = null
    private var withPreview = false
    var loaded = false
    private var loading = false

    private val uiScope = CoroutineScope(Dispatchers.Main)

    fun loadMeta(withPreview: Boolean = false) {
        if (loading) return
        if (!loaded) {
            loading = true
            this.withPreview = withPreview

            uiScope.launch {
                loadMetaTask()
            }

        } else {
            callback?.onMetaLoaded()
        }
    }

    private suspend fun loadMetaTask() {
        withContext(Dispatchers.Main) {
            backupManager.meta
            if (withPreview) {
                backupManager.meta?.preview = backupManager.readPreview()
            }
            meta = backupManager.meta
            loaded = true
            callback?.onMetaLoaded()
        }
    }

    interface Callback {
        fun onMetaLoaded()
    }
}