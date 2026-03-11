package com.dev.habitwallpaper.core.wallpaper

import android.content.Context
import android.content.Intent

/**
 * Handles communication with the Live Wallpaper service.
 * Using the application context ensures we don't leak UI components.
 */
class WallpaperManager(private val context: Context) {
    
    fun triggerUpdate() {
        val intent = Intent("com.dev.habitwallpaper.UPDATE_WALLPAPER")
        // Always use applicationContext if this manager is held long-term
        context.applicationContext.sendBroadcast(intent)
    }
}
