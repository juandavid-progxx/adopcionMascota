package co.edu.uniquindio.adopcionmascota.features.myposts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.adopcionmascota.core.data.PetRepository
import co.edu.uniquindio.adopcionmascota.core.data.SessionManager
import co.edu.uniquindio.adopcionmascota.features.feed.Pet
import co.edu.uniquindio.adopcionmascota.features.feed.PostStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPostsUiState(
    val posts: List<Pet> = emptyList(),
    val editingPet: Pet? = null,
    val editName: String = "",
    val editDescription: String = "",
    val editAge: String = "",
    val nameError: String? = null,
    val descriptionError: String? = null,
    val deletingPet: Pet? = null,
    val resolvingPet: Pet? = null
)

class MyPostsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PetRepository.pets.collect { all ->
                val myId = SessionManager.currentUser?.id
                _uiState.update { it.copy(posts = all.filter { p -> p.authorId == myId }) }
            }
        }
    }

    // ---------- Editar ----------
    fun onEditClick(pet: Pet) {
        _uiState.update {
            it.copy(
                editingPet = pet,
                editName = pet.name,
                editDescription = pet.description,
                editAge = pet.age,
                nameError = null,
                descriptionError = null
            )
        }
    }

    fun onEditNameChange(value: String) {
        _uiState.update { it.copy(editName = value, nameError = null) }
    }

    fun onEditDescriptionChange(value: String) {
        _uiState.update { it.copy(editDescription = value, descriptionError = null) }
    }

    fun onEditAgeChange(value: String) {
        _uiState.update { it.copy(editAge = value) }
    }

    fun onDismissEdit() {
        _uiState.update { it.copy(editingPet = null) }
    }

    fun onSaveEdit() {
        val state = _uiState.value
        val pet = state.editingPet ?: return

        val nameErr = if (state.editName.isBlank()) "El título es obligatorio" else null
        val descErr = if (state.editDescription.isBlank()) "La descripción es obligatoria" else null
        if (nameErr != null || descErr != null) {
            _uiState.update { it.copy(nameError = nameErr, descriptionError = descErr) }
            return
        }

        PetRepository.update(pet.id) {
            it.copy(
                name = state.editName.trim(),
                description = state.editDescription.trim(),
                age = state.editAge.trim().ifBlank { "No especificada" },
                // Si estaba rechazada, al editarla vuelve a revisión
                status = if (it.status == PostStatus.REJECTED) PostStatus.PENDING else it.status,
                rejectionReason = if (it.status == PostStatus.REJECTED) null else it.rejectionReason
            )
        }
        onDismissEdit()
    }

    // ---------- Eliminar ----------
    fun onDeleteClick(pet: Pet) {
        _uiState.update { it.copy(deletingPet = pet) }
    }

    fun onDismissDelete() {
        _uiState.update { it.copy(deletingPet = null) }
    }

    fun onConfirmDelete() {
        _uiState.value.deletingPet?.let { PetRepository.delete(it.id) }
        onDismissDelete()
    }

    // ---------- Resolver ----------
    fun onResolveClick(pet: Pet) {
        _uiState.update { it.copy(resolvingPet = pet) }
    }

    fun onDismissResolve() {
        _uiState.update { it.copy(resolvingPet = null) }
    }

    fun onConfirmResolve() {
        _uiState.value.resolvingPet?.let { pet ->
            PetRepository.update(pet.id) { it.copy(status = PostStatus.RESOLVED) }
        }
        onDismissResolve()
    }
}