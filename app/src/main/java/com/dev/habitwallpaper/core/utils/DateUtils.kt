package com.dev.habitwallpaper.core.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    fun formatDate(date: LocalDate): String {
        return date.format(dateFormatter)
    }
}
