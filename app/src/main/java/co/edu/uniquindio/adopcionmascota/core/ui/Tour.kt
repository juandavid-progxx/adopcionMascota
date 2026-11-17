package co.edu.uniquindio.adopcionmascota.core.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester

private val TourTeal = Color(0xFF00796B)

// Un paso del tutorial. Si "target" es null, la tarjeta sale centrada.
data class TourStep(
    val title: String,
    val text: String,
    val target: String? = null
)

// Estado global del tutorial (una sola instancia para toda la app)
object Tour {
    var steps by mutableStateOf<List<TourStep>>(emptyList())
        private set
    var index by mutableIntStateOf(0)
        private set
    var active by mutableStateOf(false)
        private set

    // Posición en pantalla de cada elemento marcado con tourTarget
    val bounds = mutableStateMapOf<String, Rect>()
    val requesters = mutableMapOf<String, BringIntoViewRequester>()

    private var onFinish: (() -> Unit)? = null

    fun start(steps: List<TourStep>, onFinish: () -> Unit = {}) {
        if (steps.isEmpty()) return
        this.steps = steps
        this.index = 0
        this.onFinish = onFinish
        this.active = true
    }

    fun next() { if (index < steps.lastIndex) index++ else finish() }
    fun back() { if (index > 0) index-- }

    fun finish() {
        active = false
        onFinish?.invoke()
        onFinish = null
    }
}

// Marca un elemento de la pantalla para poder resaltarlo en el tutorial
fun Modifier.tourTarget(key: String): Modifier = composed {
    val requester = remember { BringIntoViewRequester() }
    DisposableEffect(key) {
        Tour.requesters[key] = requester
        onDispose {
            Tour.bounds.remove(key)
            Tour.requesters.remove(key)
        }
    }
    bringIntoViewRequester(requester)
        .onGloballyPositioned { Tour.bounds[key] = it.boundsInRoot() }
}
// Se llama al inicio de cada pantalla. Abre el tutorial solo la primera vez
// y devuelve una función para volver a abrirlo (botón ⓘ).
@Composable
fun rememberTour(screenKey: String, steps: List<TourStep>): () -> Unit {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val prefKey = "tour_$screenKey"
    val currentSteps by rememberUpdatedState(steps)

    val start: () -> Unit = {
        Tour.start(currentSteps) { prefs.edit().putBoolean(prefKey, true).apply() }
    }

    LaunchedEffect(screenKey) {
        if (!prefs.getBoolean(prefKey, false)) {
            delay(700) // espera a que la pantalla termine de dibujarse
            start()
        }
    }
    return start
}

// Capa que dibuja el tutorial encima de toda la app (va en MainActivity)
@Composable
fun TourOverlay() {
    if (!Tour.active) return
    val steps = Tour.steps
    if (steps.isEmpty()) return
    val index = Tour.index.coerceIn(0, steps.lastIndex)
    val step = steps[index]
    val isLast = index == steps.lastIndex

    BackHandler { Tour.finish() }
    LaunchedEffect(index) {
        step.target?.let { Tour.requesters[it]?.bringIntoView() }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { } } // bloquea los toques de atrás
    ) {
        val density = LocalDensity.current
        val screenH = constraints.maxHeight.toFloat()
        val pad = with(density) { 6.dp.toPx() }

        // Si el elemento no está visible, la tarjeta sale centrada
        val rect = step.target?.let { Tour.bounds[it] }
            ?.takeIf { it.width > 0f && it.bottom > 0f && it.top < screenH }

        val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "pulse"
        )

        // Fondo oscuro con un "hueco" sobre el elemento resaltado
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(Color(0xCC000000))
            if (rect != null) {
                val topLeft = Offset(rect.left - pad, rect.top - pad)
                val size = Size(rect.width + pad * 2, rect.height + pad * 2)
                val corner = CornerRadius(16.dp.toPx())
                drawRoundRect(Color.Black, topLeft, size, corner, blendMode = BlendMode.Clear)
                drawRoundRect(
                    color = Color(0xFF00BFA5).copy(alpha = 0.55f + 0.45f * pulse),
                    topLeft = topLeft,
                    size = size,
                    cornerRadius = corner,
                    style = Stroke(width = (2f + 2f * pulse).dp.toPx())
                )
            }
        }

        // Posición de la tarjeta: debajo del elemento (si está arriba) o encima (si está abajo)
        var cardH by remember { mutableIntStateOf(0) }
        val margin = with(density) { 16.dp.roundToPx() }
        val gap = with(density) { 14.dp.roundToPx() }
        val cardY: Int = when {
            rect == null -> ((screenH - cardH) / 2f).toInt()
            rect.center.y < screenH / 2f ->
                (rect.bottom + pad + gap).toInt().coerceAtMost(screenH.toInt() - cardH - margin)
            else ->
                (rect.top - pad - gap - cardH).toInt().coerceAtLeast(margin)
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .onSizeChanged { cardH = it.height }
                .offset { IntOffset(0, cardY) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = step.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40),
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.Text(
                        text = "${index + 1}/${steps.size}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = step.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF37474F)
                )
                Spacer(Modifier.height(14.dp))

                // Puntitos de progreso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(if (i == index) 10.dp else 7.dp)
                                .clip(CircleShape)
                                .background(if (i == index) TourTeal else Color(0xFFB0BEC5))
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Botones: Omitir | ◀ | Siguiente ▶
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { Tour.finish() }) {
                        Text("Omitir", color = Color.Gray)
                    }
                    Spacer(Modifier.weight(1f))
                    if (index > 0) {
                        IconButton(
                            onClick = { Tour.back() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2F1))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Anterior",
                                tint = TourTeal
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(
                        onClick = { Tour.next() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TourTeal,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (isLast) "¡Listo!" else "Siguiente",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isLast) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

