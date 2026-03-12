package com.dev.habitwallpaper.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * JSON DTO for a single quote entry from the ZenQuotes API.
 * The API returns an array of these objects.
 *
 * Example response:
 * [{"q":"The secret of getting ahead is getting started.","a":"Mark Twain","h":"..."}]
 */
@JsonClass(generateAdapter = true)
data class QuoteDto(
    @Json(name = "q") val text: String,
    @Json(name = "a") val author: String
)

