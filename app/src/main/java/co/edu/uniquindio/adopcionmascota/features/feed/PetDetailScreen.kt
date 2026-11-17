package co.edu.uniquindio.adopcionmascota.features.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import co.edu.uniquindio.adopcionmascota.core.ui.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.adopcionmascota.core.data.SessionManager
import co.edu.uniquindio.adopcionmascota.core.data.cloudThumb
import co.edu.uniquindio.adopcionmascota.core.ui.FullScreenGallery
import coil3.compose.AsyncImage
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import co.edu.uniquindio.adopcionmascota.core.ui.GuideHelpButton
import co.edu.uniquindio.adopcionmascota.core.ui.ScreenGuideDialog
import co.edu.uniquindio.adopcionmascota.core.ui.rememberGuideState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PetDetailScreen(
    pet: Pet,
    onBack: () -> Unit,
    onAdoptClick: (Pet) -> Unit,
    onToggleFavorite: (Pet) -> Unit,
    onVote: (Pet) -> Unit = {},
    onComment: (Pet, String) -> Unit = { _, _ -> }
) {
    val isOwner = pet.authorId == SessionManager.currentUser?.id
    val guide = rememberGuideState("pet_detail")
    var viewerIndex by remember { mutableStateOf<Int?>(null) }

    ScreenGuideDialog(guide, "Detalle de la publicación", listOf(
        "Toca una foto para ampliarla.",
        "Revisa los datos, la salud y la ubicación en el mapa.",
        "Usa el botón de acción (por ejemplo \"Me interesa adoptar\") para contactar al autor.",
        "Puedes marcarla como favorita o como importante.",
        "Deja un comentario al final de la publicación."
    ))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = pet.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    GuideHelpButton { guide.open() }
                    IconButton(onClick = { onToggleFavorite(pet) }) {
                        Icon(
                            imageVector = if (pet.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (pet.isFavorite) Color.Red else Color.DarkGray
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F4F2))
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Publicado por", fontSize = 12.sp, color = Color.Gray)
                        Text(pet.shelterName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = { onAdoptClick(pet) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isOwner) pet.category.resolveLabel() else pet.category.actionLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF0F4F2))
                .verticalScroll(rememberScrollState())
        ) {
            // Cabecera: fotos (toca una para verla en pantalla completa)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(pet.colorHex))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(pet.type.emoji, fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(pet.breed, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                }

                if (pet.imageUrls.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { pet.imageUrls.size })
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model = pet.imageUrls[page].cloudThumb(1000),
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clickable { viewerIndex = page }
                        )
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = if (pet.imageUrls.size > 1)
                                "${pagerState.currentPage + 1} / ${pet.imageUrls.size} · toca para ampliar"
                            else "Toca para ampliar",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = pet.gender,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF004D40)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pet.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(pet.city, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                    if (pet.age != "No aplica") {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFB2DFDB)) {
                            Text(
                                pet.age,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF004D40)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (pet.category != PetCategory.VETERINARY) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoCard("Especie", pet.type.displayName, Modifier.weight(1f))
                        InfoCard("Tamaño", pet.size, Modifier.weight(1f))
                        InfoCard("Género", pet.gender, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Text("Sobre ${pet.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                Spacer(modifier = Modifier.height(8.dp))
                Text(pet.description, fontSize = 15.sp, color = Color.DarkGray, lineHeight = 22.sp)
                Spacer(modifier = Modifier.height(20.dp))

                // Veterinaria: tipo de servicio, horarios y teléfonos
                if (pet.category == PetCategory.VETERINARY) {
                    VetKind.values().find { it.name == pet.vetKind }?.let { kind ->
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE3F2FD)) {
                            Text(
                                "${kind.emoji} ${kind.displayName}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (pet.schedule.isNotEmpty()) {
                        Text("Horarios de atención", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        Spacer(modifier = Modifier.height(6.dp))
                        pet.schedule.forEach { Text("🕗 $it", fontSize = 15.sp, color = Color.DarkGray) }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (pet.phones.isNotEmpty()) {
                        Text("Teléfonos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        Spacer(modifier = Modifier.height(6.dp))
                        pet.phones.forEach { Text("📞 $it", fontSize = 15.sp, color = Color.DarkGray) }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (pet.healthInfo.isNotEmpty()) {
                    Text("Estado de Salud", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pet.healthInfo.forEach { info ->
                            AssistChip(
                                onClick = { },
                                label = { Text("✓ $info") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFE8F5E9),
                                    labelColor = Color(0xFF2E7D32)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Datos de quien publicó: nombre, correo y teléfono
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFB2DFDB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF004D40))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Publicado por", fontSize = 12.sp, color = Color.Gray)
                            Text(pet.shelterName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (pet.shelterContact.isNotBlank()) {
                                Text("✉️ ${pet.shelterContact}", fontSize = 14.sp, color = Color.DarkGray)
                            }
                            if (pet.authorPhone.isNotBlank()) {
                                Text("📞 ${pet.authorPhone}", fontSize = 14.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                val lat = pet.latitude
                val lng = pet.longitude
                if (lat != null && lng != null) {
                    Text("Ubicación", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("📍 ${pet.address.ifBlank { pet.city }}", fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    PetLocationMap(lat = lat, lng = lng)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                CommentsSection(pet = pet, onVote = onVote, onComment = onComment)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    viewerIndex?.let { index ->
        FullScreenGallery(images = pet.imageUrls, startIndex = index, onDismiss = { viewerIndex = null })
    }
}

@Composable
private fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
        }
    }
}

@Composable
private fun PetLocationMap(lat: Double, lng: Double) {
    val point = Point.fromLngLat(lng, lat)
    val viewport = rememberMapViewportState {
        setCameraOptions {
            center(point)
            zoom(15.0)
            pitch(0.0)
            bearing(0.0)
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))) {
        MapboxMap(modifier = Modifier.fillMaxSize(), mapViewportState = viewport) {
            ViewAnnotation(
                options = viewAnnotationOptions {
                    geometry(point)
                    annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                }
            ) {
                Text("📍", fontSize = 36.sp)
            }
        }
    }
}

@Composable
private fun CommentsSection(
    pet: Pet,
    onVote: (Pet) -> Unit,
    onComment: (Pet, String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { onVote(pet) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (pet.votedByMe) Color(0xFF00BFA5) else Color.White,
                contentColor = if (pet.votedByMe) Color.White else Color(0xFF004D40)
            )
        ) {
            Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = (if (pet.votedByMe) "Marcada como importante" else "Es importante") +
                        " · ${pet.importantVotes}",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Comentarios (${pet.comments.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (pet.comments.isEmpty()) {
            Text("Aún no hay comentarios. ¡Sé el primero!", color = Color.Gray, fontSize = 14.sp)
        } else {
            pet.comments.forEach { comment ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(comment.text, fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Escribe un comentario...") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { onComment(pet, text); text = "" },
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar comentario")
            }
        }
    }
}