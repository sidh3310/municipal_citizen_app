package com.example.municipalcitizenapp.network

import com.example.municipalcitizenapp.models.AuthResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("auth/login/")
    fun login(@Body credentials: Map<String, String>): Call<AuthResponse>

    @POST("auth/register/")
    fun register(@Body userData: Map<String, String>): Call<Void>

    @Multipart
    @POST("complaints/")
    fun submitComplaint(
        @Header("Authorization") token: String,
        @Part("category") category: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<Void>

    @GET("complaints/")
    fun getComplaints(@Header("Authorization") token: String): Call<List<com.example.municipalcitizenapp.models.Complaint>>

    @PATCH("complaints/{id}/")
    fun updateComplaintStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body statusData: Map<String, String>
    ): Call<Void>

    @GET("notifications/")
    fun getNotifications(@Header("Authorization") token: String): Call<List<Map<String, Any>>>

    @GET("complaints/stats/")
    fun getAdminStats(@Header("Authorization") token: String): Call<Map<String, Any>>

    @GET("announcements/")
    fun getAnnouncements(): Call<List<Map<String, Any>>>

    @POST("announcements/")
    fun postAnnouncement(
        @Header("Authorization") token: String,
        @Body data: Map<String, String>
    ): Call<Void>

    @GET("users/")
    fun getUsers(@Header("Authorization") token: String): Call<List<Map<String, Any>>>

    @GET("departments/")
    fun getDepartments(@Header("Authorization") token: String): Call<List<Map<String, Any>>>

    @POST("departments/")
    fun addDepartment(@Header("Authorization") token: String, @Body data: Map<String, String>): Call<Void>
}
