package com.example.workhive.model

data class Team(

    val name: String,
    val description: String,
    var members: List<TeamMember>
)
