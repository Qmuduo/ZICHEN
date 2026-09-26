package com.qmuduo.zichen.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerFinishedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != TimerAlarmScheduler.ACTION_TIMER_FINISHED) return
        TimerCoordinator(context).handleAlarm()
    }
}
