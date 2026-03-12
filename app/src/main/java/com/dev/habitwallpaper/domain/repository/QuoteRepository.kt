package com.dev.habitwallpaper.domain.repository

import com.dev.habitwallpaper.domain.model.Quote

/**
 * Domain contract for fetching motivational quotes.
 * The implementation decides whether quotes come from the network or a fallback.
 */
interface QuoteRepository {
    /**
     * Returns a random motivational [Quote].
     * On network failure a hardcoded fallback is returned so the app never crashes.
     */
    suspend fun getRandomQuote(): Quote
}

