package com.saggitt.omega.data.models

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SearchProvider(
    @PrimaryKey val id: String,
    val name: String,
    @DrawableRes val iconId: Int,
    val searchUrl: String,
    val suggestionUrl: String?,
    val enabled: Boolean,
    val order: Int,
)