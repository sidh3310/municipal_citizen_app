package com.example.municipalcitizenapp.models

data class AuthResponse(
    val access: String,
    val refresh: String,
    val role: String,
    val full_name: String
)
