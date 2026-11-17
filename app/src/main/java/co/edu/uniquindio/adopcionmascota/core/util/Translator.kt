package co.edu.uniquindio.adopcionmascota.core.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

object Translator {

    // Es "state" para que la pantalla se redibuje sola al cambiar de idioma
    var english by mutableStateOf(false)

    private val client = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.SPANISH)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
    )
    private val cache = HashMap<String, String>()

    suspend fun translate(text: String): String {
        if (!english || text.isBlank()) return text
        cache[text]?.let { return it }
        return try {
            client.downloadModelIfNeeded().await()
            client.translate(text).await().also { cache[text] = it }
        } catch (e: Exception) {
            text // si falla, se queda en español
        }
    }
}