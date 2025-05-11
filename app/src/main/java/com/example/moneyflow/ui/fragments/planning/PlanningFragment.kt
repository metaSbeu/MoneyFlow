package com.example.moneyflow.ui.fragments.planning

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.databinding.FragmentPlanningBinding
import com.example.moneyflow.ui.activities.PlanAddActivity
import com.example.moneyflow.ui.activities.PlanEditActivity
import com.example.moneyflow.ui.adapters.PlanningAdapter
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.google.android.material.snackbar.Snackbar

class PlanningFragment : Fragment() {

    private lateinit var binding: FragmentPlanningBinding
    private lateinit var adapter: PlanningAdapter
    private lateinit var viewModel: PlanningViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    Snackbar.make(
                        binding.root,
                        "Разрешите уведомления, чтобы получать напоминания о планах",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Разрешить") {
                            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .show()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Вы отключили уведомления. Вы можете включить их в настройках приложения.",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Настройки") {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                            }
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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
            val formattedSum = it.formatWithSpaces(requireContext())
            binding.textViewMonthSum.text = "$formattedSum"
        }
    }
}