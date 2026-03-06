package com.dev.habitwallpaper.data.repository

import com.dev.habitwallpaper.data.local.dao.HabitDao
import com.dev.habitwallpaper.data.local.entity.CompletionEntity
import com.dev.habitwallpaper.data.local.entity.HabitEntity
import com.dev.habitwallpaper.data.local.relation.HabitWithCompletions
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class HabitRepositoryImpl(
    private val habitDao: HabitDao
) : HabitRepository {
    override suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit.toEntity())
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

    override suspend fun toggleCompletion(habitId: Long, date: LocalDate, value: Float) {
        val epochDay = date.toEpochDay()
        val existing = habitDao.getCompletionForDate(habitId, epochDay)
        if (existing != null) {
            habitDao.deleteCompletion(existing)
        } else {
            habitDao.insertCompletion(CompletionEntity(habitId = habitId, date = epochDay, value = value))
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
        description = description,
        category = category,
        durationDays = durationDays,
        startDate = startDate.toEpochDay(),
        reminderTime = reminderTime?.let { (it.hour * 60 + it.minute).toLong() },
        reminderDays = reminderDays.joinToString(",") { it.value.toString() }.ifEmpty { null },
        createdAt = createdAt,
        isWallpaperSelected = isWallpaperSelected,
        trackingType = trackingType,
        goalValue = goalValue,
        color = color,
        icon = icon
    )

    private fun HabitWithCompletions.toDomain(): Habit {
        val completions = this.completions.map { LocalDate.ofEpochDay(it.date) }
        val today = LocalDate.now()
        val reminderLocalTime = this.habit.reminderTime?.let {
            LocalTime.of((it / 60).toInt(), (it % 60).toInt())
        }
        
        val daysList = this.habit.reminderDays?.split(",")?.map { 
            DayOfWeek.of(it.toInt()) 
        } ?: emptyList()

        return Habit(
            id = this.habit.id,
            name = this.habit.name,
            description = this.habit.description,
            category = this.habit.category,
            durationDays = this.habit.durationDays,
            startDate = LocalDate.ofEpochDay(this.habit.startDate),
            reminderTime = reminderLocalTime,
            reminderDays = daysList,
            createdAt = this.habit.createdAt,
            isCompletedToday = completions.contains(today),
            currentStreak = calculateStreak(completions),
            completedDates = completions,
            isWallpaperSelected = this.habit.isWallpaperSelected,
            trackingType = this.habit.trackingType,
            goalValue = this.habit.goalValue,
            color = this.habit.color,
            icon = this.habit.icon
        )
    }
}
