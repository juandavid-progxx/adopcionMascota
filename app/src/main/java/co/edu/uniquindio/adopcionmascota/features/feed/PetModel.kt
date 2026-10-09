package co.edu.uniquindio.adopcionmascota.features.feed

enum class PetType(val displayName: String, val emoji: String) {
    ALL("Todos", "🐾"),
    DOG("Perros", "🐶"),
    CAT("Gatos", "🐱"),
    RABBIT("Conejos", "🐰")
}

enum class LocationFilter(val displayName: String, val iconText: String) {
    NEARBY("Cercanos", "📍"),
    IN_CITY("En la ciudad", "🏢"),
    ALL_COUNTRY("Todo el país", "🇨🇴")
}

enum class ViewMode {
    LIST,
    MAP
}

enum class BottomNavItem(val title: String) {
    HOME("Inicio"),
    MAP("Mapa"),
    ALERTS("Alertas"),
    PROFILE("Perfil")
}

data class Pet(
    val id: String,
    val name: String,
    val type: PetType,
    val breed: String,
    val age: String,
    val gender: String, // "Macho" o "Hembra"
    val size: String, // "Pequeño", "Mediano", "Grande"
    val city: String,
    val locationFilter: LocationFilter,
    val description: String,
    val healthInfo: List<String>, // e.g. ["Vacunado", "Esterilizado", "Desparasitado"]
    val shelterName: String,
    val shelterContact: String,
    val isFavorite: Boolean = false,
    val colorHex: Long = 0xFF80CBC4
)

val samplePets = listOf(
    Pet(
        id = "1",
        name = "Max",
        type = PetType.DOG,
        breed = "Golden Retriever",
        age = "2 años",
        gender = "Macho",
        size = "Grande",
        city = "Armenia, Quindío",
        locationFilter = LocationFilter.IN_CITY,
        description = "Max es un perrito muy juguetón, sociable y amigable con los niños. Le encanta correr en el parque y aprender trucos nuevos.",
        healthInfo = listOf("Vacunas al día", "Esterilizado", "Desparasitado"),
        shelterName = "Fundación Huellas de Amor",
        shelterContact = "+57 310 123 4567",
        colorHex = 0xFFFFE082
    ),
    Pet(
        id = "2",
        name = "Luna",
        type = PetType.CAT,
        breed = "Siamés",
        age = "1 año",
        gender = "Hembra",
        size = "Pequeño",
        city = "Armenia, Quindío",
        locationFilter = LocationFilter.IN_CITY,
        description = "Luna es una gatita tranquila y cariñosa. Busca un hogar cálido donde le den muchos mimos y descanso.",
        healthInfo = listOf("Vacunas al día", "Esterilizada", "Microchip"),
        shelterName = "Refugio Felino Quindío",
        shelterContact = "+57 312 987 6543",
        colorHex = 0xFFB2DFDB
    ),
    Pet(
        id = "3",
        name = "Tambor",
        type = PetType.RABBIT,
        breed = "Cabeza de León",
        age = "6 meses",
        gender = "Macho",
        size = "Pequeño",
        city = "Calarca, Quindío",
        locationFilter = LocationFilter.NEARBY,
        description = "Tambor es un conejito tierno y manso. Le encanta comer verduras frescas y convivir en espacios limpios y amigables.",
        healthInfo = listOf("Chequeo veterinario al día", "Desparasitado"),
        shelterName = "Hogar de Paso Orejitas",
        shelterContact = "+57 315 555 7890",
        colorHex = 0xFFC8E6C9
    ),
    Pet(
        id = "4",
        name = "Milo",
        type = PetType.DOG,
        breed = "Criollo / Mestizo",
        age = "3 años",
        gender = "Macho",
        size = "Mediano",
        city = "Bogotá, D.C.",
        locationFilter = LocationFilter.ALL_COUNTRY,
        description = "Milo fue rescatado de la calle y es muy agradecido. Es ideal para familias activas que disfruten dar paseos diarios.",
        healthInfo = listOf("Vacunas al día", "Esterilizado"),
        shelterName = "Amigos de Cuatro Patas",
        shelterContact = "+57 320 444 1122",
        colorHex = 0xFFD1C4E9
    ),
    Pet(
        id = "5",
        name = "Bella",
        type = PetType.CAT,
        breed = "Persa Mestizo",
        age = "5 meses",
        gender = "Hembra",
        size = "Pequeño",
        city = "Medellín, Antioquia",
        locationFilter = LocationFilter.ALL_COUNTRY,
        description = "Bella es una gatita juguetona y curiosa a la que le encantan los juguetes de plumas.",
        healthInfo = listOf("Primera dosis de vacunas", "Desparasitada"),
        shelterName = "Fundación Adopta Un Amigo",
        shelterContact = "+57 300 888 9900",
        colorHex = 0xFFF8BBD0
    )
)
