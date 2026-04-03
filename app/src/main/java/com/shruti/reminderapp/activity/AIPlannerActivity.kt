package com.shruti.reminderapp.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.shruti.reminderapp.R
import com.shruti.reminderapp.databinding.ActivityAiplannerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AIPlannerActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var btnGenerate: MaterialButton
    private lateinit var btnHistory: Button
    private lateinit var tvResult: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var etDays: EditText
    private var selectedBitmap: Bitmap? = null

    private lateinit var binding: ActivityAiplannerBinding

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("StudyPlans")

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                ivPreview.setPadding(0, 0, 0, 0)
                ivPreview.load(uri)
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAiplannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivPreview = findViewById(R.id.ivPreview)
        btnGenerate = findViewById(R.id.btnGenerate)
        tvResult = findViewById(R.id.tvResult)
        progressBar = findViewById(R.id.progressBar)
        etDays = findViewById(R.id.etDays)
        btnHistory = findViewById(R.id.btnHistory)

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<View>(R.id.cardSelectImage).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnGenerate.setOnClickListener {
            val daysInput = etDays.text.toString()
            if (selectedBitmap == null) {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (daysInput.isEmpty()) {
                etDays.error = "Enter number of days"
                return@setOnClickListener
            }
            generatePlanWithGemini(selectedBitmap!!, daysInput)
        }
    }

    private fun generatePlanWithGemini(bitmap: Bitmap, days: String) {
        progressBar.visibility = View.VISIBLE
        btnGenerate.isEnabled = false
        tvResult.text = "Gemini is analyzing your syllabus..."

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "YOUR_API_KEY"
        )

        val prompt = content {
            image(bitmap)
            text("Analyze this syllabus and create a realistic $days-day study plan.")
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: ""

                withContext(Dispatchers.Main) {
                    tvResult.text = responseText
                    progressBar.visibility = View.GONE
                    btnGenerate.isEnabled = true

                    // Logic: Check for "error" word (case-insensitive)
                    if (responseText.isNotEmpty() && !responseText.contains("error", ignoreCase = true)) {
                        showSavePlanDialog(days, responseText)
                    } else if (responseText.contains("error", ignoreCase = true)) {
                        // Just show toast for the error, no dialog
                        Toast.makeText(this@AIPlannerActivity, "Plan contains errors. Not saving.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvResult.text = "Error: ${e.message}"
                    progressBar.visibility = View.GONE
                    btnGenerate.isEnabled = true
                }
            }
        }
    }

    private fun showSavePlanDialog(days: String, plan: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Save Study Plan?")
            .setMessage("Would you like to save it to your history?")
            .setPositiveButton("Save to Cloud") { _, _ ->
                savePlanToFirebase(days, plan)
            }
            .setNegativeButton("Discard") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun savePlanToFirebase(days: String, plan: String) {
        val userId = auth.currentUser?.uid ?: "Anonymous"
        val planId = dbRef.push().key ?: System.currentTimeMillis().toString()

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val studyPlan = StudyPlan(
            planId = planId,
            userId = userId,
            date = currentDate,
            days = days,
            content = plan,
            timestamp = System.currentTimeMillis()
        )

        dbRef.child(planId).setValue(studyPlan)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully saved to History!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save plan", Toast.LENGTH_SHORT).show()
            }
    }
}

data class StudyPlan(
    val planId: String = "",
    val userId: String = "",
    val date: String = "",
    val days: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)