package co.edu.uniquindio.adopcionmascota.features.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onUserAccessClick: () -> Unit = {},
    onModeratorAccessClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9)) // Fondo inferior grisáceo limpio
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Cabecera superior con degradado verde petróleo/esmeralda
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF004D40), // Verde petróleo profundo
                            Color(0xFF00332C)  // Verde esmeralda oscuro
                        )
                    )
                )
                .padding(horizontal = 28.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icono dentro de contenedor cuadrado 73 × 73 px con esquinas muy redondeadas
                Box(
                    modifier = Modifier
                        .size(73.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF91EDE0).copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Logo AdoptaYa",
                        tint = Color(0xFF311B92), // Morado/rosado oscuro
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nombre AdoptaYa
                Text(
                    text = "AdoptaYa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mensaje exacto de bienvenida
                Text(
                    text = "Explora las mascotas que buscan una familia y descubre el amor incondicional que tienen para ofrecer.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // 2. Sección inferior: Selección de acceso y botones de acción
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "¿Cómo quieres continuar?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Selecciona el tipo de acceso que necesitas",
                    fontSize = 11.sp,
                    color = Color(0xFF607D8B)
                )
            }

            // 3. Tarjeta principal: Usuario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.selectUserRole(onNavigate = onUserAccessClick) }
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Icono de usuario oscuro dentro de cuadrado verde menta
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFB2DFDB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Usuario",
                                    tint = Color(0xFF004D40),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Continuar como usuario",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF263238)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Adopta, publica y conecta con la comunidad.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF607D8B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Botón circular verde petróleo con flecha hacia la derecha
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF004D40)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Continuar",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Opciones informativas inferiores con puntos verdes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OptionBullet(text = "Buscar mascotas")
                        Spacer(modifier = Modifier.width(16.dp))
                        OptionBullet(text = "Crear publicaciones")
                    }
                }
            }

            // 4. Tarjeta secundaria: Moderador
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        viewModel.selectModeratorRole()
                        onModeratorAccessClick()
                    }
                    .border(1.dp, Color(0xFFCFD8D5), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0EC))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icono de administración en cuadrado azul claro
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Moderador",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Acceso de moderador",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF263238)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Etiqueta azul RESTRINGIDO
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1565C0)
                                ) {
                                    Text(
                                        text = "RESTRINGIDO",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gestión y revisión de contenido institucional.",
                                fontSize = 11.sp,
                                color = Color(0xFF607D8B)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Detalles moderador",
                        tint = Color(0xFF607D8B),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 5. Botones de acción directa: Iniciar sesión y Crear cuenta
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00796B)
                ),
                shape = CircleShape
            ) {
                Text(
                    text = "Iniciar sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFF00796B))
            ) {
                Text(
                    text = "Crear cuenta",
                    color = Color(0xFF00796B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Diálogo informativo para acceso restringido de moderador
    if (state.showModeratorRestrictionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissModeratorDialog() },
            title = {
                Text(
                    text = "Acceso de Moderador",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D40)
                )
            },
            text = {
                Text(
                    text = "Este acceso requiere credenciales especiales de moderador o administrador para gestionar publicaciones y usuarios.",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissModeratorDialog() },
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
private fun OptionBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF00BFA5))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF455A64)
        )
    }
}
