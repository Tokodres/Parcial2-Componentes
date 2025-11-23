package com.example.parcial2_componentes.data.model

data class Member(
    val _id: String? = null,
    val name: String,
    val planId: String,
    val contributionPerMonth: Double = 0.0,
    val joinedAt: String? = null
)

data class CreateMemberRequest(
    val name: String,
    val planId: String,
    val contributionPerMonth: Double = 0.0
)