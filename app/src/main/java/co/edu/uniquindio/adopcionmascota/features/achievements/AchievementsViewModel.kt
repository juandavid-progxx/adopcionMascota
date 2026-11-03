package co.edu.uniquindio.adopcionmascota.features.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.adopcionmascota.core.data.Badge
import co.edu.uniquindio.adopcionmascota.core.data.PetRepository
import co.edu.uniquindio.adopcionmascota.core.data.ReputationManager
import co.edu.uniquindio.adopcionmascota.core.data.SessionManager
import co.edu.uniquindio.adopcionmascota.core.data.UserLevel
import co.edu.uniquindio.adopcionmascota.core.data.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AchievementsUiState(
    val stats: UserStats = UserStats(),
    val level: UserLevel = UserLevel.FRIEND
) {
    val points: Int get() = stats.points
    val unlocked: List<Badge>
        get() = Badge.values().filter { it.isUnlocked(stats, level) }
}

class AchievementsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PetRepository.pets.collect { all ->
                val myId = SessionManager.currentUser?.id ?: ""
                val stats = ReputationManager.statsFor(myId, all)
                _uiState.value = AchievementsUiState(
                    stats = stats,
                    level = UserLevel.forPoints(stats.points)
                )
            }
        }
    }
}