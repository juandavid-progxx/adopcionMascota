package co.edu.uniquindio.adopcionmascota.features.feed

enum class PetType(val displayName: String, val emoji: String) {
    ALL("Todos", "🐾"),
    DOG("Perros", "🐶"),
    CAT("Gatos", "🐱"),
    RABBIT("Conejos", "🐰"),
    BIRD("Aves", "🦜"),
    OTHER("Otros", "🐾")
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
enum class PostStatus(val displayName: String) {
    PENDING("Pendiente"),
    VERIFIED("Verificada"),
    REJECTED("Rechazada"),
    RESOLVED("Resuelta")
}

data class Comment(
    val id: String,
    val authorName: String,
    val text: String
)
enum class PetCategory(
    val displayName: String,
    val emoji: String,
    val shortLabel: String,      // texto del botón en la tarjeta
    val actionLabel: String,     // texto del botón en el detalle
    val requestMessage: String   // texto del cuadro de confirmación
) {
    ADOPTION(
        "Adopción", "🏠", "Adoptar", "Me interesa adoptar",
        "¿Deseas enviar tu solicitud de adopción para {name}? El autor de la publicación la recibirá y se pondrá en contacto contigo."
    ),
    LOST(
        "Perdidos", "🔍", "Tengo info", "Tengo información",
        "¿Tienes información sobre {name}? Avisaremos al dueño para que se comunique contigo."
    ),
    FOUND(
        "Encontrados", "📍", "Es mía", "Creo que es mi mascota",
        "¿Crees que {name} es tu mascota? Avisaremos a quien la encontró para que te contacte."
    ),
    TEMPORARY(
        "Temporal", "🤝", "Ofrecer hogar", "Ofrezco hogar de paso",
        "¿Quieres ofrecer un hogar de paso para {name}? El autor recibirá tu ofrecimiento."
    ),
    VETERINARY(
        "Veterinaria", "💉", "Me interesa", "Me interesa",
        "¿Te interesa \"{name}\"? Avisaremos al organizador."
    )
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
    val colorHex: Long = 0xFF80CBC4,
    val authorId: String = "demo",
    val status: PostStatus = PostStatus.VERIFIED,
    val rejectionReason: String? = null,
    val importantVotes: Int = 0,
    val votedByMe: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val category: PetCategory = PetCategory.ADOPTION
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
    ),
    Pet(
        id = "6",
        name = "Rocky",
        type = PetType.DOG,
        breed = "Labrador",
        age = "4 años",
        gender = "Macho",
        size = "Grande",
        city = "Armenia, Quindío",
        locationFilter = LocationFilter.IN_CITY,
        description = "Se perdió cerca del parque Sucre el domingo. Tiene collar azul y responde a su nombre.",
        healthInfo = listOf("Vacunas al día"),
        shelterName = "Carlos Gómez",
        shelterContact = "+57 311 222 3344",
        category = PetCategory.LOST,
        colorHex = 0xFFFFCCBC
    ),
    Pet(
        id = "7",
        name = "Gato atigrado",
        type = PetType.CAT,
        breed = "Mestizo",
        age = "Adulto",
        gender = "Macho",
        size = "Mediano",
        city = "Armenia, Quindío",
        locationFilter = LocationFilter.NEARBY,
        description = "Lo encontré en el barrio Centenario, muy manso y con collar rojo sin placa.",
        healthInfo = emptyList(),
        shelterName = "María Rojas",
        shelterContact = "+57 314 777 8899",
        category = PetCategory.FOUND,
        colorHex = 0xFFFFF9C4
    ),
    Pet(
        id = "8",
        name = "Jornada de vacunación gratuita",
        type = PetType.OTHER,
        breed = "Todas las razas",
        age = "No aplica",
        gender = "No aplica",
        size = "No aplica",
        city = "Calarcá, Quindío",
        locationFilter = LocationFilter.NEARBY,
        description = "Vacunación antirrábica y esterilización gratuita para perros y gatos este sábado en el parque principal.",
        healthInfo = listOf("Vacunación gratuita", "Esterilización"),
        shelterName = "Secretaría de Salud",
        shelterContact = "+57 606 123 4567",
        category = PetCategory.VETERINARY,
        colorHex = 0xFFBBDEFB
    ),
    Pet(
        id = "9",
        name = "Nala",
        type = PetType.DOG,
        breed = "Criolla",
        age = "1 año",
        gender = "Hembra",
        size = "Mediano",
        city = "Armenia, Quindío",
        locationFilter = LocationFilter.IN_CITY,
        description = "Necesita un hogar de paso por 3 semanas mientras se recupera de una cirugía.",
        healthInfo = listOf("Vacunas al día", "Esterilizada"),
        shelterName = "Hogar de Paso Patitas",
        shelterContact = "+57 316 555 0000",
        category = PetCategory.TEMPORARY,
        colorHex = 0xFFD7CCC8
    )


)
fun PetCategory.resolveLabel(): String = when (this) {
    PetCategory.ADOPTION -> "Marcar como adoptada"
    PetCategory.LOST -> "Marcar como encontrada"
    PetCategory.FOUND -> "Marcar como entregada"
    PetCategory.TEMPORARY -> "Marcar hogar encontrado"
    PetCategory.VETERINARY -> "Marcar como finalizada"
}
