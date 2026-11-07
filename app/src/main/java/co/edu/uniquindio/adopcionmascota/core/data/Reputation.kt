package co.edu.uniquindio.adopcionmascota.core.data

import co.edu.uniquindio.adopcionmascota.features.feed.Pet
import co.edu.uniquindio.adopcionmascota.features.feed.PostStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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

    private var loadedFor: String? = null
    private var knownBadges = mutableSetOf<String>()
    private var knownLevel: UserLevel? = null

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

    fun reset() {
        loadedFor = null
        knownBadges = mutableSetOf()
        knownLevel = null
    }

    // Evalúa solo al usuario actual y guarda en su perfil lo que ya se le notificó
    fun checkCurrentUser() {
        val me = SessionManager.currentUser ?: return
        if (me.isModerator) return

        if (loadedFor != me.id) {
            loadedFor = me.id
            knownBadges = me.badges.toMutableSet()
            knownLevel = UserLevel.values().find { it.name == me.level }
        }

        val stats = statsFor(me.id, PetRepository.pets.value)
        val level = UserLevel.forPoints(stats.points)
        val userRef = FirebaseFirestore.getInstance().collection("users").document(me.id)

        val previous = knownLevel
        if (previous == null || level.ordinal > previous.ordinal) {
            if (previous != null) {
                NotificationRepository.add(
                    userId = me.id,
                    title = "¡Subiste de nivel!",
                    message = "Ahora eres ${level.displayName} ${level.emoji}. ¡Sigue ayudando a las mascotas!",
                    petId = null,
                    isAchievement = true
                )
            }
            knownLevel = level
            userRef.update("level", level.name)
        }

        Badge.values().forEach { badge ->
            if (badge.name !in knownBadges && badge.isUnlocked(stats, level)) {
                knownBadges.add(badge.name)
                NotificationRepository.add(
                    userId = me.id,
                    title = "¡Nuevo logro desbloqueado!",
                    message = "Obtuviste la insignia \"${badge.title}\" ${badge.emoji}. ¡Sigue así!",
                    petId = null,
                    isAchievement = true
                )
                userRef.update("badges", FieldValue.arrayUnion(badge.name))
            }
        }
    }
}