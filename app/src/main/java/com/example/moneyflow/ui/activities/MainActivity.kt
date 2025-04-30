package com.example.moneyflow.ui.activities

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.moneyflow.R
import com.example.moneyflow.data.AlarmReceiver
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.NotificationWorker
import com.example.moneyflow.data.Plan
import com.example.moneyflow.databinding.ActivityMainBinding
import com.example.moneyflow.ui.fragments.home.HomeFragment
import com.example.moneyflow.ui.fragments.planning.PlanningFragment
import com.example.moneyflow.ui.fragments.settings.SettingsFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Calendar
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpInsets()

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigationHome -> HomeFragment()
                R.id.navigationPlanning -> PlanningFragment()
                R.id.navigationSettings -> SettingsFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, it)
                    .commit()
                true
            } == true
        }

        createNotificationChannel()
        scheduleAllAlarms()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    private fun scheduleAllAlarms() {
        val db = Room.databaseBuilder(applicationContext, MainDatabase::class.java, "database.db").build()
        val planDao = db.planDao()
        val disposable = planDao.getPlans()
            .subscribeOn(Schedulers.io()) // Выполняем запрос в фоновом потоке
            .observeOn(AndroidSchedulers.mainThread()) // Получаем результат в главном потоке
            .subscribe({ plans ->
                for (plan in plans) {
                    if (plan.isNotificationActive) {
                        scheduleAlarmForPlan(plan)
                    } else {
                        cancelAlarmForPlan(plan)
                    }
                }
                db.close()
            }, { error ->
                // Обработка ошибки, если запрос не удался
                error.printStackTrace()
                db.close()
            })
    }

    fun scheduleAlarmForPlan(plan: Plan) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        // Сначала создаем Intent и добавляем planId
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("planId", plan.id) // <-- Добавляем ДО создания PendingIntent
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            plan.id, // Уникальный ID для каждого PendingIntent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        // Для тестирования - установите на 1 минуту вперед от текущего времени
//        val calendar = Calendar.getInstance().apply {
//            add(Calendar.MINUTE, 1)
//        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, plan.targetNotificationDayOfMonth)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Если запланированное время уже прошло сегодня, планируем на следующий месяц
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.MONTH, 1)
            }
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        // Для отладки
        Log.d("AlarmDebug", "Alarm set for plan ${plan.id} at ${calendar.time}")
    }

    fun cancelAlarmForPlan(plan: Plan) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            plan.id,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

//    fun scheduleDailyNotificationCheck(context: Context) {
//        val constraints = Constraints.Builder()
//            .build()
//
//        // Создаем PeriodicWorkRequest, который будет запускаться раз в день
//        val dailyRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
//            .setConstraints(constraints)
//            .addTag("notificationWork") // Можно добавить тег для управления запросом
//            .build()
//
//        WorkManager.getInstance(context)
//            .enqueueUniquePeriodicWork(
//                "dailyNotificationWork", // Уникальное имя для этого периодического запроса
//                ExistingPeriodicWorkPolicy.KEEP, // Что делать, если уже есть запрос с таким именем
//                dailyRequest
//            )
//    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "money_flow_channel_id" // Используйте ту же константу, что и в AlarmReceiver
            val name = "Напоминания о платежах"
            val descriptionText = "Уведомления о предстоящих платежах"
            val importance = NotificationManager.IMPORTANCE_HIGH // Повысьте важность для теста
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            // Проверка
            if (notificationManager.getNotificationChannel(channelId) == null) {
                Log.e("Notification", "Channel creation failed!")
            }
        }
    }
    private fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    companion object {
        const val CHANNEL_ID = "money_flow_channel_id"

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            return intent
        }
    }
}