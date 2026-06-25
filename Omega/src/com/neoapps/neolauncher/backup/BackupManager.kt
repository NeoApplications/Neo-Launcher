/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.neoapps.neolauncher.backup

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.android.launcher3.LauncherFiles
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.neoapps.neolauncher.preferences.NeoPrefs
import com.neoapps.neolauncher.util.hasFlag
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(val context: Context, val uri: Uri) {
    val meta by lazy { readFileInfo() }

    private fun readFileInfo(): FileInfo? {
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            val inStream = FileInputStream(pfd?.fileDescriptor)
            val zipIs = ZipInputStream(inStream)
            var entry: ZipEntry?
            var fileInfo: FileInfo? = null
            try {
                while (true) {
                    entry = zipIs.nextEntry
                    if (entry == null) break
                    if (entry.name != FileInfo.FILE_NAME) continue
                    fileInfo = FileInfo.fromString(String(zipIs.readBytes(), StandardCharsets.UTF_8))
                    return fileInfo
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Unable to read meta for $uri", t)
                return fileInfo
            } finally {
                zipIs.close()
                inStream.close()
                pfd?.close()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to read meta for $uri", t)
            return null
        }
        return null
    }

    fun readPreview(): Pair<Bitmap?, Bitmap?>? {
        var entry: ZipEntry?
        var screenshot: Bitmap? = null
        var wallpaper: Bitmap? = null
        readZip { zipIs ->
            while (true) {
                entry = zipIs.nextEntry
                if (entry == null) break
                if (entry.name == "screenshot.png") {
                    screenshot = BitmapFactory.decodeStream(zipIs)
                } else if (entry.name == WALLPAPER_FILE_NAME) {
                    wallpaper = BitmapFactory.decodeStream(zipIs)
                }
            }
        }
        if (screenshot == wallpaper) return null // both are null
        return Pair(
            getScaledDownBitmap(screenshot, 1000, false),
            getScaledDownBitmap(wallpaper, 1000, false)
        )
    }

    fun getScaledDownBitmap(
        bitmap: Bitmap?,
        threshold: Int,
        isNecessaryToKeepOrig: Boolean
    ): Bitmap? {
        if (bitmap == null) return null

        val width = bitmap.width
        val height = bitmap.height
        var newWidth = width
        var newHeight = height

        if (width > height && width > threshold) {
            newWidth = threshold
            newHeight = (height * newWidth.toFloat() / width).toInt()
        }

        if (width in (height + 1)..threshold) {
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap
        }

        if (width < height && height > threshold) {
            newHeight = threshold
            newWidth = (width * newHeight.toFloat() / height).toInt()
        }

        if (height in (width + 1)..threshold) {
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap
        }

        if (width == height && width > threshold) {
            newWidth = threshold
            newHeight = newWidth
        }

        if (width == height && width <= threshold) {
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap
        }

        return getResizedBitmap(bitmap, newWidth, newHeight, isNecessaryToKeepOrig)
    }

    private fun getResizedBitmap(
        bm: Bitmap,
        newWidth: Int,
        newHeight: Int,
        isNecessaryToKeepOrig: Boolean
    ): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = (newWidth.toFloat()) / width
        val scaleHeight = (newHeight.toFloat()) / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap: Bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
        if (!isNecessaryToKeepOrig) {
            bm.recycle()
        }
        return resizedBitmap
    }

    private inline fun readZip(body: (ZipInputStream) -> Unit) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            val inStream = FileInputStream(pfd?.fileDescriptor)
            val zipIs = ZipInputStream(inStream)
            try {
                body(zipIs)
            } catch (t: Throwable) {
                Log.e(TAG, "Unable to read zip for $uri", t)
            } finally {
                zipIs.close()
                inStream.close()
                pfd?.close()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to read zip for $uri", t)
        }
    }

    fun restore(contents: Int): Boolean {
        try {
            val contextWrapper = ContextWrapper(context)
            val cacheParent = contextWrapper.cacheDir.parent
            val settingsFile = File(cacheParent, "shared_prefs/${LauncherFiles.SHARED_PREFERENCES_KEY}.xml")
            val deviceSettingsFile = File(cacheParent, "shared_prefs/${LauncherFiles.DEVICE_PREFERENCES_KEY}.xml")
            val datastoreFile = File(cacheParent, "files/datastore/neo_launcher.preferences_pb")
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                FileInputStream(pfd.fileDescriptor).use { fileInput ->
                    ZipInputStream(fileInput).use { zipInput ->
                        var entry: ZipEntry?
                        while (zipInput.nextEntry.also { entry = it } != null) {
                            val currentEntry = entry!!
                            val entryName = currentEntry.name
                            val targetFile = when {
                                entryName == "neo_launcher.preferences_pb" -> {
                                    if (!contents.hasFlag(INCLUDE_SETTINGS)) continue
                                    datastoreFile
                                }
                                entryName.startsWith("launcher_") ||
                                        entryName == "NeoLauncher.db" ||
                                        entryName == "NeoLauncher.db-shm" ||
                                        entryName == "NeoLauncher.db-wal" -> {
                                    if (!contents.hasFlag(INCLUDE_HOME_SCREEN)) continue
                                    contextWrapper.getDatabasePath(entryName)
                                }
                                entryName == LauncherFiles.WIDGET_PREVIEWS_DB ||
                                        entryName == LauncherFiles.APP_ICONS_DB -> {
                                    if (!contents.hasFlag(INCLUDE_DATABASES)) continue
                                    contextWrapper.getDatabasePath(entryName)
                                }
                                entryName == WALLPAPER_FILE_NAME -> {
                                    if (!contents.hasFlag(INCLUDE_WALLPAPER)) continue
                                    val bitmap = BitmapFactory.decodeStream(zipInput)
                                    WallpaperManager.getInstance(context).setBitmap(bitmap)
                                    continue
                                }
                                entryName == "${LauncherFiles.SHARED_PREFERENCES_KEY}.xml" -> {
                                    if (!contents.hasFlag(INCLUDE_SETTINGS)) continue
                                    settingsFile
                                }
                                entryName == "${LauncherFiles.DEVICE_PREFERENCES_KEY}.xml" -> {
                                    if (!contents.hasFlag(INCLUDE_SETTINGS)) continue
                                    deviceSettingsFile
                                }
                                else -> continue
                            }

                            FileOutputStream(targetFile).use { output ->
                                zipInput.copyTo(output)
                            }
                            Log.d(TAG, "Restored $entryName to ${targetFile.absolutePath}")
                        }
                    }
                }
            } ?: run {
                Log.e(TAG, "Failed to open file descriptor for $uri")
                return false
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to restore $uri", t)
            false
        }
        return true
    }

    fun delete(): Boolean {
        return try {
            context.contentResolver.delete(uri, null, null) != 0
        } catch (e: UnsupportedOperationException) {
            DocumentFile.fromSingleUri(context, uri)?.delete() ?: false
        }
    }

    fun share(context: Context) {
        val shareTitle = context.getString(R.string.backup_share_title)
        val shareText = context.getString(R.string.backup_share_text)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = MIME_TYPE
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        context.startActivity(Intent.createChooser(shareIntent, shareTitle))
    }

    fun getFolder(context: Context): File {
        val folder = File(
            context.getExternalFilesDir(null),
            "backup"
        )
        Log.d(TAG, "path: $folder")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    fun listLocalBackups(context: Context): List<BackupManager> {
        return getFolder(context)
            .listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?.map {
                FileProvider.getUriForFile(
                    context,
                    "com.saggitt.omega.fileprovider",
                    it
                )
            }
            ?.map { BackupManager(context, it) }
            ?: Collections.emptyList()
    }

    private fun prepareConfig(context: Context) {
        val prefs = NeoPrefs.getInstance()
        prefs.blockingEdit {
            restoreSuccess.setValue(true)
            developerOptionsEnabled.setValue(false)
        }
    }

    private fun cleanupConfig(devOptionsEnabled: Boolean) {
        val prefs = NeoPrefs.getInstance()
        prefs.blockingEdit {
            restoreSuccess.setValue(false)
            developerOptionsEnabled.setValue(devOptionsEnabled)
        }
    }

    @RequiresPermission(anyOf = ["android.permission.READ_WALLPAPER_INTERNAL", Manifest.permission.MANAGE_EXTERNAL_STORAGE])
    fun create(context: Context, name: String, location: Uri, contents: Int): Boolean {
        val contextWrapper = ContextWrapper(context)
        val files: MutableList<File> = ArrayList()
        if (contents.hasFlag(INCLUDE_HOME_SCREEN)) {
            contextWrapper.databaseList()
                .filter { it.matches(Regex(LauncherFiles.LAUNCHER_DB_CUSTOM)) }
                .forEach { files.add(contextWrapper.getDatabasePath(it)) }
            files.add(contextWrapper.getDatabasePath("NeoLauncher.db"))
            files.add(contextWrapper.getDatabasePath("NeoLauncher.db-shm"))
            files.add(contextWrapper.getDatabasePath("NeoLauncher.db-wal"))
        }
        if (contents.hasFlag(INCLUDE_DATABASES)) {
            files.add(contextWrapper.getDatabasePath(LauncherFiles.WIDGET_PREVIEWS_DB))
            files.add(contextWrapper.getDatabasePath(LauncherFiles.APP_ICONS_DB))
        }
        if (contents.hasFlag(INCLUDE_SETTINGS)) {
            val dir = contextWrapper.cacheDir.parent
            files.add(
                File(
                    dir,
                    "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml"
                )
            )
            files.add(
                File(
                    dir,
                    "shared_prefs/" + LauncherFiles.DEVICE_PREFERENCES_KEY + ".xml"
                )
            )
            files.add(File(dir, "files/datastore/neo_launcher.preferences_pb"))
        }

        val devOptionsEnabled = NeoPrefs.getInstance().developerOptionsEnabled
        prepareConfig(context)
        val pfd = context.contentResolver.openFileDescriptor(location, "w")
        val outStream = FileOutputStream(pfd?.fileDescriptor)
        val out = ZipOutputStream(BufferedOutputStream(outStream))
        val data = ByteArray(BUFFER)
        try {
            val metaEntry = ZipEntry(FileInfo.FILE_NAME)
            out.putNextEntry(metaEntry)
            out.write(getFileInfo(name, contents).toString().toByteArray())
            if (contents.hasFlag(INCLUDE_WALLPAPER)) {
                val wallpaperManager = WallpaperManager.getInstance(context)
                val wallpaperDrawable = wallpaperManager.drawable
                val wallpaperBitmap = Utilities.drawableToBitmap(wallpaperDrawable)
                if (wallpaperBitmap != null) {
                    val wallpaperEntry = ZipEntry(WALLPAPER_FILE_NAME)
                    out.putNextEntry(wallpaperEntry)
                    wallpaperBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            files.forEach { file ->
                if (!file.exists()) {
                    Log.w(TAG, "Skipping missing file: ${file.absolutePath}")
                    return@forEach
                }
                val input = BufferedInputStream(FileInputStream(file), BUFFER)
                val entry = ZipEntry(file.name)
                out.putNextEntry(entry)
                var count: Int
                while (true) {
                    count = input.read(data, 0, BUFFER)
                    if (count == -1) break
                    out.write(data, 0, count)
                }
                input.close()
            }
            Log.e(TAG, "Success to create backup")
            return true
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to create backup", t)
            return false
        } finally {
            out.close()
            outStream.close()
            pfd?.close()
            cleanupConfig(devOptionsEnabled.getValue())
        }
    }

    private fun getFileInfo(name: String, contents: Int) = FileInfo(
        name = name,
        contents = contents,
        timestamp = getTimestamp()
    )

    private fun getTimestamp(): String {
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
        return simpleDateFormat.format(Date())
    }

    companion object {
        private const val TAG = "BackupFile"
        const val INCLUDE_HOME_SCREEN = 1 shl 0
        const val INCLUDE_SETTINGS = 1 shl 1
        const val INCLUDE_WALLPAPER = 1 shl 2
        const val INCLUDE_DATABASES = 1 shl 3

        const val BUFFER = 2018

        const val EXTENSION = "zbk"
        const val MIME_TYPE = "application/vnd.omega.backup"
        val EXTRA_MIME_TYPES = arrayOf(MIME_TYPE, "application/x-zip", "application/octet-stream")

        const val WALLPAPER_FILE_NAME = "wallpaper.png"
    }
}