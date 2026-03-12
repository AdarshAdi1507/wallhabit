package com.dev.habitwallpaper.di

import android.content.Context
import androidx.room.Room
import com.dev.habitwallpaper.data.local.HabitDatabase
import com.dev.habitwallpaper.data.local.dao.AchievementDao
import com.dev.habitwallpaper.data.local.dao.HabitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            HabitDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // Added for development simplicity during schema changes
        .build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: HabitDatabase): AchievementDao {
        return database.achievementDao()
    }
}
