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
    onPlanCreated: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: PlanViewModel
) {
    var planName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("12") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val createPlanState by viewModel.createPlanState.collectAsStateWithLifecycle()

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

                val createdPlan = (createPlanState as ApiResponse.Success<com.example.parcial2_componentes.data.model.Plan>).data
                delay(2000)
                planName = ""
                targetAmount = ""
                months = "12"
                onPlanCreated(createdPlan._id ?: "", createdPlan.name)
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
                        Text("✅ Plan creado exitosamente!")
                    }
                }
            }

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
                        Text("❌ $message")
                    }
                }
            }

            Text("Información del Plan", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp))

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
                value = targetAmount,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        targetAmount = it
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
            Text("¿Por cuántos meses durará el plan de ahorro?",
                style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (planName.isBlank() || targetAmount.isBlank() || months.isBlank()) {
                        errorMessage = "Por favor completa todos los campos obligatorios"
                        return@Button
                    }

                    val target = targetAmount.toDoubleOrNull()
                    val monthsValue = months.toIntOrNull()

                    if (target == null || target <= 0) {
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

                    viewModel.createPlan(planName, target, monthsValue)
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 Información", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• El plan familiar te permitirá agregar miembros y registrar sus pagos\n" +
                                "• Podrás hacer seguimiento del progreso de ahorro\n" +
                                "• Todos los miembros podrán ver el avance del plan",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}