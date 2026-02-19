package mx.cires.sasmex.android.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import mx.cires.sasmex.android.MainActivity
import mx.cires.sasmex.android.data.AlertaSasmex
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {
    const val CHANNEL_ID = "sasmex_alertas"
    const val CHANNEL_NAME = "Alertas sísmicas SASMEX"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas en tiempo real del Sistema de Alerta Sísmica Mexicano (SASMEX)"
                enableVibration(true)
                enableLights(true)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    /**
     * Muestra notificación solo cuando hay una NUEVA alerta SASMEX.
     * Formato según severidad:
     * - Mayor/Fuerte: 🚨 #TenemosAlerta a [ubicación]. #Sismo FUERTE en los próximos segundos. 🚨
     * - Menor/Moderada: ⚠️ #Sismo detectado. Posible RIESGO existente. ⚠️ + texto SASMEX
     */
    fun showAlertNotification(context: Context, alerta: AlertaSasmex) {
        createChannel(context)
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, body) = formatNotificationMessage(alerta)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title\n\n$body"))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) { }
    }

    private fun formatNotificationMessage(alerta: AlertaSasmex): Pair<String, String> {
        val ubicacion = alerta.ubicacion.ifEmpty { alerta.evento }
        val esFuerte = alerta.severidad.contains("Mayor", ignoreCase = true) ||
            alerta.severidad.contains("Fuerte", ignoreCase = true)

        return if (esFuerte) {
            "🚨 #TenemosAlerta a $ubicacion. #Sismo FUERTE en los próximos segundos. 🚨" to
                "Se activó la #AlertaSísmica por el #Sasmex. Mantén la calma y sigue las indicaciones de Protección Civil."
        } else {
            "⚠️ #Sismo detectado. Posible RIESGO existente. ⚠️" to
                "Se activó la #AlertaSísmica durante los primeros segundos de evaluación por el #Sasmex.\n$ubicacion"
        }
    }
}
