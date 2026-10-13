package com.example.municipalcitizenapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R

class DepartmentAdapter(private val depts: List<Map<String, Any>>) :
    RecyclerView.Adapter<DepartmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDeptName)
        val tvDesc: TextView = view.findViewById(R.id.tvDeptDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_department, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dept = depts[position]
        holder.tvName.text = dept["name"]?.toString() ?: ""
        holder.tvDesc.text = dept["description"]?.toString() ?: ""
    }

    override fun getItemCount() = depts.size
}
