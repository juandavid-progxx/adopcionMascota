package co.edu.uniquindio.adopcionmascota.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import co.edu.uniquindio.adopcionmascota.core.data.PlaceResult
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.delay
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

    var query by remember { mutableStateOf("") }
    var lastPicked by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<PlaceResult>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var searchedQuery by remember { mutableStateOf("") }

    val startPoint = selected ?: Point.fromLngLat(DEFAULT_LNG, DEFAULT_LAT)
    val viewport = rememberMapViewportState {
        setCameraOptions {
            center(startPoint)
            zoom(14.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    // Si ya había una ubicación, mostrar su dirección
    LaunchedEffect(Unit) {
        selected?.let { p ->
            loading = true
            address = MapboxGeocoder.reverse(context, p.latitude(), p.longitude())
            loading = false
        }
    }

    // Búsqueda de direcciones (espera a que dejes de escribir)
    LaunchedEffect(query) {
        if (query.trim().length < 3 || query == lastPicked) {
            results = emptyList()
            searching = false
            return@LaunchedEffect
        }
        delay(500)
        searching = true
        val near = selected
        results = MapboxGeocoder.search(
            context, query.trim(), near?.latitude() ?: DEFAULT_LAT, near?.longitude() ?: DEFAULT_LNG
        )
        searchedQuery = query
        searching = false
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
                        results = emptyList()
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

                // Buscador de direcciones sobre el mapa
                Column(modifier = Modifier.align(Alignment.TopCenter).padding(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = { Text("Busca una dirección o lugar...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = ""; results = emptyList() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    val noResults = !searching && results.isEmpty() &&
                            searchedQuery == query && query.trim().length >= 3 && query != lastPicked
                    if (searching || results.isNotEmpty() || noResults) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            shadowElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Column {
                                if (searching) {
                                    Text("Buscando...", modifier = Modifier.padding(14.dp), color = Color.Gray)
                                }
                                if (noResults) {
                                    Text("Sin resultados. Prueba con otra dirección.",
                                        modifier = Modifier.padding(14.dp), color = Color.Gray)
                                }
                                results.forEach { place ->
                                    Text(
                                        text = "📍 ${place.label}",
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val p = Point.fromLngLat(place.lng, place.lat)
                                                selected = p
                                                address = place.label
                                                lastPicked = place.label
                                                query = place.label
                                                results = emptyList()
                                                viewport.setCameraOptions {
                                                    center(p)
                                                    zoom(17.0)
                                                }
                                            }
                                            .padding(14.dp)
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }

            Surface(shadowElevation = 8.dp, color = Color.White) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = when {
                            selected == null -> "Busca una dirección o toca el mapa"
                            loading -> "Buscando la dirección..."
                            else -> "📍 $address"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF004D40)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { selected?.let { onConfirm(address, it.latitude(), it.longitude()) } },
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