// app/src/main/java/com/example/parcial2_componentes/FamilySavingsApp.kt
package com.example.parcial2_componentes

import android.app.Application
import com.example.parcial2_componentes.data.remote.RetrofitClient
import com.example.parcial2_componentes.data.repository.FamilySavingsRepository

class FamilySavingsApp : Application() {
    val repository: FamilySavingsRepository by lazy {
        FamilySavingsRepository(RetrofitClient.apiService)
    }
}