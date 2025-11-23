package com.example.parcial2_componentes.data.model

import com.google.gson.annotations.SerializedName

data class Plan(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("targetAmount") val targetAmount: Double,
    @SerializedName("motive") val motive: String? = null,
    @SerializedName("months") val months: Int,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("members") val members: List<Member>? = emptyList(),
    @SerializedName("payments") val payments: List<Payment>? = emptyList(),
    @SerializedName("totalCollected") val totalCollected: Double = 0.0
)

data class CreatePlanRequest(
    @SerializedName("name") val name: String,
    @SerializedName("targetAmount") val targetAmount: Double,
    @SerializedName("motive") val motive: String = "Ahorro familiar",
    @SerializedName("months") val months: Int
)