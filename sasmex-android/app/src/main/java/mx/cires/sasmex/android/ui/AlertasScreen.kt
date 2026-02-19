package mx.cires.sasmex.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.cires.sasmex.android.data.AlertaSasmex
import mx.cires.sasmex.android.data.SasmexRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    sasmexRepository: SasmexRepository,
    onAlertasLoaded: (List<AlertaSasmex>) -> Unit,
    modifier: Modifier = Modifier
) {
    var alertas by remember { mutableStateOf<List<AlertaSasmex>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        loading = true
        error = null
        scope.launch {
            sasmexRepository.obtenerAlertas()
                .onSuccess {
                    alertas = it
                    onAlertasLoaded(it)
                }
                .onFailure { error = it.message ?: "Error al cargar" }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Cabecera
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2B3D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "SASMEX · Centro de Alertas Sísmicas",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "rss.sasmex.net · CIRES",
                    color = Color(0xFFB0BEC5),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        // Botón actualizar
        Button(
            onClick = { if (!loading) load() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            enabled = !loading
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (loading) "Cargando…" else "Actualizar alertas")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp)) }
        Spacer(Modifier.height(8.dp))
        Text("Alertas: ${alertas.size}", style = MaterialTheme.typography.titleSmall, color = Color(0xFF64748B))
        Spacer(Modifier.height(8.dp))
        if (alertas.isEmpty() && !loading)
            Text("Sin alertas. Toca Actualizar.", color = Color.Gray, modifier = Modifier.padding(16.dp))
        else
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(alertas) { a ->
                    AlertaCard(a)
                }
            }
    }
}

@Composable
private fun AlertaCard(a: AlertaSasmex) {
    val severidadColor = when {
        a.esMayor -> Color(0xFFDC2626)
        a.esMenor -> Color(0xFF059669)
        else -> Color(0xFFD97706)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(severidadColor, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(a.evento, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(a.fechaHora),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
                Text(a.severidad, style = MaterialTheme.typography.bodySmall, color = severidadColor, fontWeight = FontWeight.SemiBold)
                if (a.descripcion.isNotEmpty())
                    Text(a.descripcion.take(150) + if (a.descripcion.length > 150) "…" else "", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
            }
        }
    }
}
