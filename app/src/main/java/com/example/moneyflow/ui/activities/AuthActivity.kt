package com.example.moneyflow.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
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

    private val viewModel: AuthViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpInsets()
        setUpIndicators()
        setUpNumberPadClickListeners()
        setupObservers()
        insertDefaultDbData()

        binding.buttonErase.setOnClickListener {
            vibrate(50)
            viewModel.removeLastDigit()
            updateEraseButtonVisibility()
        }

        binding.buttonExit.setOnClickListener {
            vibrate(50)
            finish()
        }

        fingerprintAuth()
    }

    fun insertDefaultDbData() {
        if (!PreferenceManager.areDefaultCategoriesAdded(this)) {
            viewModel.insertDefaultCategories()
            viewModel.insertDefaultWallets()
            PreferenceManager.setDefaultCategories(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewModel.passwordState.observe(this, Observer { state ->
            when (state) {
                AuthViewModel.PasswordState.EMPTY -> clearAllIndicators()
                AuthViewModel.PasswordState.IN_PROGRESS -> {
                    refreshIndicators(true)
                    updateEraseButtonVisibility()
                }
                AuthViewModel.PasswordState.COMPLETE -> viewModel.checkPassword()
                AuthViewModel.PasswordState.CORRECT -> handleCorrectPassword()
                AuthViewModel.PasswordState.INCORRECT -> handleIncorrectPassword()
                AuthViewModel.PasswordState.ERASED_LAST_DIGIT -> refreshIndicators(false)
            }
        })

        viewModel.navigateToMain.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                startActivity(MainActivity.newIntent(this))
                viewModel.onNavigationComplete()
                finish()
            }
        })

        viewModel.showError.observe(this, Observer { showError ->
            if (showError) {
                Toast.makeText(this, "INCORRECT PASSWORD", Toast.LENGTH_SHORT).show()
                viewModel.onErrorShown()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleCorrectPassword() {
        switchIndicatorToGreen()
        vibrate(100)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleIncorrectPassword() {
        switchIndicatorToRed()
        vibrate(200)
        indicators.forEach { animateIndicatorsFail(it) }
    }

    private fun updateEraseButtonVisibility() {
        binding.buttonErase.visibility = if (viewModel.password.value?.isNotEmpty() == true) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpNumberPadClickListeners() {
        getNumberPadButtons().forEach { button ->
            button.setOnClickListener {
                vibrate(50)
                viewModel.addDigit(button.text.toString().toInt())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fingerprintAuth() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    switchIndicatorToGreen()
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
}