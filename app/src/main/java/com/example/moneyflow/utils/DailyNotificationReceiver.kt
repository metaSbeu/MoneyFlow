package com.example.moneyflow.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.moneyflow.R
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Plan
import com.example.moneyflow.ui.activities.AuthActivity
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import java.util.Calendar

class DailyNotificationReceiver : BroadcastReceiver() {

    @SuppressLint("CheckResult")
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent?) {
        if (!PreferenceManager.isNotificationType1Enabled(context)) return

        val planDao = MainDatabase.getDb(context.applicationContext as Application).planDao()
        planDao.getPlans().subscribe { plans ->
            for (plan in plans) {
                if (plan.isNotificationActive && isInNotificationWindow(plan)) {
                    sendNotification(context, plan)
                }
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextAlarmTime = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, DailyNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTime.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun isInNotificationWindow(plan: Plan): Boolean {
        val today = Calendar.getInstance()
        val currentTime = today.timeInMillis

        val targetDay = plan.targetNotificationDayOfMonth.coerceAtLeast(1)

        val targetDate = Calendar.getInstance().apply {
            val lastDayOfMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, minOf(targetDay, lastDayOfMonth))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (targetDate.timeInMillis < currentTime) {
            targetDate.add(Calendar.MONTH, 1)
            val lastDayOfNewMonth = targetDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            targetDate.set(Calendar.DAY_OF_MONTH, minOf(targetDay, lastDayOfNewMonth))
        }

        val startWindow = targetDate.clone() as Calendar
        startWindow.add(Calendar.DAY_OF_MONTH, -2)

        return currentTime in startWindow.timeInMillis..targetDate.timeInMillis
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendNotification(context: Context, plan: Plan) {
        val intent = Intent(context, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formatted = plan.sum.formatWithSpaces(context)
        val builder = NotificationCompat.Builder(context, "daily_reminder")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(context.getString(R.string.notification))
            .setContentText(
                context.getString(
                    R.string.payment_notification_text,
                    plan.name,
                    formatted
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(plan.id, builder.build())
    }
}