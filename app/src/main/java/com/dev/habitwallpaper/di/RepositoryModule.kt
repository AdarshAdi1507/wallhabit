package com.dev.habitwallpaper.di

import com.dev.habitwallpaper.data.repository.HabitRepositoryImpl
import com.dev.habitwallpaper.domain.repository.HabitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository
}

