package com.example.moneyflow.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.moneyflow.R
import java.util.Calendar
import android.app.AlarmManager
import android.app.Application
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Plan

class DailyNotificationReceiver : BroadcastReceiver() {
    @SuppressLint("CheckResult")
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AlarmReceiver", "onReceive triggered!")

        val planDao = MainDatabase.getDb(context.applicationContext as Application).planDao()
        planDao.getPlans().subscribe({ plans ->
            Log.d("AlarmReceiver", "Retrieved ${plans.size} plans from DB")

            for (plan in plans) {
                Log.d("AlarmReceiver", "Checking plan: ${plan.name}, isNotificationActive = ${plan.isNotificationActive}")
                if (plan.isNotificationActive && isTodayTargetDay(plan)) {
                    sendNotification(context, plan)
                }
                scheduleNextNotification(context, plan) // Всегда переносим на следующий день
            }
        }, { error ->
            Log.e("AlarmReceiver", "Error retrieving plans: ${error.message}")
        })
    }

    private fun isTodayTargetDay(plan: Plan): Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        return plan.targetNotificationDayOfMonth == today
    }

    private fun sendNotification(context: Context, plan: Plan) {
        Log.d("AlarmReceiver", "Sending notification for plan: ${plan.name}")

        val builder = NotificationCompat.Builder(context, "daily_reminder")
            .setSmallIcon(R.drawable.logo_flow)
            .setContentTitle("Напоминание")
            .setContentText("Внесите оплату за ${plan.name} в размере ${plan.sum}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(plan.id, builder.build())
    }

    private fun scheduleNextNotification(context: Context, plan: Plan) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val newIntent = Intent(context, DailyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            plan.id,
            newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 13)
            set(Calendar.MINUTE, 40)
            set(Calendar.SECOND, 0)
        }

        Log.d("AlarmReceiver", "Scheduling next alarm for plan: ${plan.name} at ${calendar.time}")

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}
