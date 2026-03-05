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
    val reminderTime: Long? = null,
    val reminderDays: String? = null, // Store as comma-separated day numbers (1-7)
    val createdAt: Long,
    val isWallpaperSelected: Boolean = false
)
