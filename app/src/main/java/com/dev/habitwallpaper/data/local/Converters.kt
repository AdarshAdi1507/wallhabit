package com.dev.habitwallpaper.data.local

import androidx.room.TypeConverter
import com.dev.habitwallpaper.domain.model.HabitCategory
import com.dev.habitwallpaper.domain.model.TrackingType

class Converters {
    @TypeConverter
    fun fromTrackingType(value: TrackingType): String {
        return value.name
    }

    @TypeConverter
    fun toTrackingType(value: String): TrackingType {
        return TrackingType.valueOf(value)
    }

    @TypeConverter
    fun fromHabitCategory(value: HabitCategory): String {
        return value.name
    }

    @TypeConverter
    fun toHabitCategory(value: String): HabitCategory {
        return HabitCategory.valueOf(value)
    }
}
