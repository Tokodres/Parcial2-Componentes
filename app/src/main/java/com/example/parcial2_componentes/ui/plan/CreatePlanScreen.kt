// app/src/main/java/com/example/parcial2_componentes/ui/plan/CreatePlanScreen.kt
package com.example.parcial2_componentes.ui.plan

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.ui.plan.viewmodel.PlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(
    onPlanCreated: () -> Unit,
    viewModel: PlanViewModel
) {
    var planName by remember { mutableStateOf("") }
    var goalAmount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val createPlanState by viewModel.createPlanState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crear Plan Familiar",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = planName,
            onValueChange = { planName = it },
            label = { Text("Nombre del Plan") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = goalAmount,
            onValueChange = { goalAmount = it },
            label = { Text("Meta de Ahorro") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
            // Sin keyboardOptions - funcionará igual pero sin teclado numérico específico
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (planName.isNotBlank() && goalAmount.isNotBlank()) {
                    val goal = goalAmount.toDoubleOrNull() ?: 0.0
                    viewModel.createPlan(planName, goal)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = planName.isNotBlank() && goalAmount.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("Crear Plan")
            }
        }

        // Manejar el estado
        LaunchedEffect(createPlanState) {
            when (createPlanState) {
                is ApiResponse.Loading -> isLoading = true
                is ApiResponse.Success<*> -> {
                    isLoading = false
                    onPlanCreated()
                    viewModel.clearCreatePlanState()
                }
                is ApiResponse.Error -> {
                    isLoading = false
                    viewModel.clearCreatePlanState()
                }
                else -> {}
            }
        }
    }
}