// app/src/main/java/com/example/parcial2_componentes/data/repository/FamilySavingsRepository.kt
package com.example.parcial2_componentes.data.repository

import com.example.parcial2_componentes.data.remote.ApiService
import com.example.parcial2_componentes.data.model.*
import com.example.parcial2_componentes.data.remote.ApiResponse

class FamilySavingsRepository(private val apiService: ApiService) {

    suspend fun createPlan(plan: CreatePlanRequest): ApiResponse<Plan> {
        return try {
            val response = apiService.createPlan(plan)
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!)
            } else {
                ApiResponse.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getPlans(): ApiResponse<List<Plan>> {
        return try {
            val response = apiService.getPlans()
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!)
            } else {
                ApiResponse.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }
}