package com.example.municipalcitizenapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.UserAdapter
import com.example.municipalcitizenapp.network.FirebaseRepository

class ManageUsersActivity : ComponentActivity() {

    private lateinit var rvUsers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        rvUsers = findViewById(R.id.rvUsers)
        rvUsers.layoutManager = LinearLayoutManager(this@ManageUsersActivity)

        fetchUsers()
    }

    private fun fetchUsers() {
        FirebaseRepository.getUsers(
            onSuccess = { users ->
                rvUsers.adapter = UserAdapter(users)
            },
            onError = { error ->
                Toast.makeText(this@ManageUsersActivity, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
