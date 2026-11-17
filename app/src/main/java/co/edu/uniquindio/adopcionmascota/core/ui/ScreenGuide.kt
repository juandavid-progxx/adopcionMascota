package co.edu.uniquindio.adopcionmascota.core.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Guarda si la guía de una pantalla está visible y si el usuario ya la vio
class GuideState(
    private val prefs: SharedPreferences,
    private val key: String,
    firstTime: Boolean
) {
    var visible by mutableStateOf(firstTime)
        private set

    fun open() { visible = true }

    fun close() {
        visible = false
        prefs.edit().putBoolean(key, true).apply()
    }
}

@Composable
fun rememberGuideState(screenKey: String): GuideState {
    val context = LocalContext.current
    return remember(screenKey) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val key = "guide_screen_$screenKey"
        GuideState(prefs, key, firstTime = !prefs.getBoolean(key, false))
    }
}

@Composable
fun ScreenGuideDialog(state: GuideState, title: String, tips: List<String>) {
    if (!state.visible) return
    AlertDialog(
        onDismissRequest = { state.close() },
        title = {
            Text("💡 $title", fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tips.forEach { tip ->
                    Text("•  $tip", fontSize = 14.sp, color = Color(0xFF37474F))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { state.close() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
            ) {
                Text("Entendido", color = Color.White)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun GuideHelpButton(
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(Icons.Outlined.Info, contentDescription = "Ayuda", tint = tint)
    }
}

