package com.example.parcial2_componentes.ui.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.parcial2_componentes.data.model.Plan
import com.example.parcial2_componentes.data.model.Payment
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.ui.plan.viewmodel.PlanViewModel

@Composable
fun PlanListScreen(
    viewModel: PlanViewModel,
    onCreateNewPlan: () -> Unit,
    onViewPayments: (String, String) -> Unit,
    onEditPlan: (String, String) -> Unit
) {
    val plansState by viewModel.plansState.collectAsStateWithLifecycle()

    // ✅ NUEVO: Recargar planes cuando la pantalla se vuelve visible
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadPlans()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Planes Familiares",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(onClick = onCreateNewPlan) {
                Text("Nuevo Plan")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when (plansState) {
            is ApiResponse.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando planes...")
                    }
                }
            }
            is ApiResponse.Success -> {
                val plans = (plansState as ApiResponse.Success<List<Plan>>).data

                Text(
                    "Total de planes: ${plans.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (plans.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No hay planes creados",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Presiona 'Nuevo Plan' para crear el primero",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(plans) { plan ->
                            PlanItem(
                                plan = plan,
                                onViewPayments = {
                                    onViewPayments(plan._id ?: "", plan.name)
                                },
                                onEditPlan = {
                                    onEditPlan(plan._id ?: "", plan.name)
                                }
                            )
                        }
                    }
                }
            }
            is ApiResponse.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "❌ Error al cargar planes",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            (plansState as ApiResponse.Error).message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPlans() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanItem(plan: Plan, onViewPayments: () -> Unit, onEditPlan: () -> Unit) {
    // Calcular lo que falta para la meta
    val remainingAmount = plan.targetAmount - plan.totalCollected

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    plan.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )

                // Botón para editar
                TextButton(onClick = onEditPlan) {
                    Text("✏️")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ NUEVO: Información de lo que falta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (remainingAmount > 0)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (remainingAmount > 0) "💰 Falta para la meta" else "🎉 ¡Meta alcanzada!",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (remainingAmount > 0) "$${"%.2f".format(remainingAmount)}" else "Completado",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (remainingAmount > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Porcentaje de completado
                    Text(
                        "${((plan.totalCollected / plan.targetAmount) * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información financiera existente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Meta total", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "$${"%.2f".format(plan.targetAmount)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text("Recolectado", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "$${"%.2f".format(plan.totalCollected)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text("Miembros", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${plan.members?.size ?: 0}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Progress bar
            val progress = if (plan.targetAmount > 0) {
                (plan.totalCollected / plan.targetAmount).toFloat().coerceIn(0f, 1f)
            } else {
                0f
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Duración: ${plan.months} meses",
                    style = MaterialTheme.typography.bodySmall
                )
                plan.createdAt?.let {
                    Text(
                        "Creado: ${it.substring(0, 10)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onViewPayments,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💰 Pagos")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onEditPlan,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("✏️ Editar")
                }
            }
        }
    }
}