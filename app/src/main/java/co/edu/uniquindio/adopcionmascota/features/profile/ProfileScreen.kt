package co.edu.uniquindio.adopcionmascota.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onBack: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    onMyPostsClick: () -> Unit = {}

) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF4F7F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Datos del usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF80CBC4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.name?.firstOrNull() ?: 'U').uppercase(),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D40)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(user?.name ?: "Usuario", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(user?.email ?: "", fontSize = 14.sp, color = Color.Gray)
                    if (!user?.phone.isNullOrBlank()) {
                        Text("Tel: ${user?.phone}", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            // Estadísticas personales
            Text(
                "Mis estadísticas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Activas", state.active, Modifier.weight(1f))
                StatCard("Finalizadas", state.resolved, Modifier.weight(1f))
                StatCard("Pendientes", state.pending, Modifier.weight(1f))
            }

            // Acciones
            Button(
                onClick = onMyPostsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40))
            ) { Text("Mis publicaciones") }
            Button(
                onClick = { viewModel.onEditClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
            ) { Text("Editar mis datos") }

            OutlinedButton(
                onClick = { viewModel.onLogout(onLoggedOut) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar sesión") }

            OutlinedButton(
                onClick = { viewModel.onDeleteClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB71C1C))
            ) { Text("Eliminar mi cuenta") }
        }
    }

    // Diálogo: editar datos
    if (state.showEditDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissEdit() },
            title = { Text("Editar mis datos") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.editName,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Nombre") },
                        isError = state.nameError != null,
                        supportingText = { state.nameError?.let { Text(it) } },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.editPhone,
                        onValueChange = { viewModel.onPhoneChange(it) },
                        label = { Text("Teléfono") },
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

    // Diálogo: eliminar cuenta
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            title = { Text("¿Eliminar tu cuenta?") },
            text = {
                Text("Se borrarán tu cuenta, tus publicaciones y tus notificaciones. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmDelete(onLoggedOut) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDelete() }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B)
            )
            Text(label, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}