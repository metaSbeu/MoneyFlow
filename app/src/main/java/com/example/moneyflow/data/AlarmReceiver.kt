package com.example.moneyflow.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.example.moneyflow.R
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Received alarm!")
        val planId = intent.getIntExtra("planId", -1)
        if (planId == -1) {
            Log.e("AlarmReceiver", "No planId in intent!")
            return
        }

        Log.d("AlarmReceiver", "Processing planId: $planId")

        val db = Room.databaseBuilder(
            context.applicationContext,
            MainDatabase::class.java,
            "database.db"
        ).build()
        val planDao = db.planDao()

        Thread {
            val plan = planDao.getPlanById(planId)
            if (plan != null && plan.isNotificationActive) {
                showNotification(context, plan)
            }
            db.close()
        }.start()
    }

    private fun showNotification(context: Context, plan: Plan) {
        // Добавьте звук и вибрацию
        val builder = NotificationCompat.Builder(context, "money_flow_channel_id")
            .setSmallIcon(R.drawable.ic_bicycle)
            .setContentTitle("Напоминание о платеже")
            .setContentText("Сегодня в ${plan.name} необходимо внести ${plan.sum} ₽.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Измените на HIGH
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Вибрация
            .setAutoCancel(true)
            .setTimeoutAfter(3600000) // Автоотмена через 1 час

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(plan.id, builder.build())
            } catch (e: Exception) {
                Log.e("Notification", "Failed to show notification", e)
            }
        }
    }}
