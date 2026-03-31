package com.shruti.reminderapp.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val geminiApiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }
}