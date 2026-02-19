package mx.cires.sasmex.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.cires.sasmex.android.data.ChatMessage
import mx.cires.sasmex.android.data.ChatRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    chatRepository: ChatRepository,
    modifier: Modifier = Modifier
) {
    val messages by chatRepository.messages.collectAsState(initial = emptyList())
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun send() {
        val t = input.trim()
        if (t.isEmpty()) return
        input = ""
        focusManager.clearFocus()
        scope.launch {
            chatRepository.sendUserMessage(t)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Cabecera con botón borrar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1B2838),
            shadowElevation = 4.dp
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Chat SASMEX",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Asistente y últimas alertas",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(
                    onClick = { chatRepository.clearHistory() },
                    modifier = Modifier
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Borrar historial",
                        tint = Color.White
                    )
                }
            }
        }

        // Hint comandos
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFE0F2FE),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text(
                "Escribe: alerta, ayuda, 911, qué hacer, cires, borrar",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF0369A1)
            )
        }

        // Lista de mensajes
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(msg)
            }
        }

        // Campo de texto y enviar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje…") },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { send() })
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { send() },
                    modifier = Modifier.height(48.dp),
                    enabled = input.isNotBlank(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.isFromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF0EA5E9) else Color(0xFFE2E8F0)
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    msg.text,
                    color = if (isUser) Color.White else Color(0xFF0F172A),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(msg.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
