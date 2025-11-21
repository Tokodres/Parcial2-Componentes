// app/src/main/java/com/example/parcial2_componentes/MainActivity.kt
package com.example.parcial2_componentes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parcial2_componentes.ui.plan.CreatePlanScreen
import com.example.parcial2_componentes.ui.plan.viewmodel.PlanViewModel
import com.example.parcial2_componentes.ui.plan.viewmodel.PlanViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val app = application as FamilySavingsApp
                    val viewModel: PlanViewModel = viewModel(
                        factory = PlanViewModelFactory(app.repository)
                    )

                    CreatePlanScreen(
                        onPlanCreated = {
                            // Navegar a otra pantalla o mostrar mensaje
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}