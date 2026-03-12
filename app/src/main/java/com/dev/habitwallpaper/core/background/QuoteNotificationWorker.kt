package com.dev.habitwallpaper.core.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dev.habitwallpaper.MainActivity
import com.dev.habitwallpaper.R
import com.dev.habitwallpaper.domain.repository.QuoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker that fetches a motivational quote from [QuoteRepository]
 * and displays it as a high-importance notification.
 *
 * Annotated with @HiltWorker so Hilt can inject dependencies via the
 * [HiltWorkerFactory] registered in [HabitApplication].
 */
@HiltWorker
class QuoteNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val quoteRepository: QuoteRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "daily_motivation"
        const val CHANNEL_NAME = "Daily Motivation"
        const val CHANNEL_DESCRIPTION =
            "Inspirational quotes to motivate your habit consistency"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "quote_notification_periodic"
    }

    override suspend fun doWork(): Result {
        return try {
            val quote = quoteRepository.getRandomQuote()
            showNotification(quote.text, quote.author)
            Result.success()
        } catch (e: Exception) {
            // Retry once on transient failures; after that give up gracefully.
            if (runAttemptCount < 1) Result.retry() else Result.failure()
        }
    }

    private fun showNotification(quoteText: String, author: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        ensureChannelExists(notificationManager)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("\"$quoteText\"\n\n— $author")
            .setBigContentTitle("Daily Motivation")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Daily Motivation")
            .setContentText("\"$quoteText\" — $author")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannelExists(manager: NotificationManager) {
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }
}

