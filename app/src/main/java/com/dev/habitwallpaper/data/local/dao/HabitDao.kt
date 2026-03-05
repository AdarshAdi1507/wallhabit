package com.dev.habitwallpaper.data.local.dao

import androidx.room.*
import com.dev.habitwallpaper.data.local.entity.CompletionEntity
import com.dev.habitwallpaper.data.local.entity.HabitEntity
import com.dev.habitwallpaper.data.local.relation.HabitWithCompletions
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Transaction
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabitsWithCompletions(): Flow<List<HabitWithCompletions>>

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitWithCompletionsById(id: Long): Flow<HabitWithCompletions?>

    /**
     * Observes the wallpaper habit. 
     * The dummy COUNT query ensures that Room tracks the 'completions' table 
     * so that the Flow emits whenever a habit is marked as done.
     */
    @Transaction
    @Query("""
        SELECT * FROM habits 
        WHERE isWallpaperSelected = 1 
        AND (SELECT COUNT(*) FROM completions) >= 0 
        LIMIT 1
    """)
    fun getWallpaperHabitWithCompletions(): Flow<HabitWithCompletions?>

    @Query("UPDATE habits SET isWallpaperSelected = 0")
    suspend fun deselectAllWallpaperHabits()

    @Query("UPDATE habits SET isWallpaperSelected = 1 WHERE id = :id")
    suspend fun selectWallpaperHabit(id: Long)

    @Transaction
    suspend fun updateWallpaperSelection(id: Long) {
        deselectAllWallpaperHabits()
        selectWallpaperHabit(id)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: CompletionEntity)

    @Delete
    suspend fun deleteCompletion(completion: CompletionEntity)

    @Query("SELECT * FROM completions WHERE habitId = :habitId AND date = :date")
    suspend fun getCompletionForDate(habitId: Long, date: Long): CompletionEntity?
}
