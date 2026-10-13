package com.example.municipalcitizenapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.NotificationAdapter
import com.example.municipalcitizenapp.network.FirebaseRepository

class NotificationsActivity : ComponentActivity() {

    private lateinit var rvNotifications: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        rvNotifications = findViewById(R.id.rvNotifications)
        rvNotifications.layoutManager = LinearLayoutManager(this@NotificationsActivity)

        fetchNotifications()
    }

    private fun fetchNotifications() {
        FirebaseRepository.getNotifications(
            onSuccess = { list ->
                rvNotifications.adapter = NotificationAdapter(list)
            },
            onError = { error ->
                Toast.makeText(this@NotificationsActivity, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
