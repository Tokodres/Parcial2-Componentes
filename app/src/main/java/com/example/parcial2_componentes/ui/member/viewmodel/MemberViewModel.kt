package com.example.parcial2_componentes.ui.member.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.parcial2_componentes.data.model.Member
import com.example.parcial2_componentes.data.model.CreateMemberRequest
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.data.repository.FamilySavingsRepository

class MemberViewModel(private val repository: FamilySavingsRepository) : ViewModel() {

    private val _createMemberState = MutableStateFlow<ApiResponse<Member>?>(null)
    val createMemberState: StateFlow<ApiResponse<Member>?> = _createMemberState

    private val _membersState = MutableStateFlow<ApiResponse<List<Member>>>(ApiResponse.Loading)
    val membersState: StateFlow<ApiResponse<List<Member>>> = _membersState

    // ✅ NUEVO: Estado para controlar si está procesando
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    fun createMember(name: String, planId: String) {
        // ✅ NUEVO: Prevenir múltiples llamadas simultáneas
        if (_isProcessing.value) {
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            _createMemberState.value = ApiResponse.Loading

            try {
                val memberRequest = CreateMemberRequest(
                    name = name,
                    planId = planId,
                    contributionPerMonth = 0.0
                )

                // ✅ NUEVO: Intentar hasta 3 veces con retry
                var retryCount = 0
                var success = false
                var lastError: Exception? = null

                while (retryCount < 3 && !success) {
                    try {
                        val response = repository.createMember(memberRequest)
                        _createMemberState.value = response
                        success = true
                    } catch (e: Exception) {
                        lastError = e
                        retryCount++
                        if (retryCount < 3) {
                            delay(1000L * retryCount) // ✅ Retry con backoff
                        }
                    }
                }

                // ✅ NUEVO: Si después de 3 intentos falla, mostrar error
                if (!success) {
                    _createMemberState.value = ApiResponse.Error(
                        "Error de conexión después de 3 intentos: ${lastError?.message ?: "Error desconocido"}"
                    )
                }

            } catch (e: Exception) {
                _createMemberState.value = ApiResponse.Error("Error inesperado: ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun loadMembersByPlan(planId: String) {
        viewModelScope.launch {
            _membersState.value = ApiResponse.Loading

            try {
                // ✅ NUEVO: Agregar retry para cargar miembros
                var retryCount = 0
                var success = false

                while (retryCount < 3 && !success) {
                    try {
                        _membersState.value = repository.getMembersByPlan(planId)
                        success = true
                    } catch (e: Exception) {
                        retryCount++
                        if (retryCount < 3) {
                            delay(1000L * retryCount)
                        } else {
                            _membersState.value = ApiResponse.Error("Error al cargar miembros: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                _membersState.value = ApiResponse.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun clearCreateMemberState() {
        _createMemberState.value = null
    }

    // ✅ NUEVO: Función para resetear el estado de procesamiento
    fun resetProcessing() {
        _isProcessing.value = false
    }
}