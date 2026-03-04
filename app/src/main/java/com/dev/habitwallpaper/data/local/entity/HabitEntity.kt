package com.dev.habitwallpaper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val durationDays: Int,
    val startDate: Long,
    val createdAt: Long,
    val isWallpaperSelected: Boolean = false
)
