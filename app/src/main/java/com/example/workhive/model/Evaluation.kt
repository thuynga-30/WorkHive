package com.example.workhive.model

data class Evaluation(
    val group_id: Int,
    val evaluated_user: String,
    val total_subtasks: Int,
    val ontime_subtasks: Int,
    val rating: String,
    val evaluated_at: String
)
data class EvaluationResponse(
    val success: Boolean,
    val evaluations: List<Evaluation>? ,
    val message: String?
)

data class GeneralResponse(
    val success: Boolean,
    val message: String
)