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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(
    onPlanCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: PlanViewModel
) {
    var planName by remember { mutableStateOf("") }
    var goalAmount by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("12") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val createPlanState by viewModel.createPlanState.collectAsStateWithLifecycle()

    // Manejar el estado de la creación del plan
    LaunchedEffect(createPlanState) {
        when (createPlanState) {
            is ApiResponse.Loading -> {
                isLoading = true
                showSuccess = false
                errorMessage = null
            }
            is ApiResponse.Success<*> -> {
                isLoading = false
                showSuccess = true
                errorMessage = null

                // Limpiar campos después de éxito
                planName = ""
                goalAmount = ""
                months = "12"

                // Navegar después de 2 segundos
                delay(2000)
                onPlanCreated()
                viewModel.clearCreatePlanState()
                showSuccess = false
            }
            is ApiResponse.Error -> {
                isLoading = false
                showSuccess = false
                errorMessage = (createPlanState as ApiResponse.Error).message
                viewModel.clearCreatePlanState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Plan Familiar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Mostrar mensaje de éxito
            if (showSuccess) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ Plan creado exitosamente!",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Mostrar mensaje de error
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "❌ $message",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Text(
                "Información del Plan",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = planName,
                onValueChange = {
                    planName = it
                    errorMessage = null
                },
                label = { Text("Nombre del Plan *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null,
                placeholder = { Text("Ej: Vacaciones Familiares") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = goalAmount,
                onValueChange = {
                    // Permitir solo números y punto decimal
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        goalAmount = it
                        errorMessage = null
                    }
                },
                label = { Text("Meta de Ahorro ($) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null,
                placeholder = { Text("Ej: 2000000") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = months,
                onValueChange = {
                    // Permitir solo números
                    if (it.isEmpty() || it.matches(Regex("^\\d*\$"))) {
                        months = it
                        errorMessage = null
                    }
                },
                label = { Text("Duración en Meses *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null,
                placeholder = { Text("Ej: 12") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "¿Por cuántos meses durará el plan de ahorro?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Validaciones
                    if (planName.isBlank() || goalAmount.isBlank() || months.isBlank()) {
                        errorMessage = "Por favor completa todos los campos obligatorios"
                        return@Button
                    }

                    val goal = goalAmount.toDoubleOrNull()
                    val monthsValue = months.toIntOrNull()

                    if (goal == null || goal <= 0) {
                        errorMessage = "La meta debe ser un número válido mayor a 0"
                        return@Button
                    }

                    if (monthsValue == null || monthsValue <= 0) {
                        errorMessage = "Los meses deben ser un número válido mayor a 0"
                        return@Button
                    }

                    if (monthsValue > 60) {
                        errorMessage = "La duración máxima es de 60 meses (5 años)"
                        return@Button
                    }

                    viewModel.createPlan(planName, goal, monthsValue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creando Plan...")
                    }
                } else {
                    Text("Crear Plan Familiar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "💡 Información",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• El plan familiar te permitirá agregar miembros y registrar sus pagos\n" +
                                "• Podrás hacer seguimiento del progreso de ahorro\n" +
                                "• Todos los miembros podrán ver el avance del plan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}