package com.example.workhive.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitTeam {
    private const val BASE_URL = "http://10.0.2.2/api/WorkHive/"
    val teamApi: TeamApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TeamApi::class.java)
    }
}