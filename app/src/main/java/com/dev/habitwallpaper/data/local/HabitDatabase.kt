package com.dev.habitwallpaper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dev.habitwallpaper.data.local.dao.AchievementDao
import com.dev.habitwallpaper.data.local.dao.HabitDao
import com.dev.habitwallpaper.data.local.entity.AchievementEntity
import com.dev.habitwallpaper.data.local.entity.CompletionEntity
import com.dev.habitwallpaper.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, CompletionEntity::class, AchievementEntity::class],
    version = 11,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val DATABASE_NAME = "habit_db"
    }
}
