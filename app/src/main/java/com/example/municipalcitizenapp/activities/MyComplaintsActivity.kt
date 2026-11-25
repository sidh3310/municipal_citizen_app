package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.ComplaintAdapter
import com.example.municipalcitizenapp.models.Complaint
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.example.municipalcitizenapp.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        fetchComplaintsDual()
    }

    private fun fetchComplaintsDual() {
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        if (token.isNotEmpty() && !token.contains("-")) {
            val authHeader = "Bearer $token"
            RetrofitClient.instance.getComplaints(authHeader).enqueue(object : Callback<List<Complaint>> {
                override fun onResponse(call: Call<List<Complaint>>, response: Response<List<Complaint>>) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        val complaints = response.body()!!
                        displayComplaints(complaints)
                    } else {
                        fetchComplaintsFromFirebase()
                    }
                }

                override fun onFailure(call: Call<List<Complaint>>, t: Throwable) {
                    fetchComplaintsFromFirebase()
                }
            })
        } else {
            fetchComplaintsFromFirebase()
        }
    }

    private fun fetchComplaintsFromFirebase() {
        FirebaseRepository.getAllComplaints(
            onSuccess = { complaints ->
                // Sort by upvotes descending so highest community voted issues appear at the top
                val sortedComplaints = complaints.sortedByDescending { it.upvotes }
                displayComplaints(sortedComplaints)
            },
            onError = { error ->
                Toast.makeText(this@MyComplaintsActivity, "Failed to load complaints: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun displayComplaints(list: List<Complaint>) {
        adapter = ComplaintAdapter(list, isCitizen = true) { complaint ->
            val intent = Intent(this@MyComplaintsActivity, ComplaintDetailsActivity::class.java)
            intent.putExtra("complaint_json", Gson().toJson(complaint))
            startActivity(intent)
        }
        rvMyComplaints.adapter = adapter
    }
}
