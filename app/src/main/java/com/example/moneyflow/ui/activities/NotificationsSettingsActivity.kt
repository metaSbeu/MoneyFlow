package com.example.moneyflow.ui.activities

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyflow.R
import com.example.moneyflow.utils.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch

class NotificationsSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_settings)
        enableEdgeToEdge()
        val switchType1 = findViewById<MaterialSwitch>(R.id.switchType1)
        val switchType2 = findViewById<MaterialSwitch>(R.id.switchType2)

        switchType1.isChecked = PreferenceManager.isNotificationType1Enabled(this)
        switchType2.isChecked = PreferenceManager.isNotificationType2Enabled(this)

        switchType1.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.setNotificationType1Enabled(this, isChecked)
        }

        switchType2.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.setNotificationType2Enabled(this, isChecked)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonFinish = findViewById<ImageButton>(R.id.buttonFinish)
        buttonFinish.setOnClickListener {
            finish()
        }
    }
}