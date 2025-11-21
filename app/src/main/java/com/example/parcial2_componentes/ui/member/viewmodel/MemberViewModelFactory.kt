// app/src/main/java/com/example/parcial2_componentes/ui/member/viewmodel/MemberViewModelFactory.kt
package com.example.parcial2_componentes.ui.member.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parcial2_componentes.data.repository.FamilySavingsRepository

class MemberViewModelFactory(private val repository: FamilySavingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemberViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemberViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}