package com.dev.habitwallpaper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dev.habitwallpaper.data.local.dao.HabitDao
import com.dev.habitwallpaper.data.local.entity.CompletionEntity
import com.dev.habitwallpaper.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, CompletionEntity::class],
    version = 10,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        const val DATABASE_NAME = "habit_db"

        /**
         * Define migrations here. 
         * For example, if you change the schema and increment version to 11:
         * 
         * val MIGRATION_10_11 = object : Migration(10, 11) {
         *     override fun migrate(db: SupportSQLiteDatabase) {
         *         // Execute SQL to update schema
         *         // db.execSQL("ALTER TABLE habits ADD COLUMN new_column INTEGER NOT NULL DEFAULT 0")
         *     }
         * }
         */
    }
}
