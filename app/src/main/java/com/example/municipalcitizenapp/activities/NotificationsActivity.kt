package com.example.municipalcitizenapp.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.NotificationAdapter
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.example.municipalcitizenapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsActivity : ComponentActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        rvNotifications = findViewById(R.id.rvNotifications)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        rvNotifications.layoutManager = LinearLayoutManager(this@NotificationsActivity)

        fetchNotificationsDual()
    }

    private fun fetchNotificationsDual() {
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        if (token.isNotEmpty() && !token.contains("-")) {
            val authHeader = "Bearer $token"
            RetrofitClient.instance.getNotifications(authHeader).enqueue(object : Callback<List<Map<String, Any>>> {
                override fun onResponse(call: Call<List<Map<String, Any>>>, response: Response<List<Map<String, Any>>>) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        displayNotifications(response.body()!!)
                    } else {
                        fetchNotificationsFromFirebase()
                    }
                }

                override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                    fetchNotificationsFromFirebase()
                }
            })
        } else {
            fetchNotificationsFromFirebase()
        }
    }

    private fun fetchNotificationsFromFirebase() {
        FirebaseRepository.getNotifications(
            onSuccess = { list ->
                displayNotifications(list)
            },
            onError = { error ->
                Toast.makeText(this@NotificationsActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                displayNotifications(emptyList())
            }
        )
    }

    private fun displayNotifications(list: List<Map<String, Any>>) {
        if (list.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            rvNotifications.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            rvNotifications.visibility = View.VISIBLE
            rvNotifications.adapter = NotificationAdapter(list)
        }
    }
}
