package com.example.parcial2_componentes.ui.plan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.parcial2_componentes.data.model.Plan
import com.example.parcial2_componentes.data.model.CreatePlanRequest
import com.example.parcial2_componentes.data.model.Member
import com.example.parcial2_componentes.data.model.Payment
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.data.repository.FamilySavingsRepository

class PlanViewModel(private val repository: FamilySavingsRepository) : ViewModel() {

    private val _plansState = MutableStateFlow<ApiResponse<List<Plan>>>(ApiResponse.Loading)
    val plansState: StateFlow<ApiResponse<List<Plan>>> = _plansState

    private val _createPlanState = MutableStateFlow<ApiResponse<Plan>?>(null)
    val createPlanState: StateFlow<ApiResponse<Plan>?> = _createPlanState

    // Estado para miembros por plan
    private val _membersByPlan = MutableStateFlow<Map<String, List<Member>>>(emptyMap())
    val membersByPlan: StateFlow<Map<String, List<Member>>> = _membersByPlan

    // ✅ NUEVO: Estado para pagos por plan
    private val _paymentsByPlan = MutableStateFlow<Map<String, List<Payment>>>(emptyMap())
    val paymentsByPlan: StateFlow<Map<String, List<Payment>>> = _paymentsByPlan

    // ✅ NUEVO: Estado para total recaudado por plan
    private val _totalCollectedByPlan = MutableStateFlow<Map<String, Double>>(emptyMap())
    val totalCollectedByPlan: StateFlow<Map<String, Double>> = _totalCollectedByPlan

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _plansState.value = ApiResponse.Loading
            val response = repository.getPlans()
            _plansState.value = response

            // Cargar miembros y pagos para cada plan
            if (response is ApiResponse.Success) {
                loadMembersAndPaymentsForAllPlans(response.data)
            }
        }
    }

    // ✅ MODIFICADO: Cargar tanto miembros como pagos para todos los planes
    private fun loadMembersAndPaymentsForAllPlans(plans: List<Plan>) {
        viewModelScope.launch {
            val membersMap = mutableMapOf<String, List<Member>>()
            val paymentsMap = mutableMapOf<String, List<Payment>>()
            val totalCollectedMap = mutableMapOf<String, Double>()

            plans.forEach { plan ->
                plan._id?.let { planId ->
                    // Cargar miembros
                    when (val membersResponse = repository.getMembersByPlan(planId)) {
                        is ApiResponse.Success -> {
                            membersMap[planId] = membersResponse.data
                        }
                        else -> {
                            membersMap[planId] = emptyList()
                        }
                    }

                    // ✅ NUEVO: Cargar pagos y calcular total
                    when (val paymentsResponse = repository.getPaymentsByPlan(planId)) {
                        is ApiResponse.Success -> {
                            val payments = paymentsResponse.data
                            paymentsMap[planId] = payments
                            // Calcular total recaudado sumando todos los pagos
                            totalCollectedMap[planId] = payments.sumOf { it.amount }
                        }
                        else -> {
                            paymentsMap[planId] = emptyList()
                            totalCollectedMap[planId] = 0.0
                        }
                    }
                }
            }

            _membersByPlan.value = membersMap
            _paymentsByPlan.value = paymentsMap
            _totalCollectedByPlan.value = totalCollectedMap
        }
    }

    fun createPlan(name: String, targetAmount: Double, months: Int) {
        viewModelScope.launch {
            _createPlanState.value = ApiResponse.Loading

            val planRequest = CreatePlanRequest(
                name = name,
                targetAmount = targetAmount,
                motive = "Ahorro familiar",
                months = months
            )

            _createPlanState.value = repository.createPlan(planRequest)
        }
    }

    fun clearCreatePlanState() {
        _createPlanState.value = null
    }

    fun refreshPlans() {
        loadPlans()
    }
}