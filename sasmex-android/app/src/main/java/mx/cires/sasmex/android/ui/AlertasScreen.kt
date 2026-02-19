package mx.cires.sasmex.android.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onNotifyLatest: ((AlertaSasmex) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var alertas by remember { mutableStateOf<List<AlertaSasmex>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun load() {
        loading = true
        error = null
        scope.launch {
            sasmexRepository.obtenerAlertas()
                .onSuccess {
                    alertas = it
                    onAlertasLoaded(it)
                    if (it.isNotEmpty()) onNotifyLatest?.invoke(it.first())
                }
                .onFailure { error = it.message ?: "Error al cargar" }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    var selectedAlerta by remember { mutableStateOf<AlertaSasmex?>(null) }

    selectedAlerta?.let { alerta ->
        DetalleSismoScreen(
            alerta = alerta,
            onBack = { selectedAlerta = null }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Cabecera mínima
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "SASMEX",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    "Alertas sísmicas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
            Text(
                "cires.org.mx",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF0EA5E9),
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.cires.org.mx")))
                }
            )
        }

        // Botón actualizar: estilo outline, discreto
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalButton(
                onClick = { if (!loading) load() },
                enabled = !loading,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFE0F2FE),
                    contentColor = Color(0xFF0369A1)
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF0369A1),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(if (loading) "Cargando…" else "Actualizar", style = MaterialTheme.typography.labelLarge)
            }
        }

        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(20.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        if (alertas.isEmpty() && !loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin alertas. Pulsa Actualizar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "${alertas.size} alerta(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B)
                    )
                }
                items(alertas) { a ->
                    AlertaCard(
                        a,
                        context = context,
                        onClick = { selectedAlerta = a }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertaCard(a: AlertaSasmex, context: Context, onClick: () -> Unit = {}) {
    val (severidadColor, severidadLabel) = when {
        a.esMayor -> Color(0xFFDC2626) to "Mayor"
        a.esMenor -> Color(0xFF16A34A) to "Menor"
        else -> Color(0xFFEA580C) to "Moderada"
    }
    val fechaStr = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(a.fechaHora)
    val shareText = "${a.evento}\n${a.severidad}\n$fechaStr\n— CIRES / rss.sasmex.net"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(severidadColor)
                        .align(Alignment.CenterVertically)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        a.evento,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        fechaStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        severidadLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = severidadColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Toca para ver mapa y detalles",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0EA5E9)
                    )
                }
                Row {
                    IconButton(
                        onClick = {
                            val clip = ClipData.newPlainText("alerta", shareText)
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                            Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = {
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    },
                                    "Compartir"
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color(0xFF0EA5E9), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
