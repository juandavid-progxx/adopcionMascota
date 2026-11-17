package co.edu.uniquindio.adopcionmascota.core.data

import android.content.Context
import co.edu.uniquindio.adopcionmascota.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class PlaceResult(val label: String, val lat: Double, val lng: Double)

object MapboxGeocoder {

    private fun labelOf(props: JSONObject?): String {
        val full = props?.optString("full_address").orEmpty()
        if (full.isNotBlank()) return full
        val name = props?.optString("name").orEmpty()
        val place = props?.optString("place_formatted").orEmpty()
        return listOf(name, place).filter { it.isNotBlank() }.joinToString(", ")
    }

    private fun get(url: String): String? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
        }
        return try {
            if (connection.responseCode in 200..299)
                connection.inputStream.bufferedReader().use { it.readText() } else null
        } finally {
            connection.disconnect()
        }
    }

    // Coordenadas -> dirección
    suspend fun reverse(context: Context, lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            val fallback = String.format(Locale.US, "%.5f, %.5f", lat, lng)
            try {
                val token = context.getString(R.string.mapbox_access_token)
                val body = get(
                    "https://api.mapbox.com/search/geocode/v6/reverse" +
                            "?longitude=$lng&latitude=$lat&language=es&limit=1&access_token=$token"
                ) ?: return@withContext fallback
                val features = JSONObject(body).optJSONArray("features")
                if (features == null || features.length() == 0) return@withContext fallback
                labelOf(features.getJSONObject(0).optJSONObject("properties")).ifBlank { fallback }
            } catch (e: Exception) {
                fallback
            }
        }

    // Texto -> lista de lugares (para el buscador del mapa)
    suspend fun search(
        context: Context,
        query: String,
        nearLat: Double,
        nearLng: Double
    ): List<PlaceResult> = withContext(Dispatchers.IO) {
        try {
            val token = context.getString(R.string.mapbox_access_token)
            val q = URLEncoder.encode(query, "UTF-8")
            val body = get(
                "https://api.mapbox.com/search/geocode/v6/forward?q=$q&country=co&language=es" +
                        "&limit=5&proximity=$nearLng,$nearLat&access_token=$token"
            ) ?: return@withContext emptyList()
            val features = JSONObject(body).optJSONArray("features") ?: return@withContext emptyList()
            (0 until features.length()).mapNotNull { i ->
                val f = features.getJSONObject(i)
                val coords = f.optJSONObject("geometry")?.optJSONArray("coordinates")
                    ?: return@mapNotNull null
                val label = labelOf(f.optJSONObject("properties"))
                if (label.isBlank()) null else PlaceResult(label, coords.getDouble(1), coords.getDouble(0))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}