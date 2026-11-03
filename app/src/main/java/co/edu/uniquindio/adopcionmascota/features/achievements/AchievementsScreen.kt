package co.edu.uniquindio.adopcionmascota.features.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.adopcionmascota.core.data.Badge
import co.edu.uniquindio.adopcionmascota.core.data.UserLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logros y reputación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF4F7F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Niveles ----
            Title("Niveles de usuario")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserLevel.values().forEach { level ->
                    LevelCard(level, isCurrent = level == state.level, modifier = Modifier.weight(1f))
                }
            }

            // ---- Nivel actual ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB2EBE0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${state.level.emoji} ${state.level.displayName} · ${state.points} pts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40)
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { state.level.progress(state.points) },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFF00695C),
                        trackColor = Color(0x3300695C)
                    )
                    Spacer(Modifier.height(6.dp))
                    val next = state.level.next()
                    Text(
                        text = if (next != null) "${next.minPoints - state.points} pts para ${next.displayName}"
                        else "¡Alcanzaste el nivel máximo!",
                        fontSize = 14.sp,
                        color = Color(0xFF004D40)
                    )
                }
            }

            // ---- Cómo ganar puntos ----
            Title("Cómo ganar puntos")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    PointsRow("Crear publicación", "+10 pts")
                    HorizontalDivider()
                    PointsRow("Publicación verificada", "+25 pts")
                    HorizontalDivider()
                    PointsRow("Publicación resuelta", "+50 pts")
                    HorizontalDivider()
                    PointsRow("Comentar", "+5 pts")
                    HorizontalDivider()
                    PointsRow("Recibir ❤️ (Es importante)", "+2 pts")
                }
            }

            // ---- Insignias ----
            Title("Insignias (${state.unlocked.size}/${Badge.values().size})")
            Badge.values().toList().chunked(3).forEach { rowBadges ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowBadges.forEach { badge ->
                        BadgeCard(
                            badge = badge,
                            unlocked = badge in state.unlocked,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Title(text: String) {
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
}

@Composable
private fun LevelCard(level: UserLevel, isCurrent: Boolean, modifier: Modifier = Modifier) {
    val next = level.next()
    val range = if (next != null) "${level.minPoints}–${next.minPoints - 1}" else "${level.minPoints}+"
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCurrent) Color(0xFFB2EBE0) else Color(0xFFE8F0EC))
            .then(
                if (isCurrent) Modifier.border(2.dp, Color(0xFF00695C), RoundedCornerShape(16.dp))
                else Modifier
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(level.emoji, fontSize = 28.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            level.displayName,
            fontSize = 12.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Text(range, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun PointsRow(label: String, points: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 15.sp)
        Text(points, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00695C))
    }
}

@Composable
private fun BadgeCard(badge: Badge, unlocked: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .alpha(if (unlocked) 1f else 0.5f)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(badge.emoji, fontSize = 34.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            badge.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            badge.description,
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        if (!unlocked) {
            Spacer(Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFE0E0E0)) {
                Text(
                    "Bloqueada",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}