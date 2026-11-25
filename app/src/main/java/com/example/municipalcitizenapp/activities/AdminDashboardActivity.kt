package com.example.municipalcitizenapp.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.example.municipalcitizenapp.ui.CategoryPieChartView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : ComponentActivity() {

    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalComplaints: TextView
    private lateinit var pieChartView: CategoryPieChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        tvTotalUsers = findViewById(R.id.tvAdminTotalUsers)
        tvTotalComplaints = findViewById(R.id.tvAdminTotalComplaints)
        pieChartView = findViewById(R.id.pieChartView)

        fetchStatsAndPieChart()

        findViewById<MaterialCardView>(R.id.cardManageUsers).setOnClickListener {
            startActivity(Intent(this@AdminDashboardActivity, ManageUsersActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardManageDepartments).setOnClickListener {
            startActivity(Intent(this@AdminDashboardActivity, ManageDepartmentsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardAllComplaints).setOnClickListener {
            startActivity(Intent(this@AdminDashboardActivity, OfficerDashboardActivity::class.java))
        }

        findViewById<Button>(R.id.btnAddAnnouncement).setOnClickListener {
            showAnnouncementDialog()
        }

        findViewById<Button>(R.id.btnAdminLogout).setOnClickListener {
            try { FirebaseAuth.getInstance().signOut() } catch (_: Exception) {}
            val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            val intent = Intent(this@AdminDashboardActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchStatsAndPieChart()
    }

    private fun fetchStatsAndPieChart() {
        FirebaseRepository.getAdminStats(
            onSuccess = { totalUsers, totalComplaints ->
                tvTotalUsers.text = totalUsers.toString()
                tvTotalComplaints.text = totalComplaints.toString()
            },
            onError = {
                Toast.makeText(this@AdminDashboardActivity, "Error fetching stats", Toast.LENGTH_SHORT).show()
            }
        )

        FirebaseRepository.getAllComplaints(
            onSuccess = { complaints ->
                val categoryCounts = complaints.groupingBy { 
                    if (it.category.isEmpty()) "Other" else it.category
                }.eachCount()

                pieChartView.setData(categoryCounts)
            },
            onError = {}
        )
    }

    private fun showAnnouncementDialog() {
        val builder = AlertDialog.Builder(this@AdminDashboardActivity)
        builder.setTitle("Post New Announcement")

        val layout = LinearLayout(this@AdminDashboardActivity)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val etTitle = EditText(this@AdminDashboardActivity)
        etTitle.hint = "Title"
        layout.addView(etTitle)

        val etMessage = EditText(this@AdminDashboardActivity)
        etMessage.hint = "Message"
        layout.addView(etMessage)

        builder.setView(layout)

        builder.setPositiveButton("POST") { _, _ ->
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()
            if (title.isNotEmpty() && message.isNotEmpty()) {
                postAnnouncement(title, message)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun postAnnouncement(title: String, message: String) {
        FirebaseRepository.postAnnouncement(
            title = title,
            message = message,
            onSuccess = {
                Toast.makeText(this@AdminDashboardActivity, "Announcement posted!", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(this@AdminDashboardActivity, "Failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
