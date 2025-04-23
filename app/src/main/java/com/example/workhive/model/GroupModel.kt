package com.example.workhive.model

// API Response Data Class
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val created_groups: List<Group>? = null,
    val joined_groups: List<Group>? = null
)

data class Group(
    val group_id: Int,
    val name: String,
    val description: String,
    val created_by: String,
    val members: MutableList<String>
)
// Request Data Classes
data class CreateGroupRequest(
    val name: String,
    val description: String,
    val created_by: String
)

data class AddMemberRequest(
    val group_id: Int,
    val user_name: String
)

data class RemoveUserRequest(
    val group_id: Int,
    val user_name: String
)
data class Members(
    val user_name: String,
    val role: String
)
data class MemberResponse(
    val success: Boolean,
    val message: String,
    val members: List<Members>
)

data class DeleteGroupRequest(
    val group_id: Int
)
data class DeleteResponse(
    val success: Boolean,
    val message: String
)

