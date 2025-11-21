package com.example.parcial2_componentes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.parcial2_componentes.data.model.Plan
import com.example.parcial2_componentes.data.remote.ApiResponse
import com.example.parcial2_componentes.ui.plan.viewmodel.PlanViewModel
import com.example.parcial2_componentes.ui.plan.viewmodel.PlanViewModelFactory
import kotlinx.coroutines.delay

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

                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: PlanViewModel) {
    var currentScreen by remember { mutableStateOf("plan_list") }

    when (currentScreen) {
        "plan_list" -> PlanListScreen(
            viewModel = viewModel,
            onCreateNewPlan = { currentScreen = "create_plan" }
        )
        "create_plan" -> CreatePlanScreen(
            onPlanCreated = {
                currentScreen = "plan_list"
                viewModel.loadPlans() // Recargar la lista
            },
            onBack = { currentScreen = "plan_list" },
            viewModel = viewModel
        )
    }
}

// Pantalla de lista de planes
@Composable
fun PlanListScreen(
    viewModel: PlanViewModel,
    onCreateNewPlan: () -> Unit
) {
    val plansState by viewModel.plansState.collectAsStateWithLifecycle()

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
                    CircularProgressIndicator()
                }
            }
            is ApiResponse.Success -> {
                val plans = (plansState as ApiResponse.Success<List<Plan>>).data

                if (plans.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No hay planes creados")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onCreateNewPlan) {
                                Text("Crear primer plan")
                            }
                        }
                    }
                } else {
                    // Mostrar lista de planes - CORREGIDO
                    LazyColumn {
                        items(plans) { plan ->
                            PlanItemCard(plan = plan)
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
                        Text("❌ Error al cargar planes")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text((plansState as ApiResponse.Error).message)
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

// Componente separado para mostrar un plan - CORREGIDO
@Composable
fun PlanItemCard(plan: Plan) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(plan.name, style = MaterialTheme.typography.headlineSmall)
            Text("Meta: $${plan.goal}")
            Text("Recolectado: $${plan.totalCollected}")
        }
    }
}

// Pantalla para crear planes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(
    onPlanCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: PlanViewModel
) {
    var planName by remember { mutableStateOf("") }
    var goalAmount by remember { mutableStateOf("") }
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

                // Limpiar campos y navegar después de 2 segundos
                delay(2000)
                planName = ""
                goalAmount = ""
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) {
                Text("← Volver")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Crear Plan Familiar",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                    Text("✅ Plan creado exitosamente!")
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

        OutlinedTextField(
            value = planName,
            onValueChange = {
                planName = it
                errorMessage = null
            },
            label = { Text("Nombre del Plan") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = goalAmount,
            onValueChange = {
                goalAmount = it
                errorMessage = null
            },
            label = { Text("Meta de Ahorro") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (planName.isBlank() || goalAmount.isBlank()) {
                    errorMessage = "Por favor completa todos los campos"
                    return@Button
                }

                val goal = goalAmount.toDoubleOrNull()
                if (goal == null || goal <= 0) {
                    errorMessage = "La meta debe ser un número válido mayor a 0"
                    return@Button
                }

                viewModel.createPlan(planName, goal)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creando...")
                }
            } else {
                Text("Crear Plan")
            }
        }
    }
}