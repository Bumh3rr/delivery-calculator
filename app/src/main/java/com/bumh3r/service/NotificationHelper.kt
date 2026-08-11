package com.bumh3r.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumh3r.MainActivity
import com.securitech.repartidor.R

private const val CANAL_BURBUJA_ID = "burbuja_flotante"

/** Crea el canal y la notificación foreground obligatoria del [BubbleService]. */
object NotificationHelper {

    fun crearCanalSiHaceFalta(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CANAL_BURBUJA_ID) == null) {
            val canal = NotificationChannel(
                CANAL_BURBUJA_ID,
                "Burbuja flotante",
                NotificationManager.IMPORTANCE_MIN
            ).apply { description = "Mantiene activo el acceso rápido a la Calculadora" }
            manager.createNotificationChannel(canal)
        }
    }

    fun crearNotificacion(context: Context): Notification {
        val intentAbrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        val intentDetener = PendingIntent.getService(
            context,
            0,
            Intent(context, BubbleService::class.java).setAction(BubbleService.ACCION_DETENER),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CANAL_BURBUJA_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Calculadora de Rentabilidad activa")
            .setContentText("Toca la burbuja para calcular un pedido rápido")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setContentIntent(intentAbrirApp)
            .addAction(0, "Detener", intentDetener)
            .build()
    }
}
