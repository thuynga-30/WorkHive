package com.example.workhive.model

data class Task(
    val task_id: Int,
    val title: String,
    val description: String?,
    val assigned_to: String?,
    val group_id: Int,
    val status: String,
    val due_date: String?,
    val created_at: String,
    val parent_id: Int? // vì task tổng thì parent_id = null
)

data class RemoveTaskRequest(
    val task_id: Int,
    val group_id: Int
)
data class CreateTaskRequest(
    val title: String,
    val description: String,
    val group_id: Int,
    val due_date: String
)
data class getResponse(
    val success: Boolean,
    val message: String
)
data class GetTasksResponse(
    val success: Boolean,
    val message: String,
    val tasks: List<Task>?
)