package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Plan
import com.example.moneyflow.databinding.ActivityPlanAddBinding
import com.example.moneyflow.ui.fragments.planning.PlanningViewModel
import com.example.moneyflow.ui.viewmodels.PlanAddViewModel
import com.example.moneyflow.utils.setupBottomViewKeyboardVisibilityListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

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
            val plan = Plan(
                name = binding.editTextName.text.toString().trim(),
                sum = binding.editTextPlanSum.text.toString().toDouble(),
                targetNotificationDayOfMonth = binding.editTextDateNumber.text.toString().toInt(),
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