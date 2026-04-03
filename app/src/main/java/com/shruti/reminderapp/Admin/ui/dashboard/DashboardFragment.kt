package com.shruti.reminderapp.Admin.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import android.os.Environment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.shruti.reminderapp.activity.User
import com.shruti.reminderapp.adapter.UserStats
import com.shruti.reminderapp.adapter.UserStatsAdapter
import com.shruti.reminderapp.databinding.FragmentDashboardBinding
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var statsAdapter: UserStatsAdapter
    private val statsList = ArrayList<UserStats>()
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        setupRecyclerView()
        loadAllUserStats()

        binding.fabExportExcel.setOnClickListener {
            if (statsList.isNotEmpty()) {
                exportDataToExcel()
            } else {
                Toast.makeText(requireContext(), "No data available to export", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        statsAdapter = UserStatsAdapter(statsList)
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = statsAdapter
    }

    private fun loadAllUserStats() {
        binding.progressBarDashboard.visibility = View.VISIBLE
        val currentUid = auth.currentUser?.uid

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                statsList.clear()

                val usersSnap = snapshot.child("Users")
                val plansSnap = snapshot.child("StudyPlans")

                for (userSnap in usersSnap.children) {
                    val uid = userSnap.key ?: continue

                    // Skip if this is the Admin's own UID
                    if (uid == currentUid) continue

                    // 1. Extract User basic info
                    val user = User(
                        id = uid,
                        name = userSnap.child("name").value.toString(),
                        email = userSnap.child("email").value.toString()
                    )

                    // 2. Get Reminder Count for this specific user
                    val rCount = userSnap.child("reminders").childrenCount.toInt()

                    // 3. Get Study Plan Count for this specific user
                    var pCount = 0
                    for (planItem in plansSnap.children) {
                        val planUserId = planItem.child("userId").value.toString()
                        if (planUserId == uid) {
                            pCount++
                        }
                    }

                    // 4. Create the combined object and add to list
                    val userStats = UserStats(
                        user = user,
                        reminderCount = rCount,
                        planCount = pCount
                    )
                    statsList.add(userStats)
                }

                statsAdapter.notifyDataSetChanged()
                binding.progressBarDashboard.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBarDashboard.visibility = View.GONE
            }
        })
    }

    private fun exportDataToExcel() {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Admin Report")

            // Headers
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("User Name")
            header.createCell(1).setCellValue("Email")
            header.createCell(2).setCellValue("Reminders")
            header.createCell(3).setCellValue("AI Plans")

            // Data
            for ((i, item) in statsList.withIndex()) {
                val row = sheet.createRow(i + 1)
                row.createCell(0).setCellValue(item.user.name)
                row.createCell(1).setCellValue(item.user.email)
                row.createCell(2).setCellValue(item.reminderCount.toDouble())
                row.createCell(3).setCellValue(item.planCount.toDouble())
            }

            // Save File
            val fileName = "UserReport_${System.currentTimeMillis()}.xlsx"
            val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            val fos = FileOutputStream(file)
            workbook.write(fos)
            fos.close()
            workbook.close()

            Toast.makeText(requireContext(), "Report Generated Successfully!", Toast.LENGTH_SHORT).show()
            openFile(file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openFile(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share Report via:"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}