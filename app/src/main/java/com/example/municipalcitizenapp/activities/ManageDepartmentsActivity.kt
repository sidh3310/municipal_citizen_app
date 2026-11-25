package com.example.municipalcitizenapp.activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.adapters.DepartmentAdapter
import com.example.municipalcitizenapp.network.FirebaseRepository

class ManageDepartmentsActivity : ComponentActivity() {

    private lateinit var rvDepts: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_departments)

        rvDepts = findViewById(R.id.rvDepartments)
        rvDepts.layoutManager = LinearLayoutManager(this@ManageDepartmentsActivity)

        fetchDepts()

        findViewById<Button>(R.id.btnAddDept).setOnClickListener {
            showAddDeptDialog()
        }
    }

    private fun fetchDepts() {
        FirebaseRepository.getDepartments(
            onSuccess = { list ->
                rvDepts.adapter = DepartmentAdapter(list)
            },
            onError = { error ->
                Toast.makeText(this@ManageDepartmentsActivity, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showAddDeptDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Department")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val etName = EditText(this)
        etName.hint = "Department Name"
        layout.addView(etName)

        val etDesc = EditText(this)
        etDesc.hint = "Description"
        layout.addView(etDesc)

        builder.setView(layout)

        builder.setPositiveButton("ADD") { _, _ ->
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            if (name.isNotEmpty()) {
                addDept(name, desc)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun addDept(name: String, desc: String) {
        FirebaseRepository.addDepartment(
            name = name,
            description = desc,
            onSuccess = {
                Toast.makeText(this@ManageDepartmentsActivity, "Department added!", Toast.LENGTH_SHORT).show()
                fetchDepts()
            },
            onError = { error ->
                Toast.makeText(this@ManageDepartmentsActivity, "Failed to add department: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
