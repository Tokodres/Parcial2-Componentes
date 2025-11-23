package com.example.parcial2_componentes.ui.payment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.parcial2_componentes.data.model.Payment
import com.example.parcial2_componentes.data.model.CreatePaymentRequest
import com.example.parcial2_componentes.data.model.Member
import com.example.parcial2_componentes.data.model.Plan
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.data.repository.FamilySavingsRepository

class PaymentViewModel(private val repository: FamilySavingsRepository) : ViewModel() {

    private val _paymentsState = MutableStateFlow<ApiResponse<List<Payment>>>(ApiResponse.Loading)
    val paymentsState: StateFlow<ApiResponse<List<Payment>>> = _paymentsState

    private val _membersState = MutableStateFlow<ApiResponse<List<Member>>>(ApiResponse.Loading)
    val membersState: StateFlow<ApiResponse<List<Member>>> = _membersState

    private val _memberPaymentsState = MutableStateFlow<ApiResponse<List<Payment>>>(ApiResponse.Loading)
    val memberPaymentsState: StateFlow<ApiResponse<List<Payment>>> = _memberPaymentsState

    private val _createPaymentState = MutableStateFlow<ApiResponse<Payment>?>(null)
    val createPaymentState: StateFlow<ApiResponse<Payment>?> = _createPaymentState

    // Estado para el plan actual
    private val _currentPlan = MutableStateFlow<Plan?>(null)
    val currentPlan: StateFlow<Plan?> = _currentPlan

    fun loadPaymentsByPlan(planId: String) {
        viewModelScope.launch {
            _paymentsState.value = ApiResponse.Loading
            _paymentsState.value = repository.getPaymentsByPlan(planId)
        }
    }

    fun loadMembersByPlan(planId: String) {
        viewModelScope.launch {
            _membersState.value = ApiResponse.Loading
            _membersState.value = repository.getMembersByPlan(planId)
        }
    }

    // ✅ NUEVA: Cargar pagos de un miembro específico
    fun loadPaymentsByMember(memberId: String) {
        viewModelScope.launch {
            _memberPaymentsState.value = ApiResponse.Loading
            _memberPaymentsState.value = repository.getPaymentsByMember(memberId)
        }
    }

    // ✅ NUEVA: Cargar información del plan
    fun loadPlan(planId: String) {
        viewModelScope.launch {
            // Cargamos todos los planes y filtramos por ID
            when (val response = repository.getPlans()) {
                is ApiResponse.Success -> {
                    val plan = response.data.find { it._id == planId }
                    _currentPlan.value = plan
                    println("✅ [VIEWMODEL] Plan cargado: ${plan?.name}")
                }
                is ApiResponse.Error -> {
                    println("🔴 [VIEWMODEL] Error al cargar plan: ${response.message}")
                    _currentPlan.value = null
                }
                else -> {
                    _currentPlan.value = null
                }
            }
        }
    }

    fun createPayment(amount: Double, memberId: String, planId: String) {
        viewModelScope.launch {
            _createPaymentState.value = ApiResponse.Loading

            val paymentRequest = CreatePaymentRequest(
                amount = amount,
                memberId = memberId,
                planId = planId
            )

            _createPaymentState.value = repository.createPayment(paymentRequest)
        }
    }

    fun clearCreatePaymentState() {
        _createPaymentState.value = null
    }
}