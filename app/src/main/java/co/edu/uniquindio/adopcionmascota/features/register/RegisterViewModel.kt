package co.edu.uniquindio.adopcionmascota.features.register

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

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val profilePhoto: String? = null,
    val isRegistering: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val cityError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
    val registrationResult: RequestResult? = null
) {
    // Aliases para compatibilidad
    val name: String get() = fullName
    val address: String get() = city
    val nameError: String? get() = fullNameError
    val addressError: String? get() = cityError

    val isFormValid: Boolean
        get() = fullName.isNotBlank() &&
                email.isNotBlank() &&
                phone.isNotBlank() &&
                city.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                termsAccepted &&
                fullNameError == null &&
                emailError == null &&
                phoneError == null &&
                cityError == null &&
                passwordError == null &&
                confirmPasswordError == null &&
                termsError == null
}

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(newFullName: String) {
        _uiState.update { state ->
            state.copy(
                fullName = newFullName,
                fullNameError = validateFullName(newFullName)
            )
        }
    }

    // Alias para compatibilidad
    fun onNameChange(newName: String) = onFullNameChange(newName)

    fun onEmailChange(newEmail: String) {
        _uiState.update { state ->
            state.copy(
                email = newEmail,
                emailError = validateEmail(newEmail)
            )
        }
    }

    fun onPhoneChange(newPhone: String) {
        _uiState.update { state ->
            state.copy(
                phone = newPhone,
                phoneError = validatePhone(newPhone)
            )
        }
    }

    fun onCityChange(newCity: String) {
        _uiState.update { state ->
            state.copy(
                city = newCity,
                cityError = validateCity(newCity)
            )
        }
    }

    // Alias para compatibilidad
    fun onAddressChange(newAddress: String) = onCityChange(newAddress)

    fun onPasswordChange(newPassword: String) {
        _uiState.update { state ->
            state.copy(
                password = newPassword,
                passwordError = validatePassword(newPassword),
                confirmPasswordError = validateConfirmPassword(newPassword, state.confirmPassword)
            )
        }
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _uiState.update { state ->
            state.copy(
                confirmPassword = newConfirmPassword,
                confirmPasswordError = validateConfirmPassword(state.password, newConfirmPassword)
            )
        }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _uiState.update { state ->
            state.copy(
                termsAccepted = accepted,
                termsError = if (accepted) null else "Debes aceptar los términos y condiciones"
            )
        }
    }

    fun onSelectProfilePhoto() {
        _uiState.update { state ->
            val hasPhoto = state.profilePhoto != null
            state.copy(
                profilePhoto = if (hasPhoto) null else "foto_perfil.jpg"
            )
        }
    }

    fun resetForm() {
        _uiState.value = RegisterUiState()
    }

    fun register(onSuccess: () -> Unit = {}) {
        val state = _uiState.value

        val nameErr = validateFullName(state.fullName)
        val emailErr = validateEmail(state.email)
        val phoneErr = validatePhone(state.phone)
        val cityErr = validateCity(state.city)
        val passErr = validatePassword(state.password)
        val confirmErr = validateConfirmPassword(state.password, state.confirmPassword)
        val termsErr = if (state.termsAccepted) null else "Debes aceptar los términos y condiciones para continuar"

        if (nameErr != null || emailErr != null || phoneErr != null || cityErr != null ||
            passErr != null || confirmErr != null || termsErr != null
        ) {
            _uiState.update { currentState ->
                currentState.copy(
                    fullNameError = nameErr,
                    emailError = emailErr,
                    phoneError = phoneErr,
                    cityError = cityErr,
                    passwordError = passErr,
                    confirmPasswordError = confirmErr,
                    termsError = termsErr,
                    registrationResult = RequestResult.Failure("Por favor corrige los errores antes de continuar")
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRegistering = true,
                    registrationResult = RequestResult.Loading
                )
            }

            delay(1200) // Simulación de registro en red / BD

            _uiState.update {
                it.copy(
                    isRegistering = false,
                    registrationResult = RequestResult.Success("¡Cuenta creada con éxito!")
                )
            }

            delay(500)
            onSuccess()
        }
    }

    private fun validateFullName(fullName: String): String? {
        return when {
            fullName.isBlank() -> "El nombre completo es obligatorio"
            fullName.trim().split(" ").size < 2 -> "Ingresa tu nombre y apellido"
            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingresa un correo electrónico válido"
            else -> null
        }
    }

    private fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "El teléfono es obligatorio"
            phone.length < 7 -> "Ingresa un número telefónico válido"
            else -> null
        }
    }

    private fun validateCity(city: String): String? {
        return when {
            city.isBlank() -> "La ciudad es obligatoria"
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

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Debes confirmar la contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
    }
}
