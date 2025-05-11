package com.example.workhive.model

data class Notification(
    val id :Int,
    val user_name: String?,
    val content : String,
    val is_read: Int,
    val created_at: String
)
data class GetNotifyResponse(
    val success: Boolean,
    val message: String,
    val notify: List<Notification>?
)
data class SimpleResponse(
    val success: Boolean,
    val message: String
)
