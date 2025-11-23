package com.example.parcial2_componentes.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.parcial2_componentes.data.model.Member
import com.example.parcial2_componentes.data.model.Payment
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.ui.payment.viewmodel.PaymentViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(
    member: Member,
    planId: String,
    planName: String,
    onPaymentRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: PaymentViewModel
) {
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOverpaymentWarning by remember { mutableStateOf(false) }
    var pendingPaymentAmount by remember { mutableStateOf(0.0) }

    val createPaymentState by viewModel.createPaymentState.collectAsStateWithLifecycle()
    val memberPaymentsState by viewModel.memberPaymentsState.collectAsStateWithLifecycle()
    val currentPlan by viewModel.currentPlan.collectAsStateWithLifecycle()

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadPaymentsByMember(member._id ?: "")
        viewModel.loadPlan(planId)
    }

    // Calcular total pagado por el miembro
    val totalPaidByMember = remember(memberPaymentsState) {
        when (memberPaymentsState) {
            is ApiResponse.Success -> {
                (memberPaymentsState as ApiResponse.Success<List<Payment>>).data.sumOf { it.amount }
            }
            else -> 0.0
        }
    }

    // Calcular meta restante
    val remainingGoal = remember(currentPlan, totalPaidByMember) {
        val goal = currentPlan?.targetAmount ?: 0.0
        val remaining = goal - totalPaidByMember
        println("🔍 [DEBUG] Meta: $goal, Pagado: $totalPaidByMember, Restante: $remaining")
        remaining
    }

    // Manejar el estado de la creación del pago
    LaunchedEffect(createPaymentState) {
        when (createPaymentState) {
            is ApiResponse.Loading -> {
                isLoading = true
                showSuccess = false
                errorMessage = null
                showOverpaymentWarning = false
            }
            is ApiResponse.Success<*> -> {
                isLoading = false
                showSuccess = true
                errorMessage = null
                showOverpaymentWarning = false

                // Recargar datos después del éxito
                viewModel.loadPaymentsByMember(member._id ?: "")
                viewModel.loadPlan(planId)

                // Limpiar campo y navegar después de éxito
                delay(2000)
                amount = ""
                onPaymentRegistered()
                viewModel.clearCreatePaymentState()
                showSuccess = false
            }
            is ApiResponse.Error -> {
                isLoading = false
                showSuccess = false
                errorMessage = (createPaymentState as ApiResponse.Error).message
                viewModel.clearCreatePaymentState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Pago") },
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
            verticalArrangement = Arrangement.Top
        ) {
            // Información del miembro y resumen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registrar Pago para:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(member.name, style = MaterialTheme.typography.headlineSmall)
                    Text("Plan: $planName", style = MaterialTheme.typography.bodyMedium)

                    // Mostrar resumen de pagos
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("📊 Resumen de Pagos:", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total pagado: $${"%.2f".format(totalPaidByMember)}",
                        style = MaterialTheme.typography.bodyMedium)

                    currentPlan?.let { plan ->
                        Text("Meta del plan: $${"%.2f".format(plan.targetAmount)}",
                            style = MaterialTheme.typography.bodyMedium)

                        val remainingText = if (remainingGoal > 0) {
                            "Restante: $${"%.2f".format(remainingGoal)}"
                        } else {
                            "✅ ¡Meta alcanzada!"
                        }

                        Text(remainingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (remainingGoal <= 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

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
                        Text("✅ Pago registrado exitosamente!")
                    }
                }
            }

            // Mostrar advertencia de sobrepago
            if (showOverpaymentWarning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ Advertencia", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("El pago de $${"%.2f".format(pendingPaymentAmount)} excede la meta restante de $${"%.2f".format(remainingGoal)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    showOverpaymentWarning = false
                                    errorMessage = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Cancelar")
                            }
                            Button(
                                onClick = {
                                    showOverpaymentWarning = false
                                    viewModel.createPayment(pendingPaymentAmount, member._id ?: "", planId)
                                }
                            ) {
                                Text("Continuar")
                            }
                        }
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
                        Text("❌ $message")
                    }
                }
            }

            // Formulario de pago
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    // Permitir solo números y punto decimal
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        amount = it
                        errorMessage = null
                        showOverpaymentWarning = false
                    }
                },
                label = { Text("Monto del Pago ($) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null,
                placeholder = { Text("Ej: 150000") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Cancelar")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (amount.isBlank()) {
                            errorMessage = "Por favor ingresa el monto del pago"
                            return@Button
                        }

                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0) {
                            errorMessage = "El monto debe ser un número válido mayor a 0"
                            return@Button
                        }

                        // Validar si el pago excede la meta
                        if (remainingGoal > 0 && amountValue > remainingGoal) {
                            pendingPaymentAmount = amountValue
                            showOverpaymentWarning = true
                            errorMessage = "El pago excede la meta restante"
                            return@Button
                        }

                        viewModel.createPayment(amountValue, member._id ?: "", planId)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && amount.isNotBlank()
                ) {
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Registrando...")
                        }
                    } else {
                        Text("💰 Registrar Pago")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Historial de pagos del miembro
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📋 Historial de Pagos",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when (memberPaymentsState) {
                        is ApiResponse.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            }
                        }
                        is ApiResponse.Success -> {
                            val payments = (memberPaymentsState as ApiResponse.Success<List<Payment>>).data
                            if (payments.isEmpty()) {
                                Text(
                                    "No hay pagos registrados",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                payments.sortedByDescending { it.date }.forEach { payment ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            payment.date?.substring(0, 10) ?: "Fecha no disponible",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            "$${"%.2f".format(payment.amount)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    if (payment != payments.last()) {
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                        is ApiResponse.Error -> {
                            Text(
                                "Error al cargar historial",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
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
                        "• El sistema validará automáticamente si el pago excede la meta\n" +
                                "• Puedes ver el historial completo de pagos del miembro\n" +
                                "• El progreso se actualiza en tiempo real\n" +
                                "• Los pagos que excedan la meta serán marcados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}