package com.example.municipalcitizenapp.adapters

import android.content.Context
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
    private val isCitizen: Boolean = false,
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
        val context = holder.itemView.context
        val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userFullName = sharedPref.getString("full_name", null)
            ?: currentUser?.displayName
            ?: currentUser?.email
            ?: "Citizen User"

        holder.tvCategory.text = complaint.category
        holder.tvTitle.text = complaint.title
        holder.tvStatus.text = complaint.status
        holder.tvDate.text = complaint.created_at.split("T")[0]

        if (isCitizen) {
            holder.btnUpvote.isEnabled = true
            holder.btnUpvote.text = "👍 ${complaint.upvotes} Me Too"
            holder.btnUpvote.setOnClickListener {
                if (complaint.docId.isNotEmpty() && userFullName.isNotEmpty()) {
                    FirebaseRepository.upvoteComplaint(
                        docId = complaint.docId,
                        userId = userFullName,
                        onSuccess = { newVotes ->
                            complaint.upvotes = newVotes
                            holder.btnUpvote.text = "👍 $newVotes Me Too"
                            Toast.makeText(context, "Upvoted as $userFullName!", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(context, "Already upvoted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        } else {
            // Read-Only mode for Officers and Admins
            holder.btnUpvote.isEnabled = false
            holder.btnUpvote.text = "👍 ${complaint.upvotes} Votes"
        }

        holder.itemView.setOnClickListener { onItemClick(complaint) }
    }

    override fun getItemCount() = complaints.size
}
