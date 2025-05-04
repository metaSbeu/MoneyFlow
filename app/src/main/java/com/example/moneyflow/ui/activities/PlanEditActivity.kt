package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyflow.R
import com.example.moneyflow.data.Plan
import com.example.moneyflow.databinding.ActivityPlanEditBinding
import com.example.moneyflow.ui.viewmodels.PlanEditViewModel
import com.example.moneyflow.utils.setupBottomViewKeyboardVisibilityListener

class PlanEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanEditBinding
    private lateinit var viewModel: PlanEditViewModel
    private var planToEdit: Plan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlanEditBinding.inflate(layoutInflater)
        viewModel = PlanEditViewModel(application)
        setContentView(binding.root)
        setupInsets()
        setupBottomViewKeyboardVisibilityListener(binding.bottomButtonsContainer)

        planToEdit = intent.getSerializableExtra(EXTRA_PLAN) as? Plan // Получаем редактируемый план
        planToEdit?.let { plan ->
            binding.editTextName.setText(plan.name)
            binding.editTextPlanSum.setText(plan.sum.toString())
            binding.editTextDateNumber.setText(plan.targetNotificationDayOfMonth.toString())
        }

        binding.buttonSave.setOnClickListener {
            planToEdit?.let { existingPlan -> // Используем существующий план и обновляем его свойства
                val name = binding.editTextName.text.toString()
                val sum = binding.editTextPlanSum.text.toString().toDouble()
                val date = binding.editTextDateNumber.text.toString().toInt()
                val updatedPlan = existingPlan.copy(
                    name = name,
                    sum = sum,
                    targetNotificationDayOfMonth = date
                )
                viewModel.editPlan(updatedPlan) // Передаем обновленный существующий план
            }
        }

        binding.buttonDelete.setOnClickListener {
            planToEdit?.let {
                viewModel.removePlan(it)
            }
        }

        viewModel.shouldCloseScreen.observe(this) {
            if (it) {
                finish()
            }
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
        const val EXTRA_PLAN = "plan"

        fun newIntent(context: Context, plan: Plan): Intent {
            val intent = Intent(context, PlanEditActivity::class.java)
            intent.putExtra(EXTRA_PLAN, plan)
            return intent
        }
    }
}