package com.shruti.reminderapp.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.shruti.reminderapp.adapter.StudyPlanAdapter
import com.shruti.reminderapp.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("StudyPlans")
    private val planList = mutableListOf<StudyPlan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchStudyHistory()

        binding.btnClearHistory.setOnClickListener {
            clearUserHistory()
        }
    }

    private fun setupRecyclerView() {
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchStudyHistory() {
        val currentUserId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        // Query to filter plans by current UserID
        dbRef.orderByChild("userId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    planList.clear()
                    for (snap in snapshot.children) {
                        val plan = snap.getValue(StudyPlan::class.java)
                        if (plan != null) planList.add(plan)
                    }

                    // Sort newest first
                    planList.sortByDescending { it.timestamp }

                    binding.progressBar.visibility = View.GONE
                    if (planList.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvHistory.adapter = StudyPlanAdapter(planList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@HistoryActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun clearUserHistory() {
        // Logic to delete only the current user's plans
        val currentUserId = auth.currentUser?.uid ?: return
        dbRef.orderByChild("userId").equalTo(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    snap.ref.removeValue()
                }
                Toast.makeText(this@HistoryActivity, "History Cleared", Toast.LENGTH_SHORT).show()
            }
            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}