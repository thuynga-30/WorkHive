package com.example.workhive.api

import com.example.workhive.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


interface TaskApi {
    @GET("todo_manager.php?action=group")
    fun getTask(
        @Query("group_id") groupId: Int
    ): Call<GetTasksResponse>
    @GET("todo_manager.php?action=subtask")
    suspend fun getSubTask(
        @Query("group_id") groupId: Int,
        @Query("task_id") taskId: Int
    ): Response<GetTasksResponse>
    @GET("todo_manager.php?action=stats_group")
    suspend fun getTaskProgress(
        @Query("task_id") taskId: Int
    ): Response<ProgressResponse>
    @GET("todo_manager.php?action=user")
    suspend fun getTaskByUser(
        @Header("Authorization") userName: String
    ): GetTasksResponse
    @GET("todo_manager.php?action=check_over")
    fun checkOver(): Call<ResponseBody>
    @POST("todo_manager.php?action=remove")
    fun removeTask(
        @Body requestBody: RemoveTaskRequest
    ): Call<getResponse>
    @POST("todo_manager.php?action=create")
    fun createTask(
        @Body body: CreateTaskRequest
    ): Call<getResponse>
    @POST("todo_manager.php?action=createSubTask")
    fun createSubTask(
        @Body body: CreateSubTaskRequest
    ):Call<getResponse>
    @POST("todo_manager.php?action=delete")
    fun deleteSubTask(
        @Body body: DeleteRequest
    ): Call<getResponse>
    @PUT("todo_manager.php?action=status")
    suspend fun updateStatus(
        @Body body: UpdateRequest
    ): Response<getResponse>
    @PUT("todo_manager.php?action=update")
    fun updateTask(
        @Body body: UpdateTaskRequest
    ): Call<getResponse>
}