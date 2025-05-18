package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Plan
import com.example.moneyflow.databinding.ActivityPlanAddBinding
import com.example.moneyflow.ui.viewmodels.PlanAddViewModel
import com.example.moneyflow.utils.setupBottomViewKeyboardVisibilityListener

class PlanAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanAddBinding
    private lateinit var viewModel: PlanAddViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPlanAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()
        setupBottomViewKeyboardVisibilityListener(binding.buttonSave)
        viewModel = ViewModelProvider(this)[PlanAddViewModel::class.java]

        binding.buttonSave.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val sumText = binding.editTextPlanSum.text.toString()
            val dayText = binding.editTextDateNumber.text.toString()

            if (name.isEmpty() || sumText.isEmpty() || dayText.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val sum = sumText.toDoubleOrNull()
            val day = dayText.toIntOrNull()

            if (sum == null || day == null || day !in 1..31) {
                Toast.makeText(this, getString(R.string.incorrect_data), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sum <= 0) {
                Toast.makeText(
                    this, getString(R.string.sum_must_be_more_than_zero), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val plan = Plan(
                name = name,
                sum = sum,
                targetNotificationDayOfMonth = day,
                isNotificationActive = true
            )
            viewModel.addPlan(plan)
        }

        viewModel.shouldCloseScreen.observe(this) {
            finish()
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PlanAddActivity::class.java)
        }
    }
}