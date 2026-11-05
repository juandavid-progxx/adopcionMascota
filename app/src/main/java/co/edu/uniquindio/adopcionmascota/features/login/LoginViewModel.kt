package co.edu.uniquindio.adopcionmascota.features.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.adopcionmascota.core.data.AuthRepository
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
        _uiState.update { it.copy(email = newEmail, emailError = validateEmail(newEmail)) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, passwordError = validatePassword(newPassword)) }
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
            _uiState.update { it.copy(isLoggingIn = true, loginResult = RequestResult.Loading) }
            try {
                AuthRepository.login(state.email, state.password)
                _uiState.update {
                    it.copy(
                        isLoggingIn = false,
                        loginResult = RequestResult.Success("¡Inicio de sesión exitoso!")
                    )
                }
                delay(300)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoggingIn = false,
                        loginResult = RequestResult.Failure(AuthRepository.friendlyError(e))
                    )
                }
            }
        }
    }

   //Función de iniciar sesión con Google
    fun loginWithGoogle(getToken: suspend () -> String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingIn = true, loginResult = RequestResult.Loading) }
            try {
                val idToken = getToken()
                AuthRepository.loginWithGoogle(idToken)
                _uiState.update {
                    it.copy(
                        isLoggingIn = false,
                        loginResult = RequestResult.Success("¡Sesión iniciada con Google!")
                    )
                }
                delay(300)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoggingIn = false,
                        loginResult = RequestResult.Failure(AuthRepository.friendlyError(e))
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
            else -> null
        }
    }
}