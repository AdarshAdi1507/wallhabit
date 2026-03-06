package com.dev.habitwallpaper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dev.habitwallpaper.domain.model.HabitCategory
import com.dev.habitwallpaper.domain.model.TrackingType

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: HabitCategory = HabitCategory.GENERAL,
    val durationDays: Int,
    val startDate: Long,
    val reminderTime: Long? = null,
    val reminderDays: String? = null, // Store as comma-separated day numbers (1-7)
    val createdAt: Long,
    val isWallpaperSelected: Boolean = false,
    val trackingType: TrackingType = TrackingType.BINARY,
    val goalValue: Float = 1f,
    val color: Int? = null,
    val icon: String? = null
)
