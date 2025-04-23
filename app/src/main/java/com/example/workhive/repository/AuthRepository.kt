package com.example.workhive.repository


import com.example.workhive.api.RetrofitClient
import com.example.workhive.model.LoginRequest
import com.example.workhive.model.LoginResponse
import com.example.workhive.model.RegisterResponse
import com.example.workhive.model.Users
import retrofit2.Call

class AuthRepository {
    fun login(request: LoginRequest): Call<LoginResponse> {
        return RetrofitClient.api.login(request)
    }
    fun register(user: Users): Call<RegisterResponse> {
        return RetrofitClient.api.register(user)
    }
}

