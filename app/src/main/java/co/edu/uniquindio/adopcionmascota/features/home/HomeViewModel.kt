package co.edu.uniquindio.adopcionmascota.features.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class UserRole {
    USER,
    MODERATOR
}

data class HomeUiState(
    val selectedRole: UserRole? = null,
    val showModeratorRestrictionDialog: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun selectUserRole(onNavigate: () -> Unit = {}) {
        _uiState.update { it.copy(selectedRole = UserRole.USER) }
        onNavigate()
    }

    fun selectModeratorRole() {
        _uiState.update { it.copy(showModeratorRestrictionDialog = true) }
    }

    fun dismissModeratorDialog() {
        _uiState.update { it.copy(showModeratorRestrictionDialog = false) }
    }
}
