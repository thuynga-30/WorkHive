package com.example.workhive.api

import com.example.workhive.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface EvaluateApi {
    @GET("evaluations.php")
    fun getEvaluations(
        @Query("group_id") groupId: Int
    ): Call<EvaluationResponse>

    @POST("evaluations.php")
    fun createEvaluations(
        @Query("group_id") groupId: Int
    ): Call<GeneralResponse>
}