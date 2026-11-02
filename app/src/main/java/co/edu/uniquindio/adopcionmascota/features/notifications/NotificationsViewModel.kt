package co.edu.uniquindio.adopcionmascota.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.adopcionmascota.core.data.AppNotification
import co.edu.uniquindio.adopcionmascota.core.data.NotificationRepository
import co.edu.uniquindio.adopcionmascota.core.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val items: List<AppNotification> = emptyList()
) {
    val unreadCount: Int get() = items.count { !it.isRead }
}

class NotificationsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            NotificationRepository.all.collect { all ->
                val me = SessionManager.currentUser?.id
                _uiState.update { it.copy(items = all.filter { n -> n.userId == me }) }
            }
        }
    }

    fun onOpen(notification: AppNotification) {
        NotificationRepository.markRead(notification.id)
    }

    fun onToggleRead(notification: AppNotification) {
        NotificationRepository.toggleRead(notification.id)
    }

    fun onMarkAllRead() {
        SessionManager.currentUser?.id?.let { NotificationRepository.markAllRead(it) }
    }
}