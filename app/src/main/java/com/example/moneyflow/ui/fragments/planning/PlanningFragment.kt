package com.example.moneyflow.ui.fragments.planning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.example.moneyflow.databinding.FragmentPlanningBinding
import com.example.moneyflow.ui.activities.PlanAddActivity
import com.example.moneyflow.ui.activities.PlanEditActivity
import com.example.moneyflow.ui.adapters.PlanningAdapter

class PlanningFragment : Fragment() {

    private lateinit var binding: FragmentPlanningBinding
    private lateinit var adapter: PlanningAdapter
    private lateinit var viewModel: PlanningViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_planning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlanningBinding.bind(view)
        viewModel = ViewModelProvider(this)[PlanningViewModel::class.java]
        adapter = PlanningAdapter(
            {
                startActivity(PlanAddActivity.newIntent(requireContext()))
            },
            { plan, isActive ->
                viewModel.setNotificationActive(plan, isActive)
            }, {
                startActivity(PlanEditActivity.newIntent(requireContext(), it))
            })
        observeViewmodel()
        binding.recyclerViewPlans.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPlans()
    }

    fun observeViewmodel() {
        viewModel.plans.observe(viewLifecycleOwner) {
            adapter.plans = it
        }
        viewModel.monthSum.observe(viewLifecycleOwner) {
            val formattedSum = it.formatWithSpaces()
            binding.textViewMonthSum.text = "$formattedSum ₽"
        }
    }
}