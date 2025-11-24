package com.example.parcial2_componentes.ui.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.ui.member.viewmodel.MemberViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    planId: String,
    planName: String,
    onMembersAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: MemberViewModel,
    isAddingMoreMembers: Boolean = false
) {
    var memberName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryCount by remember { mutableStateOf(0) }

    val createMemberState by viewModel.createMemberState.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    LaunchedEffect(createMemberState) {
        when (createMemberState) {
            is ApiResponse.Loading -> {
                isLoading = true
                showSuccess = false
                errorMessage = null
                retryCount = 0
            }
            is ApiResponse.Success<*> -> {
                isLoading = false
                showSuccess = true
                errorMessage = null
                retryCount = 0

                delay(1000)
                memberName = ""
                showSuccess = false
                viewModel.clearCreateMemberState()
                viewModel.resetProcessing()
            }
            is ApiResponse.Error -> {
                isLoading = false
                showSuccess = false
                val error = (createMemberState as ApiResponse.Error).message
                errorMessage = error
                retryCount++
                viewModel.clearCreateMemberState()
                viewModel.resetProcessing()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isAddingMoreMembers) "Agregar Más Miembros - $planName"
                        else "Agregar Miembros - $planName"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isAddingMoreMembers) {
                // ✅ CORREGIDO: Remover el parámetro 'enabled' que no existe
                FloatingActionButton(
                    onClick = {
                        if (!isProcessing) {
                            onMembersAdded()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    // ✅ CORREGIDO: Cambiar opacidad cuando está procesando
                    Box(
                        modifier = if (isProcessing) Modifier.alpha(0.5f) else Modifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
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
                        Text("✅ Miembro agregado exitosamente!")
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("❌ $message")
                        }
                        if (retryCount > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Reintento: $retryCount/3",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (isProcessing && retryCount > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reintentando... ($retryCount/3)")
                    }
                }
            }

            Text(
                if (isAddingMoreMembers) "Agregar Más Miembros al Plan"
                else "Agregar Miembro al Plan",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = memberName,
                onValueChange = {
                    memberName = it
                    errorMessage = null
                    retryCount = 0
                },
                label = { Text("Nombre del Miembro *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null,
                placeholder = { Text("Ej: Juan Pérez") },
                enabled = !isProcessing
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (memberName.isBlank()) {
                        errorMessage = "Por favor ingresa el nombre del miembro"
                        return@Button
                    }
                    if (isProcessing) {
                        errorMessage = "Espere, procesando solicitud anterior..."
                        return@Button
                    }
                    viewModel.createMember(memberName, planId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && !isProcessing
            ) {
                if (isLoading || isProcessing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (retryCount > 0) "Reintentando..." else "Agregando...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Text("Agregar Miembro")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isProcessing) {
                        onMembersAdded()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                enabled = !isProcessing
            ) {
                Text(
                    if (isAddingMoreMembers) "Volver a Planes"
                    else "Finalizar y Volver a Planes"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        if (isAddingMoreMembers) {
                            "• Puedes agregar más miembros al plan existente\n" +
                                    "• Solo necesitas el nombre de cada miembro\n" +
                                    "• Los nuevos miembros aparecerán inmediatamente en el plan\n" +
                                    "• El sistema reintentará automáticamente si hay errores de conexión"
                        } else {
                            "• Puedes agregar múltiples miembros al plan\n" +
                                    "• Solo necesitas el nombre de cada miembro\n" +
                                    "• Presiona 'Finalizar' cuando hayas agregado todos los miembros\n" +
                                    "• El sistema reintentará automáticamente si hay errores de conexión"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}