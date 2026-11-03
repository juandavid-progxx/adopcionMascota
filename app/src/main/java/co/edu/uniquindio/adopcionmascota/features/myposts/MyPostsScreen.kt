package co.edu.uniquindio.adopcionmascota.features.myposts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.adopcionmascota.features.feed.Pet
import co.edu.uniquindio.adopcionmascota.features.feed.PostStatus
import co.edu.uniquindio.adopcionmascota.features.feed.resolveLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(
    viewModel: MyPostsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis publicaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (state.posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐾", fontSize = 56.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Aún no has publicado nada", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF4F7F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.posts, key = { it.id }) { pet ->
                    MyPostCard(
                        pet = pet,
                        onEdit = { viewModel.onEditClick(pet) },
                        onDelete = { viewModel.onDeleteClick(pet) },
                        onResolve = { viewModel.onResolveClick(pet) }
                    )
                }
            }
        }
    }

    // Diálogo: editar
    state.editingPet?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissEdit() },
            title = { Text("Editar publicación") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.editName,
                        onValueChange = { viewModel.onEditNameChange(it) },
                        label = { Text("Título") },
                        isError = state.nameError != null,
                        supportingText = { state.nameError?.let { e -> Text(e) } },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.editDescription,
                        onValueChange = { viewModel.onEditDescriptionChange(it) },
                        label = { Text("Descripción") },
                        isError = state.descriptionError != null,
                        supportingText = { state.descriptionError?.let { e -> Text(e) } },
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = state.editAge,
                        onValueChange = { viewModel.onEditAgeChange(it) },
                        label = { Text("Edad") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.onSaveEdit() }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissEdit() }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo: eliminar
    state.deletingPet?.let { pet ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            title = { Text("¿Eliminar publicación?") },
            text = { Text("Se eliminará \"${pet.name}\" de forma definitiva. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDelete() }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo: resolver
    state.resolvingPet?.let { pet ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissResolve() },
            title = { Text(pet.category.resolveLabel()) },
            text = { Text("¿Estás seguro de que quieres marcar \"${pet.name}\" como resuelta? Dejará de mostrarse en el feed público.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmResolve() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                ) { Text("Sí, marcar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissResolve() }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun MyPostCard(
    pet: Pet,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResolve: () -> Unit
) {
    val (chipBg, chipText) = when (pet.status) {
        PostStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        PostStatus.VERIFIED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        PostStatus.REJECTED -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
        PostStatus.RESOLVED -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${pet.type.emoji} ${pet.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = RoundedCornerShape(12.dp), color = chipBg) {
                    Text(
                        text = pet.status.displayName,
                        color = chipText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${pet.category.emoji} ${pet.category.displayName} • ${pet.city}",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = pet.description,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "👍 ${pet.importantVotes} importantes  •  💬 ${pet.comments.size} comentarios",
                fontSize = 12.sp,
                color = Color.Gray
            )

            pet.rejectionReason?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Motivo de rechazo: $it",
                    fontSize = 13.sp,
                    color = Color(0xFFB71C1C)
                )
                Text(
                    text = "Edítala y vuelve a enviarla a revisión.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Editar", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB71C1C))
                ) {
                    Text("Eliminar", fontSize = 13.sp)
                }
                if (pet.status == PostStatus.PENDING || pet.status == PostStatus.VERIFIED) {
                    Button(
                        onClick = onResolve,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                    ) {
                        Text("Resolver", fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }
    }
}