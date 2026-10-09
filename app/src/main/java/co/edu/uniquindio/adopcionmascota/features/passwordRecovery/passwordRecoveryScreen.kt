package co.edu.uniquindio.adopcionmascota.features.passwordRecovery

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.adopcionmascota.core.util.RequestResult

@Composable
fun PasswordRecoveryScreen(
    viewModel: PasswordRecoveryViewModel = viewModel(),
    onBack: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FAF8)) // Fondo mint claro
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. APP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color(0xFF0F2A2A)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Recuperar acceso",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF0F2A2A)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. TARJETA INFORMATIVA (Hero Card)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFA0F0E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ícono de sobre de correo con @ (estilo ilustración)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD1C4E9)), // Tono lila
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = "Correo",
                            tint = Color(0xFF311B92),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Restablece tu contraseña",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F2A2A)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Ingresa tu correo y te enviaremos un enlace para recuperar tu acceso",
                        fontSize = 14.sp,
                        color = Color(0xFF2C4A4A),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. CAMPO DE TEXTO (Outlined TextField)
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text(text = "Correo electrónico", fontSize = 13.sp) },
                    placeholder = { Text(text = "maria.rodriguez@email.com", color = Color(0xFF809A9A), fontSize = 14.sp) },
                    isError = state.emailError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF00695C), // Borde verde oscuro 1.5dp
                        unfocusedBorderColor = Color(0xFF00695C),
                        errorBorderColor = Color.Red,
                        focusedLabelColor = Color(0xFF00695C),
                        unfocusedLabelColor = Color(0xFF00695C)
                    )
                )

                state.emailError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. BOTÓN PRINCIPAL
            Button(
                onClick = { viewModel.sendRecoveryLink() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.isSending,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00695C),
                    disabledContainerColor = Color(0xFF80CBC4)
                ),
                shape = CircleShape // Forma de píldora
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Enviar enlace de recuperación",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. TARJETA DE ESTADO DE ÉXITO (Solo aparece al enviar con éxito)
            if (state.recoveryResult is RequestResult.Success) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC3F0D4)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00695C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Enviado",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "¡Enlace enviado!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E3A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Revisa tu bandeja de entrada y sigue las instrucciones. El enlace expira en 30 minutos.",
                                fontSize = 13.sp,
                                color = Color(0xFF1B5E3A),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else if (state.recoveryResult is RequestResult.Failure) {
                Text(
                    text = (state.recoveryResult as RequestResult.Failure).errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 6. PIE DE PÁGINA
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable { onLoginClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Recordaste tu contraseña? ",
                fontSize = 13.sp,
                color = Color(0xFF4A6060)
            )
            Text(
                text = "Inicia sesión",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00695C)
            )
        }
    }
}
