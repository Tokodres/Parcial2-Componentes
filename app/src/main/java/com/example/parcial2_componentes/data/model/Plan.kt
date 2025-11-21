
package com.example.parcial2_componentes.data.model

data class Plan(
    val _id: String? = null,
    val name: String,
    val goal: Double,
    val createdAt: String? = null,
    val members: List<Member> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val totalCollected: Double = 0.0
)

data class CreatePlanRequest(
    val name: String,
    val goal: Double
)