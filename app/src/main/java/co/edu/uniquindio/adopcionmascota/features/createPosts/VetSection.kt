package co.edu.uniquindio.adopcionmascota.features.createPosts

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.adopcionmascota.features.feed.VetKind
import java.util.Locale

private val DAY_LETTERS = listOf("L", "M", "X", "J", "V", "S", "D")

private fun pickTime(context: Context, current: String, onPicked: (String) -> Unit) {
    val hour = current.substringBefore(":").toIntOrNull() ?: 8
    val minute = current.substringAfter(":", "0").toIntOrNull() ?: 0
    TimePickerDialog(
        context,
        { _, h, m -> onPicked(String.format(Locale.US, "%02d:%02d", h, m)) },
        hour, minute, true
    ).show()
}

@Composable
fun VetKindSelector(selected: VetKind, onSelected: (VetKind) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        VetKind.values().forEach { kind ->
            val isSelected = kind == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0xFF80CBC4) else Color(0xFFE8F0EC))
                    .border(
                        1.5.dp,
                        if (isSelected) Color(0xFF004D40) else Color(0xFFD0E0D8),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelected(kind) }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(kind.emoji, fontSize = 26.sp)
                Text(
                    kind.displayName,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color(0xFF004D40)
                )
            }
        }
    }
}

@Composable
fun ServiceProviderForm(
    blocks: List<ScheduleBlock>,
    phones: List<String>,
    error: String?,
    onToggleDay: (Int, Int) -> Unit,
    onTimeChange: (Int, Boolean, String) -> Unit,
    onAddBlock: () -> Unit,
    onRemoveBlock: (Int) -> Unit,
    onPhoneChange: (Int, String) -> Unit,
    onAddPhone: () -> Unit,
    onRemovePhone: (Int) -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Horarios de atención *", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF004D40))

        blocks.forEachIndexed { index, block ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0EC))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Horario ${index + 1}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        if (blocks.size > 1) {
                            TextButton(onClick = { onRemoveBlock(index) }) { Text("Quitar") }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DAY_LETTERS.forEachIndexed { day, letter ->
                            val on = day in block.days
                            Text(
                                text = letter,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (on) Color.White else Color(0xFF004D40),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (on) Color(0xFF00897B) else Color.White)
                                    .clickable { onToggleDay(index, day) }
                                    .padding(top = 9.dp)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { pickTime(context, block.open) { onTimeChange(index, true, it) } }) {
                            Text("Abre  ${block.open}")
                        }
                        Text("–")
                        OutlinedButton(onClick = { pickTime(context, block.close) { onTimeChange(index, false, it) } }) {
                            Text("Cierra  ${block.close}")
                        }
                    }
                }
            }
        }
        if (blocks.size < 4) {
            OutlinedButton(onClick = onAddBlock, modifier = Modifier.fillMaxWidth()) {
                Text("+ Agregar otro horario")
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Teléfonos de contacto *", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF004D40))
        phones.forEachIndexed { index, phone ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { onPhoneChange(index, it) },
                    placeholder = { Text("Ej: 300 123 4567") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
                if (phones.size > 1) {
                    IconButton(onClick = { onRemovePhone(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar teléfono")
                    }
                }
            }
        }
        if (phones.size < 3) {
            OutlinedButton(onClick = onAddPhone, modifier = Modifier.fillMaxWidth()) {
                Text("+ Agregar otro teléfono")
            }
        }

        error?.let { Text(it, color = Color(0xFFB71C1C), fontSize = 12.sp) }
    }
}