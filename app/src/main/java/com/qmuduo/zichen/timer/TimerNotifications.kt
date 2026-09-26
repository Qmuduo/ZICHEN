package com.qmuduo.zichen.timer

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.qmuduo.zichen.MainActivity
import com.qmuduo.zichen.R

class TimerNotifications(private val context: Context) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val quiet = NotificationChannel(
            CHANNEL_QUIET,
            "计时完成",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "仪式计时结束时的静音提醒"
            setSound(null, null)
        }
        val sound = NotificationChannel(
            CHANNEL_CHIME,
            "计时完成 · 钟磬",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "仪式计时结束时播放一次本地钟磬音"
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            setSound(Uri.parse("android.resource://${context.packageName}/${R.raw.gentle_chime}"), audioAttributes)
        }
        manager.createNotificationChannel(quiet)
        manager.createNotificationChannel(sound)
    }

    fun canPost(soundEnabled: Boolean = false): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = if (soundEnabled) CHANNEL_CHIME else CHANNEL_QUIET
            val channel = manager.getNotificationChannel(channelId) ?: return false
            if (channel.importance == NotificationManager.IMPORTANCE_NONE) return false
        }
        return manager.areNotificationsEnabled()
    }

    fun postCompleted(state: TimerState): Boolean {
        ensureChannels()
        if (!canPost(state.soundEnabled)) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = if (state.soundEnabled) CHANNEL_CHIME else CHANNEL_QUIET
            val channel = manager.getNotificationChannel(channelId) ?: return false
            if (channel.importance == NotificationManager.IMPORTANCE_NONE) return false
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            COMPLETION_ACTIVITY_REQUEST_CODE,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val channelId = if (state.soundEnabled) CHANNEL_CHIME else CHANNEL_QUIET
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("时辰 · 计时完成")
            .setContentText("${state.preset.title}已毕，愿此刻从容。")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .build()

        return runCatching {
            manager.notify(COMPLETION_NOTIFICATION_ID, notification)
            true
        }.getOrDefault(false)
    }

    fun clearCompleted() {
        manager.cancel(COMPLETION_NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_QUIET = "timer_finished_quiet"
        const val CHANNEL_CHIME = "timer_finished_chime"
        private const val COMPLETION_NOTIFICATION_ID = 4108
        private const val COMPLETION_ACTIVITY_REQUEST_CODE = 4109
    }
}
