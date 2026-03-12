package com.dev.habitwallpaper.core.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and manages the periodic [QuoteNotificationWorker].
 *
 * The interval is intentionally a named constant so it can be changed to
 * [TimeUnit.DAYS] with a value of 1 for production without touching
 * any other part of the code.
 *
 * WorkManager enforces a minimum of 15 minutes for periodic work.
 * The 2-minute value used here is for development / testing convenience only;
 * WorkManager will clamp it to 15 minutes on release builds.
 */
@Singleton
class QuoteWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ── Interval configuration ────────────────────────────────────────────
    // DEV: every 15 minutes (WorkManager minimum; 2-min requests are clamped)
    // PROD: change to 1L and TimeUnit.DAYS
    private val intervalAmount = 15L
    private val intervalUnit = TimeUnit.MINUTES
    // ─────────────────────────────────────────────────────────────────────

    fun scheduleQuoteNotifications() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<QuoteNotificationWorker>(
            repeatInterval = intervalAmount,
            repeatIntervalTimeUnit = intervalUnit
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            QuoteNotificationWorker.WORK_NAME,
            // KEEP: never replace a running worker; a restart after data-clear
            // will re-enqueue because the unique name will no longer exist.
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelQuoteNotifications() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(QuoteNotificationWorker.WORK_NAME)
    }
}

