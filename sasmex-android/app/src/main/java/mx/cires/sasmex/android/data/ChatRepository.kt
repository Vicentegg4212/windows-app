package mx.cires.sasmex.android.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Chat local con bot SASMEX. Todas las actualizaciones de estado se hacen en Main
 * para que la UI responda al 100%.
 */
class ChatRepository(
    private val sasmexRepository: SasmexRepository
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Hola. Soy el asistente SASMEX. Escribe \"alerta\" para la última alerta, \"ayuda\" para ver todos los comandos, \"911\" para emergencias, \"qué hacer\" para recomendaciones en sismo.",
                isFromUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var lastAlertas: List<AlertaSasmex> = emptyList()

    fun setLastAlertas(alertas: List<AlertaSasmex>) {
        lastAlertas = alertas
    }

    suspend fun sendUserMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), text = trimmed, isFromUser = true)
        withContext(Dispatchers.Main.immediate) {
            _messages.value = _messages.value + userMsg
        }
        val botReply = buildBotReply(trimmed)
        val botMsg = ChatMessage(id = UUID.randomUUID().toString(), text = botReply, isFromUser = false)
        withContext(Dispatchers.Main.immediate) {
            _messages.value = if (botReply == "Historial borrado.") {
                listOf(userMsg, botMsg)
            } else {
                _messages.value + botMsg
            }
        }
    }

    fun clearHistory() {
        _messages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Historial borrado. Escribe \"ayuda\" para ver comandos.",
                isFromUser = false
            )
        )
    }

    private suspend fun buildBotReply(userText: String): String {
        val lower = userText.lowercase().trim()
        return when {
            // 1. Última alerta / sismo
            lower.contains("alerta") || lower.contains("sismo") || lower.contains("última") || lower.contains("ultima") -> {
                if (lastAlertas.isEmpty()) {
                    sasmexRepository.obtenerAlertas().getOrNull()?.let { lastAlertas = it }
                }
                if (lastAlertas.isEmpty()) "No hay alertas recientes. Ve a la pestaña Alertas y pulsa «Actualizar alertas»."
                else {
                    val a = lastAlertas.first()
                    val desc = a.descripcion.take(150).let { if (a.descripcion.length > 150) "$it…" else it }
                    "📌 Última alerta SASMEX:\n\n${a.evento}\n${a.severidad}\n${java.text.SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(a.fechaHora)}\n${if (desc.isNotBlank()) "\n$desc" else ""}"
                }
            }
            // 2. Ayuda / comandos
            lower.contains("ayuda") || lower.contains("help") || lower.contains("opciones") || lower == "?" || lower == "comandos" -> {
                """📋 Comandos disponibles:
• alerta / sismo — Ver última alerta SASMEX
• ayuda — Esta lista de comandos
• 911 / emergencia — Número de emergencias
• qué hacer — Qué hacer durante un sismo
• compartir — Cómo compartir una alerta
• cires / sasmex — Enlace oficial CIRES
• borrar — Borrar historial del chat
• contacto — Contacto Protección Civil

Fuente: rss.sasmex.net · CIRES"""
            }
            // 3. 911 / emergencia
            lower.contains("911") || lower.contains("emergencia") || lower.contains("emergencias") -> {
                "🆘 En emergencia marca 911. Mantén la calma y sigue las indicaciones de Protección Civil. Si estás en zona sísmica, aléjate de ventanas y objetos que puedan caer."
            }
            // 4. Qué hacer en sismo
            lower.contains("qué hacer") || lower.contains("que hacer") || lower.contains("durante") || lower.contains("sismo recomendaciones") -> {
                """🏠 Qué hacer durante un sismo:
1. Mantén la calma.
2. Si estás dentro: quédate, protégete bajo una mesa sólida o marco de puerta.
3. Aléjate de ventanas, espejos y objetos que puedan caer.
4. Si estás en la calle: aléjate de edificios, postes y cables.
5. No uses ascensores.
6. Sigue las indicaciones de las autoridades y de la alerta sísmica SASMEX."""
            }
            // 5. Compartir
            lower.contains("compartir") || lower.contains("share") -> {
                "Para compartir una alerta: ve a la pestaña Alertas, actualiza y toca el botón compartir en la alerta que quieras enviar."
            }
            // 6. CIRES / SASMEX enlace
            lower.contains("cires") || lower.contains("sasmex") || lower.contains("página") || lower.contains("web") -> {
                "🌐 Página oficial: https://www.cires.org.mx\nSASMEX · Sistema de Alerta Sísmica Mexicano. Fuente de datos: rss.sasmex.net"
            }
            // 7. Borrar historial (el caller puede reaccionar a este texto)
            lower.contains("borrar") || lower.contains("limpiar") || lower.contains("nuevo chat") -> {
                "Historial borrado."
            }
            // 8. Contacto / protección civil
            lower.contains("contacto") || lower.contains("protección") || lower.contains("civil") -> {
                "Protección Civil México: 911 (emergencias). Información SASMEX/CIRES: https://www.cires.org.mx"
            }
            // 9. Hola / gracias
            lower == "hola" || lower == "hi" || lower == "buenos días" || lower == "buenas tardes" || lower == "buenas noches" -> {
                "Hola. Escribe \"ayuda\" para ver qué puedo hacer por ti."
            }
            lower.contains("gracias") || lower.contains("thanks") -> {
                "De nada. Cuídate y mantente informado con SASMEX."
            }
            // Por defecto
            else -> "Escribe \"alerta\" para ver la última alerta sísmica, \"ayuda\" para ver todos los comandos o \"qué hacer\" para recomendaciones en sismo."
        }
    }
}
