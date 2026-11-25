package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.municipalcitizenapp.R
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvName: TextView = findViewById(R.id.tvProfileName)
        val tvEmail: TextView = findViewById(R.id.tvProfileEmail)
        val btnLogout: Button = findViewById(R.id.btnLogoutProfile)

        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: sharedPref.getString("email", "Email not set")

        tvName.text = sharedPref.getString("full_name", "User")
        tvEmail.text = email

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sharedPref.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
