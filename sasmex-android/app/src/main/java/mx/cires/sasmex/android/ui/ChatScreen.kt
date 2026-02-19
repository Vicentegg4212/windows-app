package mx.cires.sasmex.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.cires.sasmex.android.data.ChatMessage
import mx.cires.sasmex.android.data.ChatRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(chatRepository: ChatRepository, modifier: Modifier = Modifier) {
    val messages by chatRepository.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Cabecera chat
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2B3D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Chat SASMEX", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Asistente y últimas alertas", color = Color(0xFFB0BEC5), style = MaterialTheme.typography.bodySmall)
            }
        }
        // Lista de mensajes
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }
        // Input
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje…") },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            scope.launch { chatRepository.sendUserMessage(input) }
                            input = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color(0xFF2563EB))
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
                containerColor = if (isUser) Color(0xFF2563EB) else Color(0xFFE2E8F0)
            ),
            modifier = Modifier.widthIn(max = 280.dp)
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
