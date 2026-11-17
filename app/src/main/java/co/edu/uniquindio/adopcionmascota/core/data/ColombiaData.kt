package co.edu.uniquindio.adopcionmascota.core.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.text.Collator
import java.util.Locale

object ColombiaData {
    private var cache: Map<String, List<String>>? = null
    private val collator: Collator = Collator.getInstance(Locale.forLanguageTag("es"))

    private fun load(context: Context): Map<String, List<String>> {
        cache?.let { return it }
        return try {
            val text = context.assets.open("colombia.json").bufferedReader().use { it.readText() }
            val array = JSONArray(text)
            val map = linkedMapOf<String, List<String>>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val cities = obj.getJSONArray("ciudades")
                map[obj.getString("departamento")] =
                    List(cities.length()) { cities.getString(it) }.sortedWith(collator)
            }
            cache = map
            map
        } catch (e: Exception) {
            Log.e("ColombiaData", "No se pudo leer colombia.json", e)
            emptyMap()
        }
    }

    fun departments(context: Context): List<String> = load(context).keys.sortedWith(collator)
    fun municipalities(context: Context, department: String): List<String> =
        load(context)[department].orEmpty()
}