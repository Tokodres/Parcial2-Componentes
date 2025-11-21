// app/src/main/java/com/example/parcial2_componentes/ui/member/AddMemberScreen.kt
package com.example.parcial2_componentes.ui.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    viewModel: MemberViewModel
) {
    var memberName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val createMemberState by viewModel.createMemberState.collectAsStateWithLifecycle()

    // Manejar el estado de la creación del miembro
    LaunchedEffect(createMemberState) {
        when (createMemberState) {
            is ApiResponse.Loading -> {
                isLoading = true
                showSuccess = false
                errorMessage = null
            }
            is ApiResponse.Success<*> -> {
                isLoading = false
                showSuccess = true
                errorMessage = null

                // Limpiar campo después de éxito
                delay(1500)
                memberName = ""
                showSuccess = false
                viewModel.clearCreateMemberState()
            }
            is ApiResponse.Error -> {
                isLoading = false
                showSuccess = false
                errorMessage = (createMemberState as ApiResponse.Error).message
                viewModel.clearCreateMemberState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Miembros - $planName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onMembersAdded,
                icon = { Text("✓") },
                text = { Text("Finalizar") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
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
                        Text("✅ Miembro agregado exitosamente!")
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

            Text(
                "Agregar Miembro al Plan",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = memberName,
                onValueChange = {
                    memberName = it
                    errorMessage = null
                },
                label = { Text("Nombre del Miembro *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null,
                placeholder = { Text("Ej: Juan Pérez") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (memberName.isBlank()) {
                        errorMessage = "Por favor ingresa el nombre del miembro"
                        return@Button
                    }

                    viewModel.createMember(memberName, planId) // ✅ Solo nombre y planId
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
                        Text("Agregando...")
                    }
                } else {
                    Text("Agregar Miembro")
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
                        "• Puedes agregar múltiples miembros al plan\n" +
                                "• Solo necesitas el nombre de cada miembro\n" +
                                "• Presiona 'Finalizar' cuando hayas agregado todos los miembros",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}