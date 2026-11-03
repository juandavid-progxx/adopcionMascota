package co.edu.uniquindio.adopcionmascota.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.adopcionmascota.core.data.NotificationRepository
import co.edu.uniquindio.adopcionmascota.core.data.PetRepository
import co.edu.uniquindio.adopcionmascota.core.data.SessionManager
import co.edu.uniquindio.adopcionmascota.core.data.User
import co.edu.uniquindio.adopcionmascota.features.feed.PostStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import co.edu.uniquindio.adopcionmascota.core.data.ReputationManager
import co.edu.uniquindio.adopcionmascota.core.data.UserLevel


data class ProfileUiState(
    val user: User? = null,
    val active: Int = 0,      // verificadas
    val resolved: Int = 0,    // finalizadas
    val pending: Int = 0,     // pendientes de verificación
    val showEditDialog: Boolean = false,
    val editName: String = "",
    val editPhone: String = "",
    val nameError: String? = null,
    val showDeleteDialog: Boolean = false,
    val points: Int = 0,
    val level: UserLevel = UserLevel.FRIEND
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(user = SessionManager.currentUser))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PetRepository.pets.collect { all ->
                val myId = SessionManager.currentUser?.id
                val mine = all.filter { it.authorId == myId }
                val stats = ReputationManager.statsFor(myId ?: "", all)
                _uiState.update {
                    it.copy(
                        active = mine.count { p -> p.status == PostStatus.VERIFIED },
                        resolved = mine.count { p -> p.status == PostStatus.RESOLVED },
                        pending = mine.count { p -> p.status == PostStatus.PENDING },
                        points = stats.points,
                        level = UserLevel.forPoints(stats.points)
                    )
                }
            }
        }
    }

    // ---------- Editar datos ----------
    fun onEditClick() {
        val user = _uiState.value.user
        _uiState.update {
            it.copy(
                showEditDialog = true,
                editName = user?.name ?: "",
                editPhone = user?.phone ?: "",
                nameError = null
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(editName = value, nameError = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(editPhone = value) }
    }

    fun onDismissEdit() {
        _uiState.update { it.copy(showEditDialog = false) }
    }

    fun onSaveEdit() {
        val state = _uiState.value
        if (state.editName.trim().length < 2) {
            _uiState.update { it.copy(nameError = "El nombre debe tener al menos 2 caracteres") }
            return
        }
        SessionManager.updateProfile(state.editName.trim(), state.editPhone.trim())
        _uiState.update {
            it.copy(user = SessionManager.currentUser, showEditDialog = false)
        }
    }

    // ---------- Eliminar cuenta ----------
    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDismissDelete() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onConfirmDelete(onDone: () -> Unit) {
        val id = SessionManager.currentUser?.id
        if (id != null) {
            PetRepository.deleteByAuthor(id)
            NotificationRepository.deleteForUser(id)
        }
        SessionManager.logout()
        onDone()
    }

    // ---------- Cerrar sesión ----------
    fun onLogout(onDone: () -> Unit) {
        SessionManager.logout()
        onDone()
    }
}