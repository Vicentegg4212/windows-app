package mx.cires.sasmex.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.cires.sasmex.android.data.ChatRepository
import mx.cires.sasmex.android.data.SasmexRepository
import mx.cires.sasmex.android.notifications.NotificationHelper
import mx.cires.sasmex.android.ui.AlertasScreen
import mx.cires.sasmex.android.ui.ChatScreen

class MainActivity : ComponentActivity() {

    private lateinit var sasmexRepository: SasmexRepository
    private lateinit var chatRepository: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannel(this)
        sasmexRepository = SasmexRepository()
        chatRepository = ChatRepository(sasmexRepository)

        setContent {
            SasmexTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
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
                            onNotifyLatest = { alerta ->
                                NotificationHelper.showAlertNotification(this@MainActivity, alerta)
                            },
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
