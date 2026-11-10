package co.edu.uniquindio.adopcionmascota.core.data

import android.content.Context
import co.edu.uniquindio.adopcionmascota.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object MapboxGeocoder {

    // Devuelve un nombre legible (barrio o calle, ciudad) para unas coordenadas
    suspend fun reverse(context: Context, lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            val fallback = String.format(Locale.US, "%.5f, %.5f", lat, lng)
            try {
                val token = context.getString(R.string.mapbox_access_token)
                val url = URL(
                    "https://api.mapbox.com/search/geocode/v6/reverse" +
                            "?longitude=$lng&latitude=$lat&language=es&limit=1&access_token=$token"
                )
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10_000
                    readTimeout = 15_000
                }
                if (connection.responseCode !in 200..299) return@withContext fallback

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val features = JSONObject(body).optJSONArray("features")
                if (features == null || features.length() == 0) return@withContext fallback

                val props = features.getJSONObject(0).optJSONObject("properties")
                val name = props?.optString("name").orEmpty()
                val place = props?.optJSONObject("context")
                    ?.optJSONObject("place")?.optString("name").orEmpty()

                listOf(name, place).filter { it.isNotBlank() }.distinct()
                    .joinToString(", ").ifBlank { fallback }
            } catch (e: Exception) {
                fallback
            }
        }
}