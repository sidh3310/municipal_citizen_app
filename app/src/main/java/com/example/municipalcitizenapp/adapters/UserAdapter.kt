package com.example.municipalcitizenapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R

class UserAdapter(private val users: List<Map<String, Any>>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvUserName)
        val tvEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvRole: TextView = view.findViewById(R.id.tvUserRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user["full_name"]?.toString() ?: ""
        holder.tvEmail.text = user["email"]?.toString() ?: ""
        holder.tvRole.text = user["role"]?.toString() ?: "CITIZEN"
    }

    override fun getItemCount() = users.size
}
