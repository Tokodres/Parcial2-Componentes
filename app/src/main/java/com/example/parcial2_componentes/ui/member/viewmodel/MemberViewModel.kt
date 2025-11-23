package com.example.parcial2_componentes.ui.member.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.parcial2_componentes.data.model.Member
import com.example.parcial2_componentes.data.model.CreateMemberRequest
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.data.repository.FamilySavingsRepository

class MemberViewModel(private val repository: FamilySavingsRepository) : ViewModel() {

    private val _createMemberState = MutableStateFlow<ApiResponse<Member>?>(null)
    val createMemberState: StateFlow<ApiResponse<Member>?> = _createMemberState

    private val _membersState = MutableStateFlow<ApiResponse<List<Member>>>(ApiResponse.Loading)
    val membersState: StateFlow<ApiResponse<List<Member>>> = _membersState

    fun createMember(name: String, planId: String) {
        viewModelScope.launch {
            _createMemberState.value = ApiResponse.Loading

            val memberRequest = CreateMemberRequest(
                name = name,
                planId = planId,
                contributionPerMonth = 0.0
            )

            _createMemberState.value = repository.createMember(memberRequest)
        }
    }

    fun loadMembersByPlan(planId: String) {
        viewModelScope.launch {
            _membersState.value = ApiResponse.Loading
            _membersState.value = repository.getMembersByPlan(planId)
        }
    }

    fun clearCreateMemberState() {
        _createMemberState.value = null
    }
}