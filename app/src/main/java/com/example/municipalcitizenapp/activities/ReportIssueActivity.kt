package com.example.municipalcitizenapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.example.municipalcitizenapp.network.NotificationHelper
import com.example.municipalcitizenapp.network.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ReportIssueActivity : ComponentActivity() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnTakePhoto: Button
    private lateinit var ivPhotoPreview: ImageView
    private lateinit var btnGetLocation: Button
    private lateinit var tvLocationDisplay: TextView
    private lateinit var btnSubmit: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null
    private var lastLocation: Location? = null

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupLaunchers()
        setupCategorySpinner()

        btnTakePhoto.setOnClickListener { checkCameraPermission() }
        btnGetLocation.setOnClickListener { checkLocationPermission() }
        btnSubmit.setOnClickListener { validateAndSubmit() }
    }

    private fun initViews() {
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview)
        btnGetLocation = findViewById(R.id.btnGetLocation)
        tvLocationDisplay = findViewById(R.id.tvLocationDisplay)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    private fun setupLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineLocationGranted || coarseLocationGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                ivPhotoPreview.visibility = View.VISIBLE
                ivPhotoPreview.setImageURI(photoUri)
            }
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            "Select Category", 
            "Road damage / potholes", 
            "Broken streetlights", 
            "Illegal dumping", 
            "Water leakage", 
            "Damaged traffic signals",
            "Environmental problems",
            "Public infrastructure",
            "Parking problems",
            "Other"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    lastLocation = location
                    tvLocationDisplay.text = "Lat: ${location.latitude}, Long: ${location.longitude}"
                } else {
                    Toast.makeText(this, "Could not get location. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        
        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                this,
                "com.example.municipalcitizenapp.fileprovider",
                it
            )
            takePictureLauncher.launch(photoUri)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun validateAndSubmit() {
        val category = spinnerCategory.selectedItem.toString()
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (category == "Select Category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        if (title.isEmpty()) {
            etTitle.error = "Title is required"
            return
        }
        if (description.isEmpty()) {
            etDescription.error = "Description is required"
            return
        }
        if (photoUri == null || currentPhotoPath == null) {
            Toast.makeText(this, "Please take a photo of the issue", Toast.LENGTH_SHORT).show()
            return
        }
        if (lastLocation == null) {
            Toast.makeText(this, "Please provide your location", Toast.LENGTH_SHORT).show()
            return
        }

        uploadDualComplaint(category, title, description)
    }

    private fun uploadDualComplaint(category: String, title: String, description: String) {
        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."

        // 1. Save to Firebase Cloud (Firestore + Firebase Storage)
        FirebaseRepository.submitComplaint(
            category = category,
            title = title,
            description = description,
            latitude = lastLocation?.latitude?.toString() ?: "",
            longitude = lastLocation?.longitude?.toString() ?: "",
            imageFilePath = currentPhotoPath,
            imageUri = photoUri,
            onSuccess = {
                btnSubmit.isEnabled = true
                btnSubmit.text = "SUBMIT COMPLAINT"
                NotificationHelper.showNotification(
                    this@ReportIssueActivity,
                    "Complaint Submitted! 📩",
                    "Your complaint '$title' has been registered."
                )
                Toast.makeText(this@ReportIssueActivity, "Complaint submitted to Firebase Cloud & PostgreSQL!", Toast.LENGTH_LONG).show()
                finish()
            },
            onError = { error ->
                btnSubmit.isEnabled = true
                btnSubmit.text = "SUBMIT COMPLAINT"
                Toast.makeText(this@ReportIssueActivity, "Submission Error: $error", Toast.LENGTH_LONG).show()
            }
        )

        // 2. Also submit to Django REST + PostgreSQL
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""
        if (token.isNotEmpty() && !token.contains("-")) {
            val authHeader = "Bearer $token"
            val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val latPart = lastLocation!!.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val longPart = lastLocation!!.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val file = File(currentPhotoPath!!)
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            RetrofitClient.instance.submitComplaint(
                authHeader, categoryPart, titlePart, descPart, latPart, longPart, imagePart
            ).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
        }
    }
}
