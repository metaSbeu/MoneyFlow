package com.example.moneyflow.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyflow.R
import com.example.moneyflow.data.PreferenceManager
import com.example.moneyflow.databinding.ActivityAuthBinding
import com.example.moneyflow.ui.viewmodels.AuthViewModel
import java.util.concurrent.Executor

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var indicators: List<ImageView>
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var isSetupMode: Boolean = false
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
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

//        val notificationChannel = NotificationChannel(
//            "TEST_CHANNEL",
//            "TEST DESCRIPTION",
//            NotificationManager.IMPORTANCE_DEFAULT
//        )
//        val notificationManager = getSystemService(NotificationManager::class.java)
//        notificationManager.createNotificationChannel(notificationChannel)
//
//        val notification = NotificationCompat.Builder(this, "TEST_CHANNEL")
//            .setContentTitle("Test title")
//            .setContentText("Test text")
//            .setSmallIcon(R.drawable.ic_bicycle)
//            .build()
//        notificationManager.notify(1, notification)
//        scheduleDailyReminder(this)
    }

//    fun scheduleDailyReminder(context: Context) {
//        val DAILY_REMINDER_REQUEST_CODE = 123
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(context, DailyReminderReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            DAILY_REMINDER_REQUEST_CODE,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        val calendar = Calendar.getInstance(Locale.getDefault()).apply {
//            timeInMillis = System.currentTimeMillis()
//            set(Calendar.HOUR_OF_DAY, 23) // 20:00
//            set(Calendar.MINUTE, 56)
//            set(Calendar.SECOND, 50)
//            set(Calendar.MILLISECOND, 0)
//
//            // Если текущее время уже после 20:00, запланируйте на следующий день
//            if (timeInMillis <= System.currentTimeMillis()) {
//                add(Calendar.DAY_OF_YEAR, 1)
//            }
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(
//                AlarmManager.RTC_WAKEUP,
//                calendar.timeInMillis,
//                pendingIntent
//            )
//            android.util.Log.d("AlarmScheduler", "Exact alarm scheduled for: ${calendar.time}")
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setExact(
//                AlarmManager.RTC_WAKEUP,
//                calendar.timeInMillis,
//                pendingIntent
//            )
//            android.util.Log.d("AlarmScheduler", "Exact alarm scheduled for (KitKat+): ${calendar.time}")
//        } else {
//            alarmManager.set(
//                AlarmManager.RTC_WAKEUP,
//                calendar.timeInMillis,
//                pendingIntent
//            )
//            android.util.Log.d("AlarmScheduler", "Alarm scheduled (older versions): ${calendar.time}")
//        }
//    }

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
            viewModel.insertDefaultCategories()
            viewModel.insertDefaultWallets()
            PreferenceManager.setDefaultCategories(this)
        }
    }

    private fun setupObservers() {
        viewModel.passwordState.observe(this) { state ->
            android.util.Log.d(
                "AuthActivity",
                "Password State: $state, Password Length: ${viewModel.password.value?.length}, isSetupMode: $isSetupMode, Mode: ${
                    intent.getIntExtra(
                        EXTRA_MODE,
                        MODE_AUTH
                    )
                }"
            )
            when (state) {
                AuthViewModel.PasswordState.EMPTY -> clearAllIndicators()
                AuthViewModel.PasswordState.IN_PROGRESS -> refreshIndicators(true)
                AuthViewModel.PasswordState.COMPLETE -> {
                    android.util.Log.d("AuthActivity", "Password COMPLETE triggered")
                    when (intent.getIntExtra(EXTRA_MODE, MODE_AUTH)) {
                        MODE_CHANGE_PIN -> viewModel.handlePinChange(this)
                        else -> {
                            if (isSetupMode) {
                                android.util.Log.d(
                                    "AuthActivity",
                                    "Handling setup mode with pin: ${viewModel.password.value}"
                                )
                                viewModel.handleSetupMode(viewModel.password.value ?: "")
                            } else {
                                android.util.Log.d(
                                    "AuthActivity",
                                    "Checking password: ${viewModel.password.value}"
                                )
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
                    else -> R.string.pin_codes_dont_match // Для режима setup
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
        indicators.forEach { animateIndicatorsScale(it) } // Анимация при успешном вводе
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

        vibrator.vibrate(
            VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
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
            view, "translationX",
            0f, 15f, -15f, 10f, -10f, 5f, -5f, 0f
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
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    switchIndicatorToGreen()
                    indicators.forEach { animateIndicatorsScale(it) } // Анимация при успехе биометрии
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

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_title))
            .setNegativeButtonText(getString(R.string.auth_negative_button_text))
            .build()

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