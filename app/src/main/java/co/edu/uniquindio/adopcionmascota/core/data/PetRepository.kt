package co.edu.uniquindio.adopcionmascota.core.data

import co.edu.uniquindio.adopcionmascota.features.feed.Pet
import co.edu.uniquindio.adopcionmascota.features.feed.samplePets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object PetRepository {

    private val _pets = MutableStateFlow(samplePets)
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    fun add(pet: Pet) {
        _pets.update { listOf(pet) + it }
        ReputationManager.checkAll()
    }

    fun update(id: String, transform: (Pet) -> Pet) {
        _pets.update { list -> list.map { if (it.id == id) transform(it) else it } }
        ReputationManager.checkAll()
    }

    fun delete(id: String) {
        _pets.update { list -> list.filter { it.id != id } }
    }

    fun deleteByAuthor(authorId: String) {
        _pets.update { list -> list.filter { it.authorId != authorId } }
    }
}