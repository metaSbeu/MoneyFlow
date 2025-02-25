package com.example.moneyflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Найдём кнопку "0" по её ID и установим обработчик нажатия
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout3)
        linearLayout.setOnClickListener {
            // Запускаем MainActivity
            val intent = Intent(this, TransactionListActivity::class.java)
            startActivity(intent)
        }

        // Найдём кнопку "0" по её ID и установим обработчик нажатия
        val linearLayout2 = findViewById<LinearLayout>(R.id.linearLayout7)
        linearLayout2.setOnClickListener {
            // Запускаем MainActivity
            val intent = Intent(this, TransactionAddActivity::class.java)
            startActivity(intent)
        }

        // Найдём кнопку "0" по её ID и установим обработчик нажатия
        val buttonPlanning = findViewById<Button>(R.id.buttonPlanning)
        buttonPlanning.setOnClickListener {
            // Запускаем MainActivity
            val intent = Intent(this, PlanningListActivity::class.java)
            startActivity(intent)
        }

        val buttonSettings = findViewById<Button>(R.id.buttonSettings)
        buttonSettings.setOnClickListener {
            // Запускаем MainActivity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}