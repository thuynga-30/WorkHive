package com.example.workhive.api

import com.example.workhive.model.*
import retrofit2.Call
import retrofit2.http.*


interface TaskApi {
    @GET("todo_manager.php?action=group")
    fun getTask(
        @Query("group_id") groupId: Int
    ): Call<GetTasksResponse>
    @POST("todo_manager.php?action=remove")
    fun removeTask(
        @Body requestBody: RemoveTaskRequest
    ): Call<getResponse>
    @POST("todo_manager.php?action=create")
    fun createTask(
        @Body body: CreateTaskRequest
    ): Call<getResponse>

}