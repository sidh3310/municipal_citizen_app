package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.ComplaintAdapter
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

class OfficerDashboardActivity : ComponentActivity() {

    private lateinit var rvOfficerComplaints: RecyclerView
    private lateinit var tvOfficerName: TextView
    private lateinit var tvTotalComplaints: TextView
    private lateinit var tvResolvedComplaints: TextView
    private lateinit var ivLogout: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_officer_dashboard)

        tvOfficerName = findViewById(R.id.tvOfficerName)
        tvTotalComplaints = findViewById(R.id.tvTotalComplaints)
        tvResolvedComplaints = findViewById(R.id.tvResolvedComplaints)
        rvOfficerComplaints = findViewById(R.id.rvOfficerComplaints)
        ivLogout = findViewById(R.id.ivLogout)
        rvOfficerComplaints.layoutManager = LinearLayoutManager(this)

        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        val name = sharedPref.getString("full_name", "Officer")
        tvOfficerName.text = "Welcome, $name"

        ivLogout.setOnClickListener { logout() }
    }

    override fun onResume() {
        super.onResume()
        fetchOfficerComplaints()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    private fun fetchOfficerComplaints() {
        FirebaseRepository.getAllComplaints(
            onSuccess = { complaints ->
                rvOfficerComplaints.adapter = ComplaintAdapter(complaints) { complaint ->
                    val intent = Intent(this@OfficerDashboardActivity, OfficerComplaintDetailsActivity::class.java)
                    intent.putExtra("complaint_json", Gson().toJson(complaint))
                    startActivity(intent)
                }

                val pendingCount = complaints.count { it.status != "RESOLVED" && it.status != "CLOSED" }
                val resolvedCount = complaints.count { it.status == "RESOLVED" }

                tvTotalComplaints.text = pendingCount.toString()
                tvResolvedComplaints.text = resolvedCount.toString()
            },
            onError = { error ->
                Toast.makeText(this@OfficerDashboardActivity, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
