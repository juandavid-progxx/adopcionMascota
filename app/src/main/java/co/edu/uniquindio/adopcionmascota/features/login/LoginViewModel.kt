package co.edu.uniquindio.adopcionmascota.features.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.adopcionmascota.core.util.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginResult: RequestResult? = null
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                emailError == null &&
                passwordError == null
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update { state ->
            state.copy(
                email = newEmail,
                emailError = validateEmail(newEmail)
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { state ->
            state.copy(
                password = newPassword,
                passwordError = validatePassword(newPassword)
            )
        }
    }

    fun resetForm() {
        _uiState.value = LoginUiState()
    }

    fun login(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        val emailErr = validateEmail(state.email)
        val passwordErr = validatePassword(state.password)

        if (emailErr != null || passwordErr != null) {
            _uiState.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passwordErr,
                    loginResult = RequestResult.Failure("Por favor ingresa un correo y contraseña válidos.")
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoggingIn = true,
                    loginResult = RequestResult.Loading
                )
            }

            delay(1000) // Simulación de verificación con el servicio de autenticación

            // Validación de credenciales de prueba o cualquier correo/password válido
            val isSuccess = (state.email.isNotBlank() && state.password.length >= 3)
            if (isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoggingIn = false,
                        loginResult = RequestResult.Success("¡Inicio de sesión exitoso!")
                    )
                }
                delay(300)
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoggingIn = false,
                        loginResult = RequestResult.Failure("Correo o contraseña incorrectos.")
                    )
                }
            }
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingresa un correo electrónico válido"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 3 -> "La contraseña debe tener al menos 3 caracteres"
            else -> null
        }
    }
}
