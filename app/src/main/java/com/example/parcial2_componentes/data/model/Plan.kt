// app/src/main/java/com/example/parcial2_componentes/data/model/Plan.kt
package com.example.parcial2_componentes.data.model

import com.google.gson.annotations.SerializedName

data class Plan(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("targetAmount") val goal: Double,
    @SerializedName("motive") val description: String? = null,
    @SerializedName("month") val month: Int? = null,
    @SerializedName("months") val months: Int? = null, // ✅ Agregar este campo
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("members") val members: List<Member> = emptyList(),
    @SerializedName("payments") val payments: List<Payment> = emptyList(),
    @SerializedName("totalCollected") val totalCollected: Double = 0.0
)

data class CreatePlanRequest(
    @SerializedName("name") val name: String,
    @SerializedName("targetAmount") val targetAmount: Double,
    @SerializedName("motive") val motive: String = "Ahorro familiar",
    @SerializedName("months") val months: Int = 12 // ✅ Agregar con valor por defecto
)