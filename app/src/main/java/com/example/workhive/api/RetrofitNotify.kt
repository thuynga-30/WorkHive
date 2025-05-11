package com.example.workhive.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitNotify {
    private const val BASE_URL = "http://10.0.2.2/api/WorkHive/"
    val notifyApi: NotifyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotifyApi::class.java)
    }
}