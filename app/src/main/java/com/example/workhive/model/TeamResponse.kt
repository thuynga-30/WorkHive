package com.example.workhive.model

data class TeamResponse(
    val success: Boolean,
    val created_groups: List<Team>,
    val joined_groups: List<Team>
)
