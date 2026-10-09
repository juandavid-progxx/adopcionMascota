package co.edu.uniquindio.adopcionmascota.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.adopcionmascota.core.util.RequestResult


@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = CenterVertically)
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = {
                viewModel.onEmailChange(newEmail = it)

            },
            label = {
                Text(text = "Email")
            },
            isError = state.emailError != null, // Se muestra el borde rojo si hay error
            supportingText = {
                state.emailError?.let {
                    Text(text = it) // Mensaje de error debajo del campo
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email) // Opcional pero recomendado para email
        )

        OutlinedTextField(
            value = state.password, // Estado del campo de password
            onValueChange = {
                viewModel.onEmailChange(newEmail = it)

            }, // Actualiza el estado cuando el usuario escribe
            visualTransformation = PasswordVisualTransformation(),
            label = {
                Text(text = "Password")
            },
            isError = state.passwordError != null, // Se muestra el borde rojo si hay error
            supportingText = {
                state.passwordError?.let {
                    Text(text = it) // Mensaje de error debajo del campo
                }
            }

        )
        Button(
            onClick = {

            },
            enabled = state.isFormValid,
            content = {
                Text(text = "Iniciar Sesión")
            }
        )
        when (state.loginResult) {
            is RequestResult.Loading -> {
                Text(
                    text = "cargando"
                )
            }

            is RequestResult.Success -> {
                Text(
                    text = "login exitoso",
                    color = Color.Green

                )

            }

            is RequestResult.Failure -> {
                Text(
                    text =
                        "login fallido",
                    color = Color.Red
                )
            }


            else -> {}
        }

    }
}

