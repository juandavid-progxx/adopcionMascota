package co.edu.uniquindio.adopcionmascota.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.adopcionmascota.core.data.AppNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOpenPet: (String) -> Unit = {},
    onOpenRequester: (String) -> Unit = {},
    onOpenAchievements: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones (${state.unreadCount} sin leer)", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.onMarkAllRead() }) { Text("Marcar todas") }
                }
            )
        }
    ) { padding ->
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes notificaciones", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(state.items, key = { it.id }) { n ->
                    NotificationItem(
                        n = n,
                        onClick = {
                            viewModel.onOpen(n)
                            when {
                                n.contactName != null -> onOpenRequester(n.id)   // solicitud: perfil de contacto
                                n.isAchievement -> onOpenAchievements()           // logro o nivel: pantalla de logros
                                else -> n.petId?.let { onOpenPet(it) }            // el resto: la publicación
                            }
                        },
                        onToggleRead = { viewModel.onToggleRead(n) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    n: AppNotification,
    onClick: () -> Unit,
    onToggleRead: () -> Unit
) {
    val dateText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(n.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (n.isRead) Color.White else Color(0xFFE0F7F3)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!n.isRead) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF00BFA5)))
                Spacer(Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    n.title,
                    fontWeight = if (n.isRead) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(n.message, fontSize = 13.sp, color = Color.DarkGray)
                Text(dateText, fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(onClick = onToggleRead) {
                Icon(
                    imageVector = if (n.isRead) Icons.Default.MarkEmailUnread else Icons.Default.MarkEmailRead,
                    contentDescription = if (n.isRead) "Marcar como no leída" else "Marcar como leída"
                )
            }
        }
    }
}