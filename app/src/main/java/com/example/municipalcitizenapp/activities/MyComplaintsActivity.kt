package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.ComplaintAdapter
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

class MyComplaintsActivity : ComponentActivity() {

    private lateinit var rvMyComplaints: RecyclerView
    private lateinit var adapter: ComplaintAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        rvMyComplaints = findViewById(R.id.rvMyComplaints)
        rvMyComplaints.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        fetchComplaints()
    }

    private fun fetchComplaints() {
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""

        FirebaseRepository.getUserComplaints(
            userId = token,
            onSuccess = { complaints ->
                adapter = ComplaintAdapter(complaints) { complaint ->
                    val intent = Intent(this@MyComplaintsActivity, ComplaintDetailsActivity::class.java)
                    intent.putExtra("complaint_json", Gson().toJson(complaint))
                    startActivity(intent)
                }
                rvMyComplaints.adapter = adapter
            },
            onError = { error ->
                Toast.makeText(this@MyComplaintsActivity, "Failed to load complaints: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
