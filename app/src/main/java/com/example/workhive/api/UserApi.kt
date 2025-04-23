package com.example.workhive.api

import com.example.workhive.model.LoginRequest
import com.example.workhive.model.LoginResponse
import com.example.workhive.model.RegisterResponse
import com.example.workhive.model.Users
import retrofit2.*
import retrofit2.http.*

interface UserApi {
    @Headers("Content-Type: application/json")
    @POST("register.php")
    fun register(@Body request: Users): Call<RegisterResponse>
    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

}