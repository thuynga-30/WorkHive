package com.example.workhive.api

import com.example.workhive.model.*
import retrofit2.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface NotifyApi {
    @GET ("notification_manager.php?")
    suspend fun getNotify(
        @Header("Authorization") userName: String
    ): Response<GetNotifyResponse>
    @PUT("notification_manager.php")
    suspend fun markAllAsRead(
        @Header("Authorization") userName: String
    ): Response<SimpleResponse>
    @POST("notification_manager.php")
    suspend fun deleteAll(
        @Header("Authorization") userName: String
    ):Response<SimpleResponse>

}