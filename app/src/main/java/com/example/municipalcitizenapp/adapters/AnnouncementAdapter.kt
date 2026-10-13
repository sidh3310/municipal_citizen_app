package com.example.municipalcitizenapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R

class AnnouncementAdapter(private val announcements: List<Map<String, Any>>) :
    RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvAnnouncementTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvAnnouncementMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val announcement = announcements[position]
        holder.tvTitle.text = announcement["title"]?.toString() ?: ""
        holder.tvMessage.text = announcement["message"]?.toString() ?: ""
    }

    override fun getItemCount() = announcements.size
}
