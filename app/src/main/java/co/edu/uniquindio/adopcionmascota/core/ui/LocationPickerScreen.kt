package co.edu.uniquindio.adopcionmascota.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.adopcionmascota.core.data.MapboxGeocoder
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.launch

// Armenia, Quindío (punto de partida del mapa)
const val DEFAULT_LAT = 4.5339
const val DEFAULT_LNG = -75.6811

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    title: String,
    initialLat: Double? = null,
    initialLng: Double? = null,
    onConfirm: (address: String, lat: Double, lng: Double) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selected by remember {
        mutableStateOf<Point?>(
            if (initialLat != null && initialLng != null) Point.fromLngLat(initialLng, initialLat) else null
        )
    }
    var address by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // Si ya había una ubicación, mostrar su dirección
    LaunchedEffect(Unit) {
        selected?.let { p ->
            loading = true
            address = MapboxGeocoder.reverse(context, p.latitude(), p.longitude())
            loading = false
        }
    }

    val startPoint = selected ?: Point.fromLngLat(DEFAULT_LNG, DEFAULT_LAT)
    val viewport = rememberMapViewportState {
        setCameraOptions {
            center(startPoint)
            zoom(14.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = viewport,
                    onMapClickListener = OnMapClickListener { point ->
                        selected = point
                        loading = true
                        scope.launch {
                            address = MapboxGeocoder.reverse(context, point.latitude(), point.longitude())
                            loading = false
                        }
                        true
                    }
                ) {
                    selected?.let { p ->
                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(p)
                                annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                            }
                        ) {
                            Text("📍", fontSize = 40.sp)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "Toca el mapa para marcar el punto",
                        fontSize = 13.sp,
                        color = Color(0xFF004D40),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Surface(shadowElevation = 8.dp, color = Color.White) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = when {
                            selected == null -> "Aún no has elegido un punto"
                            loading -> "Buscando la dirección..."
                            else -> "📍 $address"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF004D40)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            selected?.let {
                                onConfirm(address, it.latitude(), it.longitude())
                            }
                        },
                        enabled = selected != null && !loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                    ) {
                        Text("Confirmar ubicación", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}