package com.example.municipalcitizenapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.models.Complaint
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.example.municipalcitizenapp.network.NotificationHelper
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class OfficerComplaintDetailsActivity : ComponentActivity() {

    private lateinit var ivDetailPhoto: ImageView
    private lateinit var tvDetailTitle: TextView
    private lateinit var tvDetailCategory: TextView
    private lateinit var tvDetailDescription: TextView
    private lateinit var spinnerStatus: Spinner
    private lateinit var spinnerDepartment: Spinner
    private lateinit var spinnerPriority: Spinner
    private lateinit var etResolutionNotes: EditText
    private lateinit var btnTakeResolvedPhoto: Button
    private lateinit var cardResolvedPhotoPreview: MaterialCardView
    private lateinit var ivResolvedPhotoPreview: ImageView
    private lateinit var btnUpdateStatus: Button
    private lateinit var btnViewMap: Button
    private lateinit var complaint: Complaint

    private var resolvedPhotoPath: String? = null
    private var resolvedPhotoUri: Uri? = null

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_officer_complaint_details)

        initViews()
        setupLaunchers()

        val json = intent.getStringExtra("complaint_json")
        complaint = Gson().fromJson(json, Complaint::class.java)

        displayDetails()
        setupSpinners()

        btnTakeResolvedPhoto.setOnClickListener {
            checkCameraPermission()
        }

        btnUpdateStatus.setOnClickListener {
            updateComplaint()
        }

        btnViewMap.setOnClickListener {
            openMap()
        }
    }

    private fun initViews() {
        ivDetailPhoto = findViewById(R.id.ivDetailPhoto)
        tvDetailTitle = findViewById(R.id.tvDetailTitle)
        tvDetailCategory = findViewById(R.id.tvDetailCategory)
        tvDetailDescription = findViewById(R.id.tvDetailDescription)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        spinnerDepartment = findViewById(R.id.spinnerDepartment)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        etResolutionNotes = findViewById(R.id.etResolutionNotes)
        btnTakeResolvedPhoto = findViewById(R.id.btnTakeResolvedPhoto)
        cardResolvedPhotoPreview = findViewById(R.id.cardResolvedPhotoPreview)
        ivResolvedPhotoPreview = findViewById(R.id.ivResolvedPhotoPreview)
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus)
        btnViewMap = findViewById(R.id.btnViewMap)
    }

    private fun setupLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cardResolvedPhotoPreview.visibility = View.VISIBLE
                ivResolvedPhotoPreview.setImageURI(resolvedPhotoUri)
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (_: IOException) {
            null
        }

        photoFile?.also {
            resolvedPhotoUri = FileProvider.getUriForFile(
                this,
                "com.example.municipalcitizenapp.fileprovider",
                it
            )
            takePictureLauncher.launch(resolvedPhotoUri)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("RESOLVED_${timeStamp}_", ".jpg", storageDir).apply {
            resolvedPhotoPath = absolutePath
        }
    }

    private fun displayDetails() {
        tvDetailTitle.text = complaint.title
        tvDetailCategory.text = complaint.category.uppercase()
        tvDetailDescription.text = complaint.description
        etResolutionNotes.setText(complaint.resolution_notes ?: "")

        val tvUpvotedUsersList: TextView = findViewById(R.id.tvUpvotedUsersList)
        if (complaint.upvotedBy.isNotEmpty()) {
            val count = complaint.upvotes
            val names = complaint.upvotedBy.joinToString("\n• ")
            tvUpvotedUsersList.text = "Total Upvotes: $count\n\nUpvoted by Citizens:\n• $names"
        } else {
            tvUpvotedUsersList.text = "Total Upvotes: ${complaint.upvotes}\nNo citizen upvotes recorded yet."
        }

        displayImage(complaint.image, ivDetailPhoto)
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

    private fun setupSpinners() {
        val statuses = arrayOf("SUBMITTED", "UNDER_REVIEW", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        val statusIndex = statuses.indexOf(complaint.status)
        if (statusIndex >= 0) spinnerStatus.setSelection(statusIndex)

        val departments = arrayOf(
            "Public Works & Roads",
            "Electrical & Streetlighting",
            "Sanitation & Waste Management",
            "Water & Sewage Department",
            "Environmental & Parks",
            "Traffic & Transportation"
        )
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDepartment.adapter = deptAdapter

        val priorities = arrayOf("LOW", "MEDIUM", "HIGH")
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter

        val priorityIndex = priorities.indexOf(complaint.priority)
        if (priorityIndex >= 0) spinnerPriority.setSelection(priorityIndex)
    }

    private fun openMap() {
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
            Toast.makeText(this, "Location data not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateComplaint() {
        var newStatus = spinnerStatus.selectedItem.toString()
        val selectedDept = spinnerDepartment.selectedItem.toString()
        val newPriority = spinnerPriority.selectedItem.toString()
        val newNotes = etResolutionNotes.text.toString().trim()

        if (newStatus == "SUBMITTED" && selectedDept.isNotEmpty()) {
            newStatus = "ASSIGNED"
        }

        val fullNotes = if (selectedDept.isNotEmpty()) {
            "Assigned to: $selectedDept. $newNotes".trim()
        } else {
            newNotes
        }

        if (complaint.docId.isEmpty()) {
            Toast.makeText(this, "Error: Complaint document ID missing", Toast.LENGTH_SHORT).show()
            return
        }

        btnUpdateStatus.isEnabled = false

        FirebaseRepository.updateComplaintStatusWithPhoto(
            docId = complaint.docId,
            status = newStatus,
            priority = newPriority,
            resolutionNotes = fullNotes,
            resolvedImageFilePath = resolvedPhotoPath,
            onSuccess = {
                btnUpdateStatus.isEnabled = true
                NotificationHelper.showNotification(
                    this@OfficerComplaintDetailsActivity,
                    "Complaint Status Updated 🔔",
                    "Complaint '${complaint.title}' updated to $newStatus ($selectedDept)."
                )
                Toast.makeText(this@OfficerComplaintDetailsActivity, "Update successful!", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { error ->
                btnUpdateStatus.isEnabled = true
                Toast.makeText(this@OfficerComplaintDetailsActivity, "Update failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
