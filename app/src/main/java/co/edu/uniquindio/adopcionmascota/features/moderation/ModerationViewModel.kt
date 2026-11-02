package co.edu.uniquindio.adopcionmascota.features.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.adopcionmascota.core.data.PetRepository
import co.edu.uniquindio.adopcionmascota.features.feed.Pet
import co.edu.uniquindio.adopcionmascota.features.feed.PostStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ModerationUiState(
    val selectedTab: Int = 0,
    val pending: List<Pet> = emptyList(),
    val all: List<Pet> = emptyList(),
    val rejectingPet: Pet? = null,
    val rejectReason: String = "",
    val reasonError: String? = null
)

class ModerationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ModerationUiState())
    val uiState: StateFlow<ModerationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PetRepository.pets.collect { all ->
                _uiState.update {
                    it.copy(
                        all = all,
                        pending = all.filter { p -> p.status == PostStatus.PENDING }
                    )
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun onVerify(pet: Pet) {
        PetRepository.update(pet.id) {
            it.copy(status = PostStatus.VERIFIED, rejectionReason = null)
        }
    }

    fun onRejectClick(pet: Pet) {
        _uiState.update { it.copy(rejectingPet = pet, rejectReason = "", reasonError = null) }
    }

    fun onReasonChange(text: String) {
        _uiState.update { it.copy(rejectReason = text, reasonError = null) }
    }

    fun onDismissReject() {
        _uiState.update { it.copy(rejectingPet = null, rejectReason = "", reasonError = null) }
    }

    fun onConfirmReject() {
        val state = _uiState.value
        val pet = state.rejectingPet ?: return
        if (state.rejectReason.isBlank()) {
            _uiState.update { it.copy(reasonError = "Debes escribir el motivo del rechazo") }
            return
        }
        PetRepository.update(pet.id) {
            it.copy(status = PostStatus.REJECTED, rejectionReason = state.rejectReason.trim())
        }
        onDismissReject()
    }

    fun onResolve(pet: Pet) {
        PetRepository.update(pet.id) { it.copy(status = PostStatus.RESOLVED) }
    }
}