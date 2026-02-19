package mx.cires.sasmex.android.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Obtiene alertas desde la API SASMEX/CIRES (misma que el bot y la app Windows).
 * https://rss.sasmex.net/api/v1/alerts/latest/cap/
 */
class SasmexRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiUrl = "https://rss.sasmex.net/api/v1/alerts/latest/cap/"

    suspend fun obtenerAlertas(): Result<List<AlertaSasmex>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("User-Agent", "SasmexAlertas-Android/1.0")
                .addHeader("Accept", "application/xml, text/xml, */*")
                .get()
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
            val body = response.body?.string() ?: return@withContext Result.success(emptyList())
            val list = parseAtomRss(body)
            Result.success(list.sortedByDescending { it.fechaHora })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAtomRss(xml: String): List<AlertaSasmex> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser().apply { setInput(StringReader(xml)) }
        val entries = mutableListOf<AlertaSasmex>()
        var event = parser.eventType
        var currentEntry: MutableMap<String, String>? = null
        var currentTag = ""
        var depthEntry = 0

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name
                    when {
                        name == "entry" || name == "item" -> {
                            currentEntry = mutableMapOf()
                            depthEntry = parser.depth
                        }
                        currentEntry != null && parser.depth == depthEntry + 1 -> {
                            currentTag = name
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (currentEntry != null && currentTag.isNotEmpty()) {
                        val key = when (currentTag) {
                            "title" -> "title"
                            "updated", "published", "pubDate" -> "updated"
                            "id", "guid" -> "id"
                            "content", "description", "summary" -> "content"
                            else -> null
                        }
                        if (key != null) {
                            val existing = currentEntry[key] ?: ""
                            currentEntry[key] = existing + parser.text.trim()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "entry", "item" -> {
                            currentEntry?.let { e ->
                                buildAlerta(e)?.let { entries.add(it) }
                            }
                            currentEntry = null
                            currentTag = ""
                        }
                        else -> if (parser.depth <= depthEntry + 1) currentTag = ""
                    }
                }
            }
            event = parser.next()
        }
        return entries
    }

    private fun buildAlerta(e: Map<String, String>): AlertaSasmex? {
        val title = e["title"].orEmpty().ifEmpty { "Alerta Sísmica SASMEX" }
        val updated = e["updated"].orEmpty()
        val content = (e["content"].orEmpty() + " " + title).lowercase()
        val severidad = when {
            content.contains("menor") || content.contains("no ameritó") || content.contains("moderado") -> "Severidad: Menor"
            content.contains("mayor") || content.contains("ameritó alerta") || content.contains("fuerte") -> "Severidad: Mayor"
            else -> "Severidad: Moderada"
        }
        val fecha = try {
            if (updated.isNotEmpty()) {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).parse(updated)
                    ?: java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.US).parse(updated)
                    ?: Date()
            } else Date()
        } catch (_: Exception) { Date() }
        val desc = e["content"].orEmpty()
            .split("\n")
            .map { it.trim() }
            .filter { line ->
                line.isNotEmpty() &&
                        !line.startsWith("ALERTA SÍSMICA SASMEX", true) &&
                        !line.startsWith("Sistema de Alerta Sísmica", true) &&
                        !Regex("Consulta:\\s*https?://cires").containsMatchIn(line)
            }
            .joinToString("\n")
            .trim()

        return AlertaSasmex(
            id = e["id"].orEmpty().ifEmpty { "alerta-${e.hashCode()}" },
            fechaHora = fecha,
            evento = title,
            severidad = severidad,
            descripcion = desc
        )
    }
}
