package com.dev.habitwallpaper.di

import android.content.Context
import com.dev.habitwallpaper.core.notification.AlarmScheduler
import com.dev.habitwallpaper.core.wallpaper.WallpaperManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAlarmScheduler(@ApplicationContext context: Context): AlarmScheduler {
        return AlarmScheduler(context)
    }

    @Provides
    @Singleton
    fun provideWallpaperManager(@ApplicationContext context: Context): WallpaperManager {
        return WallpaperManager(context)
    }
}

