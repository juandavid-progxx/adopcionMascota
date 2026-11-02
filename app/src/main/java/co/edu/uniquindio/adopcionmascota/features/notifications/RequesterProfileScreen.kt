package co.edu.uniquindio.adopcionmascota.features.notifications

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.adopcionmascota.core.data.NotificationRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequesterProfileScreen(
    notificationId: String,
    onBack: () -> Unit = {},
    onOpenPet: (String) -> Unit = {}
) {
    val all by NotificationRepository.all.collectAsState()
    val n = all.find { it.id == notificationId }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del solicitante", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (n == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se encontró la información", color = Color.Gray)
            }
            return@Scaffold
        }

        val name = n.contactName ?: "Usuario"
        val email = n.contactEmail.orEmpty()
        val phone = n.contactPhone.orEmpty()

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF4F7F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF80CBC4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D40)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(n.message, fontSize = 15.sp, color = Color.DarkGray)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Datos de contacto", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    Text("Correo: ${email.ifBlank { "No registrado" }}", fontSize = 16.sp)
                    Text("Teléfono: ${phone.ifBlank { "No registrado" }}", fontSize = 16.sp)
                }
            }

            Button(
                onClick = {
                    launch(context, Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
                },
                enabled = email.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
            ) { Text("Escribir un correo", fontSize = 16.sp) }

            OutlinedButton(
                onClick = {
                    launch(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                },
                enabled = phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Llamar", fontSize = 16.sp) }

            n.petId?.let { petId ->
                TextButton(onClick = { onOpenPet(petId) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver la publicación", fontSize = 16.sp)
                }
            }
        }
    }
}

private fun launch(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No hay una aplicación disponible para esta acción", Toast.LENGTH_SHORT).show()
    }
}