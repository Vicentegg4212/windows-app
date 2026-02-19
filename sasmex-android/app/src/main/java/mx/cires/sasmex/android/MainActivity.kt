package mx.cires.sasmex.android

import android.Manifest
import androidx.compose.material3.ExperimentalMaterial3Api
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mx.cires.sasmex.android.data.AlertaSasmex
import mx.cires.sasmex.android.data.AppPreferences
import mx.cires.sasmex.android.data.ChatRepository
import mx.cires.sasmex.android.data.SasmexRepository
import mx.cires.sasmex.android.notifications.NotificationHelper
import mx.cires.sasmex.android.ui.AlertasScreen
import mx.cires.sasmex.android.ui.ChatScreen
import mx.cires.sasmex.android.ui.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private lateinit var sasmexRepository: SasmexRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannel(this)
        appPreferences = AppPreferences(this)
        sasmexRepository = SasmexRepository()
        chatRepository = ChatRepository(sasmexRepository)

        setContent {
            var theme by remember { mutableStateOf("system") }
            var showSettings by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                theme = appPreferences.getTheme()
            }
            val darkTheme = when (theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            SasmexTheme(darkTheme = darkTheme) {
                var selectedTab by remember { mutableIntStateOf(0) }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }
                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                if (showSettings) {
                    SettingsScreen(
                        currentTheme = theme,
                        onThemeSelected = {
                            theme = it
                            scope.launch { appPreferences.setTheme(it) }
                        },
                        onBack = { showSettings = false }
                    )
                    return@SasmexTheme
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { },
                            actions = {
                                IconButton(onClick = { showSettings = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
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
                            onCheckNewAlert = { list ->
                                scope.launch {
                                    if (list.isEmpty()) return@launch
                                    val currentId = list.first().id
                                    val lastId = appPreferences.getLastNotifiedAlertId()
                                    if (lastId.isEmpty()) {
                                        appPreferences.setLastNotifiedAlertId(currentId)
                                        return@launch
                                    }
                                    if (currentId == lastId) return@launch
                                    NotificationHelper.showAlertNotification(this@MainActivity, list.first())
                                    appPreferences.setLastNotifiedAlertId(currentId)
                                }
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
private fun SasmexTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF0EA5E9),
            onPrimary = Color.White,
            secondary = Color(0xFF38BDF8),
            surface = Color(0xFF0F172A),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFF94A3B8)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF0EA5E9),
            onPrimary = Color.White,
            secondary = Color(0xFF0369A1),
            surface = Color(0xFFF8FAFC),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFF64748B)
        )
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
