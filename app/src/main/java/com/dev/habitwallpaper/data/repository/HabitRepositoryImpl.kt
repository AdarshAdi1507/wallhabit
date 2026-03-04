package com.dev.habitwallpaper.data.repository

import com.dev.habitwallpaper.data.local.dao.HabitDao
import com.dev.habitwallpaper.data.local.entity.CompletionEntity
import com.dev.habitwallpaper.data.local.entity.HabitEntity
import com.dev.habitwallpaper.data.local.relation.HabitWithCompletions
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class HabitRepositoryImpl(
    private val habitDao: HabitDao
) : HabitRepository {
    override suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit.toEntity())
    }

    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabitsWithCompletions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getHabitById(id: Long): Flow<Habit?> {
        return habitDao.getHabitWithCompletionsById(id).map { it?.toDomain() }
    }

    override fun getWallpaperHabit(): Flow<Habit?> {
        return habitDao.getWallpaperHabitWithCompletions().map { it?.toDomain() }
    }

    override suspend fun setAsWallpaperHabit(id: Long) {
        habitDao.updateWallpaperSelection(id)
    }

    override suspend fun toggleCompletion(habitId: Long, date: LocalDate) {
        val epochDay = date.toEpochDay()
        val existing = habitDao.getCompletionForDate(habitId, epochDay)
        if (existing != null) {
            habitDao.deleteCompletion(existing)
        } else {
            habitDao.insertCompletion(CompletionEntity(habitId = habitId, date = epochDay))
        }
    }

    private fun calculateStreak(completedDates: List<LocalDate>): Int {
        if (completedDates.isEmpty()) return 0
        val sortedDates = completedDates.sortedDescending()
        var streak = 0
        var currentDate = LocalDate.now()
        
        if (!sortedDates.contains(currentDate)) {
            currentDate = currentDate.minusDays(1)
        }
        
        for (date in sortedDates) {
            if (date == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (date.isBefore(currentDate)) {
                break
            }
        }
        return streak
    }

    private fun Habit.toEntity(): HabitEntity = HabitEntity(
        id = id,
        name = name,
        durationDays = durationDays,
        startDate = startDate.toEpochDay(),
        createdAt = createdAt,
        isWallpaperSelected = isWallpaperSelected
    )

    private fun HabitWithCompletions.toDomain(): Habit {
        val completions = this.completions.map { LocalDate.ofEpochDay(it.date) }
        val today = LocalDate.now()
        return Habit(
            id = this.habit.id,
            name = this.habit.name,
            durationDays = this.habit.durationDays,
            startDate = LocalDate.ofEpochDay(this.habit.startDate),
            createdAt = this.habit.createdAt,
            isCompletedToday = completions.contains(today),
            currentStreak = calculateStreak(completions),
            completedDates = completions,
            isWallpaperSelected = this.habit.isWallpaperSelected
        )
    }
}
