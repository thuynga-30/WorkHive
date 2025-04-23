package com.example.workhive.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: Users?
)

