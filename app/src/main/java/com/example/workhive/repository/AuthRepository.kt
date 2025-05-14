package com.example.workhive.repository


import com.example.workhive.api.RetrofitClient
import com.example.workhive.model.LoginRequest
import com.example.workhive.model.LoginResponse
import com.example.workhive.model.RegisterResponse
import com.example.workhive.model.Users
import retrofit2.Call
import retrofit2.Response

class AuthRepository {
    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return RetrofitClient.api.login(request)
    }
    suspend fun register(user: Users): Response<RegisterResponse> {
        return RetrofitClient.api.register(user)
    }
}

