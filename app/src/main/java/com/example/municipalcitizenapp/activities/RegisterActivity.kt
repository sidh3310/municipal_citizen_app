package com.example.municipalcitizenapp.activities

import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.example.municipalcitizenapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : ComponentActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        spinnerRole = findViewById(R.id.spinnerRole)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        setupRoleSpinner()

        btnRegister.setOnClickListener {
            validateAndRegister()
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("Citizen", "Municipal Officer", "Administrator")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter
    }

    private fun validateAndRegister() {
        val name = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        val selectedRole = when (spinnerRole.selectedItemPosition) {
            1 -> "OFFICER"
            2 -> "ADMIN"
            else -> "CITIZEN"
        }

        if (name.isEmpty()) {
            etFullName.error = "Name cannot be empty"
            etFullName.requestFocus()
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Valid email is required"
            etEmail.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            etPhone.error = "Phone number is required"
            etPhone.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password minimum length 6 characters"
            etPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords must match"
            etConfirmPassword.requestFocus()
            return
        }

        btnRegister.isEnabled = false
        performDualRegister(name, email, phone, password, selectedRole)
    }

    private fun performDualRegister(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String
    ) {
        val userData = mapOf(
            "full_name" to name,
            "email" to email,
            "phone" to phone,
            "password" to password,
            "role" to role
        )

        // 1. Register in Django REST + PostgreSQL
        RetrofitClient.instance.register(userData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // 2. Sync Registration with Firebase Cloud
                FirebaseRepository.registerUser(name, email, phone, password, role, onSuccess = {}, onError = {})

                btnRegister.isEnabled = true
                Toast.makeText(this@RegisterActivity, "Account Created ($role)", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Fallback to Firebase Registration if local server offline
                FirebaseRepository.registerUser(
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    role = role,
                    onSuccess = {
                        btnRegister.isEnabled = true
                        Toast.makeText(this@RegisterActivity, "Account Created on Cloud ($role)", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onError = { error ->
                        btnRegister.isEnabled = true
                        Toast.makeText(this@RegisterActivity, "Registration failed: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        })
    }
}
