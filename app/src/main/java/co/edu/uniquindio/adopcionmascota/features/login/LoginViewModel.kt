package co.edu.uniquindio.adopcionmascota.features.login

import android.util.Patterns
import android.view.PixelCopy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import co.edu.uniquindio.adopcionmascota.core.util.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginResult: RequestResult? = null
) {
    // Propiedad calculada: la pantalla no tiene que repetir esta lógica
    val isFormValid: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                emailError == null &&
                passwordError == null


}
class LoginViewModel : ViewModel() {
    // Estado para los campos de texto
    // Un único flujo con el estado completo de la pantalla
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

    // Es útil para limpiar el formulario después de un login exitoso
    fun resetForm() {
        _uiState.value = LoginUiState()
    }

    fun login() {
        val state = _uiState.value
        val result = if (state.email == "juan@gmail.com" && state.password == "111") {
            RequestResult.Success("login Exitoso")
        } else {
            RequestResult.Failure(" valores invalidos")
        }
        _uiState.update { state  ->
            state.copy(loginResult = result)
        }


    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingresa un email válido"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }

}