package mx.cires.sasmex.android.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Chat local: mensajes guardados en memoria (persisten mientras la app esté abierta).
 * El "bot" responde con información SASMEX según palabras clave.
 */
class ChatRepository(
    private val sasmexRepository: SasmexRepository
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(mutableListOf(
        ChatMessage(
            id = UUID.randomUUID().toString(),
            text = "Hola. Soy el asistente SASMEX. Escribe \"alerta\" para ver la última alerta, \"ayuda\" para opciones o cualquier pregunta.",
            isFromUser = false
        )
    ))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var lastAlertas: List<AlertaSasmex> = emptyList()

    fun setLastAlertas(alertas: List<AlertaSasmex>) {
        lastAlertas = alertas
    }

    suspend fun sendUserMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), text = trimmed, isFromUser = true)
        _messages.value = _messages.value + userMsg

        val botReply = buildBotReply(trimmed)
        val botMsg = ChatMessage(id = UUID.randomUUID().toString(), text = botReply, isFromUser = false)
        _messages.value = _messages.value + botMsg
    }

    private suspend fun buildBotReply(userText: String): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("alerta") || lower.contains("sismo") || lower.contains("última") -> {
                if (lastAlertas.isEmpty()) {
                    val result = sasmexRepository.obtenerAlertas()
                    result.getOrNull()?.let { lastAlertas = it }
                }
                if (lastAlertas.isEmpty()) "No hay alertas recientes. Toca la pestaña Alertas y pulsa Actualizar."
                else {
                    val a = lastAlertas.first()
                    "Última alerta:\n${a.evento}\n${a.severidad}\n${a.fechaHora}\n${a.descripcion.take(200)}${if (a.descripcion.length > 200) "…" else ""}"
                }
            }
            lower.contains("ayuda") || lower.contains("help") || lower.contains("opciones") -> {
                "Comandos:\n• \"alerta\" o \"sismo\" — Ver última alerta SASMEX\n• \"ayuda\" — Esta ayuda\n• \"911\" — Número de emergencias\nFuente: rss.sasmex.net · CIRES"
            }
            lower.contains("911") || lower.contains("emergencia") -> "En emergencia marca 911. Mantén la calma y sigue las indicaciones de Protección Civil."
            else -> "Escribe \"alerta\" para ver la última alerta sísmica o \"ayuda\" para ver opciones."
        }
    }
}
