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
    val descripcion: String
) {
    val esMayor: Boolean get() = severidad.contains("Mayor", ignoreCase = true)
    val esMenor: Boolean get() = severidad.contains("Menor", ignoreCase = true)
}
