// app/src/main/java/com/example/parcial2_componentes/data/repository/FamilySavingsRepository.kt
package com.example.parcial2_componentes.data.repository

import com.example.parcial2_componentes.data.remote.ApiService
import com.example.parcial2_componentes.data.model.*
import com.example.parcial2_componentes.data.remote.ApiResponse

class FamilySavingsRepository(private val apiService: ApiService) {

    suspend fun createPlan(plan: CreatePlanRequest): ApiResponse<Plan> {
        return try {
            println("🟡 ENVIANDO AL BACKEND: $plan")
            val response = apiService.createPlan(plan)
            println("🟡 RESPUESTA BACKEND - Código: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                println("✅ PLAN CREADO EXITOSAMENTE: ${response.body()}")
                ApiResponse.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error sin mensaje"
                println("🔴 ERROR DEL BACKEND: $errorMsg")
                ApiResponse.Error("Error del servidor: $errorMsg")
            }
        } catch (e: Exception) {
            println("🔴 EXCEPCIÓN: ${e.message}")
            e.printStackTrace()
            ApiResponse.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getPlans(): ApiResponse<List<Plan>> {
        return try {
            println("🟡 SOLICITANDO PLANES AL BACKEND")
            val response = apiService.getPlans()
            println("🟡 RESPUESTA PLANES - Código: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                println("✅ PLANES RECIBIDOS: ${response.body()!!.size} planes")
                ApiResponse.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error sin mensaje"
                println("🔴 ERROR AL OBTENER PLANES: $errorMsg")
                ApiResponse.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            println("🔴 EXCEPCIÓN AL OBTENER PLANES: ${e.message}")
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }

    // Members
    suspend fun createMember(member: CreateMemberRequest): ApiResponse<Member> {
        return try {
            val response = apiService.createMember(member)
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!)
            } else {
                ApiResponse.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getMembersByPlan(planId: String): ApiResponse<List<Member>> {
        return try {
            val response = apiService.getMembersByPlan(planId)
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!)
            } else {
                ApiResponse.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }

    // Payments
    suspend fun createPayment(payment: CreatePaymentRequest): ApiResponse<Payment> {
        return try {
            val response = apiService.createPayment(payment)
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