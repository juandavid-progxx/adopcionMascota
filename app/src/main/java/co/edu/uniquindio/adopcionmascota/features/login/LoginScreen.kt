package co.edu.uniquindio.adopcionmascota.features.login

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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.adopcionmascota.core.util.RequestResult

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3FAF7)) // Fondo menta claro
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 2. Barra de estado simulada
            SimulatedStatusBar()

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Logotipo y nombre de la aplicación AdoptaYa
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF91EDE0)), // Turquesa claro
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "Logo AdoptaYa",
                    tint = Color(0xFF311B92), // Morado oscuro
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "AdoptaYa",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF263238)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Texto descriptivo
            Text(
                text = "Conectamos mascotas con hogares llenos de amor",
                fontSize = 13.sp,
                color = Color(0xFF455A64),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 5. Formulario de inicio de sesión
            // Campo 1: Correo electrónico
            CustomLoginField(
                label = "Correo electrónico",
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                placeholder = "maria.rodriguez@email.com",
                isError = state.emailError != null,
                errorMessage = state.emailError,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo 2: Contraseña
            CustomLoginField(
                label = "Contraseña",
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                placeholder = "••••••••",
                isError = state.passwordError != null,
                errorMessage = state.passwordError,
                isPassword = true,
                keyboardType = KeyboardType.Password
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Recuperación de contraseña
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00796B),
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7. Botón principal: Iniciar sesión
            Button(
                onClick = { viewModel.login(onSuccess = onLoginSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !state.isLoggingIn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00796B),
                    disabledContainerColor = Color(0xFF80CBC4)
                ),
                shape = CircleShape
            ) {
                if (state.isLoggingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Iniciar sesión",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Mensaje de resultado (Error)
            when (val result = state.loginResult) {
                is RequestResult.Failure -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
                is RequestResult.Success -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.message,
                        color = Color(0xFF00796B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 8. Separador: o continúa con
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFC7D6D2),
                    thickness = 1.dp
                )
                Text(
                    text = "  o continúa con  ",
                    fontSize = 12.sp,
                    color = Color(0xFF455A64)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFC7D6D2),
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 9. Botón de inicio de sesión con Google
            OutlinedButton(
                onClick = onGoogleLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC7D6D2))
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo vectorial/representativo de Google
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        color = Color(0xFF4285F4)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "G",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 10. Enlace para registrarse
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable { onRegisterClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿No tienes cuenta? ",
                fontSize = 13.sp,
                color = Color(0xFF455A64)
            )
            Text(
                text = "Regístrate",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
        }
    }
}

// -------------------------------------------------------------------
// Componentes Auxiliares
// -------------------------------------------------------------------

@Composable
private fun SimulatedStatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "9:41",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF263238)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = Icons.Default.SignalCellular4Bar,
                contentDescription = null,
                tint = Color(0xFF263238),
                modifier = Modifier.size(15.dp)
            )
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = Color(0xFF263238),
                modifier = Modifier.size(15.dp)
            )
            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = null,
                tint = Color(0xFF263238),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun CustomLoginField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label, fontSize = 13.sp) },
            placeholder = { Text(text = placeholder, color = Color(0xFF90A4AE), fontSize = 13.sp) },
            isError = isError,
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF00796B), // Verde azulado oscuro al enfocar
                unfocusedBorderColor = Color(0xFFC7D6D2), // Borde gris verdoso
                errorBorderColor = Color.Red,
                focusedLabelColor = Color(0xFF00796B),
                unfocusedLabelColor = Color(0xFF455A64)
            )
        )
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
