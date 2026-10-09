package co.edu.uniquindio.adopcionmascota.features.createPosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.adopcionmascota.core.util.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PostCategory(val displayName: String, val emoji: String) {
    DOG("Perro", "🐕"),
    CAT("Gato", "🐈"),
    RABBIT("Conejo", "🐇"),
    BIRD("Ave", "🦜"),
    OTHER("Otro", "🐾")
}

enum class PetSize(val displayName: String, val emoji: String) {
    SMALL("Pequeño", "🐩"),
    MEDIUM("Mediano", "🐕"),
    LARGE("Grande", "🐕")
}

data class PostLocation(
    val address: String,
    val latitude: Double,
    val longitude: Double
)

data class CreatePostUiState(
    val photos: List<String> = listOf("foto_luna_1.jpg", "foto_luna_2.jpg"),
    val selectedCategory: PostCategory? = PostCategory.DOG,
    val title: String = "",
    val breed: String = "",
    val age: String = "",
    val selectedSize: PetSize? = PetSize.LARGE,
    val isVaccinated: Boolean = true,
    val description: String = "",
    val selectedLocation: PostLocation? = PostLocation("Chapinero, Bogotá", 4.658420, -74.065680),
    val showMapSelector: Boolean = false,
    val isPublishing: Boolean = false,
    val titleError: String? = null,
    val breedError: String? = null,
    val categoryError: String? = null,
    val sizeError: String? = null,
    val descriptionError: String? = null,
    val locationError: String? = null,
    val photosError: String? = null,
    val publishResult: RequestResult? = null
) {
    val maxDescriptionLength: Int = 500

    val isFormValid: Boolean
        get() = title.isNotBlank() &&
                breed.isNotBlank() &&
                selectedCategory != null &&
                selectedSize != null &&
                description.isNotBlank() &&
                selectedLocation != null &&
                photos.isNotEmpty() &&
                titleError == null &&
                breedError == null &&
                categoryError == null &&
                sizeError == null &&
                descriptionError == null &&
                locationError == null &&
                photosError == null
}

class CreatePostsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun onAddPhoto() {
        _uiState.update { state ->
            if (state.photos.size >= 5) return@update state
            val newPhotoName = "foto_mascota_${state.photos.size + 1}.jpg"
            val newPhotos = state.photos + newPhotoName
            state.copy(
                photos = newPhotos,
                photosError = if (newPhotos.isNotEmpty()) null else "Agrega al menos 1 foto"
            )
        }
    }

    fun onRemovePhoto(photo: String) {
        _uiState.update { state ->
            val newPhotos = state.photos.filter { it != photo }
            state.copy(
                photos = newPhotos,
                photosError = if (newPhotos.isEmpty()) "Debes incluir al menos una fotografía" else null
            )
        }
    }

    fun onCategorySelected(category: PostCategory) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                categoryError = null
            )
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { state ->
            state.copy(
                title = newTitle,
                titleError = if (newTitle.isBlank()) "El título es obligatorio" else null
            )
        }
    }

    fun onBreedChange(newBreed: String) {
        _uiState.update { state ->
            state.copy(
                breed = newBreed,
                breedError = if (newBreed.isBlank()) "La raza es obligatoria (puedes poner Mestiza)" else null
            )
        }
    }

    fun onAgeChange(newAge: String) {
        _uiState.update { state ->
            state.copy(age = newAge)
        }
    }

    fun onSizeSelected(size: PetSize) {
        _uiState.update { state ->
            state.copy(
                selectedSize = size,
                sizeError = null
            )
        }
    }

    fun onVaccinatedChange(vaccinated: Boolean) {
        _uiState.update { state ->
            state.copy(isVaccinated = vaccinated)
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { state ->
            if (newDescription.length > state.maxDescriptionLength) return@update state
            state.copy(
                description = newDescription,
                descriptionError = if (newDescription.isBlank()) "La descripción es obligatoria" else null
            )
        }
    }

    fun onOpenMapSelector() {
        _uiState.update { state ->
            state.copy(showMapSelector = true)
        }
    }

    fun onCloseMapSelector() {
        _uiState.update { state ->
            state.copy(showMapSelector = false)
        }
    }

    fun onSelectMapLocation(location: PostLocation) {
        _uiState.update { state ->
            state.copy(
                selectedLocation = location,
                locationError = null,
                showMapSelector = false
            )
        }
    }

    fun publishPost(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validación exhaustiva antes de publicar
        val titleErr = if (currentState.title.isBlank()) "El título es obligatorio" else null
        val breedErr = if (currentState.breed.isBlank()) "La raza es obligatoria" else null
        val categoryErr = if (currentState.selectedCategory == null) "Selecciona una categoría" else null
        val sizeErr = if (currentState.selectedSize == null) "Selecciona un tamaño" else null
        val descErr = if (currentState.description.isBlank()) "La descripción es obligatoria" else null
        val locErr = if (currentState.selectedLocation == null) "Selecciona una ubicación en el mapa" else null
        val photosErr = if (currentState.photos.isEmpty()) "Debes agregar al menos una foto" else null

        if (titleErr != null || breedErr != null || categoryErr != null || sizeErr != null ||
            descErr != null || locErr != null || photosErr != null
        ) {
            _uiState.update { state ->
                state.copy(
                    titleError = titleErr,
                    breedError = breedErr,
                    categoryError = categoryErr,
                    sizeError = sizeErr,
                    descriptionError = descErr,
                    locationError = locErr,
                    photosError = photosErr,
                    publishResult = RequestResult.Failure("Por favor completa los campos obligatorios (*)")
                )
            }
            return
        }

        // Inicia el proceso simulado de envío
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isPublishing = true,
                    publishResult = RequestResult.Loading
                )
            }

            delay(1200) // Simula la llamada a red / BD

            _uiState.update { state ->
                state.copy(
                    isPublishing = false,
                    publishResult = RequestResult.Success("¡Publicación creada exitosamente!")
                )
            }

            delay(500)
            onSuccess()
        }
    }

    fun resetResult() {
        _uiState.update { state ->
            state.copy(publishResult = null)
        }
    }
}
