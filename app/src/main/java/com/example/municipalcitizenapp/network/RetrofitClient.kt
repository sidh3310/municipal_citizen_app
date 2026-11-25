package com.example.municipalcitizenapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    /** 
     * PERMANENT CONNECTION STRATEGY:
     * 1. 127.0.0.1 routes through the USB cable (via ADB Reverse).
     * 2. If you unplug the cable, the connection breaks.
     * 3. Use the "Fix_App_Connection" file on your Desktop if it stops working.
     */
    private const val BASE_URL = "http://127.0.0.1:8000/api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS) // Maximum patience for slow bridges
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true) // Automatically try again if it drops
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        retrofit.create(ApiService::class.java)
    }
}
