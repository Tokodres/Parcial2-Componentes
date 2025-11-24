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

    // Estado para almacenar miembros por plan
    private val _membersByPlan = MutableStateFlow<Map<String, List<Member>>>(emptyMap())
    val membersByPlan: StateFlow<Map<String, List<Member>>> = _membersByPlan

    // Estado para almacenar pagos por plan
    private val _paymentsByPlan = MutableStateFlow<Map<String, List<Payment>>>(emptyMap())
    val paymentsByPlan: StateFlow<Map<String, List<Payment>>> = _paymentsByPlan

    // Estado para almacenar total recaudado por plan
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

            // Si la respuesta es exitosa, cargar miembros y pagos para cada plan
            if (response is ApiResponse.Success) {
                loadMembersAndPaymentsForPlans(response.data)
            }
        }
    }

    private fun loadMembersAndPaymentsForPlans(plans: List<Plan>) {
        viewModelScope.launch {
            plans.forEach { plan ->
                plan._id?.let { planId ->
                    // Cargar miembros para este plan
                    launch {
                        when (val membersResponse = repository.getMembersByPlan(planId)) {
                            is ApiResponse.Success -> {
                                _membersByPlan.value = _membersByPlan.value + (planId to membersResponse.data)
                            }
                            else -> {
                                // En caso de error, usar lista vacía
                                _membersByPlan.value = _membersByPlan.value + (planId to emptyList())
                            }
                        }
                    }

                    // Cargar pagos para este plan
                    launch {
                        when (val paymentsResponse = repository.getPaymentsByPlan(planId)) {
                            is ApiResponse.Success -> {
                                _paymentsByPlan.value = _paymentsByPlan.value + (planId to paymentsResponse.data)

                                // Calcular total recaudado para este plan
                                val totalCollected = paymentsResponse.data.sumOf { it.amount }
                                _totalCollectedByPlan.value = _totalCollectedByPlan.value + (planId to totalCollected)
                            }
                            else -> {
                                // En caso de error, usar lista vacía y total 0
                                _paymentsByPlan.value = _paymentsByPlan.value + (planId to emptyList())
                                _totalCollectedByPlan.value = _totalCollectedByPlan.value + (planId to 0.0)
                            }
                        }
                    }
                }
            }
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

    // ✅ NUEVA FUNCIÓN: Recargar miembros específicos de un plan
    fun loadMembersForPlan(planId: String) {
        viewModelScope.launch {
            when (val response = repository.getMembersByPlan(planId)) {
                is ApiResponse.Success -> {
                    _membersByPlan.value = _membersByPlan.value + (planId to response.data)
                }
                else -> {
                    // En caso de error, mantener el estado actual o limpiar?
                    // Depende de tu lógica de negocio
                }
            }
        }
    }

    // ✅ NUEVA FUNCIÓN: Recargar pagos específicos de un plan
    fun loadPaymentsForPlan(planId: String) {
        viewModelScope.launch {
            when (val response = repository.getPaymentsByPlan(planId)) {
                is ApiResponse.Success -> {
                    _paymentsByPlan.value = _paymentsByPlan.value + (planId to response.data)

                    // Recalcular total recaudado para este plan
                    val totalCollected = response.data.sumOf { it.amount }
                    _totalCollectedByPlan.value = _totalCollectedByPlan.value + (planId to totalCollected)
                }
                else -> {
                    // En caso de error, mantener el estado actual
                }
            }
        }
    }

    // ✅ NUEVA FUNCIÓN: Recargar todo para un plan específico (miembros y pagos)
    fun refreshPlanData(planId: String) {
        viewModelScope.launch {
            // Cargar miembros
            launch {
                when (val membersResponse = repository.getMembersByPlan(planId)) {
                    is ApiResponse.Success -> {
                        _membersByPlan.value = _membersByPlan.value + (planId to membersResponse.data)
                    }
                    else -> {
                        // Manejar error si es necesario
                    }
                }
            }

            // Cargar pagos
            launch {
                when (val paymentsResponse = repository.getPaymentsByPlan(planId)) {
                    is ApiResponse.Success -> {
                        _paymentsByPlan.value = _paymentsByPlan.value + (planId to paymentsResponse.data)

                        // Recalcular total recaudado
                        val totalCollected = paymentsResponse.data.sumOf { it.amount }
                        _totalCollectedByPlan.value = _totalCollectedByPlan.value + (planId to totalCollected)
                    }
                    else -> {
                        // Manejar error si es necesario
                    }
                }
            }
        }
    }

    // Función para obtener un plan específico por ID
    fun getPlanById(planId: String): Plan? {
        return when (val state = _plansState.value) {
            is ApiResponse.Success -> {
                state.data.find { it._id == planId }
            }
            else -> null
        }
    }

    // Función para limpiar todos los estados
    fun clearAllState() {
        _plansState.value = ApiResponse.Loading
        _createPlanState.value = null
        _membersByPlan.value = emptyMap()
        _paymentsByPlan.value = emptyMap()
        _totalCollectedByPlan.value = emptyMap()
    }
}