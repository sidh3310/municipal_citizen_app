package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.models.Complaint
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson

class ComplaintDetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint_details)

        val ivPhoto: ImageView = findViewById(R.id.ivCitizenDetailPhoto)
        val tvTitle: TextView = findViewById(R.id.tvCitizenDetailTitle)
        val tvCategory: TextView = findViewById(R.id.tvCitizenDetailCategory)
        val tvStatus: TextView = findViewById(R.id.tvCitizenDetailStatus)
        val tvDescription: TextView = findViewById(R.id.tvCitizenDetailDescription)
        val tvResolutionNotes: TextView = findViewById(R.id.tvResolutionNotes)
        val btnViewMap: Button = findViewById(R.id.btnViewLocationMap)
        val tvAfterLabel: TextView = findViewById(R.id.tvAfterLabel)
        val cardAfterPhoto: MaterialCardView = findViewById(R.id.cardAfterPhoto)
        val ivResolvedPhoto: ImageView = findViewById(R.id.ivResolvedDetailPhoto)

        val json = intent.getStringExtra("complaint_json")
        val complaint = Gson().fromJson(json, Complaint::class.java)

        tvTitle.text = complaint.title
        tvCategory.text = complaint.category.uppercase()
        tvStatus.text = complaint.status
        tvDescription.text = complaint.description
        tvResolutionNotes.text = if (complaint.resolution_notes.isNullOrEmpty()) {
            "No resolution notes yet."
        } else {
            complaint.resolution_notes
        }

        displayImage(complaint.image, ivPhoto)

        if (!complaint.resolved_image.isNullOrEmpty()) {
            tvAfterLabel.visibility = View.VISIBLE
            cardAfterPhoto.visibility = View.VISIBLE
            displayImage(complaint.resolved_image, ivResolvedPhoto)
        }

        btnViewMap.setOnClickListener {
            if (!complaint.latitude.isNullOrEmpty() && !complaint.longitude.isNullOrEmpty()) {
                val gmmIntentUri = Uri.parse("geo:${complaint.latitude},${complaint.longitude}?q=${complaint.latitude},${complaint.longitude}(Issue Location)")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                }
            } else {
                Toast.makeText(this, "GPS coordinates not available for this report", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayImage(imageStr: String?, imageView: ImageView) {
        if (imageStr.isNullOrEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image)
            return
        }

        val cleanStr = imageStr.trim()

        if (cleanStr.startsWith("http://") || cleanStr.startsWith("https://") || cleanStr.startsWith("file://")) {
            val formattedUrl = cleanStr.replace("http://localhost:8000", "http://127.0.0.1:8000")
            Glide.with(this)
                .load(formattedUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView)
            return
        }

        try {
            val base64Data = if (cleanStr.contains("base64,")) cleanStr.substringAfter("base64,") else cleanStr
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            Glide.with(this)
                .asBitmap()
                .load(decodedBytes)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView)
        } catch (_: Exception) {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }
}
