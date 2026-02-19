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
 * La info viene en formato CAP (Common Alerting Protocol); los .cap definen dónde se registra el sismo.
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
        val rawTitle = e["title"].orEmpty().trim()
        val rawContent = e["content"].orEmpty()
        val updated = e["updated"].orEmpty()
        val contentLower = (rawContent + " " + rawTitle).lowercase()

        val severidad = when {
            contentLower.contains("menor") || contentLower.contains("no ameritó") || contentLower.contains("moderado") -> "Menor"
            contentLower.contains("mayor") || contentLower.contains("ameritó alerta") || contentLower.contains("fuerte") -> "Mayor"
            else -> "Moderada"
        }

        val cap = parseCapFromContent(rawContent)
        val fecha = parseEventDateFromCap(cap, rawTitle, rawContent, updated)
        val evento = if (cap.headline.isNotBlank()) cap.headline.trim().take(120) else cleanEventTitle(rawTitle, rawContent)
        val descripcionLimpia = if (cap.description.isNotBlank()) cap.description.trim().take(500) else cleanDescription(rawContent)
        val (magnitud, ubicacion, profundidad, lat, lon) = extractDetailFromContent(rawContent)
        val ubicacionFinal = if (ubicacion.isBlank() && cap.areaDescs.isNotEmpty()) {
            cap.areaDescs.firstOrNull { !isSoloCDMX(it) }?.trim() ?: ""
        } else ubicacion

        return AlertaSasmex(
            id = e["id"].orEmpty().ifEmpty { "alerta-${e.hashCode()}" },
            fechaHora = fecha,
            evento = evento,
            severidad = "Severidad: $severidad",
            descripcion = descripcionLimpia,
            magnitud = magnitud,
            ubicacion = ubicacionFinal,
            profundidad = profundidad,
            lat = lat,
            lon = lon
        )
    }

    /** Datos extraídos del CAP (Common Alerting Protocol); los .cap en rss.sasmex.net son la fuente oficial. */
    private data class CapData(
        val effective: String,
        val sent: String,
        val areaDescs: List<String>,
        val headline: String,
        val description: String
    )

    /** Parsea elementos CAP del contenido XML (alert/sent, info/effective, info/area/areaDesc, info/headline, info/description). */
    private fun parseCapFromContent(raw: String): CapData {
        val effective = Regex("<(?:[\\w.]+:)?effective[^>]*>([^<]+)</(?:[\\w.]+:)?effective>", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)?.trim() ?: ""
        val sent = Regex("<(?:[\\w.]+:)?sent[^>]*>([^<]+)</(?:[\\w.]+:)?sent>", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)?.trim() ?: ""
        val areaDescs = Regex("<(?:[\\w.]+:)?areaDesc[^>]*>([^<]+)</(?:[\\w.]+:)?areaDesc>", RegexOption.IGNORE_CASE).findAll(raw).map { it.groupValues[1].trim() }.filter { it.isNotBlank() }.toList()
        val headline = Regex("<(?:[\\w.]+:)?headline[^>]*>([^<]*)</(?:[\\w.]+:)?headline>", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)?.trim() ?: ""
        val description = Regex("<(?:[\\w.]+:)?description[^>]*>([^<]*)</(?:[\\w.]+:)?description>", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)?.trim() ?: ""
        return CapData(effective = effective, sent = sent, areaDescs = areaDescs, headline = headline, description = description)
    }

    private fun parseEventDateFromCap(cap: CapData, rawTitle: String, rawContent: String, updated: String): Date {
        val isoFormats = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.US)
        )
        for (s in listOf(cap.effective, cap.sent)) {
            if (s.isBlank()) continue
            val clean = s.replace(Regex("[+-]\\d{2}:\\d{2}$"), "").trim()
            for (fmt in isoFormats) {
                try {
                    fmt.parse(clean)?.let { return it }
                } catch (_: Exception) { }
            }
        }
        return parseEventDate(rawTitle, rawContent, updated)
    }

    private fun extractDetailFromContent(raw: String): Extraction {
        var magnitud: Double? = null
        var ubicacion = ""
        var profundidad = ""
        var lat: Double? = null
        var lon: Double? = null

        Regex("(?:magnitud|magnitude|M\\s*)[:\\.]?\\s*(\\d+\\.\\d+)", RegexOption.IGNORE_CASE).find(raw)?.let {
            magnitud = it.groupValues[1].toDoubleOrNull()
        }
        if (magnitud == null) {
            Regex("\\b(\\d\\.\\d)\\s*(?=\\s*(?:en|de|km|magnitud))", RegexOption.IGNORE_CASE).find(raw)?.let {
                magnitud = it.groupValues[1].toDoubleOrNull()
            }
        }
        if (magnitud == null) {
            Regex("(\\d+\\.\\d+)\\s*\\d+\\s*km\\s*al", RegexOption.IGNORE_CASE).find(raw)?.let {
                magnitud = it.groupValues[1].toDoubleOrNull()
            }
        }

        // Epicentro real: "123 KM AL SUR DE LAZARO CARDENAS, MICH"
        Regex("(\\d+)\\s*KM\\s*AL\\s*(?:SUR|NORTE|ESTE|OESTE)\\s+DE\\s+[^,]+,\\s*[A-ZÁÉÍÓÚÑ\\s]+", RegexOption.IGNORE_CASE).find(raw)?.let {
            ubicacion = it.value.trim().uppercase()
        }
        if (ubicacion.isEmpty()) {
            Regex("(?:epicentro|epicento|ubicación|ubicacion|localización)\\s*[:\\.]?\\s*([^\\n.]{15,80})", RegexOption.IGNORE_CASE).find(raw)?.let {
                ubicacion = it.groupValues[1].trim()
            }
        }
        // Región del evento: "Sismo en Costa Mich" -> Costa Mich (no usar CDMX como epicentro)
        if (ubicacion.isEmpty()) {
            Regex("(?:Sismo|Alerta|Evento)\\s+(?:Moderado|Menor|Mayor|Fuerte)?\\s*en\\s+([A-Za-zÀ-ú\\s]{4,50})", RegexOption.IGNORE_CASE).find(raw)?.let { m ->
                val region = m.groupValues[1].trim()
                if (!isSoloCDMX(region)) ubicacion = region
            }
        }
        if (ubicacion.isEmpty() && raw.contains("en ", ignoreCase = true)) {
            val fromTitle = extractHumanTitleFromContent(raw)
            val loc = when {
                fromTitle.length in 15..70 -> fromTitle
                else -> null
            }
            if (loc != null && !isSoloCDMX(loc)) ubicacion = loc
        }
        // Nunca mostrar CDMX como ubicación del sismo (es donde se emite la alerta, no el epicentro)
        if (isSoloCDMX(ubicacion)) ubicacion = ""

        Regex("(?:profundidad|depth)\\s*[:\\.]?\\s*(\\d+)\\s*km", RegexOption.IGNORE_CASE).find(raw)?.let {
            profundidad = "${it.groupValues[1]} km"
        }
        if (profundidad.isEmpty()) {
            Regex("(\\d+)\\s*km\\s*(?:de\\s+)?profundidad", RegexOption.IGNORE_CASE).find(raw)?.let {
                profundidad = "${it.groupValues[1]} km"
            }
        }

        Regex("<point>\\s*([-\\d.]+)\\s+([-\\d.]+)\\s*</point>", RegexOption.IGNORE_CASE).find(raw)?.let {
            lat = it.groupValues[1].toDoubleOrNull()
            lon = it.groupValues[2].toDoubleOrNull()
        }
        if (lat == null) {
            Regex("<circle>\\s*([-\\d.]+)\\s*,\\s*([-\\d.]+)").find(raw)?.let {
                lat = it.groupValues[1].toDoubleOrNull()
                lon = it.groupValues[2].toDoubleOrNull()
            }
        }
        if (lat == null && raw.contains("lat") && raw.contains("lon")) {
            Regex("(?:lat|latitude)[^\\d]*([-]?\\d+\\.\\d+).*?(?:lon|longitude)[^\\d]*([-]?\\d+\\.\\d+)", RegexOption.IGNORE_CASE).find(raw)?.let {
                lat = it.groupValues[1].toDoubleOrNull()
                lon = it.groupValues[2].toDoubleOrNull()
            }
        }

        return Extraction(magnitud, ubicacion, profundidad, lat, lon)
    }

    /** Parsea la fecha/hora del evento desde título o contenido (como marca el RSS), no la de actualización del feed. */
    private fun parseEventDate(rawTitle: String, rawContent: String, updated: String): Date {
        val text = "$rawTitle $rawContent"
        val formats = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US),
            java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss", java.util.Locale.US),
            java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.US)
        )
        // 2026-02-19 01:02:41 o 19 Feb 2026 01:02:41
        val dateInText = Regex("(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})").find(text)?.groupValues?.get(1)
            ?: Regex("(\\d{1,2}\\s+\\w{3}\\s+\\d{4}\\s+\\d{2}:\\d{2}:\\d{2})", RegexOption.IGNORE_CASE).find(text)?.groupValues?.get(1)
        if (!dateInText.isNullOrBlank()) {
            for (fmt in formats) {
                try {
                    fmt.parse(dateInText.trim())?.let { return it }
                } catch (_: Exception) { }
            }
        }
        return try {
            if (updated.isNotEmpty()) {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).parse(updated)
                    ?: java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US).parse(updated)
                    ?: java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.US).parse(updated)
                    ?: Date()
            } else Date()
        } catch (_: Exception) { Date() }
    }

    /** CDMX es donde se emite/recibe la alerta, no el epicentro; no usarla como ubicación del sismo. */
    private fun isSoloCDMX(text: String): Boolean {
        if (text.isBlank()) return false
        val t = text.trim().lowercase()
        return t == "cdmx" || t == "ciudad de méxico" || t == "ciudad de mexico" ||
            t.startsWith("cdmx ") || t.endsWith(" cdmx") ||
            t.contains("ciudad de méxico") || t.contains("ciudad de mexico") ||
            t.contains("distrito federal")
    }

    private data class Extraction(
        val magnitud: Double?,
        val ubicacion: String,
        val profundidad: String,
        val lat: Double?,
        val lon: Double?
    )

    private fun cleanEventTitle(rawTitle: String, rawContent: String): String {
        if (rawTitle.isBlank()) return "Alerta Sísmica SASMEX"
        if (looksLikeRawData(rawTitle)) {
            val fromContent = extractHumanTitleFromContent(rawContent)
            if (fromContent.isNotBlank()) return fromContent
            return "Alerta Sísmica SASMEX"
        }
        return rawTitle.trim().take(120)
    }

    private fun looksLikeRawData(s: String): Boolean {
        if (s.length > 80) return true
        if (s.contains("cires.org.mx") || s.contains("CIRES") && s.any { it.isDigit() } && s.length > 30) return true
        if (Regex("^[A-Za-z0-9]+.*ActualAlert|Publices|GeoSASM").containsMatchIn(s)) return true
        if (Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}").containsMatchIn(s) && s.length > 40) return true
        return false
    }

    private fun extractHumanTitleFromContent(content: String): String {
        val patterns = listOf(
            Regex("Sismo\\s+(?:Moderado|Menor|Mayor|Fuerte)\\s+en\\s+[^\\n.]+", RegexOption.IGNORE_CASE),
            Regex("(?:Sismo|Alerta)[^\\n.]*(?:en|En)\\s+[^\\n.]+", RegexOption.IGNORE_CASE),
            Regex("Sismo\\s+(?:en\\s+)?[A-Za-zÀ-ú\\s]{10,60}", RegexOption.IGNORE_CASE),
            Regex("(?:ALERTA|Alerta)\\s+S[íi]smica[^\\n]{5,80}", RegexOption.IGNORE_CASE)
        )
        for (p in patterns) {
            p.find(content)?.let { return it.value.trim().take(100) }
        }
        return content.split("\n", ":", ";")
            .map { it.trim() }
            .firstOrNull { line ->
                line.length in 10..90 &&
                        (line.contains("Sismo", true) || line.contains("Alerta", true)) &&
                        !looksLikeRawData(line)
            } ?: "Alerta Sísmica SASMEX"
    }

    private fun cleanDescription(rawContent: String): String {
        val lines = rawContent.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val good = lines.filter { line ->
            !looksLikeRawData(line) &&
                    !line.startsWith("ALERTA SÍSMICA SASMEX", true) &&
                    !line.startsWith("Sistema de Alerta", true) &&
                    !Regex("https?://").containsMatchIn(line) &&
                    !Regex("^[A-Z0-9]{20,}").containsMatchIn(line) &&
                    line.length in 5..150
        }
        return good.firstOrNull() ?: ""
    }
}
