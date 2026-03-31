package com.shruti.reminderapp.activity

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.shruti.reminderapp.R

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val tvHistoryContent = findViewById<TextView>(R.id.tvHistoryContent)
        val btnClearHistory = findViewById<MaterialButton>(R.id.btnClearHistory)

        // Access the same SharedPreferences file used in AIPlannerActivity
        val sharedPrefs = getSharedPreferences("StudyPlannerPrefs", Context.MODE_PRIVATE)

        // Load the saved string data
        val historyData = sharedPrefs.getString("all_plans", "Your history is currently empty.")
        tvHistoryContent.text = historyData

        // Logic to clear history
        btnClearHistory.setOnClickListener {
            sharedPrefs.edit().remove("all_plans").apply()
            tvHistoryContent.text = "History cleared."
            Toast.makeText(this, "History Deleted", Toast.LENGTH_SHORT).show()
        }
    }
}