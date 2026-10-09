package co.edu.uniquindio.adopcionmascota.features.register

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3FAF7)) // Fondo menta claro
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 2. Encabezado (Fila con flecha atrás y título)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color(0xFF263238)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Crear cuenta",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF263238)
            )
        }

        // Subtítulo
        Text(
            text = "Completa tus datos para comenzar",
            fontSize = 14.sp,
            color = Color(0xFF607D8B),
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Sección para agregar fotografía
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.onSelectProfilePhoto() }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF91EDE0)), // Turquesa claro
                contentAlignment = Alignment.Center
            ) {
                if (state.profilePhoto != null) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Foto agregada",
                        tint = Color(0xFF00796B),
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Huella de mascota",
                        tint = Color(0xFF311B92), // Morado oscuro
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = if (state.profilePhoto != null) "Foto seleccionada ✓" else "Agregar foto",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00796B)
                )
                Text(
                    text = if (state.profilePhoto != null) "Toca para cambiar" else "Opcional",
                    fontSize = 12.sp,
                    color = Color(0xFF78909C)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4. Formulario de registro (6 campos)

        // Campo 1: Nombre completo
        CustomRegisterField(
            label = "Nombre completo",
            value = state.fullName,
            onValueChange = { viewModel.onFullNameChange(it) },
            placeholder = "María Rodríguez",
            isError = state.fullNameError != null,
            errorMessage = state.fullNameError,
            keyboardType = KeyboardType.Text
        )

        // Campo 2: Correo electrónico
        CustomRegisterField(
            label = "Correo electrónico",
            value = state.email,
            onValueChange = { viewModel.onEmailChange(it) },
            placeholder = "maria@email.com",
            isError = state.emailError != null,
            errorMessage = state.emailError,
            keyboardType = KeyboardType.Email
        )

        // Campo 3: Teléfono
        CustomRegisterField(
            label = "Teléfono",
            value = state.phone,
            onValueChange = { viewModel.onPhoneChange(it) },
            placeholder = "+57 300 123 4567",
            isError = state.phoneError != null,
            errorMessage = state.phoneError,
            keyboardType = KeyboardType.Phone
        )

        // Campo 4: Ciudad
        CustomRegisterField(
            label = "Ciudad",
            value = state.city,
            onValueChange = { viewModel.onCityChange(it) },
            placeholder = "Bogotá, Colombia",
            isError = state.cityError != null,
            errorMessage = state.cityError,
            keyboardType = KeyboardType.Text
        )

        // Campo 5: Contraseña
        CustomRegisterField(
            label = "Contraseña",
            value = state.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            placeholder = "••••••••",
            isError = state.passwordError != null,
            errorMessage = state.passwordError,
            isPassword = true,
            keyboardType = KeyboardType.Password
        )

        // Campo 6: Confirmar contraseña
        CustomRegisterField(
            label = "Confirmar contraseña",
            value = state.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            placeholder = "••••••••",
            isError = state.confirmPasswordError != null,
            errorMessage = state.confirmPasswordError,
            isPassword = true,
            keyboardType = KeyboardType.Password
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 5. Términos y condiciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.onTermsAcceptedChange(!state.termsAccepted) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.termsAccepted,
                onCheckedChange = { viewModel.onTermsAcceptedChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF00796B),
                    uncheckedColor = Color(0xFFCFD8D5)
                ),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Acepto los términos y condiciones y la política de privacidad",
                fontSize = 13.sp,
                color = Color(0xFF263238),
                lineHeight = 18.sp
            )
        }
        state.termsError?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. Botón principal
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { viewModel.register(onSuccess = onBack) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                enabled = !state.isRegistering,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00796B),
                    disabledContainerColor = Color(0xFF80CBC4)
                ),
                shape = CircleShape
            ) {
                if (state.isRegistering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Crear cuenta",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Feedback de resultado (éxito o falla)
        when (val result = state.registrationResult) {
            is RequestResult.Success -> {
                Text(
                    text = result.message,
                    color = Color(0xFF00796B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is RequestResult.Failure -> {
                Text(
                    text = result.errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CustomRegisterField(
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
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF00796B), // Turquesa oscuro
                unfocusedBorderColor = Color(0xFFCFD8D5), // Borde normal
                errorBorderColor = Color.Red,
                focusedLabelColor = Color(0xFF00796B),
                unfocusedLabelColor = Color(0xFF607D8B)
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
