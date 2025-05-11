package com.example.workhive.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitTask {
    val gson = GsonBuilder()
        .setLenient()
        .create()
    private const val BASE_URL = "http://10.0.2.2/api/WorkHive/"
    val taskApi: TaskApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TaskApi::class.java)
    }
}