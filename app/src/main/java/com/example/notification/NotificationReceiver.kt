package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 1001)
        val channelId = intent.getStringExtra(NotificationHelper.EXTRA_CHANNEL_ID) ?: NotificationHelper.CHANNEL_APPOINTMENTS
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Lembrete - Escala Medina"
        val message = intent.getStringExtra(NotificationHelper.EXTRA_MESSAGE) ?: "Você tem um evento agendado."

        NotificationHelper.showNotification(
            context = context,
            notificationId = id,
            channelId = channelId,
            title = title,
            message = message
        )
    }
}
