// app/src/main/java/com/example/parcial2_componentes/data/model/Member.kt
package com.example.parcial2_componentes.data.model

data class Member(
    val _id: String? = null,
    val name: String,
    val planId: String
)

data class CreateMemberRequest(
    val name: String,
    val planId: String
)