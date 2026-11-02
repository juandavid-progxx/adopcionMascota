package co.edu.uniquindio.adopcionmascota.features.feed

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RequestDialogs(
    state: FeedUiState,
    viewModel: FeedScreenViewModel
) {
    // Diálogo de confirmación de la solicitud
    state.adoptionDialogPet?.let { pet ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissAdoptionDialog() },
            title = {
                Text(
                    text = "${pet.category.actionLabel}: ${pet.name}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D40)
                )
            },
            text = {
                Text(
                    text = pet.category.requestMessage.replace("{name}", pet.name),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmAdoption(pet) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                ) {
                    Text("Confirmar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissAdoptionDialog() }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Mensaje de éxito
    state.adoptionSuccessMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissSuccessMessage() },
            title = {
                Text(
                    text = "✅ ¡Solicitud enviada!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
            },
            text = { Text(text = message) },
            confirmButton = {
                Button(
                    onClick = { viewModel.onDismissSuccessMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                ) {
                    Text("Entendido", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}
@Composable
fun ResolveDialog(
    state: FeedUiState,
    viewModel: FeedScreenViewModel
) {
    state.resolveDialogPet?.let { pet ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissResolve() },
            title = {
                Text(
                    text = pet.category.resolveLabel(),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D40)
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres marcar \"${pet.name}\" como resuelta? Dejará de mostrarse en el feed público.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmResolve() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                ) {
                    Text("Sí, marcar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissResolve() }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}