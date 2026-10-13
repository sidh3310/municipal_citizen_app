package com.example.municipalcitizenapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.municipalcitizenapp.R
import com.example.municipalcitizenapp.models.Complaint
import com.example.municipalcitizenapp.network.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth

class ComplaintAdapter(
    private val complaints: List<Complaint>,
    private val onItemClick: (Complaint) -> Unit
) : RecyclerView.Adapter<ComplaintAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvComplaintCategory)
        val tvTitle: TextView = view.findViewById(R.id.tvComplaintTitle)
        val tvStatus: TextView = view.findViewById(R.id.tvComplaintStatus)
        val tvDate: TextView = view.findViewById(R.id.tvComplaintDate)
        val btnUpvote: Button = view.findViewById(R.id.btnUpvote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_complaint, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val complaint = complaints[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        holder.tvCategory.text = complaint.category
        holder.tvTitle.text = complaint.title
        holder.tvStatus.text = complaint.status
        holder.tvDate.text = complaint.created_at.split("T")[0]
        holder.btnUpvote.text = "👍 ${complaint.upvotes} Me Too"

        holder.btnUpvote.setOnClickListener {
            if (complaint.docId.isNotEmpty() && currentUserId.isNotEmpty()) {
                FirebaseRepository.upvoteComplaint(
                    docId = complaint.docId,
                    userId = currentUserId,
                    onSuccess = { newVotes ->
                        complaint.upvotes = newVotes
                        holder.btnUpvote.text = "👍 $newVotes Me Too"
                        Toast.makeText(holder.itemView.context, "Upvoted! Priority updated.", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(holder.itemView.context, "Already upvoted", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        holder.itemView.setOnClickListener { onItemClick(complaint) }
    }

    override fun getItemCount() = complaints.size
}
