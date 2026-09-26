package com.qmuduo.zichen.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

class TimerAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(state: TimerState) {
        val deadline = state.deadlineEpochMillis
        if (state.status != TimerStatus.RUNNING || deadline == null) {
            cancel()
            return
        }

        val pendingIntent = alarmPendingIntent()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            runCatching {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadline, pendingIntent)
            }.onFailure {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadline, pendingIntent)
            }
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadline, pendingIntent)
        }
    }

    fun cancel() {
        alarmManager.cancel(alarmPendingIntent())
    }

    fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    fun exactAlarmSettingsIntent(): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || canScheduleExactAlarms()) return null
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun alarmPendingIntent(): PendingIntent {
        val intent = Intent(context, TimerFinishedReceiver::class.java).apply {
            action = ACTION_TIMER_FINISHED
        }
        return PendingIntent.getBroadcast(
            context,
            TIMER_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_TIMER_FINISHED = "com.qmuduo.zichen.action.TIMER_FINISHED"
        private const val TIMER_ALARM_REQUEST_CODE = 4107
    }
}
