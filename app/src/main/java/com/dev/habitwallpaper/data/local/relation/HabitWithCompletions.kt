package com.dev.habitwallpaper.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.dev.habitwallpaper.data.local.entity.CompletionEntity
import com.dev.habitwallpaper.data.local.entity.HabitEntity

data class HabitWithCompletions(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val completions: List<CompletionEntity>
)
