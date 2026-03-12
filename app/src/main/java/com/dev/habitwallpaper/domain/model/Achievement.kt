package com.dev.habitwallpaper.domain.model

import java.time.LocalDate

data class Achievement(
    val id: Long = 0,
    val habitId: Long,
    val milestoneValue: Int,
    val milestoneTitle: String,
    val achievedDate: LocalDate
)

object MilestoneThresholds {
    private val milestones = listOf(
        Milestone(3, "Getting Started"),
        Milestone(7, "One Week Strong"),
        Milestone(14, "Consistency Builder"),
        Milestone(21, "Habit Formation"),
        Milestone(30, "Discipline Level"),
        Milestone(50, "Momentum Builder"),
        Milestone(100, "Master Consistency"),
        Milestone(365, "Legendary Consistency")
    )

    fun getMilestoneForStreak(streak: Int): Milestone? {
        return milestones.find { it.value == streak }
    }

    fun getNextMilestone(currentStreak: Int): Milestone? {
        return milestones.find { it.value > currentStreak }
    }
}

data class Milestone(
    val value: Int,
    val title: String
)
