package mx.cires.sasmex.android.data

import java.util.Date

/**
 * Representa una alerta del Sistema de Alerta Sísmica Mexicano (SASMEX/CIRES).
 * Fuente: https://rss.sasmex.net
 */
data class AlertaSasmex(
    val id: String,
    val fechaHora: Date,
    val evento: String,
    val severidad: String,
    val descripcion: String,
    val magnitud: Double? = null,
    val ubicacion: String = "",
    val profundidad: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val distancia: String = ""
) {
    val esMayor: Boolean get() = severidad.contains("Mayor", ignoreCase = true)
    val esMenor: Boolean get() = severidad.contains("Menor", ignoreCase = true)

    fun tiempoRelativo(now: Long = System.currentTimeMillis()): String {
        val diff = (now - fechaHora.time) / 1000
        return when {
            diff < 60 -> "hace menos de 1 min"
            diff < 3600 -> "hace ${diff / 60} min"
            diff < 86400 -> "hace ${diff / 3600} hora(s)"
            else -> "hace ${diff / 86400} día(s)"
        }
    }
}
