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

    suspend fun createMember(member: CreateMemberRequest): ApiResponse<Member> {
        return try {
            println("🟡 [REPOSITORY] Enviando miembro al backend: $member")
            val startTime = System.currentTimeMillis() // ✅ Medir tiempo
            val response = apiService.createMember(member)
            val endTime = System.currentTimeMillis()
            println("🟡 [REPOSITORY] Respuesta recibida en ${endTime - startTime}ms - Código: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val createdMember = response.body()!!
                println("✅ [REPOSITORY] MIEMBRO CREADO EXITOSAMENTE en ${endTime - startTime}ms:")
                println("   - ID: ${createdMember._id}")
                println("   - Nombre: ${createdMember.name}")
                println("   - Plan ID: ${createdMember.planId}")
                ApiResponse.Success(createdMember)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error sin cuerpo"
                println("🔴 [REPOSITORY] ERROR en ${endTime - startTime}ms: $errorBody")
                ApiResponse.Error("Error del servidor: $errorBody")
            }
        } catch (e: Exception) {
            println("🔴 [REPOSITORY] EXCEPCIÓN: ${e.message}")
            e.printStackTrace()
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

    suspend fun createPayment(payment: CreatePaymentRequest): ApiResponse<Payment> {
        return try {
            println("🟡 [REPOSITORY] Enviando pago al backend: $payment")
            val response = apiService.createPayment(payment)
            println("🟡 [REPOSITORY] Respuesta - Código: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val createdPayment = response.body()!!
                println("✅ [REPOSITORY] PAGO REGISTRADO EXITOSAMENTE:")
                println("   - ID: ${createdPayment._id}")
                println("   - Monto: ${createdPayment.amount}")
                println("   - Miembro ID: ${createdPayment.memberId}")
                println("   - Plan ID: ${createdPayment.planId}")
                ApiResponse.Success(createdPayment)
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

    suspend fun getPaymentsByPlan(planId: String): ApiResponse<List<Payment>> {
        return try {
            println("🟡 [REPOSITORY] Solicitando pagos para plan: $planId")
            val response = apiService.getPaymentsByPlan(planId)
            println("🟡 [REPOSITORY] Respuesta - Código: ${response.code()}")

            if (response.isSuccessful) {
                val payments = response.body() ?: emptyList()
                println("✅ [REPOSITORY] PAGOS RECIBIDOS: ${payments.size}")
                ApiResponse.Success(payments)
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

    // ✅ CORREGIDO: Obtener pagos por miembro - versión mejorada
    suspend fun getPaymentsByMember(memberId: String): ApiResponse<List<Payment>> {
        return try {
            println("🟡 [REPOSITORY] Solicitando pagos para miembro: $memberId")

            // Obtenemos todos los planes
            val plansResponse = getPlans()

            if (plansResponse is ApiResponse.Success) {
                val allPayments = mutableListOf<Payment>()

                // Recorrer todos los planes y recolectar pagos del miembro
                for (plan in plansResponse.data) {
                    plan._id?.let { planId ->
                        try {
                            val paymentsResponse = getPaymentsByPlan(planId)
                            if (paymentsResponse is ApiResponse.Success) {
                                val memberPayments = paymentsResponse.data.filter { it.memberId == memberId }
                                allPayments.addAll(memberPayments)
                            }
                        } catch (e: Exception) {
                            println("🔴 [REPOSITORY] Error obteniendo pagos del plan $planId: ${e.message}")
                            // Continuar con el siguiente plan si hay error
                        }
                    }
                }

                println("✅ [REPOSITORY] PAGOS DEL MIEMBRO ENCONTRADOS: ${allPayments.size}")
                ApiResponse.Success(allPayments)
            } else {
                ApiResponse.Error("No se pudieron obtener los planes para buscar los pagos")
            }
        } catch (e: Exception) {
            println("🔴 [REPOSITORY] EXCEPCIÓN: ${e.message}")
            ApiResponse.Error("Error de conexión: ${e.message}")
        }
    }
}