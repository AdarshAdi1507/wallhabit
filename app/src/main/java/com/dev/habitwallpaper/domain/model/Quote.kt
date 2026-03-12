package com.dev.habitwallpaper.domain.model

/**
 * Pure domain model for a motivational quote.
 * Contains no Android/network/persistence dependencies.
 */
data class Quote(
    val text: String,
    val author: String
)

