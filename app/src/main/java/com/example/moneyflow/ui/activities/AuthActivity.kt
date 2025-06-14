package com.example.moneyflow.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyflow.R
import com.example.moneyflow.databinding.ActivityAuthBinding
import com.example.moneyflow.ui.viewmodels.AuthViewModel
import com.example.moneyflow.utils.DailyNotificationReceiver
import com.example.moneyflow.utils.DailyPurchaseReminderReceiver
import com.example.moneyflow.utils.PreferenceManager
import java.util.Calendar
import java.util.concurrent.Executor

@Suppress("DEPRECATION")
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var indicators: List<ImageView>
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var isSetupMode: Boolean = false
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppTheme()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mode = intent.getIntExtra(EXTRA_MODE, MODE_AUTH)
        isSetupMode = PreferenceManager.isFirstLaunch(this)

        when (mode) {
            MODE_CHANGE_PIN -> setupForChangePin()
            else -> {
                if (isSetupMode) {
                    setupForFirstLaunch()
                    viewModel.setSetupModeFirstEntry()
                } else {
                    setupForAuth()
                }
            }
        }

        setUpInsets()
        setUpIndicators()
        setUpNumberPadClickListeners()
        setupObservers()
        insertDefaultDbData()
        viewModel.getCurrency()
        createNotificationChannel()
        scheduleDailyNotification()

        binding.buttonErase.setOnClickListener {
            vibrate(50)
            viewModel.removeLastDigit()
        }

        binding.buttonExit.setOnClickListener {
            vibrate(50)
            if (mode == MODE_CHANGE_PIN) {
                finish()
            } else {
                finishAffinity()
            }
        }

        if (mode != MODE_CHANGE_PIN) {
            fingerprintAuth()
        }
    }

    private fun applyAppTheme() {
        when (PreferenceManager.getSelectedTheme(this)) {
            getString(R.string.theme_light) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            getString(R.string.theme_dark) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            getString(R.string.theme_system_default) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleDailyNotification() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            AlertDialog.Builder(this).setTitle(getString(R.string.permission_required))
                .setMessage(getString(R.string.please_allow_accurate_notifications))
                .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = "package:$packageName".toUri()
                    }
                    startActivity(intent)
                }.setNegativeButton(getString(R.string.cancel), null).show()
            return
        }

        val intent = Intent(this, DailyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
        )

        val reminderIntent = Intent(this, DailyPurchaseReminderReceiver::class.java)
        val reminderPendingIntent = PendingIntent.getBroadcast(
            this,
            9999,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val reminderCalendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (reminderCalendar.timeInMillis < System.currentTimeMillis()) {
            reminderCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, reminderCalendar.timeInMillis, reminderPendingIntent
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "daily_reminder", "Daily notifications", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.daily_notifications_about_plans)
        }

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val purchaseChannel = NotificationChannel(
            "purchase_reminder", "Purchase Reminder", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.daily_transactions_add_notification)
        }
        manager.createNotificationChannel(purchaseChannel)

    }

    private fun setupForChangePin() {
        binding.textViewTitle.visibility = View.VISIBLE
        binding.buttonFingerprint.visibility = View.GONE
        binding.buttonErase.visibility = View.VISIBLE
        binding.buttonExit.visibility = View.VISIBLE
        binding.textViewTitle.text = getString(R.string.enter_old_pin)
        viewModel.setChangePinMode()
    }

    private fun setupForFirstLaunch() {
        binding.buttonFingerprint.visibility = View.GONE
        binding.buttonErase.visibility = View.VISIBLE
        binding.buttonExit.visibility = View.VISIBLE
        binding.textViewTitle.text = getString(R.string.setup_pin_title)
    }

    private fun setupForAuth() {
        binding.textViewTitle.visibility = View.GONE
        binding.buttonExit.visibility = View.VISIBLE
        fingerprintAuth()
    }

    fun insertDefaultDbData() {
        if (!PreferenceManager.areDefaultCategoriesAdded(this)) {
            viewModel.insertDefaultCategories(this)
            viewModel.insertDefaultWallets(this)
            PreferenceManager.setDefaultCategories(this)
        }
    }

    private fun setupObservers() {
        viewModel.passwordState.observe(this) { state ->
            Log.d(
                "AuthActivity",
                "Password State: $state, Password Length: ${viewModel.password.value?.length}, isSetupMode: $isSetupMode, Mode: ${
                    intent.getIntExtra(
                        EXTRA_MODE, MODE_AUTH
                    )
                }"
            )
            when (state) {
                AuthViewModel.PasswordState.EMPTY -> clearAllIndicators()
                AuthViewModel.PasswordState.IN_PROGRESS -> refreshIndicators(true)
                AuthViewModel.PasswordState.COMPLETE -> {
                    when (intent.getIntExtra(EXTRA_MODE, MODE_AUTH)) {
                        MODE_CHANGE_PIN -> viewModel.handlePinChange(this)
                        else -> {
                            if (isSetupMode) {
                                viewModel.handleSetupMode(viewModel.password.value ?: "")
                            } else {
                                viewModel.checkPassword(this)
                            }
                        }
                    }
                }

                AuthViewModel.PasswordState.CORRECT -> handleCorrectPassword()
                AuthViewModel.PasswordState.INCORRECT -> handleIncorrectPassword()
                AuthViewModel.PasswordState.ERASED_LAST_DIGIT -> refreshIndicators(false)
            }
        }
        viewModel.setupState.observe(this) { state ->
            when (state) {
                AuthViewModel.SetupState.FIRST_ENTRY -> {
                    binding.textViewTitle.text = getString(R.string.setup_pin_title)
                }

                AuthViewModel.SetupState.CONFIRMATION -> {
                    binding.textViewTitle.text = getString(R.string.confirm_pin_title)
                }

                else -> Unit
            }
        }

        viewModel.changePinState.observe(this) { state ->
            when (state) {
                AuthViewModel.ChangePinState.ENTER_OLD_PIN -> {
                    binding.textViewTitle.text = getString(R.string.enter_old_pin)
                }

                AuthViewModel.ChangePinState.ENTER_NEW_PIN -> {
                    binding.textViewTitle.text = getString(R.string.enter_new_pin)
                }

                AuthViewModel.ChangePinState.CONFIRM_NEW_PIN -> {
                    binding.textViewTitle.text = getString(R.string.confirm_new_pin_title)
                }

                AuthViewModel.ChangePinState.SUCCESS -> {
                    Toast.makeText(this, R.string.pin_changed_successfully, Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }

                else -> Unit
            }
        }

        viewModel.navigateToMain.observe(this) { shouldNavigate ->
            if (shouldNavigate) {
                startActivity(MainActivity.newIntent(this))
                finish()
            }
        }

        viewModel.showError.observe(this) { showError ->
            if (showError) {
                val errorMessageResId = when (viewModel.changePinState.value) {
                    AuthViewModel.ChangePinState.ENTER_OLD_PIN -> R.string.incorrect_old_pin
                    AuthViewModel.ChangePinState.CONFIRM_NEW_PIN -> R.string.pins_dont_match
                    else -> R.string.pin_codes_dont_match
                }
                Toast.makeText(this, getString(errorMessageResId), Toast.LENGTH_SHORT).show()
                if (viewModel.changePinState.value != AuthViewModel.ChangePinState.SUCCESS) {
                    indicators.forEach { animateIndicatorsFail(it) }
                }
                viewModel.onErrorShown()
            }
        }

        viewModel.password.observe(this) { password ->
            if (!isSetupMode && intent.getIntExtra(EXTRA_MODE, MODE_AUTH) == MODE_AUTH) {
                if (password.isNotEmpty()) {
                    binding.buttonFingerprint.visibility = View.GONE
                    binding.buttonErase.visibility = View.VISIBLE
                } else {
                    binding.buttonFingerprint.visibility = View.VISIBLE
                    binding.buttonErase.visibility = View.GONE
                }
            } else if (isSetupMode) {
                binding.buttonErase.visibility =
                    if (password.isNotEmpty()) View.VISIBLE else View.GONE
                binding.buttonFingerprint.visibility = View.GONE
            } else if (intent.getIntExtra(EXTRA_MODE, MODE_AUTH) == MODE_CHANGE_PIN) {
                binding.buttonErase.visibility =
                    if (password.isNotEmpty()) View.VISIBLE else View.GONE
                binding.buttonFingerprint.visibility = View.GONE
            }
        }
    }

    private fun handleCorrectPassword() {
        switchIndicatorToGreen()
        indicators.forEach { animateIndicatorsScale(it) }
        vibrate(100)
    }

    private fun handleIncorrectPassword() {
        switchIndicatorToRed()
        vibrate(200)
        indicators.forEach { animateIndicatorsFail(it) }
    }

    fun vibrate(duration: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VibratorManager::class.java) as VibratorManager).defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun setUpIndicators() {
        indicators = listOf(
            binding.imageViewIndicator1,
            binding.imageViewIndicator2,
            binding.imageViewIndicator3,
            binding.imageViewIndicator4
        )
    }

    private fun animateIndicatorsFail(view: View) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.5f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.5f)
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.5f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.5f, 1f)

        scaleUpX.duration = 100
        scaleUpY.duration = 100
        scaleDownX.duration = 500
        scaleDownY.duration = 500

        val shake = ObjectAnimator.ofFloat(
            view, "translationX", 0f, 15f, -15f, 10f, -10f, 5f, -5f, 0f
        )
        shake.duration = 600

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleUpX, scaleUpY, scaleDownX, scaleDownY, shake)
        disableNumberPadButtons()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                viewModel.clearPassword()
                enableNumberPadButtons()
            }
        })
        animatorSet.start()
    }

    private fun animateIndicatorsScale(view: View) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.5f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.5f)

        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.5f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.5f, 1f)

        scaleUpX.duration = 100
        scaleUpY.duration = 100
        scaleDownX.duration = 300
        scaleDownY.duration = 300

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(listOf(scaleUpX, scaleUpY, scaleDownX, scaleDownY))
        animatorSet.start()
    }

    private fun disableNumberPadButtons() {
        getNumberPadButtons().forEach {
            it.isClickable = false
        }
    }

    private fun enableNumberPadButtons() {
        getNumberPadButtons().forEach {
            it.isClickable = true
        }
    }

    private fun refreshIndicators(shouldAnimate: Boolean) {
        clearAllIndicators()
        val length = viewModel.password.value?.length ?: 0
        for (i in 0 until length) {
            indicators[i].setImageResource(R.drawable.circle_indicator_blue)
            if (shouldAnimate) {
                animateIndicatorsScale(indicators[length - 1])
            }
        }
    }

    private fun switchIndicatorToRed() {
        indicators.forEach {
            it.setImageResource(R.drawable.circle_indicator_red)
        }
    }

    private fun switchIndicatorToGreen() {
        indicators.forEach {
            it.setImageResource(R.drawable.circle_indicator_green)
        }
    }

    private fun clearAllIndicators() {
        indicators.forEach {
            it.setImageResource(R.drawable.circle_indicator_gray)
        }
    }

    private fun setUpNumberPadClickListeners() {
        getNumberPadButtons().forEach { button ->
            button.setOnClickListener {
                vibrate(50)
                viewModel.addDigit(button.text.toString().toInt())
            }
        }
    }

    private fun fingerprintAuth() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    switchIndicatorToGreen()
                    indicators.forEach { animateIndicatorsScale(it) }
                    startActivity(MainActivity.newIntent(this@AuthActivity))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.authentication_failed_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.auth_title))
            .setNegativeButtonText(getString(R.string.auth_negative_button_text)).build()

        binding.buttonFingerprint.setOnClickListener {
            vibrate(50)
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun getNumberPadButtons(): List<Button> {
        return listOf(
            binding.button0,
            binding.button1,
            binding.button2,
            binding.button3,
            binding.button4,
            binding.button5,
            binding.button6,
            binding.button7,
            binding.button8,
            binding.button9
        )
    }

    private fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        const val EXTRA_MODE = "auth_mode"
        const val MODE_AUTH = 0
        const val MODE_CHANGE_PIN = 1

        fun newIntent(context: Context, mode: Int = MODE_AUTH): Intent {
            return Intent(context, AuthActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode)
            }
        }
    }
}