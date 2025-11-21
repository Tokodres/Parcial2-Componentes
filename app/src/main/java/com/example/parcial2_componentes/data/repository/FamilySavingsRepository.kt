// app/src/main/java/com/example/parcial2_componentes/data/repository/FamilySavingsRepository.kt
package com.example.parcial2_componentes.data.repository

import com.example.parcial2_componentes.data.remote.ApiService
import com.example.parcial2_componentes.data.model.*
import com.example.parcial2_componentes.data.remote.ApiResponse

class FamilySavingsRepository(private val apiService: ApiService) {

    suspend fun createPlan(plan: CreatePlanRequest): ApiResponse<Plan> {
        return try {
            println("🟡 [REPOSITORY] Enviando plan al backend: $plan")
            val response = apiService.createPlan(plan)
            println("🟡 [REPOSITORY] Respuesta - Código: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val createdPlan = response.body()!!
                println("✅ [REPOSITORY] PLAN CREADO EXITOSAMENTE:")
                println("   - ID: ${createdPlan._id}")
                println("   - Nombre: ${createdPlan.name}")
                println("   - Meta: ${createdPlan.targetAmount}")
                println("   - Meses: ${createdPlan.months}")
                ApiResponse.Success(createdPlan)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error sin cuerpo"
                println("🔴 [REPOSITORY] ERROR: $errorBody")
                ApiResponse.Error("Error del servidor: $errorBody")
            }
        } catch (e: Exception) {
            println("🔴 [REPOSITORY] EXCEPCIÓN: ${e.message}")
            ApiResponse.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getPlans(): ApiResponse<List<Plan>> {
        return try {
            println("🟡 [REPOSITORY] Solicitando planes al backend...")
            val response = apiService.getPlans()
            println("🟡 [REPOSITORY] Código de respuesta: ${response.code()}")

            if (response.isSuccessful) {
                val plans = response.body() ?: emptyList()
                println("✅ [REPOSITORY] ¡PLANES RECIBIDOS! Cantidad: ${plans.size}")
                ApiResponse.Success(plans)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error sin mensaje"
                println("🔴 [REPOSITORY] Error al obtener planes: $errorMsg")
                ApiResponse.Error("Error del servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            println("🔴 [REPOSITORY] Excepción al obtener planes: ${e.message}")
            e.printStackTrace()
            ApiResponse.Error("Error de conexión: ${e.message}")
        }
    }

    // Resto de funciones para miembros y pagos...
    suspend fun createMember(member: CreateMemberRequest): ApiResponse<Member> {
        return try {
            println("🟡 [REPOSITORY] Enviando miembro al backend: $member")
            val response = apiService.createMember(member)
            println("🟡 [REPOSITORY] Respuesta - Código: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val createdMember = response.body()!!
                println("✅ [REPOSITORY] MIEMBRO CREADO EXITOSAMENTE:")
                println("   - ID: ${createdMember._id}")
                println("   - Nombre: ${createdMember.name}")
                println("   - Plan ID: ${createdMember.planId}")
                ApiResponse.Success(createdMember)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error sin cuerpo"
                println("🔴 [REPOSITORY] ERROR: $errorBody")
                ApiResponse.Error("Error del servidor: $errorBody")
            }
        } catch (e: Exception) {
            println("🔴 [REPOSITORY] EXCEPCIÓN: ${e.message}")
            ApiResponse.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getMembersByPlan(planId: String): ApiResponse<List<Member>> {
        return try {
            println("🟡 [REPOSITORY] Solicitando miembros para plan: $planId")
            val response = apiService.getMembersByPlan(planId)
            println("🟡 [REPOSITORY] Respuesta - Código: ${response.code()}")

            if (response.isSuccessful) {
                val members = response.body() ?: emptyList()
                println("✅ [REPOSITORY] MIEMBROS RECIBIDOS: ${members.size}")
                ApiResponse.Success(members)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error sin mensaje"
                println("🔴 [REPOSITORY] ERROR: $errorMsg")
                ApiResponse.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            println("🔴 [REPOSITORY] EXCEPCIÓN: ${e.message}")
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }

    // Funciones para pagos...
}