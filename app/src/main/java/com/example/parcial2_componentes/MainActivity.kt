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
import com.example.parcial2_componentes.ui.member.AddMemberScreen
import com.example.parcial2_componentes.ui.member.viewmodel.MemberViewModel
import com.example.parcial2_componentes.ui.member.viewmodel.MemberViewModelFactory
import com.example.parcial2_componentes.ui.plan.CreatePlanScreen
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
                    val planViewModel: PlanViewModel = viewModel(
                        factory = PlanViewModelFactory(app.repository)
                    )

                    AppNavigation(planViewModel = planViewModel, app = app)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(planViewModel: PlanViewModel, app: FamilySavingsApp) {
    var currentScreen by remember { mutableStateOf("plan_list") }
    var createdPlanId by remember { mutableStateOf<String?>(null) }
    var createdPlanName by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        "plan_list" -> PlanListScreen(
            viewModel = planViewModel,
            onCreateNewPlan = { currentScreen = "create_plan" }
        )
        "create_plan" -> CreatePlanScreen(
            onPlanCreated = { planId, planName ->
                createdPlanId = planId
                createdPlanName = planName
                currentScreen = "add_members"
            },
            onBack = { currentScreen = "plan_list" },
            viewModel = planViewModel
        )
        "add_members" -> {
            val planId = createdPlanId ?: ""
            val planName = createdPlanName ?: "Plan"
            val memberViewModel: MemberViewModel = viewModel(
                factory = MemberViewModelFactory(app.repository)
            )
            AddMemberScreen(
                planId = planId,
                planName = planName,
                onMembersAdded = {
                    currentScreen = "plan_list"
                    planViewModel.loadPlans() // Recargar la lista
                },
                onBack = { currentScreen = "plan_list" },
                viewModel = memberViewModel
            )
        }
    }
}

// Pantalla de lista de planes
@Composable
fun PlanListScreen(
    viewModel: PlanViewModel,
    onCreateNewPlan: () -> Unit
) {
    val plansState by viewModel.plansState.collectAsStateWithLifecycle()

    // Recargar planes al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadPlans()
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

// Componente para mostrar un plan (sin botón de eliminar)
@Composable
fun PlanItemCard(plan: Plan) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                plan.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Meta: $${plan.targetAmount}")
            Text("Recolectado: $${plan.totalCollected}")
            Text("Miembros: ${plan.members?.size ?: 0}")
            Text("Duración: ${plan.months} meses")
            plan.createdAt?.let {
                Text("Creado: ${it.substring(0, 10)}")
            }

            // Mostrar nombres de los miembros si existen
            plan.members?.takeIf { it.isNotEmpty() }?.let { members ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Miembros: ${members.joinToString { it.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

            Text(
                "${(progress * 100).toInt()}% completado",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}