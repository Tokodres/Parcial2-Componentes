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

            // Información financiera
            Text("Meta: $${"%.2f".format(plan.targetAmount)}")
            Text("Recolectado: $${"%.2f".format(plan.totalCollected)}")

            // Progreso numérico
            val progressPercentage = if (plan.targetAmount > 0) {
                (plan.totalCollected / plan.targetAmount * 100).toInt()
            } else {
                0
            }
            Text("Progreso: $progressPercentage%")

            Text("Miembros: ${plan.members?.size ?: 0}")
            Text("Duración: ${plan.months} meses")
            plan.createdAt?.let {
                Text("Creado: ${it.substring(0, 10)}")
            }

            // Mostrar nombres de miembros
            plan.members?.takeIf { it.isNotEmpty() }?.let { members ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Miembros: ${members.joinToString { it.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mostrar últimos pagos
            plan.payments?.takeIf { it.isNotEmpty() }?.let { payments ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Últimos pagos:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                payments.take(3).forEach { payment ->
                    Text(
                        "  • ${payment.memberName ?: "Miembro"}: $${"%.2f".format(payment.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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