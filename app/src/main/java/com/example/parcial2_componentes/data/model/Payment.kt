package com.example.parcial2_componentes.data.model

data class Payment(
    val _id: String? = null,
    val amount: Double,
    val date: String? = null,
    val memberId: String,
    val planId: String,
    val memberName: String? = null
)

data class CreatePaymentRequest(
    val amount: Double,
    val memberId: String,
    val planId: String
)