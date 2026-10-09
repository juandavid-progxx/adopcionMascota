package co.edu.uniquindio.adopcionmascota.features.feed

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.adopcionmascota.core.util.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FeedUiState(
    val searchQuery: String = "",
    val selectedPetType: PetType = PetType.ALL,
    val selectedLocation: LocationFilter = LocationFilter.ALL_COUNTRY,
    val viewMode: ViewMode = ViewMode.LIST,
    val selectedBottomNavItem: BottomNavItem = BottomNavItem.HOME,
    val pets: List<Pet> = samplePets,
    val filteredPets: List<Pet> = samplePets,
    val selectedPetForDetail: Pet? = null,
    val adoptionDialogPet: Pet? = null,
    val adoptionSuccessMessage: String? = null,
    val showPublishDialog: Boolean = false,
    val requestResult: RequestResult? = null
)

class FeedScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val newFiltered = filterPets(
                pets = state.pets,
                query = query,
                type = state.selectedPetType,
                location = state.selectedLocation
            )
            state.copy(
                searchQuery = query,
                filteredPets = newFiltered
            )
        }
    }

    fun onPetTypeSelected(type: PetType) {
        _uiState.update { state ->
            val newFiltered = filterPets(
                pets = state.pets,
                query = state.searchQuery,
                type = type,
                location = state.selectedLocation
            )
            state.copy(
                selectedPetType = type,
                filteredPets = newFiltered
            )
        }
    }

    fun onLocationSelected(location: LocationFilter) {
        _uiState.update { state ->
            val newFiltered = filterPets(
                pets = state.pets,
                query = state.searchQuery,
                type = state.selectedPetType,
                location = location
            )
            state.copy(
                selectedLocation = location,
                filteredPets = newFiltered
            )
        }
    }

    fun onViewModeSelected(mode: ViewMode) {
        _uiState.update { state ->
            state.copy(viewMode = mode)
        }
    }

    fun onBottomNavItemSelected(item: BottomNavItem) {
        _uiState.update { state ->
            if (item == BottomNavItem.MAP) {
                state.copy(selectedBottomNavItem = item, viewMode = ViewMode.MAP)
            } else if (item == BottomNavItem.HOME) {
                state.copy(selectedBottomNavItem = item, viewMode = ViewMode.LIST)
            } else {
                state.copy(selectedBottomNavItem = item)
            }
        }
    }

    fun onPetSelectedForDetail(pet: Pet) {
        _uiState.update { state ->
            state.copy(selectedPetForDetail = pet)
        }
    }

    fun onCloseDetail() {
        _uiState.update { state ->
            state.copy(selectedPetForDetail = null)
        }
    }

    fun onAdoptClick(pet: Pet) {
        _uiState.update { state ->
            state.copy(adoptionDialogPet = pet)
        }
    }

    fun onDismissAdoptionDialog() {
        _uiState.update { state ->
            state.copy(adoptionDialogPet = null)
        }
    }

    fun onConfirmAdoption(pet: Pet) {
        _uiState.update { state ->
            state.copy(
                adoptionDialogPet = null,
                adoptionSuccessMessage = "¡Solicitud de adopción enviada con éxito para ${pet.name}! El refugio se pondrá en contacto contigo pronto.",
                requestResult = RequestResult.Success("Proceso de adopción iniciado")
            )
        }
    }

    fun onDismissSuccessMessage() {
        _uiState.update { state ->
            state.copy(adoptionSuccessMessage = null, requestResult = null)
        }
    }

    fun onToggleFavorite(pet: Pet) {
        _uiState.update { state ->
            val updatedPets = state.pets.map {
                if (it.id == pet.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            val newFiltered = filterPets(
                pets = updatedPets,
                query = state.searchQuery,
                type = state.selectedPetType,
                location = state.selectedLocation
            )
            val updatedSelectedDetail = if (state.selectedPetForDetail?.id == pet.id) {
                state.selectedPetForDetail.copy(isFavorite = !state.selectedPetForDetail.isFavorite)
            } else state.selectedPetForDetail

            state.copy(
                pets = updatedPets,
                filteredPets = newFiltered,
                selectedPetForDetail = updatedSelectedDetail
            )
        }
    }

    fun onFabClick() {
        _uiState.update { state ->
            state.copy(showPublishDialog = true)
        }
    }

    fun onDismissPublishDialog() {
        _uiState.update { state ->
            state.copy(showPublishDialog = false)
        }
    }

    private fun filterPets(
        pets: List<Pet>,
        query: String,
        type: PetType,
        location: LocationFilter
    ): List<Pet> {
        return pets.filter { pet ->
            val matchesType = type == PetType.ALL || pet.type == type
            val matchesLocation = location == LocationFilter.ALL_COUNTRY || pet.locationFilter == location
            val matchesQuery = query.isBlank() ||
                    pet.name.contains(query, ignoreCase = true) ||
                    pet.breed.contains(query, ignoreCase = true) ||
                    pet.city.contains(query, ignoreCase = true) ||
                    pet.description.contains(query, ignoreCase = true)

            matchesType && matchesLocation && matchesQuery
        }
    }
}
