package com.dev.habitwallpaper

import android.app.Application
import androidx.room.Room
import com.dev.habitwallpaper.data.local.HabitDatabase
import com.dev.habitwallpaper.data.repository.HabitRepositoryImpl
import com.dev.habitwallpaper.domain.repository.HabitRepository

class HabitApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(
            this,
            HabitDatabase::class.java,
            HabitDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val repository: HabitRepository by lazy {
        HabitRepositoryImpl(database.habitDao())
    }
}
