package com.shruti.reminderapp.retrofit

import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {


    @FormUrlEncoded
    @POST("tts")
    suspend fun generateContent(
        @Field("text") text: String,
        @Field("gender") gender: String,
        @Field("language") language:String
    ): ResponseBody

}