package com.shruti.reminderapp.retrofit

import retrofit2.Retrofit

object ApiClient {
    private const val BASE_URL = "https://44e5369c5f76.ngrok-free.app/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
