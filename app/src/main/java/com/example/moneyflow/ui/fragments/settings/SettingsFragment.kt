package com.example.moneyflow.ui.fragments.settings
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moneyflow.R
import com.example.moneyflow.databinding.FragmentSettingsBinding
import com.example.moneyflow.ui.activities.AuthActivity
import android.os.Build
import androidx.annotation.RequiresApi
import android.content.Intent

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardViewChangePIN.setOnClickListener {
            val intent = AuthActivity.newIntent(requireContext(), AuthActivity.MODE_CHANGE_PIN)
            startActivity(intent)
        }
    }
}