package com.example.parcial2_componentes.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parcial2_componentes.data.model.Member
import com.example.parcial2_componentes.data.model.Payment
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.ui.payment.viewmodel.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSummaryScreen(
    planId: String,
    planName: String,
    onRegisterPayment: (Member) -> Unit,
    onBack: () -> Unit,
    onAddMoreMembers: () -> Unit, // ✅ NUEVO: Callback para agregar más miembros
    viewModel: PaymentViewModel = viewModel()
) {
    val membersState by viewModel.membersState.collectAsStateWithLifecycle()
    val paymentsState by viewModel.paymentsState.collectAsStateWithLifecycle()
    val currentPlan by viewModel.currentPlan.collectAsStateWithLifecycle()

    // Mapa de IDs a nombres de miembros
    val memberNameMap = remember(membersState) {
        when (membersState) {
            is ApiResponse.Success -> {
                (membersState as ApiResponse.Success<List<Member>>).data.associate {
                    it._id to it.name
                }
            }
            else -> emptyMap()
        }
    }

    // Cargar datos al iniciar
    LaunchedEffect(planId) {
        viewModel.loadMembersByPlan(planId)
        viewModel.loadPaymentsByPlan(planId)
        viewModel.loadPlan(planId)
    }

    // Calcular total recaudado
    val totalCollected = remember(paymentsState) {
        when (paymentsState) {
            is ApiResponse.Success -> {
                (paymentsState as ApiResponse.Success<List<Payment>>).data.sumOf { it.amount }
            }
            else -> 0.0
        }
    }

    // Calcular pagos por miembro
    val paymentsByMember = remember(paymentsState, membersState) {
        when {
            paymentsState is ApiResponse.Success && membersState is ApiResponse.Success -> {
                val payments = (paymentsState as ApiResponse.Success<List<Payment>>).data
                val members = (membersState as ApiResponse.Success<List<Member>>).data

                members.associate { member ->
                    member to payments.filter { it.memberId == member._id }.sumOf { it.amount }
                }
            }
            else -> emptyMap()
        }
    }

    // Calcular lo que falta para la meta
    val remainingAmount = remember(currentPlan, totalCollected) {
        val targetAmount = currentPlan?.targetAmount ?: 0.0
        targetAmount - totalCollected
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagos - $planName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                },
                actions = {
                    // Botón para recargar datos
                    IconButton(
                        onClick = {
                            viewModel.loadMembersByPlan(planId)
                            viewModel.loadPaymentsByPlan(planId)
                            viewModel.loadPlan(planId)
                        }
                    ) {
                        Text("🔄")
                    }
                    // ✅ NUEVO: Botón para agregar más miembros
                    IconButton(
                        onClick = onAddMoreMembers
                    ) {
                        Text("👥")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Resumen general
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📊 Resumen General",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Recaudado", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "$${"%.2f".format(totalCollected)}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        when (membersState) {
                            is ApiResponse.Success -> {
                                val members = (membersState as ApiResponse.Success<List<Member>>).data
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Miembros", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${members.size}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            else -> {}
                        }
                    }

                    // Mostrar cuánto falta para la meta
                    currentPlan?.let { plan ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (remainingAmount > 0) "💰 Falta para la meta" else "🎉 ¡Meta alcanzada!",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                if (remainingAmount > 0) "$${"%.2f".format(remainingAmount)}" else "Completado",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (remainingAmount > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Estadísticas de pagos
                    when (paymentsState) {
                        is ApiResponse.Success -> {
                            val payments = (paymentsState as ApiResponse.Success<List<Payment>>).data
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total de pagos: ${payments.size}", style = MaterialTheme.typography.bodySmall)
                                Text("Promedio: $${"%.2f".format(if (payments.isNotEmpty()) totalCollected / payments.size else 0.0)}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        else -> {}
                    }
                }
            }

            // Lista de miembros para registrar pagos
            Text(
                "👥 Miembros del Plan",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (membersState) {
                is ApiResponse.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cargando miembros...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                is ApiResponse.Success -> {
                    val members = (membersState as ApiResponse.Success<List<Member>>).data

                    if (members.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "📝 No hay miembros en este plan",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Agrega miembros para poder registrar pagos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onAddMoreMembers
                                ) {
                                    Text("Agregar Miembros")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(members) { member ->
                                MemberPaymentItem(
                                    member = member,
                                    totalPaid = paymentsByMember[member] ?: 0.0,
                                    onRegisterPayment = { onRegisterPayment(member) }
                                )
                            }
                        }
                    }
                }
                is ApiResponse.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("❌ Error al cargar miembros", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                (membersState as ApiResponse.Error).message,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.loadMembersByPlan(planId) }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Historial de pagos recientes
            Text(
                "📋 Historial de Pagos Recientes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (paymentsState) {
                is ApiResponse.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                }
                is ApiResponse.Success -> {
                    val payments = (paymentsState as ApiResponse.Success<List<Payment>>).data

                    if (payments.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "💸 No hay pagos registrados",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Los pagos aparecerán aquí una vez registrados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(payments.sortedByDescending { it.date }.take(10)) { payment ->
                                val memberName = memberNameMap[payment.memberId] ?: "Miembro"
                                PaymentHistoryItem(payment = payment, memberName = memberName)
                                if (payment != payments.minByOrNull { it.date ?: "" }) {
                                    Divider(modifier = Modifier.padding(horizontal = 8.dp))
                                }
                            }
                        }

                        // Mostrar contador si hay más pagos
                        if (payments.size > 10) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "+ ${payments.size - 10} pagos más...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
                is ApiResponse.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("❌ Error al cargar pagos", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.loadPaymentsByPlan(planId) },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberPaymentItem(member: Member, totalPaid: Double, onRegisterPayment: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    member.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Mostrar información de pagos
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Pagado: $${"%.2f".format(totalPaid)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    member.contributionPerMonth.takeIf { it > 0 }?.let { contribution ->
                        Text(
                            "Aporte: $${"%.2f".format(contribution)}/mes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mostrar progreso visual si hay un aporte mensual
                member.contributionPerMonth.takeIf { it > 0 && totalPaid > 0 }?.let { contribution ->
                    val progress = (totalPaid / contribution).toFloat().coerceIn(0f, 1f)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onRegisterPayment,
                modifier = Modifier.width(100.dp)
            ) {
                Text("💰 Pagar")
            }
        }
    }
}

@Composable
fun PaymentHistoryItem(payment: Payment, memberName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                memberName,
                style = MaterialTheme.typography.bodyMedium
            )
            payment.date?.let { date ->
                Text(
                    date.substring(0, 10),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            "$${"%.2f".format(payment.amount)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}