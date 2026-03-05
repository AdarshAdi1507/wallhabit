package com.dev.habitwallpaper.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.features.notification.ReminderReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleHabitReminders(habit: Habit) {
        val time = habit.reminderTime ?: return
        
        // Schedule main reminder
        scheduleAlarm(habit, time, false)
        
        // Schedule 10-min pre-reminder
        val preTime = time.minusMinutes(10)
        scheduleAlarm(habit, preTime, true)
    }

    private fun scheduleAlarm(habit: Habit, time: LocalTime, isPreReminder: Boolean) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habitId", habit.id)
            putExtra("habitName", habit.name)
            putExtra("isPreReminder", isPreReminder)
        }

        val requestCode = habit.id.toInt() + (if (isPreReminder) 0 else 1000)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = LocalDateTime.now().with(time)
        val triggerAtMillis = if (calendar.isBefore(LocalDateTime.now())) {
            calendar.plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
            calendar.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}
