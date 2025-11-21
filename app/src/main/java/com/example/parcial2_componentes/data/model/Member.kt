// app/src/main/java/com/example/parcial2_componentes/data/model/Member.kt
package com.example.parcial2_componentes.data.model

data class Member(
    val _id: String? = null,
    val name: String,
    val planId: String,
    val contributionPerMonth: Double = 0.0, // Valor por defecto
    val joinedAt: String? = null
)

data class CreateMemberRequest(
    val name: String,
    val planId: String,
    val contributionPerMonth: Double = 0.0 // Valor por defecto
)