package co.edu.uniquindio.adopcionmascota.features.moderation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    viewModel: ModerationViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val list = if (state.selectedTab == 0) state.pending else state.all

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de moderación", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = state.selectedTab) {
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
                        ModerationCard(
                            pet = pet,
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
    onVerify: () -> Unit,
    onReject: () -> Unit,
    onResolve: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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