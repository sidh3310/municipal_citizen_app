package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.AnnouncementAdapter
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.example.municipalcitizenapp.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CitizenDashboardActivity : ComponentActivity() {

    private lateinit var rvAnnouncements: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_citizen_dashboard)

        rvAnnouncements = findViewById(R.id.rvAnnouncements)
        rvAnnouncements.layoutManager = LinearLayoutManager(this@CitizenDashboardActivity)

        findViewById<View>(R.id.cardReportIssue).setOnClickListener {
            startActivity(Intent(this, ReportIssueActivity::class.java))
        }

        findViewById<View>(R.id.cardMyComplaints).setOnClickListener {
            startActivity(Intent(this, MyComplaintsActivity::class.java))
        }

        findViewById<View>(R.id.cardMapView).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        findViewById<View>(R.id.cardEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        findViewById<ImageView>(R.id.ivNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<ImageView>(R.id.ivLogout).setOnClickListener {
            logout()
        }

        fetchAnnouncements()
    }

    private fun fetchAnnouncements() {
        // Dual fetch: Retrofit with Firebase Fallback
        RetrofitClient.instance.getAnnouncements().enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onResponse(call: Call<List<Map<String, Any>>>, response: Response<List<Map<String, Any>>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    rvAnnouncements.adapter = AnnouncementAdapter(response.body()!!)
                } else {
                    fetchAnnouncementsFromFirebase()
                }
            }

            override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                fetchAnnouncementsFromFirebase()
            }
        })
    }

    private fun fetchAnnouncementsFromFirebase() {
        FirebaseRepository.getAnnouncements(
            onSuccess = { list ->
                rvAnnouncements.adapter = AnnouncementAdapter(list)
            },
            onError = {}
        )
    }

    private fun logout() {
        try { FirebaseAuth.getInstance().signOut() } catch (_: Exception) {}
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
