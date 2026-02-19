package mx.cires.sasmex.android.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import mx.cires.sasmex.android.data.AlertaSasmex
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSismoScreen(
    alerta: AlertaSasmex,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }
    val lat = alerta.lat ?: 19.43
    val lon = alerta.lon ?: -99.13
    val mapHtml = remember(lat, lon) { buildMapHtml(lat, lon) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sismo", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B2838),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F172A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    label = { Text("Percepción") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF334155),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    label = { Text("Área epicentral") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF334155),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        loadDataWithBaseURL(null, mapHtml, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(0xFFEA580C),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        alerta.magnitud?.let { "%.1f".format(it) } ?: "—",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        alerta.ubicacion.ifEmpty { alerta.evento },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2
                    )
                    Text(
                        alerta.tiempoRelativo(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Fecha y hora", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                            Text(
                                SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.getDefault()).format(alerta.fechaHora) + " hrs.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                    if (alerta.profundidad.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("▼", color = Color(0xFF94A3B8), style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Profundidad", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                                Text(alerta.profundidad, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                            }
                        }
                    }
                    if (alerta.distancia.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⟷", color = Color(0xFF94A3B8), style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Distancia", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                                Text(alerta.distancia, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildMapHtml(lat: Double, lon: Double): String {
    return """
<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <style>
    body { margin:0; background:#1e293b; }
    #map { width:100%; height:100%; min-height:280px; }
  </style>
</head>
<body>
  <div id="map"></div>
  <script>
    var map = L.map('map', { zoomControl: false }).setView([$lat, $lon], 6);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; OSM, CARTODB'
    }).addTo(map);
    L.marker([$lat, $lon]).addTo(map).bindPopup('Epicentro');
    L.control.zoom({ position: 'bottomright' }).addTo(map);
  </script>
</body>
</html>
    """.trimIndent()
}
