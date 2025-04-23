package com.example.workhive.api

import com.example.workhive.model.*
import retrofit2.*
import retrofit2.http.*

interface TeamApi {
    @GET("team_manager.php")
    fun getGroups(
        @Header("Authorization") userName: String
    ): Call<ApiResponse>

    @GET("team_manager.php")
    fun getMembersOfGroup(
        @Query("group_id") groupId: Int
    ): Call<MemberResponse>

    @POST("team_manager.php")
    fun createGroup(
        @Header("Authorization") userName: String,
        @Body body: CreateGroupRequest): Call<ApiResponse>

    @POST("team_manager.php?add_member=1")
    fun addMember(
        @Header("Authorization") userName: String,
        @Body body: AddMemberRequest): Call<ApiResponse>

    @POST("team_manager.php?delete_group=1")
    fun deleteGroup(
        @Header("Authorization") userName: String,
        @Body body: DeleteGroupRequest
    ): Call<DeleteResponse>
    @POST("team_manager.php?remove_user=1")
    fun removeMember(
        @Header("Authorization") userName: String,
        @Body body: RemoveUserRequest): Call<DeleteResponse>
}