package com.dev.habitwallpaper.data.repository

import com.dev.habitwallpaper.data.local.dao.HabitDao
import com.dev.habitwallpaper.data.local.entity.HabitEntity
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HabitRepositoryImpl(
    private val habitDao: HabitDao
) : HabitRepository {
    override suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit.toEntity())
    }

    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun Habit.toEntity(): HabitEntity = HabitEntity(
        id = id,
        name = name,
        durationDays = durationDays,
        startDate = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        createdAt = createdAt
    )

    private fun HabitEntity.toDomain(): Habit = Habit(
        id = id,
        name = name,
        durationDays = durationDays,
        startDate = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate(),
        createdAt = createdAt
    )
}
