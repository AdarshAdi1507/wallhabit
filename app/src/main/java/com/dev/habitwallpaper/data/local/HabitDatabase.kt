package com.dev.habitwallpaper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dev.habitwallpaper.data.local.dao.HabitDao
import com.dev.habitwallpaper.data.local.entity.CompletionEntity
import com.dev.habitwallpaper.data.local.entity.HabitEntity

@Database(entities = [HabitEntity::class, CompletionEntity::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        const val DATABASE_NAME = "habit_db"
    }
}
