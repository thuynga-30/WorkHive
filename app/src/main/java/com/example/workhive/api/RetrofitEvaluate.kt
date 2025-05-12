package com.example.workhive.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitEvaluate {
    private const val BASE_URL = "http://10.0.2.2/api/WorkHive/"
    val api: EvaluateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EvaluateApi::class.java)
    }
}