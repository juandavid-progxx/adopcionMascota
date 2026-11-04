package co.edu.uniquindio.adopcionmascota.core.data

import co.edu.uniquindio.adopcionmascota.features.feed.Pet
import co.edu.uniquindio.adopcionmascota.features.feed.PostStatus

data class UserStats(
    val posts: Int = 0,
    val verified: Int = 0,
    val resolved: Int = 0,
    val comments: Int = 0,
    val votesReceived: Int = 0
) {
    val points: Int
        get() = posts * 10 + verified * 25 + resolved * 50 + comments * 5 + votesReceived * 2
}

// Umbrales de cada nivel. Cámbialos aquí si quieres otros rangos.
enum class UserLevel(val displayName: String, val emoji: String, val minPoints: Int) {
    FRIEND("Amigo Animal", "🐾", 0),
    PROTECTOR("Protector", "🛡️", 100),
    GUARDIAN("Guardián", "🥇", 300),
    HERO("Héroe de las Mascotas", "👑", 700);

    val number: Int get() = ordinal + 1

    fun next(): UserLevel? = values().getOrNull(ordinal + 1)

    fun progress(points: Int): Float {
        val n = next() ?: return 1f
        return ((points - minPoints).toFloat() / (n.minPoints - minPoints)).coerceIn(0f, 1f)
    }

    companion object {
        fun forPoints(points: Int): UserLevel = values().last { points >= it.minPoints }
    }
}

enum class Badge(
    val title: String,
    val emoji: String,
    val description: String,
    val isUnlocked: (UserStats, UserLevel) -> Boolean
) {
    FIRST_POST("Primera publicación", "🐾", "Publicaste tu primera mascota", { s, _ -> s.posts >= 1 }),
    COMMENTER("Comentarista", "💬", "5 comentarios realizados", { s, _ -> s.comments >= 5 }),
    FEATURED("Favorito del mes", "❤️", "Publicación destacada por la comunidad", { s, _ -> s.votesReceived >= 1 }),
    TEN_VERIFIED("10 Verificadas", "🏆", "10 publicaciones verificadas", { s, _ -> s.verified >= 10 }),
    RESCUER("Súper Rescatador", "🌟", "3 publicaciones resueltas", { s, _ -> s.resolved >= 3 }),
    HERO_ANIMAL("Héroe Animal", "🦸", "Nivel 4 alcanzado", { _, l -> l == UserLevel.HERO })
}

object ReputationManager {

    private val unlockedBadges = mutableMapOf<String, MutableSet<Badge>>()
    private val lastLevel = mutableMapOf<String, UserLevel>()

    fun statsFor(userId: String, pets: List<Pet>): UserStats {
        val mine = pets.filter { it.authorId == userId }
        return UserStats(
            posts = mine.count { it.status != PostStatus.REJECTED },
            verified = mine.count { it.status == PostStatus.VERIFIED || it.status == PostStatus.RESOLVED },
            resolved = mine.count { it.status == PostStatus.RESOLVED },
            comments = pets.sumOf { p -> p.comments.count { it.authorId == userId } },
            votesReceived = mine.sumOf { it.importantVotes }
        )
    }

    // Se llama cada vez que cambian las publicaciones: detecta niveles e insignias nuevas
    fun checkAll() {
        val pets = PetRepository.pets.value
        val ids = (pets.map { it.authorId } + pets.flatMap { p -> p.comments.map { it.authorId } })
            .filter { it.isNotBlank() && it != "demo" }
            .toSet()
        ids.forEach { check(it, pets) }
    }

    private fun check(userId: String, pets: List<Pet>) {
        val stats = statsFor(userId, pets)
        val level = UserLevel.forPoints(stats.points)

        val previous = lastLevel[userId]
        if (previous != null && level.ordinal > previous.ordinal) {
            NotificationRepository.add(
                userId = userId,
                title = "¡Subiste de nivel!",
                message = "Ahora eres ${level.displayName} ${level.emoji}. ¡Sigue ayudando a las mascotas!",
                petId = null,
                isAchievement = true
            )
        }
        lastLevel[userId] = level

        val unlocked = unlockedBadges.getOrPut(userId) { mutableSetOf() }
        Badge.values().forEach { badge ->
            if (badge !in unlocked && badge.isUnlocked(stats, level)) {
                unlocked.add(badge)
                NotificationRepository.add(
                    userId = userId,
                    title = "¡Nuevo logro desbloqueado!",
                    message = "Obtuviste la insignia \"${badge.title}\" ${badge.emoji}. ¡Sigue así!",
                    petId = null,
                    isAchievement = true
                )
            }
        }
    }
}