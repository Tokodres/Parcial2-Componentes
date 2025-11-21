// app/src/main/java/com/example/parcial2_componentes/data/model/Payment.kt
package com.example.parcial2_componentes.data.model

data class Payment(
    val _id: String? = null,
    val amount: Double,
    val date: String? = null, // Hacer opcional si el backend la genera
    val memberId: String,
    val planId: String
)

data class CreatePaymentRequest(
    val amount: Double,
    val memberId: String,
    val planId: String
    // date puede ser opcional si el backend la genera automáticamente
)