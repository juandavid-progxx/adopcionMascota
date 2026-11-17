package co.edu.uniquindio.adopcionmascota.core.data

import android.util.Log
import co.edu.uniquindio.adopcionmascota.BuildConfig
import co.edu.uniquindio.adopcionmascota.features.feed.PetCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer

data class AiSuggestion(
    val category: PetCategory,
    val reason: String,
    val source: String
)

object AiClassifier {

    // Modelo gratuito de OpenRouter.
    private const val MODEL = "openrouter/free"

    private const val SYSTEM_PROMPT = """
Eres un clasificador para una app de mascotas. Lee el título y la descripción de una publicación y elige UNA categoría:
- ADOPTION: alguien da una mascota en adopción o busca familia para ella.
- LOST: una mascota se perdió y su dueño la busca.
- FOUND: alguien encontró una mascota que no es suya.
- TEMPORARY: se necesita o se ofrece un hogar de paso temporal.
- VETERINARY: jornada de vacunación, esterilización u otro servicio veterinario.
Responde SOLO con un JSON, sin texto adicional: {"category":"LOST","reason":"frase corta en español"}
"""

    suspend fun classify(title: String, description: String): AiSuggestion =
        withContext(Dispatchers.IO) {
            val key = BuildConfig.OPENROUTER_API_KEY.trim()
            if (key.isBlank()) {
                return@withContext localRules(title, description)
                    .copy(source = "Reglas locales · no hay clave en local.properties")
            }
            try {
                askModel(key, title, description)
            } catch (e: Exception) {
                Log.e("AiClassifier", "OpenRouter falló, se usan reglas locales", e)
                localRules(title, description)
                    .copy(source = "Reglas locales · ${e.message?.take(120)}")
            }
        }

    private fun askModel(key: String, title: String, description: String): AiSuggestion {
        val body = JSONObject().apply {
            put("model", MODEL)
            put("temperature", 0)
            put("max_tokens", 120)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
                put(JSONObject().put("role", "user").put("content", "Título: $title\nDescripción: $description"))
            })
        }

        val connection = (URL("https://openrouter.ai/api/v1/chat/completions")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Authorization", "Bearer $key")
            setRequestProperty("Content-Type", "application/json")
        }
        connection.outputStream.use { it.write(body.toString().toByteArray()) }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) throw IllegalStateException("OpenRouter respondió $code: $text")

        val content = JSONObject(text).getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message").optString("content")
        val json = Regex("\\{[\\s\\S]*?\\}").find(content)?.value
            ?: throw IllegalStateException("La respuesta no trae JSON: $content")

        val parsed = JSONObject(json)
        val category = PetCategory.values()
            .find { it.name == parsed.optString("category").trim().uppercase() }
            ?: throw IllegalStateException("Categoría desconocida: $json")

        return AiSuggestion(
            category = category,
            reason = parsed.optString("reason").ifBlank { "Según el contenido de tu publicación." },
            source = "IA · OpenRouter"
        )
    }

    // ---------- Plan B: reglas de palabras clave (sin internet ni clave) ----------
    private fun normalize(s: String): String =
        Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

    private fun localRules(title: String, description: String): AiSuggestion {
        val text = normalize("$title $description")
        val rules = mapOf(
            PetCategory.LOST to listOf("perdi", "extravi", "desaparec", "recompensa", "se escapo", "se salio", "busco a mi"),
            PetCategory.FOUND to listOf("encontr", "halle", "recogi", "deambul", "vagando", "sin collar"),
            PetCategory.TEMPORARY to listOf("hogar de paso", "temporal", "transitorio", "por unas semanas", "mientras se recupera"),
            PetCategory.VETERINARY to listOf("vacunacion", "jornada", "esteriliz", "castracion", "veterinari", "desparasit", "gratuita"),
            PetCategory.ADOPTION to listOf("adopt", "regalo", "busca hogar", "busca familia", "busca una familia")
        )
        val scores = rules.mapValues { (_, words) -> words.count { text.contains(it) } }
        val best = scores.maxByOrNull { it.value }
        val category = if (best == null || best.value == 0) PetCategory.ADOPTION else best.key

        return AiSuggestion(
            category = category,
            reason = "Encontré palabras clave de esta categoría en tu texto.",
            source = "Reglas locales (sin conexión con la IA)"
        )
    }
}