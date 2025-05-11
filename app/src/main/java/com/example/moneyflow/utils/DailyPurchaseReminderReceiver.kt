package com.example.moneyflow.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.moneyflow.R
import com.example.moneyflow.ui.activities.AuthActivity
import java.util.Calendar

class DailyPurchaseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        val intentToOpenApp = Intent(context, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intentToOpenApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Показ уведомления
        val builder = NotificationCompat.Builder(context, "purchase_reminder")
            .setSmallIcon(R.drawable.logo_flow)
            .setContentTitle("Напоминание")
            .setContentText("Не забудьте внести покупки за сегодня")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(10000, builder.build())


        // Перезапланировать уведомление на следующий день
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val nextReminderTime = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1) // на завтра
        }

        val reminderIntent = Intent(context, DailyPurchaseReminderReceiver::class.java)
        val reminderPendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextReminderTime.timeInMillis,
            reminderPendingIntent
        )
    }
}
