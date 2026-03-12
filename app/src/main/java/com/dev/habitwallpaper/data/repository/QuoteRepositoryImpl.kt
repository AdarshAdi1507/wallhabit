package com.dev.habitwallpaper.data.repository

import com.dev.habitwallpaper.data.remote.QuoteApiService
import com.dev.habitwallpaper.domain.model.Quote
import com.dev.habitwallpaper.domain.repository.QuoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val api: QuoteApiService
) : QuoteRepository {

    override suspend fun getRandomQuote(): Quote {
        return try {
            val dto = api.getRandomQuote().firstOrNull()
                ?: return fallbackQuote()
            Quote(
                text = dto.text.trim(),
                author = dto.author.trim()
            )
        } catch (e: Exception) {
            fallbackQuote()
        }
    }

    // A small pool of offline fallbacks so the card is never empty.
    private fun fallbackQuote(): Quote {
        val fallbacks = listOf(
            Quote("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Aristotle"),
            Quote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
            Quote("The secret of getting ahead is getting started.", "Mark Twain"),
            Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
            Quote("Motivation is what gets you started. Habit is what keeps you going.", "Jim Ryun"),
            Quote("Small daily improvements over time lead to stunning results.", "Robin Sharma"),
            Quote("You don't rise to the level of your goals, you fall to the level of your systems.", "James Clear")
        )
        return fallbacks.random()
    }
}

