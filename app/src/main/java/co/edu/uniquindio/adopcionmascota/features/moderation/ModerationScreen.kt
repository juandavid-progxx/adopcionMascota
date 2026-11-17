package co.edu.uniquindio.adopcionmascota.features.moderation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import co.edu.uniquindio.adopcionmascota.core.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.adopcionmascota.features.feed.Pet
import co.edu.uniquindio.adopcionmascota.features.feed.PostStatus
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import co.edu.uniquindio.adopcionmascota.core.data.cloudThumb
import coil3.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import co.edu.uniquindio.adopcionmascota.core.ui.GuideHelpButton
import co.edu.uniquindio.adopcionmascota.core.ui.TourStep
import co.edu.uniquindio.adopcionmascota.core.ui.rememberTour
import co.edu.uniquindio.adopcionmascota.core.ui.tourTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    viewModel: ModerationViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val list = if (state.selectedTab == 0) state.pending else state.all
    val startTour = rememberTour(
        "moderation",
        listOf(
            TourStep(
                "Panel de moderación",
                "Desde aquí revisas las publicaciones de la comunidad."
            ),
            TourStep("Pendientes y Todas", "\"Pendientes\" espera revisión; \"Todas\" es el historial completo.", target = "mod_tabs"),
            TourStep("Revisa la publicación", "Mira la foto, los datos y quién la publicó.", target = "mod_card"),
            TourStep("Verificar o rechazar", "Verifica para aprobar. Para rechazar debes escribir el motivo, que le llegará al autor.", target = "mod_actions"),
            TourStep("Cerrar sesión", "Sal del panel de moderación.", target = "mod_logout")
        )
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de moderación", fontWeight = FontWeight.Bold) },
                actions = {
                    GuideHelpButton { startTour() }
                    IconButton(onClick = onLogout, modifier = Modifier.tourTarget("mod_logout")) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = state.selectedTab, modifier = Modifier.tourTarget("mod_tabs")) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("Pendientes (${state.pending.size})") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("Todas (${state.all.size})") }
                )
            }

            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay publicaciones para mostrar", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(list, key = { it.id }) { pet ->
                        val first = pet.id == list.first().id
                        ModerationCard(
                            pet = pet,
                            modifier = if (first) Modifier.tourTarget("mod_card") else Modifier,
                            actionsModifier = if (first) Modifier.tourTarget("mod_actions") else Modifier,
                            onVerify = { viewModel.onVerify(pet) },
                            onReject = { viewModel.onRejectClick(pet) },
                            onResolve = { viewModel.onResolve(pet) }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para escribir el motivo del rechazo (obligatorio)
    state.rejectingPet?.let { pet ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissReject() },
            title = { Text("Rechazar \"${pet.name}\"") },
            text = {
                OutlinedTextField(
                    value = state.rejectReason,
                    onValueChange = { viewModel.onReasonChange(it) },
                    label = { Text("Motivo del rechazo *") },
                    isError = state.reasonError != null,
                    supportingText = { state.reasonError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.onConfirmReject() }) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissReject() }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ModerationCard(
    pet: Pet,
    modifier: Modifier = Modifier,
    actionsModifier: Modifier = Modifier,
    onVerify: () -> Unit,
    onReject: () -> Unit,
    onResolve: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (pet.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = pet.imageUrls.first().cloudThumb(),
                    contentDescription = pet.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(10.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${pet.type.emoji} ${pet.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(onClick = {}, label = { Text(pet.status.displayName) })
            }
            Spacer(Modifier.height(4.dp))
            Text("${pet.breed} · ${pet.size} · ${pet.city}", fontSize = 13.sp, color = Color.Gray)
            Text("Publicado por: ${pet.shelterName}", fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(pet.description, fontSize = 14.sp)

            pet.rejectionReason?.let {
                Spacer(Modifier.height(8.dp))
                Text("Motivo de rechazo: $it", fontSize = 13.sp, color = Color(0xFFB71C1C))
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = actionsModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (pet.status == PostStatus.PENDING) {
                    Button(onClick = onVerify) { Text("Verificar") }
                    OutlinedButton(onClick = onReject) { Text("Rechazar") }
                }
                if (pet.status != PostStatus.RESOLVED && pet.status != PostStatus.REJECTED) {
                    TextButton(onClick = onResolve) { Text("Marcar resuelta") }
                }
            }
        }
    }
}