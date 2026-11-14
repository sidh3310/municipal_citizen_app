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
import com.example.municipalcitizenapp.models.Complaint
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.example.municipalcitizenapp.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        fetchOfficerComplaintsDual()
    }

    private fun logout() {
        try { FirebaseAuth.getInstance().signOut() } catch (_: Exception) {}
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    private fun fetchOfficerComplaintsDual() {
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        if (token.isNotEmpty() && !token.contains("-")) {
            val authHeader = "Bearer $token"
            RetrofitClient.instance.getComplaints(authHeader).enqueue(object : Callback<List<Complaint>> {
                override fun onResponse(call: Call<List<Complaint>>, response: Response<List<Complaint>>) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        displayComplaints(response.body()!!)
                    } else {
                        fetchOfficerComplaintsFromFirebase()
                    }
                }

                override fun onFailure(call: Call<List<Complaint>>, t: Throwable) {
                    fetchOfficerComplaintsFromFirebase()
                }
            })
        } else {
            fetchOfficerComplaintsFromFirebase()
        }
    }

    private fun fetchOfficerComplaintsFromFirebase() {
        FirebaseRepository.getAllComplaints(
            onSuccess = { complaints ->
                displayComplaints(complaints)
            },
            onError = { error ->
                if (error.contains("PERMISSION_DENIED")) {
                    Toast.makeText(this@OfficerDashboardActivity, "Firestore Rules locked! Set 'allow read, write: if true;' in Firebase Console -> Rules.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@OfficerDashboardActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun displayComplaints(complaints: List<Complaint>) {
        rvOfficerComplaints.adapter = ComplaintAdapter(complaints) { complaint ->
            val intent = Intent(this@OfficerDashboardActivity, OfficerComplaintDetailsActivity::class.java)
            intent.putExtra("complaint_json", Gson().toJson(complaint))
            startActivity(intent)
        }

        val pendingCount = complaints.count { it.status != "RESOLVED" && it.status != "CLOSED" }
        val resolvedCount = complaints.count { it.status == "RESOLVED" }

        tvTotalComplaints.text = pendingCount.toString()
        tvResolvedComplaints.text = resolvedCount.toString()
    }
}
