package com.saggitt.omega.data.models

import androidx.annotation.DrawableRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SearchProvider(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0,
    val name: String,
    @DrawableRes val iconId: Int,
    val searchUrl: String,
    val suggestionUrl: String?,
    val enabled: Boolean,
    val order: Int,
)