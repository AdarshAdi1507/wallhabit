package com.dev.habitwallpaper.data.local.dao

import androidx.room.*
import com.dev.habitwallpaper.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: AchievementEntity): Long

    @Query("SELECT * FROM achievements WHERE habitId = :habitId ORDER BY milestoneValue DESC")
    fun getAchievementsForHabit(habitId: Long): Flow<List<AchievementEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM achievements WHERE habitId = :habitId AND milestoneValue = :milestoneValue)")
    suspend fun hasAchievement(habitId: Long, milestoneValue: Int): Boolean

    @Query("DELETE FROM achievements WHERE habitId = :habitId AND milestoneValue > :streak")
    suspend fun deleteAchievementsAboveStreak(habitId: Long, streak: Int)
}
