package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.models.AuthResponse
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.example.municipalcitizenapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var btnDemoOfficer: Button
    private lateinit var btnDemoAdmin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        btnDemoOfficer = findViewById(R.id.btnDemoOfficer)
        btnDemoAdmin = findViewById(R.id.btnDemoAdmin)

        btnLogin.setOnClickListener {
            validateAndLogin()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnDemoOfficer.setOnClickListener {
            etEmail.setText("officer@municipal.gov")
            etPassword.setText("officer123")
            validateAndLogin()
        }

        btnDemoAdmin.setOnClickListener {
            etEmail.setText("admin@municipal.gov")
            etPassword.setText("admin123")
            validateAndLogin()
        }
    }

    private fun validateAndLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        btnLogin.isEnabled = false
        performDualLogin(email, password)
    }

    private fun performDualLogin(email: String, password: String) {
        val credentials = mapOf(
            "email" to email,
            "username" to email,
            "password" to password
        )

        // 1. Primary Authentication: Django REST + PostgreSQL
        RetrofitClient.instance.login(credentials).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val auth = response.body()
                    val token = auth?.access ?: ""
                    val role = auth?.role ?: "CITIZEN"
                    val fullName = auth?.full_name ?: email

                    saveSessionAndNavigate(token, role, fullName, email)

                    // Sync session with Firebase Auth/Firestore
                    FirebaseRepository.loginUser(email, password, onSuccess = { _, _, _ -> }, onError = {
                        FirebaseRepository.registerUser(fullName, email, "1234567890", password, role, onSuccess = {}, onError = {})
                    })
                } else {
                    // 2. Fallback Authentication: Firebase Cloud
                    loginWithFirebaseFallback(email, password)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                // Network/Offline Fallback to Firebase Cloud
                loginWithFirebaseFallback(email, password)
            }
        })
    }

    private fun loginWithFirebaseFallback(email: String, password: String) {
        FirebaseRepository.loginUser(
            email = email,
            password = password,
            onSuccess = { uid, role, fullName ->
                saveSessionAndNavigate(uid, role, fullName, email)
            },
            onError = { error ->
                btnLogin.isEnabled = true
                Toast.makeText(this@LoginActivity, "Login Failed: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun saveSessionAndNavigate(token: String, role: String, fullName: String, email: String) {
        btnLogin.isEnabled = true
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("token", token)
            putString("role", role)
            putString("full_name", fullName)
            putString("email", email)
            apply()
        }

        Toast.makeText(this@LoginActivity, "Login Successful ($role)", Toast.LENGTH_SHORT).show()

        val destination = when (role.uppercase()) {
            "OFFICER" -> OfficerDashboardActivity::class.java
            "ADMIN" -> AdminDashboardActivity::class.java
            else -> CitizenDashboardActivity::class.java
        }

        startActivity(Intent(this@LoginActivity, destination))
        finish()
    }
}
