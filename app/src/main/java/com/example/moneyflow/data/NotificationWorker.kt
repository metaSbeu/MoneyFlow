package com.example.moneyflow.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.moneyflow.R

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_MONTH)
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)

//        val notificationHour = 8
//        val notificationMinute = 21

        // Получаем доступ к базе данных Room
        val db = Room.databaseBuilder(
            applicationContext,
            MainDatabase::class.java,
            "database.db"
        ).build()
        val planDao = db.planDao()
        val activePlans = planDao.getAllWithNotificationActive()

        for (plan in activePlans) {
            if (plan.targetNotificationDayOfMonth == currentDay
//                && currentHour == notificationHour &&
//                currentMinute == notificationMinute
                ) {
                showNotification(applicationContext, plan)
            }
        }
        db.close()

        return Result.success()
    }
    private fun showNotification(context: Context, plan: Plan) {
        val builder = NotificationCompat.Builder(context, "money_flow_channel_id") // Замените на ваш ID канала
            .setSmallIcon(R.drawable.ic_bicycle) // Замените на иконку уведомления
            .setContentTitle("Напоминание о платеже")
            .setContentText("Сегодня в ${plan.name} необходимо внести ${plan.sum} ₽.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(plan.id, builder.build()) // Используйте ID плана как ID уведомления
        }
    }
}
