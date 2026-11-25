package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.ComplaintAdapter
import com.example.municipalcitizenapp.network.FirebaseRepository

class MapActivity : ComponentActivity() {

    private lateinit var rvMapComplaints: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        rvMapComplaints = findViewById(R.id.rvMapComplaints)
        rvMapComplaints.layoutManager = LinearLayoutManager(this)

        fetchComplaintsForMap()
    }

    private fun fetchComplaintsForMap() {
        FirebaseRepository.getAllComplaints(
            onSuccess = { complaints ->
                val adapter = ComplaintAdapter(complaints, isCitizen = false) { complaint ->
                    if (!complaint.latitude.isNullOrEmpty() && !complaint.longitude.isNullOrEmpty()) {
                        val gmmIntentUri = Uri.parse("geo:${complaint.latitude},${complaint.longitude}?q=${complaint.latitude},${complaint.longitude}(${complaint.title})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(packageManager) != null) {
                            startActivity(mapIntent)
                        } else {
                            startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                        }
                    } else {
                        Toast.makeText(this@MapActivity, "GPS coordinates not available for this report", Toast.LENGTH_SHORT).show()
                    }
                }
                rvMapComplaints.adapter = adapter
            },
            onError = { error ->
                if (error.contains("PERMISSION_DENIED")) {
                    Toast.makeText(this@MapActivity, "Firestore Rules locked! Set 'allow read, write: if true;' in Firebase Console -> Rules.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MapActivity, "Failed to load map data: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
