package co.edu.uniquindio.adopcionmascota.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.adopcionmascota.core.data.NotificationRepository
import co.edu.uniquindio.adopcionmascota.core.data.PetRepository
import co.edu.uniquindio.adopcionmascota.core.data.SessionManager
import co.edu.uniquindio.adopcionmascota.core.util.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val searchQuery: String = "",
    val selectedCategory: PetCategory? = null,   // null = todas las categorías
    val selectedPetType: PetType = PetType.ALL,
    val selectedLocation: LocationFilter = LocationFilter.ALL_COUNTRY,
    val viewMode: ViewMode = ViewMode.LIST,
    val selectedBottomNavItem: BottomNavItem = BottomNavItem.HOME,
    val pets: List<Pet> = samplePets,
    val filteredPets: List<Pet> = samplePets,
    val selectedPetForDetail: Pet? = null,
    val adoptionDialogPet: Pet? = null,
    val resolveDialogPet: Pet? = null,
    val adoptionSuccessMessage: String? = null,
    val showPublishDialog: Boolean = false,
    val requestResult: RequestResult? = null
)

// Aplica todos los filtros activos sobre la lista de publicaciones visibles
private fun FeedUiState.withFilters(): FeedUiState {
    val result = pets.filter { pet ->
        val matchesCategory = selectedCategory == null || pet.category == selectedCategory
        val matchesType = selectedPetType == PetType.ALL || pet.type == selectedPetType
        val matchesLocation = selectedLocation == LocationFilter.ALL_COUNTRY ||
                pet.locationFilter == selectedLocation
        val matchesQuery = searchQuery.isBlank() ||
                pet.name.contains(searchQuery, ignoreCase = true) ||
                pet.breed.contains(searchQuery, ignoreCase = true) ||
                pet.city.contains(searchQuery, ignoreCase = true) ||
                pet.description.contains(searchQuery, ignoreCase = true) ||
                pet.type.displayName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesType && matchesLocation && matchesQuery
    }
    return copy(filteredPets = result)
}

class FeedScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PetRepository.pets.collect { all ->
                _uiState.update { state ->
                    state.copy(
                        // El feed público solo muestra publicaciones verificadas
                        pets = all.filter { it.status == PostStatus.VERIFIED },
                        selectedPetForDetail = state.selectedPetForDetail?.let { sel ->
                            all.find { it.id == sel.id }
                        }
                    ).withFilters()
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).withFilters() }
    }

    fun onCategorySelected(category: PetCategory?) {
        _uiState.update { it.copy(selectedCategory = category).withFilters() }
    }

    fun onPetTypeSelected(type: PetType) {
        _uiState.update { it.copy(selectedPetType = type).withFilters() }
    }

    fun onLocationSelected(location: LocationFilter) {
        _uiState.update { it.copy(selectedLocation = location).withFilters() }
    }

    fun onViewModeSelected(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun onBottomNavItemSelected(item: BottomNavItem) {
        _uiState.update { state ->
            when (item) {
                BottomNavItem.MAP -> state.copy(selectedBottomNavItem = item, viewMode = ViewMode.MAP)
                BottomNavItem.HOME -> state.copy(selectedBottomNavItem = item, viewMode = ViewMode.LIST)
                else -> state.copy(selectedBottomNavItem = item)
            }
        }
    }

    fun onPetSelectedForDetail(pet: Pet) {
        _uiState.update { it.copy(selectedPetForDetail = pet) }
    }

    fun openPetById(id: String) {
        PetRepository.pets.value.find { it.id == id }?.let { onPetSelectedForDetail(it) }
    }

    fun onCloseDetail() {
        _uiState.update { it.copy(selectedPetForDetail = null) }
    }

    fun onAdoptClick(pet: Pet) {
        if (pet.authorId == SessionManager.currentUser?.id) {
            // Es mi publicación: primero pedimos confirmación para resolverla
            _uiState.update { it.copy(resolveDialogPet = pet) }
        } else {
            _uiState.update { it.copy(adoptionDialogPet = pet) }
        }
    }

    fun onDismissResolve() {
        _uiState.update { it.copy(resolveDialogPet = null) }
    }

    fun onConfirmResolve() {
        val pet = _uiState.value.resolveDialogPet ?: return
        PetRepository.update(pet.id) { it.copy(status = PostStatus.RESOLVED) }
        _uiState.update { it.copy(resolveDialogPet = null, selectedPetForDetail = null) }
    }

    fun onDismissAdoptionDialog() {
        _uiState.update { it.copy(adoptionDialogPet = null) }
    }

    // Solicitud según la categoría (adoptar, dar información, hogar de paso...)
    fun onConfirmAdoption(pet: Pet) {
        val user = SessionManager.currentUser
        if (user?.id != pet.authorId) {
            NotificationRepository.add(
                userId = pet.authorId,
                title = "Nueva solicitud: ${pet.category.displayName}",
                message = "${user?.name ?: "Alguien"} envió una solicitud (\"${pet.category.actionLabel}\") para tu publicación \"${pet.name}\". Toca para ver sus datos de contacto.",
                petId = pet.id,
                contactName = user?.name ?: "Usuario",
                contactEmail = user?.email,
                contactPhone = user?.phone
            )
        }
        _uiState.update {
            it.copy(
                adoptionDialogPet = null,
                adoptionSuccessMessage = "Tu solicitud para \"${pet.name}\" fue enviada. El autor de la publicación recibirá una notificación y se pondrá en contacto contigo.",
                requestResult = RequestResult.Success("Solicitud enviada")
            )
        }
    }

    fun onDismissSuccessMessage() {
        _uiState.update { it.copy(adoptionSuccessMessage = null, requestResult = null) }
    }

    fun onToggleFavorite(pet: Pet) {
        PetRepository.update(pet.id) { it.copy(isFavorite = !it.isFavorite) }
    }

    fun onVote(pet: Pet) {
        PetRepository.update(pet.id) {
            if (it.votedByMe) {
                it.copy(
                    votedByMe = false,
                    importantVotes = (it.importantVotes - 1).coerceAtLeast(0)
                )
            } else {
                it.copy(votedByMe = true, importantVotes = it.importantVotes + 1)
            }
        }
    }

    fun onAddComment(pet: Pet, text: String) {
        if (text.isBlank()) return
        val user = SessionManager.currentUser
        val author = user?.name ?: "Usuario"
        PetRepository.update(pet.id) {
            it.copy(
                comments = it.comments + Comment(
                    id = System.currentTimeMillis().toString(),
                    authorName = author,
                    text = text.trim()
                )
            )
        }
        if (pet.authorId != user?.id) {
            NotificationRepository.add(
                userId = pet.authorId,
                title = "Nuevo comentario",
                message = "$author comentó en tu publicación \"${pet.name}\"",
                petId = pet.id
            )
        }
    }

    fun onFabClick() {
        _uiState.update { it.copy(showPublishDialog = true) }
    }

    fun onDismissPublishDialog() {
        _uiState.update { it.copy(showPublishDialog = false) }
    }
}