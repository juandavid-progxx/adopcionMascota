package co.edu.uniquindio.adopcionmascota.features.passwordRecovery

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

data class PasswordRecoveryUiState(
    val email: String = "",
    val isSending: Boolean = false,
    val emailError: String? = null,
    val recoveryResult: RequestResult? = null
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && emailError == null
}

class PasswordRecoveryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordRecoveryUiState())
    val uiState: StateFlow<PasswordRecoveryUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update { state ->
            state.copy(
                email = newEmail,
                emailError = validateEmail(newEmail)
            )
        }
    }

    fun sendRecoveryLink(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        val err = validateEmail(state.email)
        if (err != null) {
            _uiState.update {
                it.copy(
                    emailError = err,
                    recoveryResult = RequestResult.Failure("Ingresa un correo electrónico válido")
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
                    recoveryResult = RequestResult.Loading
                )
            }

            delay(1000) // Simulación de envío del enlace por correo

            _uiState.update {
                it.copy(
                    isSending = false,
                    recoveryResult = RequestResult.Success("¡Enlace enviado!")
                )
            }

            onSuccess()
        }
    }

    fun resetForm() {
        _uiState.value = PasswordRecoveryUiState()
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingresa un correo electrónico válido"
            else -> null
        }
    }
}
