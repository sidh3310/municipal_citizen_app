package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.AnnouncementAdapter
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth

class CitizenDashboardActivity : ComponentActivity() {

    private lateinit var rvAnnouncements: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_citizen_dashboard)

        rvAnnouncements = findViewById(R.id.rvAnnouncements)
        rvAnnouncements.layoutManager = LinearLayoutManager(this@CitizenDashboardActivity)

        findViewById<CardView>(R.id.cardReportIssue).setOnClickListener {
            startActivity(Intent(this, ReportIssueActivity::class.java))
        }

        findViewById<CardView>(R.id.cardMyComplaints).setOnClickListener {
            startActivity(Intent(this, MyComplaintsActivity::class.java))
        }

        findViewById<CardView>(R.id.cardMapView).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        findViewById<CardView>(R.id.cardEmergency).setOnClickListener {
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
        FirebaseRepository.getAnnouncements(
            onSuccess = { list ->
                rvAnnouncements.adapter = AnnouncementAdapter(list)
            },
            onError = {}
        )
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
