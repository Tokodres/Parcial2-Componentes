// app/src/main/java/com/example/parcial2_componentes/ui/plan/viewmodel/PlanViewModel.kt
package com.example.parcial2_componentes.ui.plan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.parcial2_componentes.data.model.Plan
import com.example.parcial2_componentes.data.model.CreatePlanRequest
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.data.repository.FamilySavingsRepository

class PlanViewModel(private val repository: FamilySavingsRepository) : ViewModel() {

    private val _plansState = MutableStateFlow<ApiResponse<List<Plan>>>(ApiResponse.Loading)
    val plansState: StateFlow<ApiResponse<List<Plan>>> = _plansState

    private val _createPlanState = MutableStateFlow<ApiResponse<Plan>?>(null)
    val createPlanState: StateFlow<ApiResponse<Plan>?> = _createPlanState

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _plansState.value = ApiResponse.Loading
            _plansState.value = repository.getPlans()
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
}