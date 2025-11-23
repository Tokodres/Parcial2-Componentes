package com.example.parcial2_componentes.data.remote

import retrofit2.Response
import retrofit2.http.*
import com.example.parcial2_componentes.data.model.*

interface ApiService {
    @POST("api/plans")
    suspend fun createPlan(@Body plan: CreatePlanRequest): Response<Plan>

    @GET("api/plans")
    suspend fun getPlans(): Response<List<Plan>>

    @GET("api/plans/{id}")
    suspend fun getPlan(@Path("id") id: String): Response<Plan>

    @POST("api/members")
    suspend fun createMember(@Body member: CreateMemberRequest): Response<Member>

    @GET("api/members/plan/{planId}")
    suspend fun getMembersByPlan(@Path("planId") planId: String): Response<List<Member>>

    @POST("api/payments")
    suspend fun createPayment(@Body payment: CreatePaymentRequest): Response<Payment>

    @GET("api/payments/plan/{planId}")
    suspend fun getPaymentsByPlan(@Path("planId") planId: String): Response<List<Payment>>
}