package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationHelper {

    const val CHANNEL_APPOINTMENTS = "channel_appointments"
    const val CHANNEL_SWAPS = "channel_swaps"
    const val CHANNEL_SHIFTS = "channel_shifts"
    const val CHANNEL_UPDATES = "channel_updates"
    const val CHANNEL_HYDRATION = "channel_hydration"

    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_CHANNEL_ID = "extra_channel_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_MESSAGE = "extra_message"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val appointmentsChannel = NotificationChannel(
                CHANNEL_APPOINTMENTS,
                "Compromissos & Lembretes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes com alarme para compromissos e eventos agendados"
                enableVibration(true)
                setShowBadge(true)
            }

            val swapsChannel = NotificationChannel(
                CHANNEL_SWAPS,
                "Trocas de Plantão",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de prazos e datas de devolução/pagamento de plantões trocados"
                enableVibration(true)
                setShowBadge(true)
            }

            val shiftsChannel = NotificationChannel(
                CHANNEL_SHIFTS,
                "Escala & Retorno ao Trabalho",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos de retorno após folgas e início de novos ciclos de plantão"
                enableVibration(true)
                setShowBadge(true)
            }

            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES,
                "Atualizações do Aplicativo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos automáticos quando uma nova versão estiver disponível"
                enableVibration(true)
                setShowBadge(true)
            }

            val hydrationChannel = NotificationChannel(
                CHANNEL_HYDRATION,
                "Lembrete de Água & Saúde",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Lembretes periódicos para se manter hidratado durante o plantão"
                enableVibration(true)
                setShowBadge(true)
            }

            manager.createNotificationChannels(listOf(appointmentsChannel, swapsChannel, shiftsChannel, updatesChannel, hydrationChannel))
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun scheduleAppointmentNotification(
        context: Context,
        appointmentId: Long,
        title: String,
        notes: String?,
        date: LocalDate,
        time: LocalTime?,
        reminderMinutesBefore: Int,
        isShiftSwap: Boolean = false,
        partnerName: String? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val appointmentTime = time ?: LocalTime.of(8, 0)
        val eventDateTime = LocalDateTime.of(date, appointmentTime)
        val triggerDateTime = eventDateTime.minusMinutes(reminderMinutesBefore.toLong())

        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()

        // Don't schedule if trigger time is in the past
        if (triggerMillis <= now) return

        val notificationTitle = if (isShiftSwap) {
            if (!partnerName.isNullOrBlank()) "Troca de Plantão com $partnerName" else "Troca de Plantão: $title"
        } else {
            "Lembrete: $title"
        }

        val timeFormatted = time?.let { " às ${String.format("%02d:%02d", it.hour, it.minute)}" } ?: ""
        val advanceText = when (reminderMinutesBefore) {
            0 -> "Agora"
            15 -> "Em 15 minutos"
            30 -> "Em 30 minutos"
            60 -> "Em 1 hora"
            1440 -> "Amanhã"
            else -> "Em $reminderMinutesBefore min"
        }

        val message = StringBuilder().apply {
            append("$title$timeFormatted ($advanceText)")
            if (!notes.isNullOrBlank()) {
                append(" • $notes")
            }
        }.toString()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, (10000 + appointmentId).toInt())
            putExtra(EXTRA_CHANNEL_ID, if (isShiftSwap) CHANNEL_SWAPS else CHANNEL_APPOINTMENTS)
            putExtra(EXTRA_TITLE, notificationTitle)
            putExtra(EXTRA_MESSAGE, message)
        }

        val requestCode = (10000 + appointmentId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarmSafely(alarmManager, triggerMillis, pendingIntent)
    }

    fun cancelAppointmentNotification(context: Context, appointmentId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val requestCode = (10000 + appointmentId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleSwapPaybackNotification(
        context: Context,
        swapDate: LocalDate,
        paybackDate: LocalDate,
        partnerName: String?,
        notes: String?
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Trigger on payback day at 08:30 AM
        val triggerDateTime = LocalDateTime.of(paybackDate, LocalTime.of(8, 30))
        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()

        if (triggerMillis <= now) return

        val partnerText = partnerName?.let { " ($it)" } ?: ""
        val title = "Lembrete: Devolver Plantão$partnerText"
        val message = "Hoje é a data combinada para pagar o plantão trocado em ${swapDate.dayOfMonth}/${swapDate.monthValue}."

        val requestCode = (20000 + (paybackDate.toEpochDay() % 10000)).toInt()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_CHANNEL_ID, CHANNEL_SWAPS)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarmSafely(alarmManager, triggerMillis, pendingIntent)
    }

    fun cancelSwapPaybackNotification(context: Context, paybackDate: LocalDate) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val requestCode = (20000 + (paybackDate.toEpochDay() % 10000)).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleAlarmSafely(
        alarmManager: AlarmManager,
        triggerMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun sendImmediateTestNotification(context: Context) {
        showNotification(
            context = context,
            notificationId = 9999,
            channelId = CHANNEL_APPOINTMENTS,
            title = "🔔 Teste de Notificação - Escala Medina",
            message = "As notificações de plantões, trocas e compromissos estão funcionando com sucesso!"
        )
    }

    fun showUpdateNotification(context: Context, versionName: String, notes: String) {
        showNotification(
            context = context,
            notificationId = 10001,
            channelId = CHANNEL_UPDATES,
            title = "🚀 Nova Versão $versionName Disponível!",
            message = "Uma nova atualização do Escala Medina está pronta: $notes"
        )
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String
    ) {
        createNotificationChannels(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = R.drawable.ic_app_icon

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Handled if permission revoked
        }
    }

    fun sendWaterReminderNotification(context: Context, currentMl: Int, goalMl: Int) {
        if (!hasNotificationPermission(context)) return

        val percent = if (goalMl > 0) (currentMl * 100 / goalMl).coerceIn(0, 100) else 0
        val title = "💧 Hora de Beber Água!"
        val message = "Você já bebeu $currentMl ml da sua meta de $goalMl ml ($percent%). Beba um copo agora para manter o corpo hidratado e a mente focada no plantão!"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            9998,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_HYDRATION)
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(9998, notification)
        } catch (_: SecurityException) {}
    }

    fun scheduleWaterReminder(context: Context, intervalHours: Int = 2) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, 9998)
            putExtra(EXTRA_CHANNEL_ID, CHANNEL_HYDRATION)
            putExtra(EXTRA_TITLE, "💧 Hora de Beber Água!")
            putExtra(EXTRA_MESSAGE, "Hidrate-se! Um copo de água agora renova a energia, previne cansaço e dores de cabeça no seu plantão.")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9998,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + (intervalHours * 3600 * 1000L)
        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                intervalHours * 3600 * 1000L,
                pendingIntent
            )
        } catch (_: Exception) {}
    }

    fun cancelWaterReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9998,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }
}
