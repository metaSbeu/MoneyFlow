package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Plan
import com.example.moneyflow.databinding.ActivityPlanEditBinding
import com.example.moneyflow.ui.viewmodels.PlanEditViewModel
import com.example.moneyflow.utils.setupBottomViewKeyboardVisibilityListener
import java.text.NumberFormat
import java.util.Locale

class PlanEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanEditBinding
    private lateinit var viewModel: PlanEditViewModel
    private var planToEdit: Plan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlanEditBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[PlanEditViewModel::class.java]
        setContentView(binding.root)
        setupInsets()
        setupBottomViewKeyboardVisibilityListener(binding.bottomButtonsContainer)
        setupSumEditTextValidation() // Добавляем валидацию для поля суммы

        planToEdit = intent.getSerializableExtra(EXTRA_PLAN) as? Plan
        planToEdit?.let { plan ->
            binding.editTextName.setText(plan.name)
            binding.editTextPlanSum.setText(formatDoubleToString(plan.sum)) // Форматируем сумму
            binding.editTextDateNumber.setText(plan.targetNotificationDayOfMonth.toString())
        }

        binding.buttonSave.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val sumText = binding.editTextPlanSum.text.toString()
            val dayText = binding.editTextDateNumber.text.toString()

            if (name.isEmpty() || sumText.isEmpty() || dayText.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sum = sumText.toDoubleOrNull()
            val day = dayText.toIntOrNull()

            if (sum == null || day == null || day !in 1..31) {
                Toast.makeText(this, "Некорректные данные", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sum <= 0) {
                Toast.makeText(this, "Сумма плана должна быть больше 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            planToEdit?.let { existingPlan ->
                val updatedPlan = existingPlan.copy(
                    name = name,
                    sum = sum,
                    targetNotificationDayOfMonth = day
                )
                viewModel.editPlan(updatedPlan)
            }
        }

        binding.buttonDelete.setOnClickListener {
            planToEdit?.let {
                showDeleteConfirmationDialog(it)
            }
        }

        viewModel.shouldCloseScreen.observe(this) {
            if (it) {
                finish()
            }
        }
    }

    private fun setupSumEditTextValidation() {
        binding.editTextPlanSum.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let { editable ->
                    val text = editable.toString()
                    if (text.startsWith("0") && text.length > 1 && !text.startsWith("0.")) {
                        editable.delete(0, 1)
                    }
                    if (text == ".") {
                        editable.clear()
                    }
                }
            }
        })
    }

    private fun formatDoubleToString(value: Double): String {
        val numberFormat = NumberFormat.getInstance(Locale.getDefault())
        numberFormat.isGroupingUsed = false
        return numberFormat.format(value)
    }

    private fun showDeleteConfirmationDialog(plan: Plan) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.removal))
            .setMessage("Вы уверены что хотите удалить запланированный расход?")
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                viewModel.removePlan(plan)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
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