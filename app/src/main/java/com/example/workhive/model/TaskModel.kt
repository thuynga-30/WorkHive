package com.example.workhive.model


data class Task(
    val task_id: Int,
    val title: String,
    val description: String?,
    val assigned_to: String?,
    val group_id: Int,
    var status: String,
    val due_date: String?,
    val parent_id: Int? // vì task tổng thì parent_id = null
)

data class RemoveTaskRequest(
    val task_id: Int,
    val group_id: Int
)
data class DeleteRequest(
    val title: String
)
data class CreateTaskRequest(
    val title: String,
    val description: String,
    val group_id: Int,
    val due_date: String
)
data class CreateSubTaskRequest(
    val task_id: Int,
    val title: String,
    val description: String,
    val assigned_to: String?,
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
data class ProgressResponse(
    val success: Boolean,
    val progress_percent: Int
)
data class UpdateRequest(
    val task_id: Int,
    val status: String
)
data class UpdateTaskRequest(
    val task_id: Int,
    val title: String,
    val description: String,
    val due_date: String

)