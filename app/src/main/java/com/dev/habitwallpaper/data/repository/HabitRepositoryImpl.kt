package com.dev.habitwallpaper.data.repository

import com.dev.habitwallpaper.core.wallpaper.WallpaperManager
import com.dev.habitwallpaper.data.local.dao.AchievementDao
import com.dev.habitwallpaper.data.local.dao.HabitDao
import com.dev.habitwallpaper.data.local.entity.AchievementEntity
import com.dev.habitwallpaper.data.local.entity.CompletionEntity
import com.dev.habitwallpaper.data.local.entity.HabitEntity
import com.dev.habitwallpaper.data.local.relation.HabitWithCompletions
import com.dev.habitwallpaper.domain.model.Achievement
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.model.HabitCompletion
import com.dev.habitwallpaper.domain.model.MilestoneThresholds
import com.dev.habitwallpaper.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val achievementDao: AchievementDao,
    private val wallpaperManager: WallpaperManager
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
        wallpaperManager.triggerUpdate()
    }

    override suspend fun toggleCompletion(habitId: Long, date: LocalDate, value: Float): Achievement? {
        // Enforce no future completions
        if (date.isAfter(LocalDate.now())) return null

        val epochDay = date.toEpochDay()
        val existing = habitDao.getCompletionForDate(habitId, epochDay)
        
        var resultAchievement: Achievement? = null
        
        if (existing != null) {
            habitDao.deleteCompletion(existing)
            val newStreak = recalculateStreak(habitId)
            // If streak decreased, remove achievements that are no longer reached
            achievementDao.deleteAchievementsAboveStreak(habitId, newStreak)
        } else {
            habitDao.insertCompletion(CompletionEntity(habitId = habitId, date = epochDay, value = value))
            val newStreak = recalculateStreak(habitId)
            val milestone = MilestoneThresholds.getMilestoneForStreak(newStreak)
            
            if (milestone != null) {
                val alreadyHas = achievementDao.hasAchievement(habitId, milestone.value)
                if (!alreadyHas) {
                    val achievement = AchievementEntity(
                        habitId = habitId,
                        milestoneValue = milestone.value,
                        milestoneTitle = milestone.title,
                        achievedDate = LocalDate.now().toEpochDay()
                    )
                    achievementDao.insertAchievement(achievement)
                    resultAchievement = Achievement(
                        habitId = habitId,
                        milestoneValue = milestone.value,
                        milestoneTitle = milestone.title,
                        achievedDate = LocalDate.now()
                    )
                }
            }
        }
        
        wallpaperManager.triggerUpdate()
        return resultAchievement
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit.toEntity())
        wallpaperManager.triggerUpdate()
    }

    override suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabit(habitId)
        wallpaperManager.triggerUpdate()
    }

    override suspend fun pauseHabit(habitId: Long, isPaused: Boolean) {
        habitDao.updatePauseStatus(habitId, isPaused)
    }

    override fun getAchievementsForHabit(habitId: Long): Flow<List<Achievement>> {
        return achievementDao.getAchievementsForHabit(habitId).map { list ->
            list.map { 
                Achievement(
                    id = it.id,
                    habitId = it.habitId,
                    milestoneValue = it.milestoneValue,
                    milestoneTitle = it.milestoneTitle,
                    achievedDate = LocalDate.ofEpochDay(it.achievedDate)
                )
            }
        }
    }

    override suspend fun recalculateStreak(habitId: Long): Int {
        val habitWithCompletions = habitDao.getHabitWithCompletionsById(habitId).first()
        return habitWithCompletions?.let { 
            val completedDates = it.completions.map { c -> LocalDate.ofEpochDay(c.date) }
            calculateStreak(completedDates)
        } ?: 0
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
        icon = icon,
        isPaused = isPaused
    )

    private fun HabitWithCompletions.toDomain(): Habit {
        val completions = this.completions.map { 
            HabitCompletion(LocalDate.ofEpochDay(it.date), it.value) 
        }
        val completedDates = completions.map { it.date }
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
            isCompletedToday = completedDates.contains(today),
            currentStreak = calculateStreak(completedDates),
            completions = completions,
            isWallpaperSelected = this.habit.isWallpaperSelected,
            trackingType = this.habit.trackingType,
            goalValue = this.habit.goalValue,
            color = this.habit.color,
            icon = this.habit.icon,
            isPaused = this.habit.isPaused
        )
    }
}
