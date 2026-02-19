package mx.cires.sasmex.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import mx.cires.sasmex.android.data.ChatRepository
import mx.cires.sasmex.android.data.SasmexRepository
import mx.cires.sasmex.android.ui.AlertasScreen
import mx.cires.sasmex.android.ui.ChatScreen

class MainActivity : ComponentActivity() {

    private lateinit var sasmexRepository: SasmexRepository
    private lateinit var chatRepository: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sasmexRepository = SasmexRepository()
        chatRepository = ChatRepository(sasmexRepository)

        setContent {
            SasmexTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                                label = { Text("Alertas") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                                label = { Text("Chat") }
                            )
                        }
                    }
                ) { padding ->
                    when (selectedTab) {
                        0 -> AlertasScreen(
                            sasmexRepository = sasmexRepository,
                            onAlertasLoaded = { chatRepository.setLastAlertas(it) },
                            modifier = Modifier.padding(padding)
                        )
                        1 -> ChatScreen(chatRepository = chatRepository, modifier = Modifier.padding(padding))
                    }
                }
            }
        }
    }
}

@Composable
private fun SasmexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF1A2B3D),
            onPrimary = androidx.compose.ui.graphics.Color.White,
            secondary = androidx.compose.ui.graphics.Color(0xFF2563EB),
            surface = androidx.compose.ui.graphics.Color(0xFFF1F5F9)
        ),
        content = content
    )
}
