package com.dev.habitwallpaper.domain.model

import java.time.LocalDate

data class Habit(
    val id: Long = 0,
    val name: String,
    val durationDays: Int,
    val startDate: LocalDate,
    val createdAt: Long = System.currentTimeMillis()
)
