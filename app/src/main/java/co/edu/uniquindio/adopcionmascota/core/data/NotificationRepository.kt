package co.edu.uniquindio.adopcionmascota.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class AppNotification(
    val id: String,
    val userId: String,        // a quién le llega
    val title: String,
    val message: String,
    val petId: String? = null, // publicación relacionada
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    // Datos de contacto de quien envió la solicitud (solo en solicitudes)
    val contactName: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null
)

object NotificationRepository {

    private val _all = MutableStateFlow<List<AppNotification>>(emptyList())
    val all: StateFlow<List<AppNotification>> = _all.asStateFlow()

    fun add(
        userId: String,
        title: String,
        message: String,
        petId: String?,
        contactName: String? = null,
        contactEmail: String? = null,
        contactPhone: String? = null
    ) {
        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            message = message,
            petId = petId,
            contactName = contactName,
            contactEmail = contactEmail,
            contactPhone = contactPhone
        )
        _all.update { listOf(notification) + it }
    }

    fun markRead(id: String) {
        _all.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
    }

    fun toggleRead(id: String) {
        _all.update { list -> list.map { if (it.id == id) it.copy(isRead = !it.isRead) else it } }
    }

    fun markAllRead(userId: String) {
        _all.update { list -> list.map { if (it.userId == userId) it.copy(isRead = true) else it } }
    }

    fun deleteForUser(userId: String) {
        _all.update { list -> list.filter { it.userId != userId } }
    }
}