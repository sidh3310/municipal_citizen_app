package com.example.municipalcitizenapp.models

data class Complaint(
    var docId: String = "",
    val id: Int = 0,
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val image: String? = null,
    val resolved_image: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    var status: String = "SUBMITTED",
    var priority: String = "MEDIUM",
    var resolution_notes: String? = null,
    val created_at: String = "",
    val userId: String = "",
    val userEmail: String = "",
    var upvotes: Int = 0,
    val upvotedBy: List<String> = emptyList()
)
