package com.dev.habitwallpaper.data.remote

import com.dev.habitwallpaper.data.remote.dto.QuoteDto
import retrofit2.http.GET

/**
 * Retrofit service interface for the ZenQuotes public API.
 * Base URL is configured in [NetworkModule].
 */
interface QuoteApiService {
    /**
     * Fetches a single random motivational quote.
     * Returns a JSON array with one element.
     */
    @GET("random")
    suspend fun getRandomQuote(): List<QuoteDto>
}

