package com.example.moneyflow

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.example.moneyflow.databinding.ActivityAuthBinding
import java.util.concurrent.Executor

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val password = mutableListOf<Int>()
    private var indicatorRes = R.drawable.circle_indicator_blue
    private lateinit var indicators: List<ImageView>

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpInsets()
        indicators = binding.linearLayoutPasswordIndicator.children.map { it as ImageView }.toList()
        setUpNumberPadClickListeners()

        binding.buttonErase.setOnClickListener {
            vibrate()
            if (!password.isEmpty()) {
                password.removeAt(password.size - 1)
                switchFingerprintAndEraseButtons()
            }
            clearLastIndicator()
        }
        binding.buttonExit.setOnClickListener {
            vibrate()
            finish()
        }
        fingerprintAuth()
        biometricPrompt.authenticate(promptInfo)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ServiceCast")
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun Context.vibrate(duration: Long = 50) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }

    fun animateIndicators(view: View) {
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

    fun refreshIndicator() {
        val length = password.size
        if (length in 1..4) {
            val view = indicators[length - 1]
            view.setImageResource(indicatorRes)
            animateIndicators(view)
        }
    }

    fun switchIndicatorToGreen() {
        for (indicator in indicators) {
            indicator.setImageResource(R.drawable.circle_indicator_green)
        }
    }

    fun checkPassword() {
        if (password.size == 4) {
            if (password.joinToString("") == KEY_PASSWORD) {
                switchIndicatorToGreen()
//                binding.progressBar.visibility = View.VISIBLE
                password.clear()
                switchFingerprintAndEraseButtons()
                startActivity(MainActivity.newIntent(this))
                finish()
            } else {
                clearAllIndicators()
                password.clear()
                switchFingerprintAndEraseButtons()
                Toast.makeText(this, "INCORRECT PASSWORD", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearLastIndicator() {
        val length = password.size
        if (length in 0..3) {
            indicators[length].setImageResource(R.drawable.circle_indicator_gray)
        }
    }

    fun clearAllIndicators() {
        for (indicator in indicators) {
            indicator.setImageResource(R.drawable.circle_indicator_gray)
        }
    }

    fun switchFingerprintAndEraseButtons() {
        if (password.isEmpty()) {
            binding.buttonFingerprint.visibility = View.VISIBLE
            binding.buttonErase.visibility = View.GONE
        } else {
            binding.buttonFingerprint.visibility = View.GONE
            binding.buttonErase.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setUpNumberPadClickListeners() {
        val buttons = getNumberPadButtons()
        for (button in buttons) {
            button.setOnClickListener {
                vibrate()
                if (password.size != 4) {
                    password.add(button.text.toString().toInt())
                    switchFingerprintAndEraseButtons()
                    refreshIndicator()
                    checkPassword()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fingerprintAuth() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    switchIndicatorToGreen()
                    startActivity(MainActivity.newIntent(this@AuthActivity))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.authentication_failed_message), Toast.LENGTH_SHORT
                    ).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_title))
            .setNegativeButtonText(getString(R.string.auth_negative_button_text))
            .build()

        binding.buttonFingerprint.setOnClickListener {
            vibrate()
            biometricPrompt.authenticate(promptInfo)
        }
    }

    fun getNumberPadButtons(): List<Button> {
        return listOf<Button>(
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
        const val TAG = "AuthActivity"
        const val KEY_PASSWORD = "0000"
    }
}