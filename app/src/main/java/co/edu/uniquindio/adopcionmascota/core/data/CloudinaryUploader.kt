package co.edu.uniquindio.adopcionmascota.core.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object CloudinaryUploader {

    private const val CLOUD_NAME = "hpdoqwjq"
    private const val UPLOAD_PRESET = "adoptaya_unsigned"

    // Sube una foto de la galería y devuelve su URL pública
    suspend fun upload(context: Context, uriString: String): String = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(Uri.parse(uriString))
            ?.use { it.readBytes() }
            ?: throw IOException("No se pudo leer la imagen")

        val boundary = "----AdoptaYa${System.currentTimeMillis()}"
        val connection = (URL("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20_000
            readTimeout = 60_000
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        connection.outputStream.use { out ->
            fun text(s: String) = out.write(s.toByteArray())
            text("--$boundary\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\n$UPLOAD_PRESET\r\n")
            text("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"foto.jpg\"\r\nContent-Type: application/octet-stream\r\n\r\n")
            out.write(bytes)
            text("\r\n--$boundary--\r\n")
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) throw IOException("Cloudinary respondió $code: $body")

        JSONObject(body).getString("secure_url")
    }
}

// Pide a Cloudinary una versión más liviana de la foto (más rápida en pantalla)
fun String.cloudThumb(width: Int = 800): String =
    replace("/upload/", "/upload/c_limit,w_$width,q_auto,f_auto/")